package com.perifaflow.bemestar.messaging;

import com.perifaflow.bemestar.domain.RitmoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix="messaging", name="enabled", havingValue="false", matchIfMissing = true)
public class NoopRitmoPublisher implements RitmoPublisher {
    @Override
    public void publish(RitmoEvent event) {
        log.debug("[NOOP] Mensageria desabilitada. Evento não publicado (id={})", event.getId());
    }
}
