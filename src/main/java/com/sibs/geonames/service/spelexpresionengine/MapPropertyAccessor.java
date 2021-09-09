package com.sibs.geonames.service.spelexpresionengine;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import java.util.Map;

public class MapPropertyAccessor implements PropertyAccessor {

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return new Class<?>[]{Map.class};
    }

    @Override
    public boolean canRead(EvaluationContext aContext, Object aTarget, String aName) throws AccessException {
        if (!(aTarget instanceof Map)) {
            return false;
        }
        return ((Map) aTarget).containsKey(aName);
    }

    @Override
    public TypedValue read(EvaluationContext aContext, Object aTarget, String aName) throws AccessException {
        Map<String, Object> map = (Map<String, Object>) aTarget;
        Object value = map.get(aName);
        return new TypedValue(value, TypeDescriptor.forObject(value));
    }

    @Override
    public boolean canWrite(EvaluationContext aContext, Object aTarget, String aName) throws AccessException {
        return false;
    }

    @Override
    public void write(EvaluationContext aContext, Object aTarget, String aName, Object aNewValue) throws AccessException {
        throw new UnsupportedOperationException();
    }

}
