package com.rentcar.sigcar.app.Controlador;

import com.rentcar.sigcar.app.DTO.VehiculoDTO;
import com.rentcar.sigcar.app.Modelo.Vehiculo;
import com.rentcar.sigcar.app.Servicio.VehiculoServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Vehículos", description = "Gestión completa de la flota de vehículos de RentCar Express")
@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoControlador {

    @Autowired
    private VehiculoServicio vehiculoServicio;

    @Operation(summary = "Listar todos los vehículos activos")
    @GetMapping
    public ResponseEntity<List<Vehiculo>> listar() {
        return ResponseEntity.ok(vehiculoServicio.listarTodos());
    }

    @Operation(summary = "Listar vehículos disponibles para alquiler")
    @GetMapping("/disponibles")
    public ResponseEntity<List<Vehiculo>> disponibles() {
        return ResponseEntity.ok(vehiculoServicio.listarDisponibles());
    }

    @Operation(summary = "Listar vehículos por estado", 
               description = "Estados válidos: DISPONIBLE, ALQUILADO, EN_MANTENIMIENTO")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Vehiculo>> porEstado(@PathVariable String estado) {
        return ResponseEntity.ok(vehiculoServicio.listarPorEstado(estado));
    }

    @Operation(summary = "Listar vehículos disponibles por categoría",
               description = "Categorías válidas: ECONOMICO, SUV, CAMIONETA, LUJO, FAMILIAR")
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Vehiculo>> porCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(vehiculoServicio.listarPorCategoria(categoria));
    }

    @Operation(summary = "Buscar vehículo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(vehiculoServicio.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Crear nuevo vehículo",
               description = "La placa debe ser única en el sistema. El estado inicial es DISPONIBLE")
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody VehiculoDTO dto) {
        try {
            return ResponseEntity.ok(vehiculoServicio.crear(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Actualizar datos de un vehículo")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id,
                                        @RequestBody VehiculoDTO dto) {
        try {
            return ResponseEntity.ok(vehiculoServicio.actualizar(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Cambiar estado del vehículo",
               description = "Estados válidos: DISPONIBLE, ALQUILADO, EN_MANTENIMIENTO")
    @PatchMapping("/{id}/estado/{nuevoEstado}")
    public ResponseEntity<?> cambiarEstado(@PathVariable String id,
                                           @PathVariable String nuevoEstado) {
        try {
            return ResponseEntity.ok(vehiculoServicio.cambiarEstado(id, nuevoEstado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Actualizar kilometraje del vehículo",
               description = "Si supera el km límite de mantenimiento, cambia automáticamente a EN_MANTENIMIENTO")
    @PatchMapping("/{id}/kilometraje/{km}")
    public ResponseEntity<?> actualizarKm(@PathVariable String id,
                                          @PathVariable Integer km) {
        try {
            return ResponseEntity.ok(vehiculoServicio.actualizarKilometraje(id, km));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Activar vehículo")
    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activar(@PathVariable String id) {
        try {
            vehiculoServicio.activar(id);
            return ResponseEntity.ok("Vehículo activado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Desactivar vehículo",
               description = "No se puede desactivar un vehículo con estado ALQUILADO")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(@PathVariable String id) {
        try {
            vehiculoServicio.desactivar(id);
            return ResponseEntity.ok("Vehículo desactivado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Obtener alertas de mantenimiento",
               description = "Retorna vehículos que superaron el km límite o tienen fecha de mantenimiento próxima (7 días)")
    @GetMapping("/alertas")
    public ResponseEntity<List<Vehiculo>> alertas() {
        return ResponseEntity.ok(vehiculoServicio.obtenerAlertasMantenimiento());
    }

    @Operation(summary = "Consultar disponibilidad por rango de fechas",
               description = "Formato de fecha: yyyy-MM-dd. Ejemplo: 2026-05-25")
    @GetMapping("/disponibilidad")
    public ResponseEntity<?> consultarDisponibilidad(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            java.time.LocalDate inicio = java.time.LocalDate.parse(fechaInicio);
            java.time.LocalDate fin = java.time.LocalDate.parse(fechaFin);
            return ResponseEntity.ok(vehiculoServicio.consultarDisponibilidad(inicio, fin));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}