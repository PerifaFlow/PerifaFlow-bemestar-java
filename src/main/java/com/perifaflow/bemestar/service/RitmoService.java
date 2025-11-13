package com.perifaflow.bemestar.service;

import com.perifaflow.bemestar.api.dto.RitmoRegistroDTO;
import com.perifaflow.bemestar.domain.RitmoEvent;
import com.perifaflow.bemestar.domain.Turno;
import com.perifaflow.bemestar.messaging.RitmoPublisher;
import com.perifaflow.bemestar.repo.RitmoEventRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RitmoService {
    private final RitmoEventRepo repo;
    private final RitmoPublisher publisher;

    @Transactional
    public void registrar(RitmoRegistroDTO dto){
        if (!dto.optIn()) return;
        RitmoEvent ev = RitmoEvent.builder()
                .bairro(dto.bairro())
                .turno(Turno.valueOf(dto.turno().toUpperCase()))
                .energia(dto.energia())
                .ambiente(dto.ambiente())
                .condicao(dto.condicao())
                .build();

        ev = repo.save(ev);
        publisher.publish(ev);
    }

    @Transactional(readOnly = true)
    public Page<RitmoEvent> listar(String bairro, String turno, Pageable pageable) {
        String b = (bairro == null) ? "" : bairro;
        if (turno == null || turno.isBlank()) {
            return repo.findByBairroContainingIgnoreCase(b, pageable);
        }
        Turno t = Turno.valueOf(turno.toUpperCase());
        return repo.findByBairroContainingIgnoreCaseAndTurno(b, t, pageable);
    }
}
