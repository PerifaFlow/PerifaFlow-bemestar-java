package com.perifaflow.bemestar.messaging;

import java.time.OffsetDateTime;

public record RitmoEventMessage(
        Long id,
        String bairro,
        String turno,
        int energia,
        int ambiente,
        int condicao,
        OffsetDateTime enviadoEm
) {}
