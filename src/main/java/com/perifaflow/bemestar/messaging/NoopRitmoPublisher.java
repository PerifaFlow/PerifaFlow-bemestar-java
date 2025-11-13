package com.perifaflow.bemestar.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "messaging.enabled", havingValue = "false", matchIfMissing = true)
public class NoopRitmoPublisher implements RitmoPublisher {
    private static final Logger log = LoggerFactory.getLogger(NoopRitmoPublisher.class);
    @Override public void publish(RitmoEventMessage message) {
        log.debug("[NOOP] Mensageria desabilitada. Evento ignorado: {}", message);
    }
}
