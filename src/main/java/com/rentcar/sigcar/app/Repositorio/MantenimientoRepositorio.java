package com.rentcar.sigcar.app.Repositorio;

import com.rentcar.sigcar.app.Modelo.Mantenimiento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MantenimientoRepositorio extends MongoRepository<Mantenimiento, String> {
    List<Mantenimiento> findByIdVehiculo(String idVehiculo);
    List<Mantenimiento> findByTipo(String tipo);
    List<Mantenimiento> findByIdVehiculoOrderByFechaDesc(String idVehiculo);
}