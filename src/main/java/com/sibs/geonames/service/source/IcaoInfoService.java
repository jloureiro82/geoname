package com.sibs.geonames.service.source;

import com.sibs.geonames.config.SourcesConfiguration;
import com.sibs.geonames.service.http.HttpClientService;
import com.sibs.geonames.service.spelexpresionengine.SpelExpressionEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class IcaoInfoService {

    @Autowired
    SpelExpressionEngine expressionEngine;

    @Autowired
    private SourcesConfiguration sourcesConfiguration;

    @Autowired
    private HttpClientService httpClientService;

    public Map searchIcao(String countryId, Map sources){
        Map<String, Object> countryInfoConfiguration = (Map<String, Object>) sourcesConfiguration.getSources().get("getIcao");
        String url = (String) countryInfoConfiguration.get("url");
        Map uriVariables = (Map) countryInfoConfiguration.get("uriVariables");
        Map headerParams = (Map) countryInfoConfiguration.get("headers");


        HttpHeaders httpHeaders = new HttpHeaders();
        if (null != headerParams) {
            httpHeaders.setAll(expressionEngine.evaluate(headerParams, sources));
        }

        if (null == uriVariables) {
            uriVariables = new HashMap();
        }

        uriVariables = expressionEngine.evaluate(uriVariables, sources);
        ResponseEntity<Map> responseEntity = httpClientService.call(url, httpHeaders, uriVariables);

        if (null != responseEntity
            && null != responseEntity.getBody()
            && !responseEntity.getBody().isEmpty()) {

            return responseEntity.getBody();
        }

        return Collections.EMPTY_MAP;
    }
}
