package com.perifaflow.bemestar.messaging;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "messaging.enabled", havingValue = "true")
public class RabbitMQConfig {
    @Bean
    public DirectExchange ritmoExchange(@Value("${app.mq.ritmo.exchange}") String ex) {
        return new DirectExchange(ex, true, false);
    }

    @Bean
    public Queue ritmoQueue(@Value("${app.mq.ritmo.queue}") String q) {
        return QueueBuilder.durable(q).build();
    }

    @Bean
    public Binding ritmoBinding(Queue ritmoQueue, DirectExchange ritmoExchange,
                                @Value("${app.mq.ritmo.routingKey}") String key) {
        return BindingBuilder.bind(ritmoQueue).to(ritmoExchange).with(key);
    }
}
