package com.rentcar.sigcar.app.Modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "reservas")
public class Reserva {

    @Id
    private String id;

    private String idCliente;       // ref usuarios
    private String idVehiculo;      // ref vehiculos
    private String idAgente;        // quien creó la reserva

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer diasAlquiler;
    private Double valorTotal;

    // PENDIENTE, CONFIRMADA, EN_CURSO, FINALIZADA, CANCELADA
    private String estado;

    private String observaciones;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}