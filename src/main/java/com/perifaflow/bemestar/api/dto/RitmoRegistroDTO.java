package com.perifaflow.bemestar.api.dto;

import jakarta.validation.constraints.*;

public record RitmoRegistroDTO(
        @NotBlank String bairro,
        @NotBlank String turno,           // MANHA|TARDE|NOITE
        @Min(0) @Max(2) int energia,
        @Min(0) @Max(2) int ambiente,
        @Min(0) @Max(2) int condicao,
        boolean optIn                     // apenas controla se enviaremos o evento (já é anônimo)
) {}
