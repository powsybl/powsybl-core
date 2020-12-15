package com.powsybl.psse.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Revision {
    static final float MAX_REVISION = 99.99f;

    float since() default 33.0f;

    float until() default MAX_REVISION;
}
