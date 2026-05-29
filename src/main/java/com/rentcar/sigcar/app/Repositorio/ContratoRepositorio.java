package com.rentcar.sigcar.app.Repositorio;

import com.rentcar.sigcar.app.Modelo.Contrato;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContratoRepositorio extends MongoRepository<Contrato, String> {

    Optional<Contrato> findByIdReserva(String idReserva);
}