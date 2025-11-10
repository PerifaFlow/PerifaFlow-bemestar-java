package com.perifaflow.bemestar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service @RequiredArgsConstructor
public class InsightsService {
    private final JdbcTemplate jdbc;

    public Map<String,Object> agregados(String bairro, String de, String ate){
        LocalDate d0 = LocalDate.parse(de);
        LocalDate d1 = LocalDate.parse(ate).plusDays(1);

        Timestamp t0 = Timestamp.valueOf(LocalDateTime.of(d0.getYear(), d0.getMonth(), d0.getDayOfMonth(), 0, 0));
        Timestamp t1 = Timestamp.valueOf(LocalDateTime.of(d1.getYear(), d1.getMonth(), d1.getDayOfMonth(), 0, 0));

        Integer total = jdbc.queryForObject(
                "select count(*) from RITMO_EVENT where BAIRRO=? and ENVIADO_EM >= ? and ENVIADO_EM < ?",
                Integer.class, bairro, t0, t1);

        Integer barulhoAlto = jdbc.queryForObject(
                "select count(*) from RITMO_EVENT where BAIRRO=? and AMBIENTE=2 and ENVIADO_EM >= ? and ENVIADO_EM < ?",
                Integer.class, bairro, t0, t1);

        double percBarulho = (total==0)?0.0: (barulhoAlto * 1.0 / total);
        return Map.of(
                "bairro", bairro,
                "periodo", Map.of("de", de, "ate", ate),
                "amostras", total,
                "barreiras", Map.of("barulho_alto", percBarulho)
        );
    }
}
