package com.perifaflow.bemestar.service;

import com.perifaflow.bemestar.api.dto.RitmoRegistroDTO;
import com.perifaflow.bemestar.domain.RitmoEvent;
import com.perifaflow.bemestar.repo.RitmoEventRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class RitmoService {
    private final RitmoEventRepo repo;

    @Transactional
    public void registrar(RitmoRegistroDTO dto){
        if (!dto.optIn()) return;
        RitmoEvent ev = RitmoEvent.builder()
                .bairro(dto.bairro())
                .turno(com.perifaflow.bemestar.domain.Turno.valueOf(dto.turno()))
                .energia(dto.energia())
                .ambiente(dto.ambiente())
                .condicao(dto.condicao())
                .build();
        repo.save(ev);
    }
}
