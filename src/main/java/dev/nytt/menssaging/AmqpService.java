package dev.nytt.menssaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import dev.nytt.dto.FileProcessDto;
import dev.nytt.services.FileService;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class AmqpService {
    @Inject
    private RabbitMQClient rabbitMQClient;

    @Inject
    private FileService fileService;

    private final static String QUEUE = "process_file";
    private final Logger LOG;
    private final ObjectMapper objectMapper;

    public AmqpService() {
        objectMapper = new ObjectMapper();
        LOG = Logger.getLogger(AmqpService.class.getName());
    }


    void onStart(@Observes StartupEvent ev) throws IOException {

        Optional<Channel> channelOptional = rabbitMQClient.connect().openChannel();
        if (channelOptional.isPresent()) {
            Channel channel = channelOptional.get();
            channel.basicConsume(QUEUE, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    String payload = new String(body, StandardCharsets.UTF_8);
                    LOG.info(String.format("new file payload in queue: %s", payload));
                    try {
                        FileProcessDto fileProcessDto = objectMapper.readValue(payload, FileProcessDto.class);
                        fileService.createFileByPayload(fileProcessDto);
                    } catch (Exception e) {

                        LOG.warning("file not generated: "+e.getMessage());

                    } finally {
                        LOG.info("ack message");

                        channel.basicAck(envelope.getDeliveryTag(), false);
                    }


                }

            });
        }
    }
}
