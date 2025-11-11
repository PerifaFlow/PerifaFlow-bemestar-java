package com.perifaflow.bemestar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.perifaflow.bemestar.api.dto.SugestaoMissaoRequest;
import com.perifaflow.bemestar.api.dto.SugestaoMissaoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SugestoesService {

    private final ChatClient chatClient;           // vem do Spring AI
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SugestaoMissaoResponse sugerir(SugestaoMissaoRequest req){
        // Prompt enxuto e controlado (pedindo JSON)
        String prompt = """
            Você é um mentor de missões para portfólio.
            Regras:
            - Analise os últimos sinais de energia/ambiente/condição (0..2 cada).
            - Se a soma <= 2, sugira missão CURTA e offline=true.
            - Caso contrário, NORMAL e offline=false.
            - Responda **somente** em JSON, sem texto extra, no formato:
              {"complexidade":"CURTA|NORMAL","offline":true|false,"mensagem":"..."}
            
            Perfil: %s
            últimaEnergia: %s
            últimoAmbiente: %s
            últimaCondicao: %s
            """.formatted(
                req.perfil(),
                String.valueOf(req.ultimaEnergia()),
                String.valueOf(req.ultimoAmbiente()),
                String.valueOf(req.ultimaCondicao())
        );

        String content = chatClient
                .prompt()
                .system("Responda em pt-BR.")
                .user(prompt)
                .call()
                .content();

        // Parseia o JSON retornado
        try {
            JsonNode root = objectMapper.readTree(content);
            String complex = root.path("complexidade").asText("NORMAL");
            boolean offline = root.path("offline").asBoolean(false);
            String mensagem = root.path("mensagem").asText("Missão normal para evoluir seu portfólio.");

            String missaoId = "SUG-" + req.perfil() + "-" + complex;
            return new SugestaoMissaoResponse(missaoId, complex, offline, mensagem);

        } catch (Exception e) {
            // fallback simples se o modelo sair do formato
            int soma = nz(req.ultimaEnergia()) + nz(req.ultimoAmbiente()) + nz(req.ultimaCondicao());
            boolean low = soma <= 2;
            String complex = low ? "CURTA" : "NORMAL";
            boolean offline = low;
            String msg = low
                    ? "Dia pesado? Vamos numa missão curtinha/offline pra manter o ritmo."
                    : "Boa! Missão normal pra evoluir seu portfólio.";
            return new SugestaoMissaoResponse("SUG-" + req.perfil() + "-" + complex, complex, offline, msg);
        }
    }

    private int nz(Integer v){ return v==null?1:v; }
}
