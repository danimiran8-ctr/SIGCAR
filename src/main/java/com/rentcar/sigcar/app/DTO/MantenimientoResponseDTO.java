package com.rentcar.sigcar.app.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MantenimientoResponseDTO {
    private String id;
    private String idVehiculo;
    private String placaVehiculo;
    private String marcaModelo;
    private String idRegistradoPor;
    private String nombreTecnico;
    private String tipo;
    private LocalDate fecha;
    private String descripcion;
    private Double costo;
    private String tecnico;
    private Integer proximoKm;
    private LocalDate proximaFecha;
    private String observaciones;
    private LocalDateTime fechaCreacion;
}