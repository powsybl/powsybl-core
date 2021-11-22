/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceObserver;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A utility class to work with IIDM exporters.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Exporters {

    private static final Supplier<ExportersLoader> LOADER = Suppliers.memoize(ExportersServiceLoader::new);

    private Exporters() {
    }

    /**
     * Get all supported export formats.
     */
    public static Collection<String> getFormats(ExportersLoader loader) {
        Objects.requireNonNull(loader);
        return loader.loadExporters().stream().map(Exporter::getFormat).collect(Collectors.toSet());
    }

    public static Collection<String> getFormats() {
        return getFormats(LOADER.get());
    }

    /**
     * Get an exporter.
     *
     * @param format the export format
     * @return the exporter if one exists for the given format or
     * <code>null</code> otherwise
     */
    public static Exporter getExporter(ExportersLoader loader, String format) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(loader);
        for (Exporter e : loader.loadExporters()) {
            if (format.equals(e.getFormat())) {
                return e;
            }
        }
        return null;
    }

    public static Exporter getExporter(String format) {
        return getExporter(LOADER.get(), format);
    }

    public static DataSource createDataSource(Path directory, String fileNameOrBaseName, DataSourceObserver observer) {
        return DataSourceUtil.createDataSource(directory, fileNameOrBaseName, observer);
    }

    public static DataSource createDataSource(Path file, DataSourceObserver observer) {
        Objects.requireNonNull(file);
        if (Files.exists(file) && !Files.isRegularFile(file)) {
            throw new UncheckedIOException(new IOException("File " + file + " already exists and is not a regular file"));
        }
        Path absFile = file.toAbsolutePath();
        return createDataSource(absFile.getParent(), absFile.getFileName().toString(), observer);
    }

    public static DataSource createDataSource(Path file) {
        return createDataSource(file, null);
    }

    /**
     * A convenient method to export a model to a given format.
     *
     * @param format the export format
     * @param network the model
     * @param parameters some properties to configure the export
     * @param dataSource data source
     * @param reporter the reporter used for functional logs
     */
    public static void export(ExportersLoader loader, String format, Network network, Properties parameters, DataSource dataSource, Reporter reporter) {
        Exporter exporter = getExporter(loader, format);
        if (exporter == null) {
            throw new PowsyblException("Export format " + format + " not supported");
        }
        exporter.export(network, parameters, dataSource, reporter);
    }

    public static void export(ExportersLoader loader, String format, Network network, Properties parameters, DataSource dataSource) {
        export(loader, format, network, parameters, dataSource, Reporter.NO_OP);
    }

    public static void export(String format, Network network, Properties parameters, DataSource dataSource) {
        export(LOADER.get(), format, network, parameters, dataSource);
    }

    /**
     * A convenient method to export a model to a given format.
     *
     * @param format the export format
     * @param network the model
     * @param parameters some properties to configure the export
     * @param file the network file
     * @param reporter the reporter used for functional logs
     */
    public static void export(ExportersLoader loader, String format, Network network, Properties parameters, Path file, Reporter reporter) {
        DataSource dataSource = createDataSource(file);
        export(loader, format, network, parameters, dataSource, reporter);
    }

    public static void export(ExportersLoader loader, String format, Network network, Properties parameters, Path file) {
        export(loader, format, network, parameters, file, Reporter.NO_OP);
    }

    public static void export(String format, Network network, Properties parameters, Path file) {
        export(LOADER.get(), format, network, parameters, file);
    }

    /**
     * A convenient method to export a model to a given format.
     *
     * @param format the export format
     * @param network the model
     * @param parameters some properties to configure the export
     * @param directory the output directory where files are generated
     * @param baseName a base name for all generated files
     * @param reporter the reporter used for functional logs
     */
    public static void export(ExportersLoader loader, String format, Network network, Properties parameters, String directory, String baseName, Reporter reporter) {
        export(loader, format, network, parameters, new FileDataSource(Paths.get(directory), baseName), reporter);
    }

    public static void export(ExportersLoader loader, String format, Network network, Properties parameters, String directory, String basename) {
        export(loader, format, network, parameters, directory, basename, Reporter.NO_OP);
    }

    public static void export(String format, Network network, Properties parameters, String directory, String baseName) {
        export(LOADER.get(), format, network, parameters, directory, baseName);
    }

}
