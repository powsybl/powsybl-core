/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProvider;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.sensitivity.SensitivityAnalysisParameters;
import com.powsybl.sensitivity.SensitivityAnalysisProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class JsonSensitivityAnalysisParameters {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, SensitivityAnalysisParameters.EXTENSIONS_CATEGORY));

    public static Map<String, ExtensionJsonSerializer> getExtensionSerializers() {
        List<SensitivityAnalysisProvider> providers = new ServiceLoaderCache<>(SensitivityAnalysisProvider.class).getServices();
        // provider specific serializers take precedence: they may also be registered in the category
        return Stream.concat(SUPPLIER.get().getProviders().stream(),
                        providers.stream().flatMap(sensitivityAnalysisProvider -> sensitivityAnalysisProvider.getSpecificParametersSerializer().stream()))
                .collect(Collectors.toMap(ExtensionProvider::getExtensionName, Function.identity(), (s1, s2) -> s2));
    }

    private JsonSensitivityAnalysisParameters() {
    }

    public static SensitivityAnalysisParameters read(Path jsonFile) {
        return update(new SensitivityAnalysisParameters(), jsonFile);
    }

    /**
     * Reads parameters from a JSON file (will NOT rely on platform config).
     */
    public static SensitivityAnalysisParameters read(InputStream jsonStream) {
        return update(new SensitivityAnalysisParameters(), jsonStream);
    }

    /**
     * Updates parameters by reading the content of a JSON file.
     */
    public static SensitivityAnalysisParameters update(SensitivityAnalysisParameters parameters, Path jsonFile) {
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
    public static SensitivityAnalysisParameters update(SensitivityAnalysisParameters parameters, InputStream jsonStream) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            return objectMapper.readerForUpdating(parameters).readValue(jsonStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new SensitivityJsonModule());
    }
}
