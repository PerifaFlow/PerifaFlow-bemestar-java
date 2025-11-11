package com.perifaflow.bemestar.api;

import com.perifaflow.bemestar.api.dto.*;
import com.perifaflow.bemestar.domain.RitmoEvent;
import com.perifaflow.bemestar.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
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


    @GetMapping("/ritmo/registros")
    public Page<RitmoEvent> listarRegistros(
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String turno,
            @ParameterObject
            @PageableDefault(size = 10, sort = "enviadoEm", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return ritmoService.listar(bairro, turno, pageable);
    }
}

