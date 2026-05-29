package com.rentcar.sigcar.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String correo;
    private String rol;
    private String nombres;
}