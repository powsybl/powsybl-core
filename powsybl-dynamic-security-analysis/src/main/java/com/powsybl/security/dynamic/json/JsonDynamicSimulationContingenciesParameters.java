/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.dynamic.DynamicSimulationContingenciesParameters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Provides methods to read and write DynamicSimulationContingenciesParameters from and to JSON.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public final class JsonDynamicSimulationContingenciesParameters {

    /**
     * A configuration loader interface for the DynamicSimulationContingenciesParameters extensions loaded from the platform configuration
     * @param <E> The extension class
     */
    public interface ExtensionSerializer<E extends Extension<DynamicSimulationContingenciesParameters>> extends ExtensionJsonSerializer<DynamicSimulationContingenciesParameters, E> {
    }

    /**
     *  Lazily initialized list of extension serializers.
     */
    private static final Supplier<ExtensionProviders<ExtensionSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionSerializer.class, "dynamic-simulation-parameters"));

    /**
     *  Gets the known extension serializers.
     */
    public static ExtensionProviders<ExtensionSerializer> getExtensionSerializers() {
        return SUPPLIER.get();
    }

    private JsonDynamicSimulationContingenciesParameters() {
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static DynamicSimulationContingenciesParameters read(Path jsonFile) {
        return update(new DynamicSimulationContingenciesParameters(), jsonFile);
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static DynamicSimulationContingenciesParameters read(InputStream jsonStream) {
        return update(new DynamicSimulationContingenciesParameters(), jsonStream);
    }

    /**
     * Updates parameters by reading the content of a JSON file.
     */
    public static DynamicSimulationContingenciesParameters update(DynamicSimulationContingenciesParameters parameters, Path jsonFile) {
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
    public static DynamicSimulationContingenciesParameters update(DynamicSimulationContingenciesParameters parameters, InputStream jsonStream) {
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
    public static void write(DynamicSimulationContingenciesParameters parameters, Path jsonFile) {
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
    public static void write(DynamicSimulationContingenciesParameters parameters, OutputStream outputStream) {
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
                .registerModule(new DynamicSimulationContingenciesParametersJsonModule());
    }

    /**
     *  Low level deserialization method, to be used for instance for reading load flow parameters nested in another object.
     */
    public static DynamicSimulationContingenciesParameters deserialize(JsonParser parser, DeserializationContext context, DynamicSimulationContingenciesParameters parameters) throws IOException {
        return new DynamicSimulationContingenciesParametersDeserializer().deserialize(parser, context, parameters);
    }

    /**
     *  Low level deserialization method, to be used for instance for updating load flow parameters nested in another object.
     */
    public static DynamicSimulationContingenciesParameters deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return new DynamicSimulationContingenciesParametersDeserializer().deserialize(parser, context);
    }

    /**
     *  Low level serialization method, to be used for instance for writing load flow parameters nested in another object.
     */
    public static void serialize(DynamicSimulationContingenciesParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        new DynamicSimulationContingenciesParametersSerializer().serialize(parameters, jsonGenerator, serializerProvider);
    }
}
