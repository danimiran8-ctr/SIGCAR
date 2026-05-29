package com.rentcar.sigcar.app.Servicio;

import com.rentcar.sigcar.app.Modelo.Reserva;
import com.rentcar.sigcar.app.Modelo.Vehiculo;
import com.rentcar.sigcar.app.Repositorio.ReservaRepositorio;
import com.rentcar.sigcar.app.Repositorio.VehiculoRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReservaScheduler {

    @Autowired
    private ReservaRepositorio reservaRepositorio;

    @Autowired
    private VehiculoRepositorio vehiculoRepositorio;

    // Se ejecuta cada dia a media noche
    @Scheduled(cron = "0 0 0 * * *")
    public void actualizarEstadosReservas() {
        System.out.println("✅ Scheduler ejecutado: " + LocalDate.now());
        LocalDate hoy = LocalDate.now();

        List<Reserva> reservas = reservaRepositorio.findAll();

        for (Reserva reserva : reservas) {

            // Ignorar reservas ya terminadas
            if ("CANCELADA".equals(reserva.getEstado()) ||
                "FINALIZADA".equals(reserva.getEstado())) {
                continue;
            }

            Vehiculo vehiculo = vehiculoRepositorio
                .findById(reserva.getIdVehiculo())
                .orElse(null);

            if (vehiculo == null) continue;

            // Reserva CONFIRMADA y hoy es la fecha de inicio → pasa a EN_CURSO
            // y el vehículo a ALQUILADO
            if ("CONFIRMADA".equals(reserva.getEstado()) &&
                !hoy.isBefore(reserva.getFechaInicio())) {
                reserva.setEstado("EN_CURSO");
                reservaRepositorio.save(reserva);
                vehiculo.setEstado("ALQUILADO");
                vehiculoRepositorio.save(vehiculo);
            }

            // Reserva EN_CURSO y hoy es después de la fecha fin → pasa a FINALIZADA
            // y el vehículo vuelve a DISPONIBLE
            else if ("EN_CURSO".equals(reserva.getEstado()) &&
                     hoy.isAfter(reserva.getFechaFin())) {
                reserva.setEstado("FINALIZADA");
                reservaRepositorio.save(reserva);
                vehiculo.setEstado("DISPONIBLE");
                vehiculoRepositorio.save(vehiculo);
            }

            // Reserva PENDIENTE y ya pasó la fecha de inicio → se cancela automáticamente
            // (nadie la confirmó a tiempo)
            else if ("PENDIENTE".equals(reserva.getEstado()) &&
                     hoy.isAfter(reserva.getFechaInicio())) {
                reserva.setEstado("CANCELADA");
                reservaRepositorio.save(reserva);
                vehiculo.setEstado("DISPONIBLE");
                vehiculoRepositorio.save(vehiculo);
            }
        }
    }
}