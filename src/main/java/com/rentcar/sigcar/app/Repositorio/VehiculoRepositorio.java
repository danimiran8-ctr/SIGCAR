package com.rentcar.sigcar.app.Repositorio;

import com.rentcar.sigcar.app.Modelo.Vehiculo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepositorio extends MongoRepository<Vehiculo, String> {

    // Buscar por placa
    Optional<Vehiculo> findByPlaca(String placa);

    // Verificar si existe la placa
    Boolean existsByPlaca(String placa);

    // Listar por estado
    List<Vehiculo> findByEstado(String estado);

    // Listar por categoría
    List<Vehiculo> findByCategoria(String categoria);

    // Listar activos
    List<Vehiculo> findByActivoTrue();

    // Listar por estado y activo
    List<Vehiculo> findByEstadoAndActivoTrue(String estado);

    // Listar por categoría y activo
    List<Vehiculo> findByCategoriaAndActivoTrue(String categoria);
    
 // Buscar vehículos disponibles que NO estén en una lista de IDs
    @Query("{ 'estado': 'DISPONIBLE', 'activo': true, '_id': { $nin: ?0 } }")
    List<Vehiculo> findDisponiblesExcluyendo(List<String> idsOcupados);
    
 // Listar por categoría, estado y activo
    List<Vehiculo> findByCategoriaAndEstadoAndActivoTrue(String categoria, String estado);
    
}
