/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.converter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Provides easy access to known implementations of result "exporters". ({@link ShortCircuitAnalysisResultExporter})
 *
 * @author Boubakeur Brahimi
 */
public final class ShortCircuitAnalysisResultExporters {

    private ShortCircuitAnalysisResultExporters() {
    }

    public static Collection<String> getFormats() {
        List<String> formats = new ArrayList<>();
        for (ShortCircuitAnalysisResultExporter e : ServiceLoader.load(ShortCircuitAnalysisResultExporter.class, ShortCircuitAnalysisResultExporters.class.getClassLoader())) {
            formats.add(e.getFormat());
        }
        return formats;
    }

    public static ShortCircuitAnalysisResultExporter getExporter(String format) {
        Objects.requireNonNull(format);
        for (ShortCircuitAnalysisResultExporter e : ServiceLoader.load(ShortCircuitAnalysisResultExporter.class, ShortCircuitAnalysisResultExporters.class.getClassLoader())) {
            if (format.equals(e.getFormat())) {
                return e;
            }
        }
        return null;
    }

    public static void export(ShortCircuitAnalysisResult result, Path path, String format, Network network) {
        Objects.requireNonNull(path);
        try (Writer writer = Files.newBufferedWriter(path)) {
            export(result, writer, format, network);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void export(ShortCircuitAnalysisResult result, Writer writer, String format, Network network) throws IOException {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(format);

        ShortCircuitAnalysisResultExporter exporter = getExporter(format);
        if (exporter == null) {
            throw new PowsyblException("Unsupported format: " + format + " [" + getFormats() + "]");
        }

        exporter.export(result, writer, network);
    }
}
