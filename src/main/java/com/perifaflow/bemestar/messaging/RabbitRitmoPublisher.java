package com.perifaflow.bemestar.messaging;

import com.perifaflow.bemestar.domain.RitmoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix="messaging", name="enabled", havingValue="true")
public class RabbitRitmoPublisher implements RitmoPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.mq.ritmo.exchange}")   private String exchange;
    @Value("${app.mq.ritmo.routingKey}") private String routingKey;

    @Override
    public void publish(RitmoEvent ev) {
        RitmoEventMessage msg = RitmoEventMessage.builder()
                .id(ev.getId())
                .bairro(ev.getBairro())
                .turno(ev.getTurno())
                .energia(ev.getEnergia())
                .ambiente(ev.getAmbiente())
                .condicao(ev.getCondicao())
                .enviadoEm(ev.getEnviadoEm())
                .build();

        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
    }
}
