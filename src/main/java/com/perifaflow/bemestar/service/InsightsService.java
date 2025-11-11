package com.perifaflow.bemestar.service;

import com.perifaflow.bemestar.api.dto.InsightsDTO;
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

    public InsightsDTO agregadosDTO(String bairro, String de, String ate) {
        var d0 = LocalDate.parse(de);
        var d1 = LocalDate.parse(ate).plusDays(1);

        var t0 = Timestamp.valueOf(LocalDateTime.of(d0.getYear(), d0.getMonth(), d0.getDayOfMonth(), 0, 0));
        var t1 = Timestamp.valueOf(LocalDateTime.of(d1.getYear(), d1.getMonth(), d1.getDayOfMonth(), 0, 0));

        Integer total = jdbc.queryForObject(
                "select count(*) from RITMO_EVENT where BAIRRO=? and ENVIADO_EM >= ? and ENVIADO_EM < ?",
                Integer.class, bairro, t0, t1);

        Integer barulhoAlto = jdbc.queryForObject(
                "select count(*) from RITMO_EVENT where BAIRRO=? and AMBIENTE=2 and ENVIADO_EM >= ? and ENVIADO_EM < ?",
                Integer.class, bairro, t0, t1);

        double percBarulho = (total==null || total==0) ? 0.0 : (barulhoAlto * 1.0 / total);

        return new InsightsDTO(
                bairro,
                new InsightsDTO.Periodo(de, ate),
                total == null ? 0 : total,
                new InsightsDTO.Barreiras(percBarulho)
        );
    }



}
