package com.rentcar.sigcar.app.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MantenimientoDTO {
    private String idVehiculo;
    private String tipo;           // PREVENTIVO, CORRECTIVO
    private String fecha;          // formato: yyyy-MM-dd
    private String descripcion;
    private Double costo;
    private String tecnico;
    private Integer proximoKm;
    private String proximaFecha;   // formato: yyyy-MM-dd
    private String observaciones;
}