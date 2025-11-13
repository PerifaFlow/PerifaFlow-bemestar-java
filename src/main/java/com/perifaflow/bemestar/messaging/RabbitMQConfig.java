package com.perifaflow.bemestar.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix="messaging", name="enabled", havingValue="true")
public class RabbitMQConfig {

    @Value("${app.mq.ritmo.exchange}")   private String exchangeName;
    @Value("${app.mq.ritmo.queue}")      private String queueName;
    @Value("${app.mq.ritmo.routingKey}") private String routingKey;

    @Bean
    public TopicExchange ritmoExchange() { return new TopicExchange(exchangeName, true, false); }

    @Bean
    public Queue ritmoQueue() { return QueueBuilder.durable(queueName).build(); }

    @Bean
    public Binding ritmoBinding(Queue ritmoQueue, TopicExchange ritmoExchange) {
        return BindingBuilder.bind(ritmoQueue).to(ritmoExchange).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(conv);
        return rt;
    }
}
