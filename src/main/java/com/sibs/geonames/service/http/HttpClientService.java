package com.sibs.geonames.service.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class HttpClientService {

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<Map> call(String url, HttpHeaders headers, Map uriVariables) {

        try {

            log.info("call | request url {} header {} body {}", url, headers, uriVariables);
            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class, uriVariables);
            log.info("call | response: statusCode [{}], headers [{}] and body [{}]", responseEntity.getStatusCodeValue(), responseEntity.getHeaders(), responseEntity.getBody());

            return responseEntity;

        } catch (Exception e) {
            log.error("call | Error restTemplate url {} header {} body {}", url, headers, uriVariables, e);
        }

        return null;
    }
}
