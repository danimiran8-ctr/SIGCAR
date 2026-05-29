package com.rentcar.sigcar.app.Modelo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "mantenimientos")
public class Mantenimiento {
    @Id
    private String id;
    private String idVehiculo;
    private String idRegistradoPor;
    private String tipo;              // PREVENTIVO, CORRECTIVO
    private LocalDate fecha;
    private String descripcion;
    private Double costo;
    private String tecnico;
    private Integer proximoKm;        // km para próximo mantenimiento
    private LocalDate proximaFecha;   // fecha para próximo mantenimiento
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}