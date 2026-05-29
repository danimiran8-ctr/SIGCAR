package com.rentcar.sigcar.app.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaResponseDTO {
    private String id;
    private String idCliente;
    private String nombreCliente;
    private String idVehiculo;
    private String placaVehiculo;
    private String marcaModelo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer diasAlquiler;
    private Double valorTotal;
    private String estado;
    private LocalDateTime fechaCreacion;
}