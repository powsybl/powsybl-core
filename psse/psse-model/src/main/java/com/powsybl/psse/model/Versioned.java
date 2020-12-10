package com.powsybl.psse.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface PsseRev {
    static final int UNLIMITED = 9999;
    int since() default 33;
    int until() default UNLIMITED;
}

public class Versioned {
    private PsseRawModel model = null;

    public void setModel(PsseRawModel model) {
        this.model = model;
    }

    public void checkVersion(String fieldName) {
        // If we do not have a reference back to a model
        // We can not obtain current version and we can not perform checks
        if (model == null) {
            return;
        }
        Field field = null;
        try {
            field = this.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new PsseException("Wrong field name " + fieldName, e);
        }

        checkVersionField(field, fieldName);
    }

    public void checkVersion(String innerClassName, String fieldName) {
        // If we do not have a reference back to a model
        // We can not obtain current version and we can not perform checks
        if (model == null) {
            return;
        }
        Field field = null;
        try {
            List<Class<?>> innerClasses = Arrays.asList(this.getClass().getDeclaredClasses());
            Class<?> innerClass = innerClasses.parallelStream()
                .filter(ic -> ic.getName().contains(this.getClass().getName() + "$" + innerClassName))
                .findFirst().orElseThrow(() -> new PsseException("Wrong class name " + innerClassName));
            field = innerClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new PsseException("Wrong field name " + fieldName, e);
        }

        checkVersionField(field, fieldName);
    }

    private void checkVersionField(Field field, String fieldName) {
        if (!field.isAnnotationPresent(PsseRev.class)) {
            throw new PsseException("Missing PsseRev annotation in field " + fieldName);
        }
        int since = field.getAnnotation(PsseRev.class).since();
        int until = field.getAnnotation(PsseRev.class).until();
        int version = model.getCaseIdentification().getRev();
        if (!(since <= version && version <= until)) {
            String message = String.format(
                "Wrong version of PSSE RAW model (%d). Field '%s' is valid since version %d%s",
                version,
                fieldName,
                since,
                until != PsseRev.UNLIMITED ? " until " + until : "");
            throw new PsseException(message);
        }
    }
}

