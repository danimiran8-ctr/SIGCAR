package com.rentcar.sigcar.app.Modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "contratos")
public class Contrato {

    @Id
    private String id;

    private String idReserva;
    private String numeroContrato;
    private String urlPdf;
    private String condiciones;
    private LocalDateTime fechaGeneracion;
    private String idGeneradoPor;
}