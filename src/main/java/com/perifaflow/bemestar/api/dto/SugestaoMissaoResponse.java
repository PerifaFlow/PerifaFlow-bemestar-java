package com.perifaflow.bemestar.api.dto;

public record SugestaoMissaoResponse(
        String missaoId,                  // id lógico (a .NET mapeia para a missão real)
        String complexidade,              // CURTA | NORMAL
        boolean offline,
        String mensagem
) {}
