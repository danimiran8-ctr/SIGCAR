package com.rentcar.sigcar.app.Modelo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "pagos")
public class Pago {
    @Id
    private String id;
    private String idReserva;
    private String idCliente;
    private String idAgente;
    private Double monto;
    private String metodoPago;     // EFECTIVO, TRANSFERENCIA
    private String estado;         // PAGADO, PENDIENTE
    private String observaciones;
    private String numeroPago;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}