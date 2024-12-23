package dev.nytt.menssaging;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@ApplicationScoped
public class AmqpService {
    @Inject
    private RabbitMQClient rabbitMQClient;

    private static String QUEUE = "process_file";


    void onStart(@Observes StartupEvent ev) throws IOException {

        Optional<Channel> channel = rabbitMQClient.connect().openChannel();
        if (channel.isPresent()) {
            channel.get().basicConsume(QUEUE, new DefaultConsumer(channel.get()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    System.out.println("Received: " + new String(body, StandardCharsets.UTF_8));
                    channel.get().basicAck(envelope.getDeliveryTag(), false);
                }
            });
        }
    }
}
