package com.rentcar.sigcar.app.DTO;

import lombok.Data;

@Data
public class RegistroUsuarioDTO {
    private String nombres;
    private String apellidos;
    private String correo;
    private String password;
    private String telefono;
    private String rol;
}