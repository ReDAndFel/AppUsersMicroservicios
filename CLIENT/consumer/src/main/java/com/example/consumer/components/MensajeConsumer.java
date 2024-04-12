package com.example.consumer.components;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MensajeConsumer {

    @RabbitListener(queues = {"${rabbitmq.queue}"})
    public void recibirMensaje(String mensaje) {
        System.out.println("Mensaje recibido: " + mensaje);
    }

}
