package com.perifaflow.bemestar.api.dto;

import jakarta.validation.constraints.*;

public record RitmoRegistroDTO(
        @Pattern(regexp="MANHA|TARDE|NOITE", message="turno deve ser MANHA|TARDE|NOITE")
        @NotBlank String bairro,
        @NotBlank String turno,
        @Min(0) @Max(2) int energia,
        @Min(0) @Max(2) int ambiente,
        @Min(0) @Max(2) int condicao,
        boolean optIn
        // apenas controla se enviaremos o evento (já é anônimo)
) {}
