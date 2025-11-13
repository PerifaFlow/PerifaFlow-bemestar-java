package com.perifaflow.bemestar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perifaflow.bemestar.api.BemEstarController;
import com.perifaflow.bemestar.api.dto.SugestaoMissaoRequest;
import com.perifaflow.bemestar.api.dto.SugestaoMissaoResponse;
import com.perifaflow.bemestar.config.GlobalExceptionHandler;
import com.perifaflow.bemestar.config.I18nConfig;
import com.perifaflow.bemestar.service.InsightsService;
import com.perifaflow.bemestar.service.RitmoService;
import com.perifaflow.bemestar.service.SugestoesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(BemEstarController.class)
@Import({I18nConfig.class, GlobalExceptionHandler.class})
class BemEstarControllerTest  {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    RitmoService ritmoService;

    @MockBean
    InsightsService insightsService;

    @MockBean
    SugestoesService sugestoesService;

    @Test
    void POST_registro_ok_retorna202() throws Exception {
        String body = """
                {"bairro":"Vila Nova","turno":"NOITE","energia":1,"ambiente":2,"condicao":1,"optIn":true}
                """;
        mvc.perform(post("/v1/ritmo/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted());
    }

    @Test
    void POST_registro_invalido_retorna400() throws Exception {
        // turno inválido -> cai na validação
        String body = """
                {"bairro":"Vila Nova","turno":"MADRUGA","energia":1,"ambiente":2,"condicao":1,"optIn":true}
                """;
        mvc.perform(post("/v1/ritmo/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("VALIDATION_ERROR"));
    }

    @Test
    void GET_listarRegistros_ok() throws Exception {
        // devolve página vazia só para validar 200
        given(ritmoService.listar(eq("Vila"), eq("NOITE"), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        mvc.perform(get("/v1/ritmo/registros")
                        .param("bairro","Vila")
                        .param("turno","NOITE"))
                .andExpect(status().isOk());
    }

    @Test
    void POST_sugestoesMissao_ok() throws Exception {
        var req = new SugestaoMissaoRequest("suporte", 0, 2, 0);
        var resp = new SugestaoMissaoResponse("SUG-suporte-CURTA","CURTA", true,
                "Dia pesado? Vamos numa missão curtinha/offline pra manter o ritmo.");
        given(sugestoesService.sugerir(any())).willReturn(resp);

        mvc.perform(post("/v1/sugestoes/missao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complexidade").value("CURTA"))
                .andExpect(jsonPath("$.offline").value(true));
    }
}
