/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.security.SecurityAnalysisResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A utility class to work with security analysis result exporters
 *
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class SecurityAnalysisResultExporters {

    private SecurityAnalysisResultExporters() {
    }

    /**
     * Get all supported formats.
     *
     * @return the supported formats
     */
    public static Collection<String> getFormats() {
        List<String> formats = new ArrayList<>();
        for (SecurityAnalysisResultExporter e : ServiceLoader.load(SecurityAnalysisResultExporter.class)) {
            formats.add(e.getFormat());
        }
        return formats;
    }

    /**
     * Get the exporter for the specified format
     *
     * @param format The export format
     *
     * @return The exporter for the specified format or null if this format is not supported
     */
    public static SecurityAnalysisResultExporter getExporter(String format) {
        Objects.requireNonNull(format);
        for (SecurityAnalysisResultExporter e : ServiceLoader.load(SecurityAnalysisResultExporter.class)) {
            if (format.equals(e.getFormat())) {
                return e;
            }
        }
        return null;
    }

    public static void export(SecurityAnalysisResult result, Path path, String format) {
        Objects.requireNonNull(path);

        try (Writer writer = Files.newBufferedWriter(path)) {
            export(result, writer, format);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void export(SecurityAnalysisResult result, Writer writer, String format) {
        SecurityAnalysisResultExporter exporter = getExporter(format);
        if (exporter == null) {
            throw new PowsyblException("Unsupported format: " + format + " [" + getFormats() + "]");
        }

        exporter.export(result, writer);
    }
}
