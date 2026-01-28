/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.action.json.ActionJsonModule;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProvider;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisProvider;
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
 * Provides methods to read and write SecurityAnalysisParameters from and to JSON.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public final class JsonSecurityAnalysisParameters {

    private JsonSecurityAnalysisParameters() {
    }

    public static Map<String, ExtensionJsonSerializer> getExtensionSerializers() {
        List<SecurityAnalysisProvider> providers = new ServiceLoaderCache<>(SecurityAnalysisProvider.class).getServices();
        return providers.stream()
                .flatMap(securityAnalysisProvider -> securityAnalysisProvider.getSpecificParametersSerializer().stream())
                .collect(Collectors.toMap(ExtensionProvider::getExtensionName,
                    securityAnalysisProvider -> securityAnalysisProvider));
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static SecurityAnalysisParameters read(Path jsonFile) {
        return update(new SecurityAnalysisParameters(), jsonFile);
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static SecurityAnalysisParameters read(InputStream jsonStream) {
        return update(new SecurityAnalysisParameters(), jsonStream);
    }

    /**
     * Updates parameters by reading the content of a JSON file.
     */
    public static SecurityAnalysisParameters update(SecurityAnalysisParameters parameters, Path jsonFile) {
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
    public static SecurityAnalysisParameters update(SecurityAnalysisParameters parameters, InputStream jsonStream) {
        JsonMapper jsonMapper = createJsonMapper();
        return jsonMapper.readerForUpdating(parameters).readValue(jsonStream);
    }

    /**
     * Writes parameters as JSON to a file.
     */
    public static void write(SecurityAnalysisParameters parameters, Path jsonFile) {
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
    public static void write(SecurityAnalysisParameters parameters, OutputStream outputStream) {
        JsonMapper jsonMapper = createJsonMapper();
        ObjectWriter writer = jsonMapper.writerWithDefaultPrettyPrinter();
        writer.writeValue(outputStream, parameters);
    }

    /**
     * Low level deserialization method, to be used for instance for reading security analysis parameters nested in another object.
     */
    public static SecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext context, SecurityAnalysisParameters parameters) throws JacksonException {
        return new SecurityAnalysisParametersDeserializer().deserialize(parser, context, parameters);
    }

    /**
     * Low level deserialization method, to be used for instance for updating lsecurity analysis parameters nested in another object.
     */
    public static SecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        return new SecurityAnalysisParametersDeserializer().deserialize(parser, context);
    }

    /**
     * Low level serialization method, to be used for instance for writing security analysis parameters nested in another object.
     */
    public static void serialize(SecurityAnalysisParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        new SecurityAnalysisParametersSerializer().serialize(parameters, jsonGenerator, serializationContext);
    }

    private static JsonMapper createJsonMapper() {
        return JsonUtil.createJsonMapperBuilder()
            .addModule(new SecurityAnalysisJsonModule())
            .addModule(new ActionJsonModule())
            .build();
    }
}
