package com.perifaflow.bemestar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.perifaflow.bemestar.api.dto.SugestaoMissaoRequest;
import com.perifaflow.bemestar.api.dto.SugestaoMissaoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SugestoesService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SugestaoMissaoResponse sugerir(SugestaoMissaoRequest req) {
        try {
            String userText = """
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


            Prompt prompt = new Prompt(
                    List.<Message>of(
                            new SystemMessage("Responda em pt-BR."),
                            new UserMessage(userText)
                    ),
                    OllamaOptions.create()
                            .withModel("qwen3:4b")
                            .withTemperature(0.2)   // Double
                            .withTopP(0.9)          // Double
            );

            ChatResponse resp = chatModel.call(prompt);
            String content = resp.getResult().getOutput().getContent();
            if (content == null || content.isBlank()) {
                return fallback(req);
            }

            // Tolerância: pega só o JSON caso venha texto extra
            int i = content.indexOf('{'), j = content.lastIndexOf('}');
            if (i >= 0 && j > i) content = content.substring(i, j + 1);

            JsonNode root = objectMapper.readTree(content);
            String complex  = root.path("complexidade").asText("NORMAL");
            boolean offline = root.path("offline").asBoolean(false);
            String msg      = root.path("mensagem").asText("Missão normal para evoluir seu portfólio.");

            return new SugestaoMissaoResponse("SUG-" + req.perfil() + "-" + complex, complex, offline, msg);

        } catch (Exception e) {
            return fallback(req);
        }
    }

    private SugestaoMissaoResponse fallback(SugestaoMissaoRequest req){
        int soma = nz(req.ultimaEnergia()) + nz(req.ultimoAmbiente()) + nz(req.ultimaCondicao());
        boolean low = soma <= 2;
        String complex = low ? "CURTA" : "NORMAL";
        boolean offline = low;
        String msg = low
                ? "Dia pesado? Vamos numa missão curtinha/offline pra manter o ritmo."
                : "Boa! Missão normal pra evoluir seu portfólio.";
        return new SugestaoMissaoResponse("SUG-" + req.perfil() + "-" + complex, complex, offline, msg);
    }

    private int nz(Integer v){ return v == null ? 1 : v; }
}
