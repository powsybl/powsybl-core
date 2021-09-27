/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityAnalysisParameters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Provides methods to read and write sensitivity analysis parameters from and to JSON.
 *
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public final class JsonSensitivityAnalysisParameters {

    /**
     * A configuration loader interface for the {@link SensitivityAnalysisParameters} extensions loaded from the platform configuration
     * @param <E> The extension class
     */
    public interface ExtensionSerializer<E extends Extension<SensitivityAnalysisParameters>> extends ExtensionJsonSerializer<SensitivityAnalysisParameters, E> {
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
    public static void write(SensitivityAnalysisParameters parameters, Path jsonFile) {
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
    public static void write(SensitivityAnalysisParameters parameters, OutputStream outputStream) {
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

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new SensitivityJsonModule());
    }

    private JsonSensitivityAnalysisParameters() {
    }
}
