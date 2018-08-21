/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

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
import com.powsybl.sensitivity.SensitivityComputationParameters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Provides methods to read and write sensitivity computation parameters from and to JSON.
 *
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public final class JsonSensitivityComputationParameters {

    /**
     * A configuration loader interface for the SensitivityComputationParameters extensions loaded from the platform configuration
     * @param <E> The extension class
     */
    public interface ExtensionSerializer<E extends Extension<SensitivityComputationParameters>> extends ExtensionJsonSerializer<SensitivityComputationParameters, E> {
    }

    /**
     *  Lazily initialized list of extension serializers.
     */
    private static final Supplier<ExtensionProviders<ExtensionSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionSerializer.class, "sensitivity-parameters"));

    /**
     *  Gets the known extension serializers.
     */
    public static ExtensionProviders<ExtensionSerializer> getExtensionSerializers() {
        return SUPPLIER.get();
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static SensitivityComputationParameters read(Path jsonFile) {
        return update(new SensitivityComputationParameters(), jsonFile);
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static SensitivityComputationParameters read(InputStream jsonStream) {
        return update(new SensitivityComputationParameters(), jsonStream);
    }

    /**
     * Updates parameters by reading the content of a JSON file.
     */
    public static SensitivityComputationParameters update(SensitivityComputationParameters parameters, Path jsonFile) {
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
    public static SensitivityComputationParameters update(SensitivityComputationParameters parameters, InputStream jsonStream) {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(jsonStream);

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
    public static void write(SensitivityComputationParameters parameters, Path jsonFile) {
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
    public static void write(SensitivityComputationParameters parameters, OutputStream outputStream) {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(outputStream);

        try {
            ObjectMapper objectMapper = createObjectMapper();
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(outputStream, parameters);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     *  Low level deserialization method, to be used for instance for reading sensitivity computation parameters nested in another object.
     */
    public static SensitivityComputationParameters deserialize(JsonParser parser, DeserializationContext context, SensitivityComputationParameters parameters) throws IOException {
        return new SensitivityComputationParametersDeserializer().deserialize(parser, context, parameters);
    }

    /**
     *  Low level deserialization method, to be used for instance for updating sensitivity computation parameters nested in another object.
     */
    public static SensitivityComputationParameters deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return new SensitivityComputationParametersDeserializer().deserialize(parser, context);
    }

    /**
     *  Low level serialization method, to be used for instance for writing sensitivity computation parameters nested in another object.
     */
    public static void serialize(SensitivityComputationParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException  {
        new SensitivityComputationParametersSerializer().serialize(parameters, jsonGenerator, serializerProvider);
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new SensitivityComputationParametersJsonModule());
    }

    private JsonSensitivityComputationParameters() {
    }
}
