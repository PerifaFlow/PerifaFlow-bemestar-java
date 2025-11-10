package com.perifaflow.bemestar.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record RitmoRegistroDTO(
        @NotBlank @Size(max = 120) String bairro,
        @Pattern(regexp = "^(MANHA|TARDE|NOITE)$", message = "turno deve ser MANHA|TARDE|NOITE")
        @NotBlank String turno,
        @PositiveOrZero @Max(2) int energia,
        @PositiveOrZero @Max(2) int ambiente,
        @PositiveOrZero @Max(2) int condicao,
        boolean optIn
) {}