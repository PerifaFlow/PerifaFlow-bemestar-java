package com.perifaflow.bemestar.api.dto;

import jakarta.validation.constraints.*;

public record SugestaoMissaoRequest(
        @NotBlank String perfil,          // ex.: suporte | conteudo | administrativo
        Integer ultimaEnergia,            // 0..2 (pode ser null)
        Integer ultimoAmbiente,
        Integer ultimaCondicao
) {}
