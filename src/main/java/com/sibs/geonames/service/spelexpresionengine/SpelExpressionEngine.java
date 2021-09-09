package com.sibs.geonames.service.spelexpresionengine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.expression.*;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Setter
@Getter
@Service
public class SpelExpressionEngine implements ExpressionEngine {

    @Autowired
    Environment env;

    private static final ConversionService conversionService = DefaultConversionService.getSharedInstance();

    private Map<String, Object> functions = new HashMap<>();

    private final ExpressionParser parser = new SpelExpressionParser();
    @Autowired
    ApplicationContext applicationContext;

    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";

    private static final String RETURN_ERROR = "error";
    private static final String RETURN_NULL = "null";
    private static final String RETURN_IGNORE = "origin";

    private final Logger log = LoggerFactory.getLogger(SpelExpressionEngine.class);

    Map<String, Object> defaultOptions = Collections.singletonMap("returnOnEvaluationException", "origin");


    @Override
    public Map<String, Object> evaluate(Map<String, Object> map, Object context) {
        return evaluateInternal(map, context);
    }

    @Override
    public Map<String, Object> evaluate(Map<String, Object> map, Object context, Map<String, Object> options) {
        return evaluateInternal(map, context, options);
    }

    @Override
    public Map<String, Object> evaluate(Map<String, Object> map, Map<String, Object> context) {
        return evaluateInternal(map, context);
    }

    @Override
    public Map<String, Object> evaluate(Map<String, Object> map, Map<String, Object> context, Map<String, Object> options) {
        return evaluateInternal(map, context, options);
    }

    @Override
    public <T> T evaluate(String exp, Object context) {
        return (T) evaluateObject(exp, context);
    }

    @Override
    public <T> T evaluate(String exp, Object context, Map<String, Object> options) {
        return (T) evaluateObject(exp, context, options);
    }

    @Override
    public <T> T evaluate(String exp, Map<String, Object> context) {
        return (T) evaluateObject(exp, context);
    }

    @Override
    public <T> T evaluate(String exp, Map<String, Object> context, Map<String, Object> options) {
        return (T) evaluateObject(exp, context, options);
    }

    private Map<String, Object> evaluateInternal(Map<String, Object> map, Map<String, Object> context) {
        return evaluateInternal(map, context, defaultOptions);
    }

    private Map<String, Object> evaluateInternal(Map<String, Object> map, Object context) {
        return evaluateInternal(map, context, defaultOptions);
    }

