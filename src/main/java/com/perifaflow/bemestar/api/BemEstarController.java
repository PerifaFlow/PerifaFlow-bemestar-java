package com.perifaflow.bemestar.api;

import com.perifaflow.bemestar.api.dto.*;
import com.perifaflow.bemestar.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/v1")
@RequiredArgsConstructor
public class BemEstarController {
    private final RitmoService ritmoService;
    private final InsightsService insightsService;
    private final SugestoesService sugestoesService;

    @PostMapping("/ritmo/registro")
    public ResponseEntity<Void> registrar(@RequestBody @Valid RitmoRegistroDTO dto){
        ritmoService.registrar(dto);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/ritmo/insights")
    public Map<String,Object> insights(@RequestParam String bairro,
                                       @RequestParam String de,
                                       @RequestParam String ate){
        return insightsService.agregados(bairro,de,ate);
    }

    @PostMapping("/sugestoes/missao")
    public SugestaoMissaoResponse sugerir(@RequestBody @Valid SugestaoMissaoRequest req){
        return sugestoesService.sugerir(req);
    }
}
