/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProvider;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.shortcircuit.ShortCircuitAnalysisProvider;
import com.powsybl.shortcircuit.ShortCircuitParameters;

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
 * @author Boubakeur Brahimi
 */
public final class JsonShortCircuitParameters {
    /**
     * A configuration loader interface for the {@link JsonShortCircuitParameters} extensions loaded from the platform configuration
     * @param <E> The extension class
     */
    public interface ExtensionSerializer<E extends Extension<ShortCircuitParameters>> extends ExtensionJsonSerializer<ShortCircuitParameters, E> {
    }

    private JsonShortCircuitParameters() {
    }

    public static Map<String, ExtensionJsonSerializer> getExtensionSerializers() {
        List<ShortCircuitAnalysisProvider> providers = new ServiceLoaderCache<>(ShortCircuitAnalysisProvider.class).getServices();
        return providers.stream()
                .flatMap(shortCircuitAnalysisProvider -> shortCircuitAnalysisProvider.getSpecificParametersSerializer().stream())
                .collect(Collectors.toMap(ExtensionProvider::getExtensionName, shortCircuitAnalysisProvider -> shortCircuitAnalysisProvider));
    }

    public static ShortCircuitParameters update(ShortCircuitParameters parameters, Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            return update(parameters, is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ShortCircuitParameters update(ShortCircuitParameters parameters, InputStream is) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            return objectMapper.readerForUpdating(parameters).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper().registerModule(new ShortCircuitAnalysisJsonModule());
    }

    public static void write(ShortCircuitParameters parameters, Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (OutputStream outputStream = Files.newOutputStream(jsonFile)) {
            write(parameters, outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void write(ShortCircuitParameters parameters, OutputStream outputStream) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(outputStream, parameters);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ShortCircuitParameters read(Path jsonFile) {
        return update(new ShortCircuitParameters(), jsonFile);
    }

    public static ShortCircuitParameters read(InputStream jsonStream) {
        return update(new ShortCircuitParameters(), jsonStream);
    }
}
