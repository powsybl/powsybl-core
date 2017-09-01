/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Strings;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static void writeOptionalStringField(JsonGenerator jsonGenerator, String fieldName, String value) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (!Strings.isNullOrEmpty(value)) {
            jsonGenerator.writeStringField(fieldName, value);
        }
    }

    public static void writeOptionalEnumField(JsonGenerator jsonGenerator, String fieldName, Enum<?> value) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (value != null) {
            jsonGenerator.writeStringField(fieldName, value.name());
        }
    }

    public static void writeOptionalFloatField(JsonGenerator jsonGenerator, String fieldName, float value) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (!Float.isNaN(value)) {
            jsonGenerator.writeNumberField(fieldName, value);
        }
    }

    public static void writeOptionalIntegerField(JsonGenerator jsonGenerator, String fieldName, int value) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (value != Integer.MAX_VALUE) {
            jsonGenerator.writeNumberField(fieldName, value);
        }
    }

}
