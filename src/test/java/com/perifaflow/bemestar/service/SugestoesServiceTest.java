package com.perifaflow.bemestar.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perifaflow.bemestar.api.dto.SugestaoMissaoRequest;
import com.perifaflow.bemestar.api.dto.SugestaoMissaoResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@org.springframework.test.context.ActiveProfiles("test")
class SugestoesServiceTest {

    private SugestoesService newServiceForFallback() {
        return new SugestoesService(null, new ObjectMapper());
    }

    @Test
    void fallback_quandoSomaMenorOuIgual2_retornaCURTAOfflineTrue() {
        var service = newServiceForFallback();
        var req = new SugestaoMissaoRequest("suporte", 0, 1, 1); // soma = 2

        SugestaoMissaoResponse resp = service.sugerir(req);

        assertThat(resp.complexidade()).isEqualTo("CURTA");
        assertThat(resp.offline()).isTrue();
        assertThat(resp.mensagem()).contains("curtinha", "offline");
    }

    @Test
    void fallback_quandoSomaMaior2_retornaNORMALOfflineFalse() {
        var service = newServiceForFallback();
        var req = new SugestaoMissaoRequest("conteudo", 2, 1, 1); // soma = 4

        SugestaoMissaoResponse resp = service.sugerir(req);

        assertThat(resp.complexidade()).isEqualTo("NORMAL");
        assertThat(resp.offline()).isFalse();
        assertThat(resp.mensagem()).contains("Missão normal");
    }
}
