package com.sibs.geonames.web.rest;

import com.sibs.geonames.service.CountryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.ResponseUtil;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api")
public class CountryInfoResource {

    private final Logger log = LoggerFactory.getLogger(CountryInfoResource.class);

    private static final String ENTITY_NAME = "geonamesCountryInfo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    @Autowired
    private CountryInfo countryInfo;


    /**
     * {@code GET  /country-infos/country-id/:countryId} : get the "countryId" countryInfo.
     *
     * @param countryId the id of the countryInfoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the countryInfoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping(path = "/country-infos/country-id/{countryId}", produces = {"application/json", "application/xml"})

    public ResponseEntity<Map> getCountryInfo(@PathVariable String countryId) {
        log.debug("REST request to get CountryInfo : {}", countryId);
        Optional<Map> countryInfoDTO = countryInfo.searchByCountryId(countryId);
        return ResponseUtil.wrapOrNotFound(countryInfoDTO);
    }

}
