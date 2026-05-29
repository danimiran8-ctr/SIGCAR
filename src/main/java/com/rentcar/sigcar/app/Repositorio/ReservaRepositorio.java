package com.rentcar.sigcar.app.Repositorio;

import com.rentcar.sigcar.app.Modelo.Reserva;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepositorio extends MongoRepository<Reserva, String> {

    // Reservas por cliente
    List<Reserva> findByIdCliente(String idCliente);

    // Reservas por vehículo
    List<Reserva> findByIdVehiculo(String idVehiculo);

    // Reservas por estado
    List<Reserva> findByEstado(String estado);

    // Reservas activas de un vehículo en un rango de fechas
    // Esta es la consulta clave para validar disponibilidad
    @Query("{ 'idVehiculo': ?0, 'estado': { $in: ['PENDIENTE','CONFIRMADA','EN_CURSO'] }, " +
           "$or: [ { 'fechaInicio': { $lte: ?2 }, 'fechaFin': { $gte: ?1 } } ] }")
    List<Reserva> findReservasActivasEnFechas(
        String idVehiculo,
        LocalDate fechaInicio,
        LocalDate fechaFin
    );

    // Reservas por cliente y estado
    List<Reserva> findByIdClienteAndEstado(String idCliente, String estado);
}
