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

import com.rentcar.sigcar.app.Modelo.Contrato;
import com.rentcar.sigcar.app.Modelo.Reserva;
import com.rentcar.sigcar.app.Modelo.Usuario;
import com.rentcar.sigcar.app.Modelo.Vehiculo;
import com.rentcar.sigcar.app.Repositorio.ContratoRepositorio;
import com.rentcar.sigcar.app.Repositorio.ReservaRepositorio;
import com.rentcar.sigcar.app.Repositorio.UsuarioRepositorio;
import com.rentcar.sigcar.app.Repositorio.VehiculoRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ContratoServicio {

    @Autowired
    private ContratoRepositorio contratoRepositorio;

    @Autowired
    private ReservaRepositorio reservaRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private VehiculoRepositorio vehiculoRepositorio;

    // ── GENERAR CONTRATO PDF ────────────────────────────
    public byte[] generarContratoPdf(String idReserva, String idUsuario) {

        Reserva reserva = reservaRepositorio.findById(idReserva)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada."));

        Usuario cliente = usuarioRepositorio.findById(reserva.getIdCliente())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));

        Vehiculo vehiculo = vehiculoRepositorio.findById(reserva.getIdVehiculo())
            .orElseThrow(() -> new RuntimeException("Vehículo no encontrado."));

        // Número de contrato
        String numeroContrato = "CONT-" + System.currentTimeMillis();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(40, 50, 40, 50);

            // ── ENCABEZADO ──
            doc.add(new Paragraph("RENTCAR EXPRESS S.A.S.")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            doc.add(new Paragraph("NIT: 900.123.456-7 | Bucaramanga, Santander, Colombia")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

            doc.add(new Paragraph("Tel: +57 300 000 0000 | contacto@rentcarexpress.com")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

            doc.add(new Paragraph("\n"));

            // ── TÍTULO ──
            doc.add(new Paragraph("CONTRATO DE ARRENDAMIENTO DE VEHÍCULO")
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            doc.add(new Paragraph("Contrato N°: " + numeroContrato
                + "  |  Fecha: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

            doc.add(new Paragraph("\n"));

            // ── DATOS DEL CLIENTE ──
            doc.add(new Paragraph("1. DATOS DEL CLIENTE")
                .setFontSize(12).setBold()
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            Table tablaCliente = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
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

            // ── DATOS DEL VEHÍCULO ──
            doc.add(new Paragraph("2. DATOS DEL VEHÍCULO")
                .setFontSize(12).setBold()
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            Table tablaVehiculo = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100));

            tablaVehiculo.addCell(crearCeldaGris("Placa:"));
            tablaVehiculo.addCell(crearCelda(vehiculo.getPlaca()));
            tablaVehiculo.addCell(crearCeldaGris("Marca y modelo:"));
            tablaVehiculo.addCell(crearCelda(
                vehiculo.getMarca() + " " + vehiculo.getModelo()
                + " " + (vehiculo.getAnio() != null ? vehiculo.getAnio() : "")));
            tablaVehiculo.addCell(crearCeldaGris("Categoría:"));
            tablaVehiculo.addCell(crearCelda(vehiculo.getCategoria()));
            tablaVehiculo.addCell(crearCeldaGris("Color:"));
            tablaVehiculo.addCell(crearCelda(
                vehiculo.getColor() != null ? vehiculo.getColor() : "—"));

            doc.add(tablaVehiculo);
            doc.add(new Paragraph("\n"));

            // ── CONDICIONES DEL ALQUILER ──
            doc.add(new Paragraph("3. CONDICIONES DEL ALQUILER")
                .setFontSize(12).setBold()
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            Table tablaReserva = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100));

            tablaReserva.addCell(crearCeldaGris("Fecha de inicio:"));
            tablaReserva.addCell(crearCelda(reserva.getFechaInicio().format(fmt)));
            tablaReserva.addCell(crearCeldaGris("Fecha de fin:"));
            tablaReserva.addCell(crearCelda(reserva.getFechaFin().format(fmt)));
            tablaReserva.addCell(crearCeldaGris("Días de alquiler:"));
            tablaReserva.addCell(crearCelda(reserva.getDiasAlquiler() + " días"));
            tablaReserva.addCell(crearCeldaGris("Precio por día:"));
            tablaReserva.addCell(crearCelda(
                "$" + String.format("%,.0f", vehiculo.getPrecioPorDia()) + " COP"));
            tablaReserva.addCell(crearCeldaGris("VALOR TOTAL:"));

            Cell celdaTotal = new Cell()
                .add(new Paragraph("$" + String.format("%,.0f", reserva.getValorTotal()) + " COP")
                    .setBold().setFontSize(13))
                .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(245, 197, 24))
                .setPadding(6);
            tablaReserva.addCell(celdaTotal);

            doc.add(tablaReserva);
            doc.add(new Paragraph("\n"));

            // ── CLÁUSULAS ──
            doc.add(new Paragraph("4. CLÁUSULAS Y CONDICIONES")
                .setFontSize(12).setBold()
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            String[] clausulas = {
                "1. El arrendatario se compromete a devolver el vehículo en las mismas condiciones en que lo recibió.",
                "2. El arrendatario es responsable de cualquier daño causado al vehículo durante el período de alquiler.",
                "3. Queda prohibido el subarrendamiento del vehículo a terceros.",
                "4. El vehículo debe ser conducido únicamente por personas con licencia de conducción vigente.",
                "5. En caso de accidente, el arrendatario debe notificar inmediatamente a RentCar Express S.A.S.",
                "6. El incumplimiento de cualquiera de estas cláusulas dará lugar a la terminación inmediata del contrato."
            };

            for (String clausula : clausulas) {
                doc.add(new Paragraph(clausula)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setFontColor(ColorConstants.DARK_GRAY));
            }

            doc.add(new Paragraph("\n\n"));

            // ── FIRMAS ──
            doc.add(new Paragraph("5. FIRMAS")
                .setFontSize(12).setBold()
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            Table tablaFirmas = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

            tablaFirmas.addCell(new Cell()
                .add(new Paragraph("\n\n\n_______________________\n"
                    + cliente.getNombres() + " " + cliente.getApellidos() + "\nARRENDATARIO")
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));

            tablaFirmas.addCell(new Cell()
                .add(new Paragraph("\n\n\n_______________________\n"
                    + "RentCar Express S.A.S.\nARRENDADOR")
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));

            doc.add(tablaFirmas);

            doc.close();

            // Guardar contrato en MongoDB
            Contrato contrato = new Contrato();
            contrato.setIdReserva(idReserva);
            contrato.setNumeroContrato(numeroContrato);
            contrato.setCondiciones("Contrato estándar RentCar Express S.A.S.");
            contrato.setFechaGeneracion(LocalDateTime.now());
            contrato.setIdGeneradoPor(idUsuario);
            contratoRepositorio.save(contrato);

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el contrato PDF: " + e.getMessage());
        }
    }

    // ── HELPERS ────────────────────────────────────────
    private Cell crearCelda(String texto) {
        return new Cell()
            .add(new Paragraph(texto).setFontSize(10))
            .setPadding(5);
    }

    private Cell crearCeldaGris(String texto) {
        return new Cell()
            .add(new Paragraph(texto).setFontSize(10).setBold())
            .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(240, 240, 240))
            .setPadding(5);
    }

    // ── OBTENER CONTRATO POR RESERVA ────────────────────
    public Optional<Contrato> obtenerPorReserva(String idReserva) {
        return contratoRepositorio.findByIdReserva(idReserva);
    }
}
