package com.perifaflow.bemestar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service @RequiredArgsConstructor
public class InsightsService {
    private final JdbcTemplate jdbc;

    // MVP: agregado simples por bairro (ex.: % de barulho alto = ambiente==2)
    public Map<String,Object> agregados(String bairro, String de, String ate){
        Integer total = jdbc.queryForObject(
                "select count(*) from RITMO_EVENT where BAIRRO=? and ENVIADO_EM between ? and ?",
                Integer.class, bairro, de, ate);
        Integer barulhoAlto = jdbc.queryForObject(
                "select count(*) from RITMO_EVENT where BAIRRO=? and AMBIENTE=2 and ENVIADO_EM between ? and ?",
                Integer.class, bairro, de, ate);

        double percBarulho = (total==0)?0.0: (barulhoAlto * 1.0 / total);
        return Map.of(
                "bairro", bairro,
                "periodo", Map.of("de",de,"ate",ate),
                "amostras", total,
                "barreiras", Map.of("barulho_alto", percBarulho)
        );
    }
}
