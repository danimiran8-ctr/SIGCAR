package com.rentcar.sigcar.app.Repositorio;

import com.rentcar.sigcar.app.Modelo.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByCorreo(String correo);

    Boolean existsByCorreo(String correo);
}