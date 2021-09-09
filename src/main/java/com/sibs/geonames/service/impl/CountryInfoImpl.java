package com.sibs.geonames.service.impl;

import com.sibs.geonames.config.DestinyConfiguration;
import com.sibs.geonames.config.SourcesConfiguration;
import com.sibs.geonames.service.CountryInfo;
import com.sibs.geonames.service.source.CapitalInfoService;
import com.sibs.geonames.service.source.CountryInfoDetailsService;
import com.sibs.geonames.service.source.CountryInfoService;
import com.sibs.geonames.service.source.IcaoInfoService;
import com.sibs.geonames.service.spelexpresionengine.SpelExpressionEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Slf4j
@Service
public class CountryInfoImpl implements CountryInfo {

    private static final String SOURCES_COUNTRY_INFO = "countryInfo";
    private static final String SOURCES_COUNTRY_INFO_DETAILS = "countryInfoDetails";
    private static final String SOURCES_CAPITAL_INFO = "capitalInfo";
    private static final String SOURCES_GET_ICAO = "getIcao";


    Map<String, Object> defaultOptions = Collections.singletonMap("returnOnEvaluationException", "null");


    @Autowired
    private SourcesConfiguration sourcesConfiguration;

    @Autowired
    private DestinyConfiguration destinyConfiguration;

    @Autowired
    private CapitalInfoService capitalInfoService;

    @Autowired
    private CountryInfoService countryInfoService;

    @Autowired
    private CountryInfoDetailsService countryInfoDetailsService;

    @Autowired
    private IcaoInfoService icaoInfoService;


    @Autowired
    private SpelExpressionEngine expressionEngine;


    @Override
    public Optional<Map> searchByCountryId(String countryId) {
        log.debug("Request to get CountryInfo : {}", countryId);

        Optional<Map> result;
        Map countryInfoResponse;
        Map sources = new HashMap();

        log.debug("Sources: [{}]", sourcesConfiguration.getSources());
        Map countryInfoDestinyConfiguration = (Map) destinyConfiguration.getDestiny().get("countryInfo");
        countryInfoResponse = (Map) countryInfoDestinyConfiguration.get("body");

        CompletableFuture<Map> countryInfo = countryInfoService.searchCountryInfo(countryId);
        CompletableFuture<Map> countryInfoDetails = countryInfoDetailsService.searchCountryInfoDetails(countryId);
        CompletableFuture<Map> capitalInfo = capitalInfoService.searchCapitalInfo(countryId);


        CompletableFuture.allOf(countryInfo, countryInfoDetails, capitalInfo);
        try {
            sources.put(SOURCES_COUNTRY_INFO, countryInfo.get());
            sources.put(SOURCES_COUNTRY_INFO_DETAILS, countryInfoDetails.get());
            sources.put(SOURCES_CAPITAL_INFO, capitalInfo.get());

        } catch (InterruptedException e) {
            log.error("searchByCountryId|InterruptedException: ", e);
        } catch (ExecutionException e) {
            log.error("searchByCountryId|ExecutionException: ", e);
        }


        sources.put(SOURCES_GET_ICAO, icaoInfoService.searchIcao(countryId, sources));

        countryInfoResponse = expressionEngine.evaluate(countryInfoResponse, sources, defaultOptions);

        result = Optional.of(countryInfoResponse);
        return result;
    }
}
