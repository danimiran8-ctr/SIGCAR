package com.rentcar.sigcar.app.Repositorio;

import com.rentcar.sigcar.app.Modelo.Pago;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepositorio extends MongoRepository<Pago, String> {
    List<Pago> findByIdCliente(String idCliente);
    List<Pago> findByIdAgente(String idAgente);
    List<Pago> findByEstado(String estado);
    Optional<Pago> findByIdReserva(String idReserva);
    List<Pago> findByFechaPagoBetween(LocalDateTime inicio, LocalDateTime fin);
}