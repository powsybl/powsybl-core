package com.powsybl.psse.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static com.powsybl.psse.model.PsseVersion.MAX_VERSION;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Revision {
    static final float MAX_REVISION = 99.99f;
    float since() default 33.0f;
    float until() default MAX_REVISION;
}

public class PsseVersioned {
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
        if (!field.isAnnotationPresent(Revision.class)) {
            throw new PsseException("Missing PsseRev annotation in field " + fieldName);
        }
        PsseVersion since = PsseVersion.fromRevision(field.getAnnotation(Revision.class).since());
        PsseVersion until = PsseVersion.fromRevision(field.getAnnotation(Revision.class).until());
        PsseVersion current = PsseVersion.fromRevision(model.getCaseIdentification().getRev());
        if (!(since.getNumber() <= current.getNumber() && current.getNumber() <= until.getNumber())) {
            String message = String.format(
                "Wrong version of PSSE RAW model (%d). Field '%s' is valid since version %d%s",
                current.getMajor(),
                fieldName,
                since.getMajor(),
                until.getNumber() != MAX_VERSION.getNumber() ? " until " + until.getMajor() : "");
            throw new PsseException(message);
        }
    }

}

