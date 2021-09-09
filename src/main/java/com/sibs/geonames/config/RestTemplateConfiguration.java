package com.sibs.geonames.config;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfiguration {


    @Value("${app.restTemplate.factory.readTimeout:5000}")
    private int readTimeout;

    @Value("${app.restTemplate.factory.connectTimeout:1000}")
    private int connectTimeout;

    @Value("${app.restTemplate.httpClient.maxConnTotal:100}")
    private int maxConnTotal;

    @Value("${app.restTemplate.httpClient.maxConnPerRoute:10}")
    private int maxConnPerRoute;

    @Value("${app.restTemplate.logging.intercept.enabled:true}")
    private boolean loggingEnabled;

    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy;

    @Bean
    @Primary
    public RestTemplate restTemplate() {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectTimeout);

        HttpClient httpClient = HttpClientBuilder.create()
            .setMaxConnTotal(maxConnTotal)
            .setMaxConnPerRoute(maxConnPerRoute)
            .setKeepAliveStrategy(connectionKeepAliveStrategy)
            .evictIdleConnections(30, TimeUnit.SECONDS)
            .build();

        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }
}
