package com.rentcar.sigcar.app.Controlador;

import com.rentcar.sigcar.app.DTO.PagoDTO;
import com.rentcar.sigcar.app.DTO.PagoResponseDTO;
import com.rentcar.sigcar.app.Servicio.PagoServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Pagos", description = "Gestión de pagos y facturas de SIGCAR")
@RestController
@RequestMapping("/api/pagos")
public class PagoControlador {

    @Autowired
    private PagoServicio pagoServicio;

    @Operation(summary = "Registrar pago de una reserva")
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody PagoDTO dto,
                                       Authentication auth) {
        try {
            PagoResponseDTO pago = pagoServicio.registrar(dto, auth.getName());
            return ResponseEntity.ok(pago);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Listar todos los pagos")
    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pagoServicio.listarTodos());
    }

    @Operation(summary = "Buscar pago por reserva")
    @GetMapping("/reserva/{idReserva}")
    public ResponseEntity<?> buscarPorReserva(@PathVariable String idReserva) {
        try {
            return ResponseEntity.ok(pagoServicio.buscarPorReserva(idReserva));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Listar pagos de un cliente")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<PagoResponseDTO>> listarPorCliente(
            @PathVariable String idCliente) {
        return ResponseEntity.ok(pagoServicio.listarPorCliente(idCliente));
    }

    @Operation(summary = "Ingresos del mes actual")
    @GetMapping("/ingresos/mes")
    public ResponseEntity<?> ingresosDelMes() {
        return ResponseEntity.ok(Map.of("ingresos", pagoServicio.ingresosDelMes()));
    }

    @Operation(summary = "Descargar factura PDF de una reserva")
    @GetMapping("/reserva/{idReserva}/factura")
    public ResponseEntity<byte[]> descargarFactura(
            @PathVariable String idReserva) {
        try {
            byte[] pdf = pagoServicio.generarFacturaPdf(idReserva);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=factura-" + idReserva + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}