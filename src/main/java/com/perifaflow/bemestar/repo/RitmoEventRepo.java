package com.perifaflow.bemestar.repo;

import com.perifaflow.bemestar.domain.RitmoEvent;
import com.perifaflow.bemestar.domain.Turno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RitmoEventRepo extends JpaRepository<RitmoEvent, Long> {
    Page<RitmoEvent> findByBairroContainingIgnoreCase(String bairro, Pageable pageable);
    Page<RitmoEvent> findByBairroContainingIgnoreCaseAndTurno(String bairro, Turno turno, Pageable pageable);
}
