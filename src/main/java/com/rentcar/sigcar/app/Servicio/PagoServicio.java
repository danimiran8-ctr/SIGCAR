package com.rentcar.sigcar.app.Servicio;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import com.rentcar.sigcar.app.DTO.PagoDTO;
import com.rentcar.sigcar.app.DTO.PagoResponseDTO;
import com.rentcar.sigcar.app.Modelo.Pago;
import com.rentcar.sigcar.app.Modelo.Reserva;
import com.rentcar.sigcar.app.Modelo.Usuario;
import com.rentcar.sigcar.app.Modelo.Vehiculo;
import com.rentcar.sigcar.app.Repositorio.PagoRepositorio;
import com.rentcar.sigcar.app.Repositorio.ReservaRepositorio;
import com.rentcar.sigcar.app.Repositorio.UsuarioRepositorio;
import com.rentcar.sigcar.app.Repositorio.VehiculoRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PagoServicio {

    @Autowired private PagoRepositorio pagoRepositorio;
    @Autowired private ReservaRepositorio reservaRepositorio;
    @Autowired private UsuarioRepositorio usuarioRepositorio;
    @Autowired private VehiculoRepositorio vehiculoRepositorio;

    // ── REGISTRAR PAGO ──────────────────────────────────
    public PagoResponseDTO registrar(PagoDTO dto, String idAgente) {

        Reserva reserva = reservaRepositorio.findById(dto.getIdReserva())
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada."));

        pagoRepositorio.findByIdReserva(dto.getIdReserva()).ifPresent(p -> {
            throw new RuntimeException("Esta reserva ya tiene un pago registrado.");
        });

        if (!dto.getMetodoPago().equals("EFECTIVO") &&
            !dto.getMetodoPago().equals("TRANSFERENCIA")) {
            throw new RuntimeException("Método de pago inválido.");
        }

        Usuario cliente = usuarioRepositorio.findById(reserva.getIdCliente())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));

        Vehiculo vehiculo = vehiculoRepositorio.findById(reserva.getIdVehiculo())
            .orElseThrow(() -> new RuntimeException("Vehículo no encontrado."));

        String numeroPago = "PAG-" + System.currentTimeMillis();

        Pago pago = new Pago();
        pago.setIdReserva(dto.getIdReserva());
        pago.setIdCliente(reserva.getIdCliente());
        pago.setIdAgente(idAgente);
        pago.setMonto(reserva.getValorTotal());
        pago.setMetodoPago(dto.getMetodoPago());
        pago.setEstado("PAGADO");
        pago.setObservaciones(dto.getObservaciones());
        pago.setNumeroPago(numeroPago);
        pago.setFechaPago(LocalDateTime.now());
        pago.setFechaCreacion(LocalDateTime.now());
        pago.setFechaActualizacion(LocalDateTime.now());

        Pago guardado = pagoRepositorio.save(pago);
        return mapearResponse(guardado, cliente, vehiculo);
    }

    // ── LISTAR TODOS ────────────────────────────────────
    public List<PagoResponseDTO> listarTodos() {
        return pagoRepositorio.findAll()
            .stream().map(this::mapearResponseBasico)
            .collect(Collectors.toList());
    }

    // ── LISTAR POR CLIENTE ──────────────────────────────
    public List<PagoResponseDTO> listarPorCliente(String idCliente) {
        return pagoRepositorio.findByIdCliente(idCliente)
            .stream().map(this::mapearResponseBasico)
            .collect(Collectors.toList());
    }

    // ── BUSCAR POR RESERVA ──────────────────────────────
    public PagoResponseDTO buscarPorReserva(String idReserva) {
        Pago pago = pagoRepositorio.findByIdReserva(idReserva)
            .orElseThrow(() -> new RuntimeException(
                "No hay pago registrado para esta reserva."));
        return mapearResponseBasico(pago);
    }

    // ── INGRESOS DEL MES ────────────────────────────────
    public Double ingresosDelMes() {
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1)
            .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finMes = LocalDateTime.now();
        return pagoRepositorio.findByFechaPagoBetween(inicioMes, finMes)
            .stream()
            .filter(p -> "PAGADO".equals(p.getEstado()))
            .mapToDouble(Pago::getMonto)
            .sum();
    }

    // ── GENERAR FACTURA PDF ─────────────────────────────
    public byte[] generarFacturaPdf(String idReserva) {

        Pago pago = pagoRepositorio.findByIdReserva(idReserva)
            .orElseThrow(() -> new RuntimeException(
                "No hay pago registrado para esta reserva."));

        Reserva reserva = reservaRepositorio.findById(idReserva)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada."));

        Usuario cliente = usuarioRepositorio.findById(reserva.getIdCliente())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));

        Vehiculo vehiculo = vehiculoRepositorio.findById(reserva.getIdVehiculo())
            .orElseThrow(() -> new RuntimeException("Vehículo no encontrado."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(40, 50, 40, 50);

            // ── ENCABEZADO ──
            doc.add(new Paragraph("RENTCAR EXPRESS S.A.S.")
                .setFontSize(20).setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            doc.add(new Paragraph("NIT: 900.123.456-7 | Bucaramanga, Santander, Colombia")
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

            doc.add(new Paragraph("Tel: +57 300 000 0000 | contacto@rentcarexpress.com")
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

            doc.add(new Paragraph("\n"));

            // ── TÍTULO ──
            doc.add(new Paragraph("FACTURA DE PAGO")
                .setFontSize(16).setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            doc.add(new Paragraph("N° " + pago.getNumeroPago()
                + "  |  Fecha: " + pago.getFechaPago().format(fmt))
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

            doc.add(new Paragraph("\n"));

            // ── DATOS DEL CLIENTE ──
            doc.add(new Paragraph("1. DATOS DEL CLIENTE")
                .setFontSize(12).setBold()
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            Table tablaCliente = new Table(
                UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100));
            tablaCliente.addCell(crearCeldaGris("Nombre completo:"));
            tablaCliente.addCell(crearCelda(
                cliente.getNombres() + " " + cliente.getApellidos()));
            tablaCliente.addCell(crearCeldaGris("Correo electrónico:"));
            tablaCliente.addCell(crearCelda(cliente.getCorreo()));
            tablaCliente.addCell(crearCeldaGris("Teléfono:"));
            tablaCliente.addCell(crearCelda(
                cliente.getTelefono() != null ? cliente.getTelefono() : "—"));
            doc.add(tablaCliente);
            doc.add(new Paragraph("\n"));

            // ── DETALLE DEL SERVICIO ──
            doc.add(new Paragraph("2. DETALLE DEL SERVICIO")
                .setFontSize(12).setBold()
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            Table tablaServicio = new Table(
                UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100));
            tablaServicio.addCell(crearCeldaGris("Vehículo:"));
            tablaServicio.addCell(crearCelda(
                vehiculo.getPlaca() + " — "
                + vehiculo.getMarca() + " " + vehiculo.getModelo()));
            tablaServicio.addCell(crearCeldaGris("Período de alquiler:"));
            tablaServicio.addCell(crearCelda(
                reserva.getFechaInicio() + " → " + reserva.getFechaFin()));
            tablaServicio.addCell(crearCeldaGris("Días de alquiler:"));
            tablaServicio.addCell(crearCelda(reserva.getDiasAlquiler() + " días"));
            tablaServicio.addCell(crearCeldaGris("Precio por día:"));
            tablaServicio.addCell(crearCelda(
                "$" + String.format("%,.0f", vehiculo.getPrecioPorDia()) + " COP"));
            doc.add(tablaServicio);
            doc.add(new Paragraph("\n"));

            // ── DETALLE DEL PAGO ──
            doc.add(new Paragraph("3. DETALLE DEL PAGO")
                .setFontSize(12).setBold()
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            Table tablaPago = new Table(
                UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100));
            tablaPago.addCell(crearCeldaGris("Método de pago:"));
            tablaPago.addCell(crearCelda(pago.getMetodoPago()));
            tablaPago.addCell(crearCeldaGris("Fecha de pago:"));
            tablaPago.addCell(crearCelda(pago.getFechaPago().format(fmt)));
            tablaPago.addCell(crearCeldaGris("Estado:"));
            tablaPago.addCell(crearCelda(pago.getEstado()));
            if (pago.getObservaciones() != null
                && !pago.getObservaciones().isEmpty()) {
                tablaPago.addCell(crearCeldaGris("Observaciones:"));
                tablaPago.addCell(crearCelda(pago.getObservaciones()));
            }
            tablaPago.addCell(crearCeldaGris("TOTAL PAGADO:"));
            Cell celdaTotal = new Cell()
                .add(new Paragraph(
                    "$" + String.format("%,.0f", pago.getMonto()) + " COP")
                    .setBold().setFontSize(14))
                .setBackgroundColor(
                    new com.itextpdf.kernel.colors.DeviceRgb(245, 197, 24))
                .setPadding(6);
            tablaPago.addCell(celdaTotal);
            doc.add(tablaPago);

            doc.add(new Paragraph("\n\n"));

            // ── PIE DE PÁGINA ──
            doc.add(new Paragraph(
                "Este documento es un comprobante de pago válido "
                + "emitido por RentCar Express S.A.S.")
                .setFontSize(9).setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

            doc.add(new Paragraph("Generado el: " + LocalDateTime.now().format(fmt))
                .setFontSize(9).setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                "Error al generar la factura PDF: " + e.getMessage());
        }
    }

    // ── HELPERS ─────────────────────────────────────────
    private Cell crearCelda(String texto) {
        return new Cell()
            .add(new Paragraph(texto).setFontSize(10))
            .setPadding(5);
    }

    private Cell crearCeldaGris(String texto) {
        return new Cell()
            .add(new Paragraph(texto).setFontSize(10).setBold())
            .setBackgroundColor(
                new com.itextpdf.kernel.colors.DeviceRgb(240, 240, 240))
            .setPadding(5);
    }

    // ── MAPEAR RESPONSE ─────────────────────────────────
    private PagoResponseDTO mapearResponse(
            Pago p, Usuario cliente, Vehiculo vehiculo) {
        PagoResponseDTO dto = new PagoResponseDTO();
        dto.setId(p.getId());
        dto.setIdReserva(p.getIdReserva());
        dto.setIdCliente(p.getIdCliente());
        dto.setNombreCliente(
            cliente.getNombres() + " " + cliente.getApellidos());
        dto.setPlacaVehiculo(vehiculo.getPlaca());
        dto.setMarcaModelo(vehiculo.getMarca() + " " + vehiculo.getModelo());
        dto.setMonto(p.getMonto());
        dto.setMetodoPago(p.getMetodoPago());
        dto.setEstado(p.getEstado());
        dto.setNumeroPago(p.getNumeroPago());
        dto.setObservaciones(p.getObservaciones());
        dto.setFechaPago(p.getFechaPago());
        return dto;
    }

    private PagoResponseDTO mapearResponseBasico(Pago p) {
        PagoResponseDTO dto = new PagoResponseDTO();
        dto.setId(p.getId());
        dto.setIdReserva(p.getIdReserva());
        dto.setIdCliente(p.getIdCliente());
        dto.setMonto(p.getMonto());
        dto.setMetodoPago(p.getMetodoPago());
        dto.setEstado(p.getEstado());
        dto.setNumeroPago(p.getNumeroPago());
        dto.setObservaciones(p.getObservaciones());
        dto.setFechaPago(p.getFechaPago());

        usuarioRepositorio.findById(p.getIdCliente()).ifPresent(u ->
            dto.setNombreCliente(u.getNombres() + " " + u.getApellidos()));

        reservaRepositorio.findById(p.getIdReserva()).ifPresent(r ->
            vehiculoRepositorio.findById(r.getIdVehiculo()).ifPresent(v -> {
                dto.setPlacaVehiculo(v.getPlaca());
                dto.setMarcaModelo(v.getMarca() + " " + v.getModelo());
            }));

        return dto;
    }
}
