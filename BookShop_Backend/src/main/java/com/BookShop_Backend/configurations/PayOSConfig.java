package com.BookShop_Backend.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@Configuration
public class PayOSConfig {

    // Spring doc cac gia tri nay tu application.properties hoac environment variables.
    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.log-level}")
    private String logLevel;

    @Bean
    public PayOS payOS() {
        // Bean PayOS duoc inject vao service/controller de goi API tao payment link va verify webhook.
        ClientOptions options =
                ClientOptions.builder()
                        .clientId(clientId)
                        .apiKey(apiKey)
                        .checksumKey(checksumKey)
                        .logLevel(ClientOptions.LogLevel.valueOf(logLevel.toUpperCase()))
                        .build();
        return new PayOS(options);
    }
}
