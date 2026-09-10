package com.todolist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient sportsDbRestClient(EsportesApiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());

        String base = properties.getApiBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        // TheSportsDB v1 autentica pela chave no path: /api/v1/json/{apiKey}/
        String baseComChave = base + "/" + properties.getApiKey() + "/";

        return RestClient.builder()
                .baseUrl(baseComChave)
                .requestFactory(factory)
                .build();
    }
}
