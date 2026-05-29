package com.rentcar.sigcar.app.DTO;

import lombok.Data;

@Data
public class ReservaDTO {
    private String idCliente;
    private String idVehiculo;
    private String fechaInicio;  // formato: yyyy-MM-dd
    private String fechaFin;     // formato: yyyy-MM-dd
    private String observaciones;
}