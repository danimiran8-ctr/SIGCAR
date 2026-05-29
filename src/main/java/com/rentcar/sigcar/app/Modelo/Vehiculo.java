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
@Document(collection = "vehiculos")
public class Vehiculo {

    @Id
    private String id;

    @Indexed(unique = true)
    private String placa;

    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private String categoria; // ECONOMICO, SUV, CAMIONETA, LUJO, FAMILIAR
    private Integer numPuertas;
    private Integer capacidadPasajeros;
    private String transmision; // AUTOMATICO, MANUAL
    private Double precioPorDia;
    private Integer kilometrajeActual;
    private Integer kilometrajeMantenimiento; // km límite para alerta
    private java.time.LocalDate fechaMantenimiento; // fecha límite para alerta
    private String estado; // DISPONIBLE, ALQUILADO, EN_MANTENIMIENTO
    private String fotoUrl;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}