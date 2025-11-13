package com.perifaflow.bemestar.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "messaging.enabled", havingValue = "true")
public class RitmoListener {
    private static final Logger log = LoggerFactory.getLogger(RitmoListener.class);

    @RabbitListener(queues = "${app.mq.ritmo.queue}")
    public void onMessage(RitmoEventMessage msg) {
        // aqui você poderia acionar sugestoesService, metrics etc.
        log.info("[MQ] Recebido RitmoEventMessage: {}", msg);
    }
}
