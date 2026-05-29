package com.rentcar.sigcar.app.Controlador;

import com.rentcar.sigcar.app.DTO.ReservaDTO;
import com.rentcar.sigcar.app.DTO.ReservaResponseDTO;
import com.rentcar.sigcar.app.Servicio.ReservaServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.rentcar.sigcar.app.Servicio.ContratoServicio;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservas", description = "Gestión de reservas de vehículos")
@RestController
@RequestMapping("/api/reservas")
public class ReservaControlador {

    @Autowired
    private ReservaServicio reservaServicio;

    @Operation(summary = "Crear nueva reserva",
               description = "Valida disponibilidad del vehículo en las fechas indicadas y calcula el precio automáticamente")
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ReservaDTO dto,
                                   Authentication auth) {
        try {
            String idAgente = auth.getName();
            return ResponseEntity.ok(reservaServicio.crear(dto, idAgente));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Listar todas las reservas")
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listar() {
        return ResponseEntity.ok(reservaServicio.listarTodas());
    }

    @Operation(summary = "Listar reservas por cliente")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<ReservaResponseDTO>> porCliente(
            @PathVariable String idCliente) {
        return ResponseEntity.ok(reservaServicio.listarPorCliente(idCliente));
    }

    @Operation(summary = "Listar reservas por estado",
               description = "Estados: PENDIENTE, CONFIRMADA, EN_CURSO, FINALIZADA, CANCELADA")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ReservaResponseDTO>> porEstado(
            @PathVariable String estado) {
        return ResponseEntity.ok(reservaServicio.listarPorEstado(estado));
    }

    @Operation(summary = "Buscar reserva por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(reservaServicio.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Cambiar estado de una reserva",
               description = "Estados válidos: CONFIRMADA, EN_CURSO, FINALIZADA, CANCELADA")
    @PatchMapping("/{id}/estado/{nuevoEstado}")
    public ResponseEntity<?> cambiarEstado(@PathVariable String id,
                                           @PathVariable String nuevoEstado) {
        try {
            return ResponseEntity.ok(
                reservaServicio.cambiarEstado(id, nuevoEstado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Cancelar reserva",
               description = "Cancela la reserva y libera el vehículo automáticamente")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(reservaServicio.cancelar(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @Autowired
    private ContratoServicio contratoServicio;

    @Operation(summary = "Generar contrato PDF de una reserva")
    @GetMapping("/{id}/contrato")
    public ResponseEntity<?> generarContrato(@PathVariable String id,
                                              Authentication auth) {
        try {
            byte[] pdf = contratoServicio.generarContratoPdf(id, auth.getName());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "contrato-" + id + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}