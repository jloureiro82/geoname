package com.sibs.geonames.service.spelexpresionengine;

import java.util.Map;

/**
 * Interface for expression engine services
 *
 * @author Nuno Nunes
 */
public interface ExpressionEngine {

    Map<String, Object> evaluate(Map<String, Object> map, Object context);

    Map<String, Object> evaluate(Map<String, Object> map, Object context, Map<String, Object> options);

    Map<String, Object> evaluate(Map<String, Object> map, Map<String, Object> context);

    Map<String, Object> evaluate(Map<String, Object> map, Map<String, Object> context, Map<String, Object> options);

    <T> T evaluate(String exp, Map<String, Object> context);

    <T> T evaluate(String exp, Map<String, Object> context, Map<String, Object> options);

    <T> T evaluate(String exp, Object context);

    <T> T evaluate(String exp, Object context, Map<String, Object> options);

    void registerFunctions(String name, Object functions);

}
