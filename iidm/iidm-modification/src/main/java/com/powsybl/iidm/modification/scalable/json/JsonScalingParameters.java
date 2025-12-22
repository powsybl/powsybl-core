/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.modification.scalable.ScalingParameters;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class JsonScalingParameters {

    private JsonScalingParameters() {
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static ScalingParameters read(Path jsonFile) {
        return update(new ScalingParameters(), jsonFile);
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static ScalingParameters read(InputStream jsonStream) {
        return update(new ScalingParameters(), jsonStream);
    }

    /**
     * Updates parameters by reading the content of a JSON file.
     */
    public static ScalingParameters update(ScalingParameters parameters, Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return update(parameters, is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Updates parameters by reading the content of a JSON stream.
     */
    public static ScalingParameters update(ScalingParameters parameters, InputStream jsonStream) {
        JsonMapper jsonMapper = createJsonMapper();
        return jsonMapper.readerForUpdating(parameters).readValue(jsonStream);
    }

    /**
     * Writes parameters as JSON to a file.
     */
    public static void write(ScalingParameters parameters, Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (OutputStream outputStream = Files.newOutputStream(jsonFile)) {
            write(parameters, outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Writes parameters as JSON to an output stream.
     */
    public static void write(ScalingParameters parameters, OutputStream outputStream) {
        JsonMapper jsonMapper = createJsonMapper();
        ObjectWriter writer = jsonMapper.writerWithDefaultPrettyPrinter();
        writer.writeValue(outputStream, parameters);
    }

    private static JsonMapper createJsonMapper() {
        return JsonUtil.createJsonMapperBuilder()
            .addModule(new ScalingParametersJsonModule())
            .build();
    }

    /**
     * Low level deserialization method, to be used for instance for updating scaling parameters nested in another object.
     */
    public static ScalingParameters deserialize(JsonParser parser, DeserializationContext deserializationContext,
                                         ScalingParameters scalingParameters) throws JacksonException {
        return new ScalingParametersDeserializer().deserialize(parser, deserializationContext, scalingParameters);
    }

    /**
     * Low level deserialization method, to be used for instance for reading scaling parameters nested in another object.
     */
    public static ScalingParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        return new ScalingParametersDeserializer().deserialize(parser, deserializationContext);
    }

    /**
     * Low level serialization method, to be used for instance for writing scaling parameters nested in another object.
     */
    public static void serialize(ScalingParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        new ScalingParametersSerializer().serialize(parameters, jsonGenerator, serializationContext);
    }
}
