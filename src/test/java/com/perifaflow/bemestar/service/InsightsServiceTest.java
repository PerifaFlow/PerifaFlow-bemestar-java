package com.perifaflow.bemestar.service;

import com.perifaflow.bemestar.api.dto.InsightsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(InsightsService.class)
class InsightsServiceTest {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    InsightsService service;

    @BeforeEach
    void setupSchema() {
        jdbc.execute("""
            CREATE TABLE RITMO_EVENT(
                ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                BAIRRO VARCHAR(120) NOT NULL,
                TURNO VARCHAR(16) NOT NULL,
                ENERGIA INT NOT NULL,
                AMBIENTE INT NOT NULL,
                CONDICAO INT NOT NULL,
                ENVIADO_EM TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        // 2 registros com AMBIENTE=2 (barulho alto) e 1 sem
        jdbc.update("INSERT INTO RITMO_EVENT (BAIRRO,TURNO,ENERGIA,AMBIENTE,CONDICAO) VALUES (?,?,?,?,?)",
                "Vila Nova","MANHA",0,2,1);
        jdbc.update("INSERT INTO RITMO_EVENT (BAIRRO,TURNO,ENERGIA,AMBIENTE,CONDICAO) VALUES (?,?,?,?,?)",
                "Vila Nova","TARDE",2,2,0);
        jdbc.update("INSERT INTO RITMO_EVENT (BAIRRO,TURNO,ENERGIA,AMBIENTE,CONDICAO) VALUES (?,?,?,?,?)",
                "Vila Nova","NOITE",1,1,1);
    }

    @Test
    void agregadosDTO_comJanelaMeiaAberta() {
        InsightsDTO dto = service.agregadosDTO("Vila Nova","2025-01-01","2025-12-31");
        assertThat(dto.bairro()).isEqualTo("Vila Nova");
        assertThat(dto.amostras()).isEqualTo(3);
        // 2 de 3 com AMBIENTE=2 => ~0.6666
        assertThat(dto.barreiras().barulho_alto()).isBetween(0.66, 0.67);
    }
}
