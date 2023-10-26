/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.modification.scalable.ScalingParameters;

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
        try {
            ObjectMapper objectMapper = createObjectMapper();
            return objectMapper.readerForUpdating(parameters).readValue(jsonStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        try {
            ObjectMapper objectMapper = createObjectMapper();
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(outputStream, parameters);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new ScalingParametersJsonModule());
    }

    /**
     * Low level deserialization method, to be used for instance for updating scaling parameters nested in another object.
     */
    public static ScalingParameters deserialize(JsonParser parser, DeserializationContext deserializationContext,
                                         ScalingParameters scalingParameters) throws IOException {
        return new ScalingParametersDeserializer().deserialize(parser, deserializationContext, scalingParameters);
    }

    /**
     * Low level deserialization method, to be used for instance for reading scaling parameters nested in another object.
     */
    public static ScalingParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return new ScalingParametersDeserializer().deserialize(parser, deserializationContext);
    }

    /**
     * Low level serialization method, to be used for instance for writing scaling parameters nested in another object.
     */
    public static void serialize(ScalingParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        new ScalingParametersSerializer().serialize(parameters, jsonGenerator, serializerProvider);
    }
}
