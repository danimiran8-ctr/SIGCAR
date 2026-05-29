package com.rentcar.sigcar.app.Servicio;

import com.rentcar.sigcar.app.DTO.ReservaDTO;
import com.rentcar.sigcar.app.DTO.ReservaResponseDTO;
import com.rentcar.sigcar.app.Modelo.Reserva;
import com.rentcar.sigcar.app.Modelo.Usuario;
import com.rentcar.sigcar.app.Modelo.Vehiculo;
import com.rentcar.sigcar.app.Repositorio.ReservaRepositorio;
import com.rentcar.sigcar.app.Repositorio.UsuarioRepositorio;
import com.rentcar.sigcar.app.Repositorio.VehiculoRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaServicio {

    @Autowired
    private ReservaRepositorio reservaRepositorio;

    @Autowired
    private VehiculoRepositorio vehiculoRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    // ── CREAR RESERVA ───────────────────────────────────
    public ReservaResponseDTO crear(ReservaDTO dto, String idAgente) {

        // Parsear fechas
        LocalDate fechaInicio = LocalDate.parse(dto.getFechaInicio());
        LocalDate fechaFin    = LocalDate.parse(dto.getFechaFin());

        // Validar fechas
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new RuntimeException(
                "La fecha de fin debe ser posterior a la fecha de inicio.");
        }
        if (fechaInicio.isBefore(LocalDate.now())) {
            throw new RuntimeException(
                "La fecha de inicio no puede ser en el pasado.");
        }

        // Validar que el vehículo existe
        Vehiculo vehiculo = vehiculoRepositorio.findById(dto.getIdVehiculo())
            .orElseThrow(() -> new RuntimeException("Vehículo no encontrado."));

        // Validar que el vehículo está disponible
        if (!vehiculo.getEstado().equals("DISPONIBLE")) {
            throw new RuntimeException(
                "El vehículo no está disponible. Estado actual: " + vehiculo.getEstado());
        }

        // Validar que no hay reservas activas en esas fechas
        List<Reserva> reservasExistentes = reservaRepositorio
            .findReservasActivasEnFechas(dto.getIdVehiculo(), fechaInicio, fechaFin);

        if (!reservasExistentes.isEmpty()) {
            throw new RuntimeException(
                "El vehículo ya tiene una reserva activa en las fechas seleccionadas. " +
                "Por favor elige otras fechas.");
        }

        // Validar que el cliente existe
        Usuario cliente = usuarioRepositorio.findById(dto.getIdCliente())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));

        // Calcular días y valor total
        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        double valorTotal = dias * vehiculo.getPrecioPorDia();

        // Crear la reserva en estado PENDIENTE
        // El vehículo NO cambia de estado aquí — el scheduler lo hará
        // cuando llegue la fecha de inicio
        Reserva reserva = new Reserva();
        reserva.setIdCliente(dto.getIdCliente());
        reserva.setIdVehiculo(dto.getIdVehiculo());
        reserva.setIdAgente(idAgente);
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFin(fechaFin);
        reserva.setDiasAlquiler((int) dias);
        reserva.setValorTotal(valorTotal);
        reserva.setEstado("PENDIENTE");
        reserva.setObservaciones(dto.getObservaciones());
        reserva.setFechaCreacion(LocalDateTime.now());
        reserva.setFechaActualizacion(LocalDateTime.now());

        Reserva guardada = reservaRepositorio.save(reserva);

        return mapearResponse(guardada, cliente, vehiculo);
    }

    // ── LISTAR TODAS ────────────────────────────────────
    public List<ReservaResponseDTO> listarTodas() {
        return reservaRepositorio.findAll()
            .stream()
            .map(this::mapearResponseBasico)
            .collect(Collectors.toList());
    }

    // ── LISTAR POR CLIENTE ──────────────────────────────
    public List<ReservaResponseDTO> listarPorCliente(String idCliente) {
        return reservaRepositorio.findByIdCliente(idCliente)
            .stream()
            .map(this::mapearResponseBasico)
            .collect(Collectors.toList());
    }

    // ── LISTAR POR ESTADO ───────────────────────────────
    public List<ReservaResponseDTO> listarPorEstado(String estado) {
        return reservaRepositorio.findByEstado(estado)
            .stream()
            .map(this::mapearResponseBasico)
            .collect(Collectors.toList());
    }

    // ── BUSCAR POR ID ───────────────────────────────────
    public Reserva buscarPorId(String id) {
        return reservaRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Reserva no encontrada con ID: " + id));
    }

    // ── CAMBIAR ESTADO ──────────────────────────────────
    public Reserva cambiarEstado(String id, String nuevoEstado) {
        Reserva reserva = buscarPorId(id);
        reserva.setEstado(nuevoEstado);
        reserva.setFechaActualizacion(LocalDateTime.now());
        Reserva guardada = reservaRepositorio.save(reserva);

        // Si se pone EN_CURSO manualmente → vehículo ALQUILADO
        if (nuevoEstado.equals("EN_CURSO")) {
            vehiculoRepositorio.findById(reserva.getIdVehiculo()).ifPresent(v -> {
                v.setEstado("ALQUILADO");
                v.setFechaActualizacion(LocalDateTime.now());
                vehiculoRepositorio.save(v);
            });
        }

        // Si se cancela o finaliza → liberar el vehículo
        if (nuevoEstado.equals("CANCELADA") || nuevoEstado.equals("FINALIZADA")) {
            vehiculoRepositorio.findById(reserva.getIdVehiculo()).ifPresent(v -> {
                v.setEstado("DISPONIBLE");
                v.setFechaActualizacion(LocalDateTime.now());
                vehiculoRepositorio.save(v);
            });
        }

        return guardada;
    }

    // ── CANCELAR RESERVA ────────────────────────────────
    public Reserva cancelar(String id) {
        Reserva reserva = buscarPorId(id);
        if (reserva.getEstado().equals("FINALIZADA")) {
            throw new RuntimeException(
                "No se puede cancelar una reserva ya finalizada.");
        }
        return cambiarEstado(id, "CANCELADA");
    }

    // ── MAPEAR RESPONSE ─────────────────────────────────
    private ReservaResponseDTO mapearResponse(
            Reserva r, Usuario cliente, Vehiculo vehiculo) {
        ReservaResponseDTO dto = new ReservaResponseDTO();
        dto.setId(r.getId());
        dto.setIdCliente(r.getIdCliente());
        dto.setNombreCliente(cliente.getNombres() + " " + cliente.getApellidos());
        dto.setIdVehiculo(r.getIdVehiculo());
        dto.setPlacaVehiculo(vehiculo.getPlaca());
        dto.setMarcaModelo(vehiculo.getMarca() + " " + vehiculo.getModelo());
        dto.setFechaInicio(r.getFechaInicio());
        dto.setFechaFin(r.getFechaFin());
        dto.setDiasAlquiler(r.getDiasAlquiler());
        dto.setValorTotal(r.getValorTotal());
        dto.setEstado(r.getEstado());
        dto.setFechaCreacion(r.getFechaCreacion());
        return dto;
    }

    private ReservaResponseDTO mapearResponseBasico(Reserva r) {
        ReservaResponseDTO dto = new ReservaResponseDTO();
        dto.setId(r.getId());
        dto.setIdCliente(r.getIdCliente());
        dto.setIdVehiculo(r.getIdVehiculo());
        dto.setFechaInicio(r.getFechaInicio());
        dto.setFechaFin(r.getFechaFin());
        dto.setDiasAlquiler(r.getDiasAlquiler());
        dto.setValorTotal(r.getValorTotal());
        dto.setEstado(r.getEstado());
        dto.setFechaCreacion(r.getFechaCreacion());

        // Enriquecer con datos del cliente
        usuarioRepositorio.findById(r.getIdCliente()).ifPresent(u ->
            dto.setNombreCliente(u.getNombres() + " " + u.getApellidos()));

        // Enriquecer con datos del vehículo
        vehiculoRepositorio.findById(r.getIdVehiculo()).ifPresent(v -> {
            dto.setPlacaVehiculo(v.getPlaca());
            dto.setMarcaModelo(v.getMarca() + " " + v.getModelo());
        });

        return dto;
    }
}