package com.perifaflow.bemestar.api.dto;

public record InsightsDTO(
        String bairro,
        Periodo periodo,     // de/ate
        int amostras,
        Barreiras barreiras  // barulho_alto
) {
    public record Periodo(String de, String ate) {}
    public record Barreiras(double barulho_alto) {}
}
