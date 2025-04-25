/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
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
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProvider;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides methods to read and write DynamicSecurityAnalysisParameters from and to JSON.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public final class JsonDynamicSecurityAnalysisParameters {

    private JsonDynamicSecurityAnalysisParameters() {
    }

    public static Map<String, ExtensionJsonSerializer> getExtensionSerializers() {
        List<DynamicSecurityAnalysisProvider> providers = new ServiceLoaderCache<>(DynamicSecurityAnalysisProvider.class).getServices();
        return providers.stream()
                .flatMap(dynamicSAProvider -> dynamicSAProvider.getSpecificParametersSerializer().stream())
                .collect(Collectors.toMap(ExtensionProvider::getExtensionName,
                    dynamicSAProvider -> dynamicSAProvider));
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static DynamicSecurityAnalysisParameters read(Path jsonFile) {
        return update(new DynamicSecurityAnalysisParameters(), jsonFile);
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static DynamicSecurityAnalysisParameters read(InputStream jsonStream) {
        return update(new DynamicSecurityAnalysisParameters(), jsonStream);
    }

    /**
     * Updates parameters by reading the content of a JSON file.
     */
    public static DynamicSecurityAnalysisParameters update(DynamicSecurityAnalysisParameters parameters, Path jsonFile) {
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
    public static DynamicSecurityAnalysisParameters update(DynamicSecurityAnalysisParameters parameters, InputStream jsonStream) {
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
    public static void write(DynamicSecurityAnalysisParameters parameters, Path jsonFile) {
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
    public static void write(DynamicSecurityAnalysisParameters parameters, OutputStream outputStream) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(outputStream, parameters);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Low level deserialization method, to be used for instance for reading security analysis parameters nested in another object.
     */
    public static DynamicSecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext context, DynamicSecurityAnalysisParameters parameters) throws IOException {
        return new DynamicSecurityAnalysisParametersDeserializer().deserialize(parser, context, parameters);
    }

    /**
     * Low level deserialization method, to be used for instance for updating lsecurity analysis parameters nested in another object.
     */
    public static DynamicSecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return new DynamicSecurityAnalysisParametersDeserializer().deserialize(parser, context);
    }

    /**
     * Low level serialization method, to be used for instance for writing security analysis parameters nested in another object.
     */
    public static void serialize(DynamicSecurityAnalysisParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        new DynamicSecurityAnalysisParametersSerializer().serialize(parameters, jsonGenerator, serializerProvider);
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new DynamicSecurityAnalysisJsonModule());
    }
}
