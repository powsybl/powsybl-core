/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.converter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sensitivity.SensitivityAnalysisResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A utility class to work with sensitivity analysis result exporters
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class SensitivityAnalysisResultExporters {

    /**
     * Get all supported formats.
     *
     * @return the supported formats
     */
    public static Collection<String> getFormats() {
        List<String> formats = new ArrayList<>();
        for (SensitivityAnalysisResultExporter e : ServiceLoader.load(SensitivityAnalysisResultExporter.class, SensitivityAnalysisResultExporters.class.getClassLoader())) {
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
    public static SensitivityAnalysisResultExporter getExporter(String format) {
        Objects.requireNonNull(format);
        for (SensitivityAnalysisResultExporter e : ServiceLoader.load(SensitivityAnalysisResultExporter.class, SensitivityAnalysisResultExporters.class.getClassLoader())) {
            if (format.equals(e.getFormat())) {
                return e;
            }
        }
        return null;
    }

    /**
     * Export sensitivity analysis results in specified format and output path
     *
     * @param result The results to be exported
     * @param path The export path
     * @param format The export format
     */
    public static void export(SensitivityAnalysisResult result, Path path, String format) {
        Objects.requireNonNull(path);

        try (Writer writer = Files.newBufferedWriter(path)) {
            export(result, writer, format);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Export sensitivity analysis results in specified format and writer
     *
     * @param result The results to be exported
     * @param writer The export writer
     * @param format The export format
     */
    public static void export(SensitivityAnalysisResult result, Writer writer, String format) {
        SensitivityAnalysisResultExporter exporter = getExporter(format);
        if (exporter == null) {
            throw new PowsyblException("Unsupported format: " + format + " [" + getFormats() + "]");
        }

        exporter.export(result, writer);
    }

    private SensitivityAnalysisResultExporters() {
    }
}
