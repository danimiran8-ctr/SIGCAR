package com.rentcar.sigcar.app.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoResponseDTO {
    private String id;
    private String idReserva;
    private String idCliente;
    private String nombreCliente;
    private String placaVehiculo;
    private String marcaModelo;
    private Double monto;
    private String metodoPago;
    private String estado;
    private String numeroPago;
    private String observaciones;
    private LocalDateTime fechaPago;
}