package com.rentcar.sigcar.app.Servicio;

import com.rentcar.sigcar.app.DTO.MantenimientoDTO;
import com.rentcar.sigcar.app.DTO.MantenimientoResponseDTO;
import com.rentcar.sigcar.app.Modelo.Mantenimiento;
import com.rentcar.sigcar.app.Modelo.Vehiculo;
import com.rentcar.sigcar.app.Repositorio.MantenimientoRepositorio;
import com.rentcar.sigcar.app.Repositorio.VehiculoRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MantenimientoServicio {

    @Autowired private MantenimientoRepositorio mantenimientoRepositorio;
    @Autowired private VehiculoRepositorio vehiculoRepositorio;

    // ── REGISTRAR MANTENIMIENTO ─────────────────────────
    public MantenimientoResponseDTO registrar(MantenimientoDTO dto, String idUsuario) {

        Vehiculo vehiculo = vehiculoRepositorio.findById(dto.getIdVehiculo())
            .orElseThrow(() -> new RuntimeException("Vehículo no encontrado."));

        // Validar que el vehículo no esté alquilado
        if ("ALQUILADO".equals(vehiculo.getEstado())) {
            throw new RuntimeException(
                "No se puede registrar mantenimiento. " +
                "El vehículo está actualmente alquilado.");
        }

        if (!List.of("PREVENTIVO", "CORRECTIVO").contains(dto.getTipo())) {
            throw new RuntimeException("Tipo inválido. Use PREVENTIVO o CORRECTIVO.");
        }

        Mantenimiento m = new Mantenimiento();
        m.setIdVehiculo(dto.getIdVehiculo());
        m.setIdRegistradoPor(idUsuario);
        m.setTipo(dto.getTipo());
        m.setFecha(LocalDate.parse(dto.getFecha()));
        m.setDescripcion(dto.getDescripcion());
        m.setCosto(dto.getCosto());
        m.setTecnico(dto.getTecnico());
        m.setObservaciones(dto.getObservaciones());
        m.setFechaCreacion(LocalDateTime.now());
        m.setFechaActualizacion(LocalDateTime.now());

        if (dto.getProximoKm() != null) {
            m.setProximoKm(dto.getProximoKm());
            vehiculo.setKilometrajeMantenimiento(dto.getProximoKm());
        }
        if (dto.getProximaFecha() != null && !dto.getProximaFecha().isEmpty()) {
            LocalDate proxFecha = LocalDate.parse(dto.getProximaFecha());
            m.setProximaFecha(proxFecha);
            vehiculo.setFechaMantenimiento(proxFecha);
        }

        // Al registrar el mantenimiento el vehículo pasa a EN_MANTENIMIENTO
        // y vuelve a DISPONIBLE al terminar — indicado por el campo proximoKm
        // o proximaFecha. Si no tiene próximo programado, queda DISPONIBLE.
        if (dto.getProximoKm() != null || 
            (dto.getProximaFecha() != null && !dto.getProximaFecha().isEmpty())) {
            // Tiene próximo mantenimiento programado → vuelve a DISPONIBLE
            vehiculo.setEstado("DISPONIBLE");
        } else {
            // Sin próximo programado → también DISPONIBLE, el mecánico terminó
            vehiculo.setEstado("DISPONIBLE");
        }

        vehiculo.setFechaActualizacion(LocalDateTime.now());
        vehiculoRepositorio.save(vehiculo);

        Mantenimiento guardado = mantenimientoRepositorio.save(m);
        return mapearResponse(guardado, vehiculo);
    }

    // ── INICIAR MANTENIMIENTO ───────────────────────────
    // Cambia el vehículo a EN_MANTENIMIENTO
    public void iniciarMantenimiento(String idVehiculo) {
        Vehiculo vehiculo = vehiculoRepositorio.findById(idVehiculo)
            .orElseThrow(() -> new RuntimeException("Vehículo no encontrado."));

        if ("ALQUILADO".equals(vehiculo.getEstado())) {
            throw new RuntimeException(
                "No se puede poner en mantenimiento. El vehículo está alquilado.");
        }

        vehiculo.setEstado("EN_MANTENIMIENTO");
        vehiculo.setFechaActualizacion(LocalDateTime.now());
        vehiculoRepositorio.save(vehiculo);
    }

    // ── LISTAR TODOS ────────────────────────────────────
    public List<MantenimientoResponseDTO> listarTodos() {
        return mantenimientoRepositorio.findAll()
            .stream().map(this::mapearResponseBasico)
            .collect(Collectors.toList());
    }

    // ── LISTAR POR VEHÍCULO ─────────────────────────────
    public List<MantenimientoResponseDTO> listarPorVehiculo(String idVehiculo) {
        return mantenimientoRepositorio
            .findByIdVehiculoOrderByFechaDesc(idVehiculo)
            .stream().map(this::mapearResponseBasico)
            .collect(Collectors.toList());
    }

    // ── LISTAR POR TIPO ─────────────────────────────────
    public List<MantenimientoResponseDTO> listarPorTipo(String tipo) {
        return mantenimientoRepositorio.findByTipo(tipo)
            .stream().map(this::mapearResponseBasico)
            .collect(Collectors.toList());
    }

    // ── BUSCAR POR ID ───────────────────────────────────
    public Mantenimiento buscarPorId(String id) {
        return mantenimientoRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Mantenimiento no encontrado con ID: " + id));
    }

    // ── ELIMINAR ────────────────────────────────────────
    public void eliminar(String id) {
        buscarPorId(id);
        mantenimientoRepositorio.deleteById(id);
    }

    // ── MAPEAR RESPONSE ─────────────────────────────────
    private MantenimientoResponseDTO mapearResponse(
            Mantenimiento m, Vehiculo vehiculo) {
        MantenimientoResponseDTO dto = new MantenimientoResponseDTO();
        dto.setId(m.getId());
        dto.setIdVehiculo(m.getIdVehiculo());
        dto.setPlacaVehiculo(vehiculo.getPlaca());
        dto.setMarcaModelo(vehiculo.getMarca() + " " + vehiculo.getModelo());
        dto.setIdRegistradoPor(m.getIdRegistradoPor());
        dto.setTipo(m.getTipo());
        dto.setFecha(m.getFecha());
        dto.setDescripcion(m.getDescripcion());
        dto.setCosto(m.getCosto());
        dto.setTecnico(m.getTecnico());
        dto.setProximoKm(m.getProximoKm());
        dto.setProximaFecha(m.getProximaFecha());
        dto.setObservaciones(m.getObservaciones());
        dto.setFechaCreacion(m.getFechaCreacion());
        return dto;
    }

    private MantenimientoResponseDTO mapearResponseBasico(Mantenimiento m) {
        MantenimientoResponseDTO dto = new MantenimientoResponseDTO();
        dto.setId(m.getId());
        dto.setIdVehiculo(m.getIdVehiculo());
        dto.setIdRegistradoPor(m.getIdRegistradoPor());
        dto.setTipo(m.getTipo());
        dto.setFecha(m.getFecha());
        dto.setDescripcion(m.getDescripcion());
        dto.setCosto(m.getCosto());
        dto.setTecnico(m.getTecnico());
        dto.setProximoKm(m.getProximoKm());
        dto.setProximaFecha(m.getProximaFecha());
        dto.setObservaciones(m.getObservaciones());
        dto.setFechaCreacion(m.getFechaCreacion());

        vehiculoRepositorio.findById(m.getIdVehiculo()).ifPresent(v -> {
            dto.setPlacaVehiculo(v.getPlaca());
            dto.setMarcaModelo(v.getMarca() + " " + v.getModelo());
        });

        return dto;
    }
}