    private Map<String, Object> evaluateInternal(Map<String, Object> map, Map<String, Object> context, Map<String, Object> options) {
        Map<String, Object> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            newMap.put(entry.getKey(), evaluateObject(entry.getValue(), context, options));
        }
        return newMap;
    }

    private Map<String, Object> evaluateInternal(Map<String, Object> map, Object context, Map<String, Object> options) {
        Map<String, Object> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            newMap.put(entry.getKey(), evaluateObject(entry.getValue(), context, options));
        }
        return newMap;
    }

    private Object evaluateObject(Object value, Object contextObj) {
        return evaluateObject(value, contextObj, defaultOptions);
    }

    private Object evaluateObject(Object value, Object contextObj, Map<String, Object> options) {
        StandardEvaluationContext context = createEvaluationContext(contextObj);
        if (value instanceof String) {
            Expression expression = parser.parseExpression((String) value, new TemplateParserContext(PREFIX, SUFFIX));
            try {
                return (expression.getValue(context));
            } catch (SpelEvaluationException e) {
                String op = (String) options.getOrDefault("returnOnEvaluationException", RETURN_IGNORE);
                switch (op) {
                    case RETURN_ERROR:
                        throw e;
                    case RETURN_NULL:
                        return null;
                    case RETURN_IGNORE:
                        return value;
                    default:
                        log.debug(e.getMessage());
                        return value;
                }
            }
        } else if (value instanceof List) {
            List<Object> evaluatedList = new ArrayList<>();
            List<Object> list = (List<Object>) value;
            for (Object item : list) {
                evaluatedList.add(evaluateObject(item, contextObj, options));
            }
            return evaluatedList;
        } else if (value instanceof Map) {
            return evaluateInternal((Map<String, Object>) value, contextObj, options);
        }
        return value;
    }

    @Override
    public void registerFunctions(String name, Object functions) {
        this.functions.put(name, functions);
    }

    private StandardEvaluationContext createEvaluationContext(Object contextObj) {
        StandardEvaluationContext context = new StandardEvaluationContext(contextObj);
        context.addPropertyAccessor(new MapPropertyAccessor());
        context.addMethodResolver(methodResolver());
        context.setBeanResolver((_self, beanName) ->
            this.applicationContext.getBean(beanName));
        context.setVariables(functions);
        return context;
    }

    private MethodResolver methodResolver() {
        return (ctx, target, name, args) -> {
            switch (name) {
                case "systemProperty":
                    return this::systemProperty;
                case "environmentProperty":
                    return this::environmentProperty;
                case "range":
                    return range();
                case "boolean":
                    return cast(Boolean.class);
                case "byte":
                    return cast(Byte.class);
                case "char":
                    return cast(Character.class);
                case "short":
                    return cast(Short.class);
                case "int":
                    return cast(Integer.class);
                case "long":
                    return cast(Long.class);
                case "float":
                    return cast(Float.class);
                case "double":
                    return cast(Double.class);
                case "join":
                    return join();
                case "concat":
                    return concat();
                case "flatten":
                    return flatten();
                case "eval":
                    return eval();

                /*case "jsonPath":
                    return jsonPath();*/
                default:
                    return null;
            }
        };
    }

    private TypedValue systemProperty(EvaluationContext evaluationContext, Object target, Object... args) throws AccessException {
        return new TypedValue(System.getProperty((String) args[0]));
    }

    private TypedValue environmentProperty(EvaluationContext evaluationContext, Object target, Object... args) throws AccessException {
        return new TypedValue(env.getProperty((String) args[0]));
    }

    private MethodExecutor range() {
        return (ctx, target, args) -> {
            List<Integer> value = IntStream.rangeClosed((int) args[0], (int) args[1])
                .boxed()
                .collect(Collectors.toList());
            return new TypedValue(value);
        };
    }

    private <T> MethodExecutor cast(Class<T> type) {
        return (ctx, target, args) -> {
            T value = type.cast(conversionService.convert(args[0], type));
            return new TypedValue(value);
        };
    }

    private <T> MethodExecutor join() {
        return (ctx, target, args) -> {
            String separator = (String) args[0];
            List<T> values = (List<T>) args[1];
            String str = values.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(separator));
            return new TypedValue(str);
        };
    }

    private <T> MethodExecutor concat() {
        return (ctx, target, args) -> {
            List<T> l1 = (List<T>) args[0];
            List<T> l2 = (List<T>) args[1];
            List<T> joined = new ArrayList<T>(l1.size() + l2.size());
            joined.addAll(l1);
            joined.addAll(l2);
            return new TypedValue(joined);
        };
    }

    private <T> MethodExecutor flatten() {
        return (ctx, target, args) -> {
            List<List<T>> list = (List<List<T>>) args[0];
            List<T> flat = list.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
            return new TypedValue(flat);
        };
    }

    private <T> MethodExecutor eval() {
        return (ctx, target, args) -> {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName((String) args[0]);
            Object obj = null;
            try {
                Bindings bindings = engine.createBindings();
                bindings.putAll((Map<? extends String, ? extends Object>) ctx.getRootObject().getValue());
                obj = engine.eval((String) args[1], bindings);
            } catch (ScriptException e) {
                log.error("error running eval:", e);
                return new TypedValue("${eval(\"".concat((String) args[0]).concat("\",\"").concat((String) args[1]).concat("\")}"));
            }
            return new TypedValue(obj);
        };
    }

    private <T> MethodExecutor jsonPath() {
        return (ctx, target, args) -> {

            Object obj;
            Object targetObj = args[1];

            if (targetObj instanceof Map) {
                targetObj = writeValueAsString(targetObj);           }

            if (targetObj instanceof String) {
                String query = (String) args[0];

                try {
                    obj = JsonPath.read((String) targetObj, query);

                } catch (Exception e) {
                    log.error("Unable to use jsonPath");
                    throw new SpelEvaluationException(SpelMessage.valueOf("Unable to parse jsonPath, exception: " + e.getCause()));
                }
            } else {
                obj = "${jsonPath('" + args[0] + "','" + args[1] + "')}";
            }
            return new TypedValue(obj);
        };
    }



    public String writeValueAsString(Object value) {

        if (Objects.nonNull(value)) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.NON_NULL));
            try {
                return  objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                log.error("writeValueAsString | JsonProcessingException: ", e);
            }
        }

        return null;
    }



}
