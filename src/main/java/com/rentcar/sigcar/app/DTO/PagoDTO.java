package com.rentcar.sigcar.app.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoDTO {
    private String idReserva;
    private String metodoPago;     // EFECTIVO, TRANSFERENCIA
    private String observaciones;
}
