package com.rentcar.sigcar.app.Controlador;

import com.rentcar.sigcar.app.Servicio.ReporteServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "Reportes", description = "Reportes gerenciales de SIGCAR")
@RestController
@RequestMapping("/api/reportes")
public class ReporteControlador {

    @Autowired
    private ReporteServicio reporteServicio;

    @Operation(summary = "Reporte de ocupación de flota por período")
    @GetMapping("/ocupacion")
    public ResponseEntity<?> ocupacion(
            @RequestParam(defaultValue = "") String fechaInicio,
            @RequestParam(defaultValue = "") String fechaFin) {
        try {
            // Si no se pasan fechas usar el mes actual
            if (fechaInicio.isEmpty()) {
                fechaInicio = LocalDate.now().withDayOfMonth(1).toString();
            }
            if (fechaFin.isEmpty()) {
                fechaFin = LocalDate.now().toString();
            }
            return ResponseEntity.ok(
                reporteServicio.reporteOcupacion(fechaInicio, fechaFin));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Reporte de facturación mensual")
    @GetMapping("/facturacion")
    public ResponseEntity<?> facturacion(
            @RequestParam(defaultValue = "") String mes,
            @RequestParam(defaultValue = "") String anio) {
        try {
            if (mes.isEmpty())  mes  = String.valueOf(LocalDate.now().getMonthValue());
            if (anio.isEmpty()) anio = String.valueOf(LocalDate.now().getYear());
            return ResponseEntity.ok(
                reporteServicio.reporteFacturacion(mes, anio));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Exportar reporte de ocupación en PDF")
    @GetMapping("/ocupacion/pdf")
    public ResponseEntity<byte[]> ocupacionPdf(
            @RequestParam(defaultValue = "") String fechaInicio,
            @RequestParam(defaultValue = "") String fechaFin) {
        try {
            if (fechaInicio.isEmpty()) {
                fechaInicio = LocalDate.now().withDayOfMonth(1).toString();
            }
            if (fechaFin.isEmpty()) {
                fechaFin = LocalDate.now().toString();
            }
            byte[] pdf = reporteServicio.generarPdfOcupacion(fechaInicio, fechaFin);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=reporte-ocupacion.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Exportar reporte de facturación en PDF")
    @GetMapping("/facturacion/pdf")
    public ResponseEntity<byte[]> facturacionPdf(
            @RequestParam(defaultValue = "") String mes,
            @RequestParam(defaultValue = "") String anio) {
        try {
            if (mes.isEmpty())  mes  = String.valueOf(LocalDate.now().getMonthValue());
            if (anio.isEmpty()) anio = String.valueOf(LocalDate.now().getYear());
            byte[] pdf = reporteServicio.generarPdfFacturacion(mes, anio);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=reporte-facturacion.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}