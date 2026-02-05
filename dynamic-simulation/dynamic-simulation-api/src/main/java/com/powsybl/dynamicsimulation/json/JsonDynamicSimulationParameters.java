/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProvider;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;
import com.powsybl.dynamicsimulation.DynamicSimulationProvider;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;

/**
 * Provides methods to read and write DynamicSimulationParameters from and to JSON.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class JsonDynamicSimulationParameters {

    /**
     * A configuration loader interface for the DynamicSimulationParameters extensions loaded from the platform configuration
     * @param <E> The extension class
     */
    public interface ExtensionSerializer<E extends Extension<DynamicSimulationParameters>> extends ExtensionJsonSerializer<DynamicSimulationParameters, E> {
    }

    private JsonDynamicSimulationParameters() {
    }

    /**
     *  Gets the known extension serializers.
     */
    public static Map<String, ExtensionJsonSerializer> getExtensionSerializers() {
        List<DynamicSimulationProvider> providers = new ServiceLoaderCache<>(DynamicSimulationProvider.class).getServices();
        return providers.stream()
                .flatMap(provider -> provider.getSpecificParametersSerializer().stream())
                .collect(Collectors.toMap(ExtensionProvider::getExtensionName, Function.identity()));
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static DynamicSimulationParameters read(Path jsonFile) {
        return update(new DynamicSimulationParameters(), jsonFile);
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static DynamicSimulationParameters read(InputStream jsonStream) {
        return update(new DynamicSimulationParameters(), jsonStream);
    }

    /**
     * Updates parameters by reading the content of a JSON file.
     */
    public static DynamicSimulationParameters update(DynamicSimulationParameters parameters, Path jsonFile) {
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
    public static DynamicSimulationParameters update(DynamicSimulationParameters parameters, InputStream jsonStream) {
        JsonMapper jsonMapper = createJsonMapper();
        return jsonMapper.readerForUpdating(parameters).readValue(jsonStream);
    }

    /**
     * Writes parameters as JSON to a file.
     */
    public static void write(DynamicSimulationParameters parameters, Path jsonFile) {
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
    public static void write(DynamicSimulationParameters parameters, OutputStream outputStream) {
        JsonMapper jsonMapper = createJsonMapper();
        ObjectWriter writer = jsonMapper.writerWithDefaultPrettyPrinter();
        writer.writeValue(outputStream, parameters);
    }

    private static JsonMapper createJsonMapper() {
        return JsonUtil.createJsonMapperBuilder()
            .addModule(new DynamicSimulationParametersJsonModule())
            .build();
    }

    /**
     *  Low level deserialization method, to be used for instance for reading load flow parameters nested in another object.
     */
    public static DynamicSimulationParameters deserialize(JsonParser parser, DeserializationContext context, DynamicSimulationParameters parameters) throws JacksonException {
        return new DynamicSimulationParametersDeserializer().deserialize(parser, context, parameters);
    }

    /**
     *  Low level deserialization method, to be used for instance for updating load flow parameters nested in another object.
     */
    public static DynamicSimulationParameters deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        return new DynamicSimulationParametersDeserializer().deserialize(parser, context);
    }

    /**
     *  Low level serialization method, to be used for instance for writing load flow parameters nested in another object.
     */
    public static void serialize(DynamicSimulationParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        new DynamicSimulationParametersSerializer().serialize(parameters, jsonGenerator, serializationContext);
    }
}
