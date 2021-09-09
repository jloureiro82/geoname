package com.sibs.geonames.service;

import java.util.Map;
import java.util.Optional;


public interface CountryInfo {


    /**
     * Get the "countryId" countryInfo.
     *
     * @param countryId the id of the entity.
     * @return the entity.
     */
    Optional<Map> searchByCountryId(String countryId);
}
