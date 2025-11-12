package com.perifaflow.bemestar.service;

import com.perifaflow.bemestar.api.dto.RitmoRegistroDTO;
import com.perifaflow.bemestar.domain.RitmoEvent;
import com.perifaflow.bemestar.repo.RitmoEventRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(RitmoService.class)
class   RitmoServiceTest {

    @Autowired
    RitmoService service;

    @Autowired
    RitmoEventRepo repo;

    @Test
    void deveRegistrarQuandoOptInTrue() {
        var dto = new RitmoRegistroDTO("Vila Nova","NOITE",1,2,1,true);
        service.registrar(dto);

        var todos = repo.findAll();
        assertThat(todos).hasSize(1);
        RitmoEvent ev = todos.get(0);
        assertThat(ev.getBairro()).isEqualTo("Vila Nova");
        assertThat(ev.getTurno().name()).isEqualTo("NOITE");
        assertThat(ev.getEnergia()).isEqualTo(1);
    }

    @Test
    void naoDeveRegistrarQuandoOptInFalse() {
        var dto = new RitmoRegistroDTO("Centro","MANHA",0,0,0,false);
        service.registrar(dto);
        assertThat(repo.count()).isZero();
    }

    @Test
    void listarSemTurnoFiltraPorBairro() {
        // seed
        service.registrar(new RitmoRegistroDTO("Vila Nova","MANHA",0,1,2,true));
        service.registrar(new RitmoRegistroDTO("Vila Nova","TARDE",2,1,2,true));
        service.registrar(new RitmoRegistroDTO("Outro Bairro","NOITE",1,2,1,true));

        Page<RitmoEvent> page = service.listar("Vila", null, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(e -> e.getBairro().toLowerCase().contains("vila"));
    }

    @Test
    void listarComTurnoFiltraBairroETurno() {
        // seed
        service.registrar(new RitmoRegistroDTO("Vila Nova","MANHA",0,1,2,true));
        service.registrar(new RitmoRegistroDTO("Vila Nova","TARDE",2,1,2,true));
        service.registrar(new RitmoRegistroDTO("Vila Nova","NOITE",1,2,1,true));

        Page<RitmoEvent> page = service.listar("Vila", "TARDE", PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTurno().name()).isEqualTo("TARDE");
    }
}
