package com.perifaflow.bemestar.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "messaging.enabled", havingValue = "true")
public class RabbitRitmoPublisher implements RitmoPublisher {
    private final RabbitTemplate template;
    private final String exchange;
    private final String routingKey;

    public RabbitRitmoPublisher(
            RabbitTemplate template,
            @Value("${app.mq.ritmo.exchange}") String exchange,
            @Value("${app.mq.ritmo.routingKey}") String routingKey) {
        this.template = template;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void publish(RitmoEventMessage message) {
        template.convertAndSend(exchange, routingKey, message);
    }
}
