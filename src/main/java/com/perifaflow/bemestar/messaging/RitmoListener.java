package com.perifaflow.bemestar.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix="messaging", name="enabled", havingValue="true")
public class RitmoListener {

    @RabbitListener(queues = "${app.mq.ritmo.queue}")
    public void handle(RitmoEventMessage msg) {
        log.info("Recebido RitmoEventMessage: bairro={}, turno={}, energia={}",
                msg.getBairro(), msg.getTurno(), msg.getEnergia());

    }
}
