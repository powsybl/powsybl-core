/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.json;

import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProvider;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisProvider;
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
        JsonMapper jsonMapper = createJsonMapper();
        return jsonMapper.readerForUpdating(parameters).readValue(jsonStream);
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
        JsonMapper jsonMapper = createJsonMapper();
        ObjectWriter writer = jsonMapper.writerWithDefaultPrettyPrinter();
        writer.writeValue(outputStream, parameters);
    }

    /**
     * Low level deserialization method, to be used for instance for reading security analysis parameters nested in another object.
     */
    public static DynamicSecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext context, DynamicSecurityAnalysisParameters parameters) throws JacksonException {
        return new DynamicSecurityAnalysisParametersDeserializer().deserialize(parser, context, parameters);
    }

    /**
     * Low level deserialization method, to be used for instance for updating lsecurity analysis parameters nested in another object.
     */
    public static DynamicSecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        return new DynamicSecurityAnalysisParametersDeserializer().deserialize(parser, context);
    }

    /**
     * Low level serialization method, to be used for instance for writing security analysis parameters nested in another object.
     */
    public static void serialize(DynamicSecurityAnalysisParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        new DynamicSecurityAnalysisParametersSerializer().serialize(parameters, jsonGenerator, serializationContext);
    }

    private static JsonMapper createJsonMapper() {
        return JsonUtil.createJsonMapperBuilder()
            .addModule(new DynamicSecurityAnalysisJsonModule())
            .build();
    }
}
