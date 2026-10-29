/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeSerializer extends StdSerializer<ReportNode> {

    ReportNodeSerializer() {
        super(ReportNode.class);
    }

    @Override
    public void serialize(ReportNode reportNode, JsonGenerator generator, SerializationContext serializationContext) throws JacksonException {
        generator.writeStartObject();

        generator.writeStringProperty("version", ReportConstants.CURRENT_VERSION.toString());

        generator.writeName("dictionaries");
        generator.writeStartObject();
        generator.writePOJOProperty("default", reportNode.getTreeContext().getDictionary());
        generator.writeEndObject();

        generator.writeName("reportRoot");
        generator.writeStartObject();
        reportNode.writeJson(generator);
        generator.writeEndObject();

        generator.writeEndObject();
    }

    public static void write(ReportNode reportNode, Path jsonFile) {
        Objects.requireNonNull(reportNode);
        Objects.requireNonNull(jsonFile);
        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            createJsonMapper().writerWithDefaultPrettyPrinter().writeValue(os, reportNode);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JsonMapper createJsonMapper() {
        return JsonMapper.builder()
            .addModule(new ReportNodeJsonModule())
            .build();
    }

    protected static final class TypedValueSerializer extends StdSerializer<TypedValue> {
        TypedValueSerializer() {
            super(TypedValue.class);
        }

        @Override
        public void serialize(TypedValue typedValue, JsonGenerator generator, SerializationContext serializationContext) throws JacksonException {
            generator.writeStartObject();
            serializationContext.defaultSerializeProperty("value", typedValue.getValue(), generator);
            if (!TypedValue.UNTYPED_TYPE.equals(typedValue.getType())) {
                generator.writeStringProperty("type", typedValue.getType());
            }
            generator.writeEndObject();
        }
    }
}
