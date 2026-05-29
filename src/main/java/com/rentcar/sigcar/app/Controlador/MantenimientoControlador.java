package com.rentcar.sigcar.app.Controlador;

import com.rentcar.sigcar.app.DTO.MantenimientoDTO;
import com.rentcar.sigcar.app.DTO.MantenimientoResponseDTO;
import com.rentcar.sigcar.app.Servicio.MantenimientoServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Mantenimientos", description = "Gestión de mantenimientos de la flota SIGCAR")
@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoControlador {

    @Autowired
    private MantenimientoServicio mantenimientoServicio;

    @Operation(summary = "Registrar un mantenimiento")
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody MantenimientoDTO dto,
                                       Authentication auth) {
        try {
            MantenimientoResponseDTO m =
                mantenimientoServicio.registrar(dto, auth.getName());
            return ResponseEntity.ok(m);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Listar todos los mantenimientos")
    @GetMapping
    public ResponseEntity<List<MantenimientoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(mantenimientoServicio.listarTodos());
    }

    @Operation(summary = "Listar mantenimientos por vehículo")
    @GetMapping("/vehiculo/{idVehiculo}")
    public ResponseEntity<List<MantenimientoResponseDTO>> listarPorVehiculo(
            @PathVariable String idVehiculo) {
        return ResponseEntity.ok(
            mantenimientoServicio.listarPorVehiculo(idVehiculo));
    }

    @Operation(summary = "Listar mantenimientos por tipo")
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<MantenimientoResponseDTO>> listarPorTipo(
            @PathVariable String tipo) {
        return ResponseEntity.ok(
            mantenimientoServicio.listarPorTipo(tipo));
    }

    @Operation(summary = "Buscar mantenimiento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(mantenimientoServicio.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Eliminar mantenimiento")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            mantenimientoServicio.eliminar(id);
            return ResponseEntity.ok("Mantenimiento eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @Operation(summary = "Iniciar mantenimiento — pone el vehículo EN_MANTENIMIENTO")
    @PatchMapping("/vehiculo/{idVehiculo}/iniciar")
    public ResponseEntity<?> iniciarMantenimiento(@PathVariable String idVehiculo) {
        try {
            mantenimientoServicio.iniciarMantenimiento(idVehiculo);
            return ResponseEntity.ok("Vehículo puesto en mantenimiento correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
}