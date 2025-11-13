package com.perifaflow.bemestar.messaging;

import com.perifaflow.bemestar.domain.Turno;
import lombok.*;

import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RitmoEventMessage {
    private Long id;
    private String bairro;
    private Turno turno;
    private Integer energia;
    private Integer ambiente;
    private Integer condicao;
    private OffsetDateTime enviadoEm;
}
