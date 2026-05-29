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

import com.rentcar.sigcar.app.Modelo.Pago;
import com.rentcar.sigcar.app.Modelo.Reserva;
import com.rentcar.sigcar.app.Modelo.Vehiculo;
import com.rentcar.sigcar.app.Repositorio.PagoRepositorio;
import com.rentcar.sigcar.app.Repositorio.ReservaRepositorio;
import com.rentcar.sigcar.app.Repositorio.VehiculoRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteServicio {

    @Autowired private ReservaRepositorio reservaRepositorio;
    @Autowired private PagoRepositorio pagoRepositorio;
    @Autowired private VehiculoRepositorio vehiculoRepositorio;

    // ── REPORTE DE OCUPACIÓN ────────────────────────────
    public Map<String, Object> reporteOcupacion(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin    = LocalDate.parse(fechaFin);

        List<Reserva> reservas = reservaRepositorio.findAll().stream()
            .filter(r -> !List.of("CANCELADA").contains(r.getEstado()))
            .filter(r -> r.getFechaInicio() != null && r.getFechaFin() != null)
            .filter(r -> !r.getFechaFin().isBefore(inicio) &&
                         !r.getFechaInicio().isAfter(fin))
            .collect(Collectors.toList());

        // Agrupar días por vehículo
        Map<String, Long> diasPorVehiculo = new HashMap<>();
        Map<String, String> nombreVehiculo = new HashMap<>();

        for (Reserva r : reservas) {
            LocalDate dInicio = r.getFechaInicio().isBefore(inicio) ? inicio : r.getFechaInicio();
            LocalDate dFin    = r.getFechaFin().isAfter(fin) ? fin : r.getFechaFin();
            long dias = ChronoUnit.DAYS.between(dInicio, dFin);
            diasPorVehiculo.merge(r.getIdVehiculo(), dias, Long::sum);

            vehiculoRepositorio.findById(r.getIdVehiculo()).ifPresent(v ->
                nombreVehiculo.put(r.getIdVehiculo(),
                    v.getPlaca() + " — " + v.getMarca() + " " + v.getModelo()));
        }

        long totalDiasPeriodo = ChronoUnit.DAYS.between(inicio, fin);
        int totalVehiculos = (int) vehiculoRepositorio.findAll().stream()
            .filter(v -> Boolean.TRUE.equals(v.getActivo())).count();

        List<Map<String, Object>> detalle = diasPorVehiculo.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(e -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("idVehiculo", e.getKey());
                item.put("vehiculo", nombreVehiculo.getOrDefault(e.getKey(), e.getKey()));
                item.put("diasAlquilado", e.getValue());
                item.put("porcentajeOcupacion",
                    totalDiasPeriodo > 0
                        ? Math.round((e.getValue() * 100.0) / totalDiasPeriodo)
                        : 0);
                return item;
            })
            .collect(Collectors.toList());

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("fechaInicio", fechaInicio);
        resultado.put("fechaFin", fechaFin);
        resultado.put("totalVehiculos", totalVehiculos);
        resultado.put("totalReservas", reservas.size());
        resultado.put("detalle", detalle);
        return resultado;
    }

    // ── REPORTE DE FACTURACIÓN ──────────────────────────
    public Map<String, Object> reporteFacturacion(String mes, String anio) {
        int m = Integer.parseInt(mes);
        int a = Integer.parseInt(anio);

        LocalDateTime inicio = LocalDateTime.of(a, m, 1, 0, 0);
        LocalDateTime fin    = inicio.plusMonths(1).minusSeconds(1);

        List<Pago> pagos = pagoRepositorio.findByFechaPagoBetween(inicio, fin)
            .stream()
            .filter(p -> "PAGADO".equals(p.getEstado()))
            .collect(Collectors.toList());

        double totalIngresos = pagos.stream().mapToDouble(Pago::getMonto).sum();

        // Vehículo más rentable
        Map<String, Double> ingresosPorVehiculo = new HashMap<>();
        Map<String, String> nombreVehiculo = new HashMap<>();

        for (Pago p : pagos) {
            reservaRepositorio.findById(p.getIdReserva()).ifPresent(r -> {
                ingresosPorVehiculo.merge(r.getIdVehiculo(), p.getMonto(), Double::sum);
                vehiculoRepositorio.findById(r.getIdVehiculo()).ifPresent(v ->
                    nombreVehiculo.put(r.getIdVehiculo(),
                        v.getPlaca() + " — " + v.getMarca() + " " + v.getModelo()));
            });
        }

        String vehiculoMasRentable = ingresosPorVehiculo.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> nombreVehiculo.getOrDefault(e.getKey(), e.getKey()))
            .orElse("Sin datos");

        // Métodos de pago
        long efectivo = pagos.stream()
            .filter(p -> "EFECTIVO".equals(p.getMetodoPago())).count();
        long transferencia = pagos.stream()
            .filter(p -> "TRANSFERENCIA".equals(p.getMetodoPago())).count();

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("mes", mes);
        resultado.put("anio", anio);
        resultado.put("totalIngresos", totalIngresos);
        resultado.put("totalPagos", pagos.size());
        resultado.put("vehiculoMasRentable", vehiculoMasRentable);
        resultado.put("pagosEfectivo", efectivo);
        resultado.put("pagosTransferencia", transferencia);
        return resultado;
    }

    // ── PDF OCUPACIÓN ───────────────────────────────────
    public byte[] generarPdfOcupacion(String fechaInicio, String fechaFin) {
        Map<String, Object> datos = reporteOcupacion(fechaInicio, fechaFin);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(40, 50, 40, 50);

            encabezado(doc, "REPORTE DE OCUPACIÓN DE FLOTA",
                "Período: " + fechaInicio + " al " + fechaFin);

            doc.add(new Paragraph("Resumen del período")
                .setFontSize(12).setBold()
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            Table resumen = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(100));
            resumen.addCell(crearCeldaGris("Total vehículos activos:"));
            resumen.addCell(crearCelda(String.valueOf(datos.get("totalVehiculos"))));
            resumen.addCell(crearCeldaGris("Total reservas en el período:"));
            resumen.addCell(crearCelda(String.valueOf(datos.get("totalReservas"))));
            doc.add(resumen);
            doc.add(new Paragraph("\n"));

            doc.add(new Paragraph("Detalle por vehículo")
                .setFontSize(12).setBold()
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));

            Table tabla = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100));
            tabla.addHeaderCell(crearCeldaGris("Vehículo"));
            tabla.addHeaderCell(crearCeldaGris("Días alquilado"));
            tabla.addHeaderCell(crearCeldaGris("% Ocupación"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detalle =
                (List<Map<String, Object>>) datos.get("detalle");

            if (detalle.isEmpty()) {
                tabla.addCell(new Cell(1, 3)
                    .add(new Paragraph("No hay reservas en el período seleccionado")
                        .setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
            } else {
                for (Map<String, Object> item : detalle) {
                    tabla.addCell(crearCelda(String.valueOf(item.get("vehiculo"))));
                    tabla.addCell(crearCelda(item.get("diasAlquilado") + " días"));
                    tabla.addCell(crearCelda(item.get("porcentajeOcupacion") + "%"));
                }
            }
            doc.add(tabla);
            pie(doc);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }

    // ── PDF FACTURACIÓN ─────────────────────────────────
    public byte[] generarPdfFacturacion(String mes, String anio) {
        Map<String, Object> datos = reporteFacturacion(mes, anio);
        String[] meses = {"","Enero","Febrero","Marzo","Abril","Mayo","Junio",
                          "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        String nombreMes = meses[Integer.parseInt(mes)];

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(40, 50, 40, 50);

            encabezado(doc, "REPORTE DE FACTURACIÓN MENSUAL",
                nombreMes + " " + anio);

            Table tabla = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(100));

            tabla.addCell(crearCeldaGris("Total ingresos del mes:"));
            Cell celdaTotal = new Cell()
                .add(new Paragraph("$" + String.format("%,.0f",
                    ((Number) datos.get("totalIngresos")).doubleValue()) + " COP")
                    .setBold().setFontSize(13))
                .setBackgroundColor(
                    new com.itextpdf.kernel.colors.DeviceRgb(245, 197, 24))
                .setPadding(6);
            tabla.addCell(celdaTotal);

            tabla.addCell(crearCeldaGris("Total pagos registrados:"));
            tabla.addCell(crearCelda(String.valueOf(datos.get("totalPagos"))));
            tabla.addCell(crearCeldaGris("Vehículo más rentable:"));
            tabla.addCell(crearCelda(String.valueOf(datos.get("vehiculoMasRentable"))));
            tabla.addCell(crearCeldaGris("Pagos en efectivo:"));
            tabla.addCell(crearCelda(String.valueOf(datos.get("pagosEfectivo"))));
            tabla.addCell(crearCeldaGris("Pagos por transferencia:"));
            tabla.addCell(crearCelda(String.valueOf(datos.get("pagosTransferencia"))));

            doc.add(tabla);
            pie(doc);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }

    // ── HELPERS PDF ─────────────────────────────────────
    private void encabezado(Document doc, String titulo, String subtitulo)
            throws Exception {
        doc.add(new Paragraph("RENTCAR EXPRESS S.A.S.")
            .setFontSize(18).setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));
        doc.add(new Paragraph("NIT: 900.123.456-7 | Bucaramanga, Santander")
            .setFontSize(10).setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.GRAY));
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph(titulo)
            .setFontSize(14).setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 26, 46)));
        doc.add(new Paragraph(subtitulo)
            .setFontSize(10).setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.GRAY));
        doc.add(new Paragraph("\n"));
    }

    private void pie(Document doc) throws Exception {
        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph(
            "Reporte generado el: " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
            .setFontSize(9).setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.GRAY));
    }

    private Cell crearCelda(String texto) {
        return new Cell()
            .add(new Paragraph(texto).setFontSize(10)).setPadding(5);
    }

    private Cell crearCeldaGris(String texto) {
        return new Cell()
            .add(new Paragraph(texto).setFontSize(10).setBold())
            .setBackgroundColor(
                new com.itextpdf.kernel.colors.DeviceRgb(240, 240, 240))
            .setPadding(5);
    }
}