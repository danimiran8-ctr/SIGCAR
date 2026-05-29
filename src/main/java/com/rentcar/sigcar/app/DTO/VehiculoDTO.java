package com.rentcar.sigcar.app.DTO;

import lombok.Data;

@Data
public class VehiculoDTO {

    private String placa;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private String categoria;
    private Integer numPuertas;
    private Integer capacidadPasajeros;
    private String transmision;
    private Double precioPorDia;
    private Integer kilometrajeActual;
    private Integer kilometrajeMantenimiento;
    private String fechaMantenimiento; // formato: yyyy-MM-dd
    private String fotoUrl;
}