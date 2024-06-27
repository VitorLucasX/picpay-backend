package com.picpay.desafio.notification;

import com.picpay.desafio.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificationConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationConsumer.class);

    private RestClient restClient;

    public NotificationConsumer(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://util.devi.tools/api/v2/authorize")
                .build();
    }

    @KafkaListener(topics = "transaction-noficiation", groupId = "picpay-desafio-backend")
    public void receivedNotification(Transaction transaction) {
        LOGGER.info("Notifying transaction: {}", transaction);

        var response = restClient.get()
                .retrieve()
                .toEntity(Notification.class);

        if (response.getStatusCode().isError() || !response.getBody().message()) {
            throw new NotificationException("Error sending notification!");

        }
        LOGGER.info("Notification has been sent: {}", transaction);
    }
}
