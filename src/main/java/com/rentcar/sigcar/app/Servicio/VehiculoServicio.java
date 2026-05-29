package com.rentcar.sigcar.app.Servicio;

import com.rentcar.sigcar.app.DTO.VehiculoDTO;
import com.rentcar.sigcar.app.Modelo.Vehiculo;
import com.rentcar.sigcar.app.Repositorio.VehiculoRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehiculoServicio {

    @Autowired
    private VehiculoRepositorio vehiculoRepositorio;

    // ── CREAR ──────────────────────────────────────────
    public Vehiculo crear(VehiculoDTO dto) {

        // Validar placa única
        if (vehiculoRepositorio.existsByPlaca(dto.getPlaca())) {
            throw new RuntimeException(
                "Ya existe un vehículo con la placa: " + dto.getPlaca());
        }

        Vehiculo v = new Vehiculo();
        v.setPlaca(dto.getPlaca().toUpperCase().trim());
        v.setMarca(dto.getMarca());
        v.setModelo(dto.getModelo());
        v.setAnio(dto.getAnio());
        v.setColor(dto.getColor());
        v.setCategoria(dto.getCategoria());
        v.setNumPuertas(dto.getNumPuertas());
        v.setCapacidadPasajeros(dto.getCapacidadPasajeros());
        v.setTransmision(dto.getTransmision());
        v.setPrecioPorDia(dto.getPrecioPorDia());
        v.setKilometrajeActual(dto.getKilometrajeActual() != null
                ? dto.getKilometrajeActual() : 0);
        v.setKilometrajeMantenimiento(dto.getKilometrajeMantenimiento());
        v.setFotoUrl(dto.getFotoUrl());

        if (dto.getFechaMantenimiento() != null
                && !dto.getFechaMantenimiento().isEmpty()) {
            v.setFechaMantenimiento(LocalDate.parse(dto.getFechaMantenimiento()));
        }

        v.setEstado("DISPONIBLE");
        v.setActivo(true);
        v.setFechaCreacion(LocalDateTime.now());
        v.setFechaActualizacion(LocalDateTime.now());

        return vehiculoRepositorio.save(v);
    }

    // ── LISTAR TODOS ACTIVOS ────────────────────────────
    public List<Vehiculo> listarTodos() {
        return vehiculoRepositorio.findByActivoTrue();
    }

    // ── LISTAR POR ESTADO ───────────────────────────────
    public List<Vehiculo> listarPorEstado(String estado) {
        return vehiculoRepositorio.findByEstadoAndActivoTrue(estado);
    }

    // ── LISTAR POR CATEGORÍA ────────────────────────────
    public List<Vehiculo> listarPorCategoria(String categoria) {
        return vehiculoRepositorio
            .findByCategoriaAndEstadoAndActivoTrue(categoria, "DISPONIBLE");
    }

    // ── LISTAR DISPONIBLES ──────────────────────────────
    public List<Vehiculo> listarDisponibles() {
        return vehiculoRepositorio.findByEstadoAndActivoTrue("DISPONIBLE");
    }

    // ── BUSCAR POR ID ───────────────────────────────────
    public Vehiculo buscarPorId(String id) {
        return vehiculoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Vehículo no encontrado con ID: " + id));
    }

    // ── BUSCAR POR PLACA ────────────────────────────────
    public Vehiculo buscarPorPlaca(String placa) {
        return vehiculoRepositorio.findByPlaca(placa.toUpperCase().trim())
                .orElseThrow(() -> new RuntimeException(
                        "Vehículo no encontrado con placa: " + placa));
    }

    // ── ACTUALIZAR ──────────────────────────────────────
    public Vehiculo actualizar(String id, VehiculoDTO dto) {
        Vehiculo v = buscarPorId(id);

        // Validar placa única si cambió
        if (!v.getPlaca().equals(dto.getPlaca().toUpperCase().trim())) {
            if (vehiculoRepositorio.existsByPlaca(dto.getPlaca())) {
                throw new RuntimeException(
                    "Ya existe un vehículo con la placa: " + dto.getPlaca());
            }
            v.setPlaca(dto.getPlaca().toUpperCase().trim());
        }

        v.setMarca(dto.getMarca());
        v.setModelo(dto.getModelo());
        v.setAnio(dto.getAnio());
        v.setColor(dto.getColor());
        v.setCategoria(dto.getCategoria());
        v.setNumPuertas(dto.getNumPuertas());
        v.setCapacidadPasajeros(dto.getCapacidadPasajeros());
        v.setTransmision(dto.getTransmision());
        v.setPrecioPorDia(dto.getPrecioPorDia());
        v.setKilometrajeActual(dto.getKilometrajeActual());
        v.setKilometrajeMantenimiento(dto.getKilometrajeMantenimiento());
        v.setFotoUrl(dto.getFotoUrl());

        if (dto.getFechaMantenimiento() != null
                && !dto.getFechaMantenimiento().isEmpty()) {
            v.setFechaMantenimiento(LocalDate.parse(dto.getFechaMantenimiento()));
        }

        v.setFechaActualizacion(LocalDateTime.now());
        return vehiculoRepositorio.save(v);
    }

    // ── CAMBIAR ESTADO ──────────────────────────────────
    public Vehiculo cambiarEstado(String id, String nuevoEstado) {
        Vehiculo v = buscarPorId(id);
        v.setEstado(nuevoEstado);
        v.setFechaActualizacion(LocalDateTime.now());
        return vehiculoRepositorio.save(v);
    }

    // ── ACTUALIZAR KILOMETRAJE ──────────────────────────
    public Vehiculo actualizarKilometraje(String id, Integer nuevoKm) {
        Vehiculo v = buscarPorId(id);
        v.setKilometrajeActual(nuevoKm);
        v.setFechaActualizacion(LocalDateTime.now());

        // Verificar alerta de mantenimiento por km
        if (v.getKilometrajeMantenimiento() != null
                && nuevoKm >= v.getKilometrajeMantenimiento()) {
            v.setEstado("EN_MANTENIMIENTO");
        }

        return vehiculoRepositorio.save(v);
    }

    // ── DESACTIVAR (eliminado lógico) ───────────────────
    public void desactivar(String id) {
        Vehiculo v = buscarPorId(id);
        if (v.getEstado().equals("ALQUILADO")) {
            throw new RuntimeException(
                "No se puede desactivar un vehículo que está alquilado.");
        }
        v.setActivo(false);
        v.setFechaActualizacion(LocalDateTime.now());
        vehiculoRepositorio.save(v);
    }

    // ── ACTIVAR ─────────────────────────────────────────
    public void activar(String id) {
        Vehiculo v = buscarPorId(id);
        v.setActivo(true);
        v.setFechaActualizacion(LocalDateTime.now());
        vehiculoRepositorio.save(v);
    }

    // ── ALERTAS DE MANTENIMIENTO ────────────────────────
    public List<Vehiculo> obtenerAlertasMantenimiento() {
        LocalDate hoy = LocalDate.now();
        LocalDate en7Dias = hoy.plusDays(7);

        return vehiculoRepositorio.findByActivoTrue().stream()
                .filter(v -> {
                    boolean alertaFecha = v.getFechaMantenimiento() != null
                            && !v.getFechaMantenimiento().isAfter(en7Dias);
                    boolean alertaKm = v.getKilometrajeMantenimiento() != null
                            && v.getKilometrajeActual() != null
                            && v.getKilometrajeActual() >= v.getKilometrajeMantenimiento();
                    return alertaFecha || alertaKm;
                })
                .toList();
    }
    
 // Consultar disponibilidad por rango de fechas
 // Por ahora retorna todos los disponibles
 // En Sprint 3 se filtrará según reservas activas en esas fechas
 public List<Vehiculo> consultarDisponibilidad(
         LocalDate fechaInicio, LocalDate fechaFin) {

     if (fechaInicio == null || fechaFin == null) {
         throw new RuntimeException("Las fechas de inicio y fin son obligatorias.");
     }
     if (fechaFin.isBefore(fechaInicio)) {
         throw new RuntimeException(
             "La fecha de fin no puede ser anterior a la fecha de inicio.");
     }
     if (fechaInicio.isBefore(LocalDate.now())) {
         throw new RuntimeException(
             "La fecha de inicio no puede ser en el pasado.");
     }

     // En Sprint 3 aquí se consultarán las reservas activas
     // y se excluirán los vehículos ocupados en esas fechas
     return vehiculoRepositorio.findByEstadoAndActivoTrue("DISPONIBLE");
 }
    
}