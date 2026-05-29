package com.rentcar.sigcar.app.Modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nombres;
    private String apellidos;

    @Indexed(unique = true)
    private String correo;

    private String password;
    private String telefono;
    private String fotoUrl;

    private String rol; // ADMINISTRADOR, AGENTE, CLIENTE, MECANICO, AUDITOR

    private Boolean estado; // true = activo, false = inactivo

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}