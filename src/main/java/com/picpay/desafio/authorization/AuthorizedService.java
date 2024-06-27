package com.picpay.desafio.authorization;

import com.picpay.desafio.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthorizedService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizedService.class);

    private RestClient restClient;

    public AuthorizedService(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://util.devi.tools/api/v2/authorize").build();
    }

    public void authorize(Transaction transaction) {
        LOGGER.info("Authorizing transaction: {}", transaction);

        var response = restClient.get()
                .retrieve()
                .toEntity(Authorization.class);

        if (response.getStatusCode().isError() || !response.getBody().isAuthorized()) {
            throw new UnauthorizedTransactionException("Unauthorized transaction!");
        }

        LOGGER.info("Transaction authorized: {}", transaction);
    }
}
