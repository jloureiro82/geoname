package com.sibs.geonames.service.source;

import com.sibs.geonames.config.SourcesConfiguration;
import com.sibs.geonames.service.http.HttpClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CountryInfoDetailsService {


    @Autowired
    private SourcesConfiguration sourcesConfiguration;

    @Autowired
    private HttpClientService httpClientService;

    @Async
    public CompletableFuture<Map> searchCountryInfoDetails(String countryId){

        Map<String, Object> countryInfoConfiguration = (Map<String, Object>) sourcesConfiguration.getSources().get("countryInfoDetails");
        String url = (String) countryInfoConfiguration.get("url");
        Map uriVariables = (Map) countryInfoConfiguration.get("uriVariables");
        Map headerParams = (Map) countryInfoConfiguration.get("headers");

        HttpHeaders httpHeaders = new HttpHeaders();
        if (null != headerParams) {
            httpHeaders.setAll(headerParams);
        }

        if (null == uriVariables) {
            uriVariables = new HashMap();
        }

        uriVariables.put("country", countryId);
        ResponseEntity<Map> responseEntity = httpClientService.call(url, httpHeaders, uriVariables);

        if (null != responseEntity
            && null != responseEntity.getBody()
            && !responseEntity.getBody().isEmpty()) {

            return CompletableFuture.completedFuture(responseEntity.getBody());
        }

        return CompletableFuture.completedFuture(Collections.EMPTY_MAP);
    }
}
