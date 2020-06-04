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
import com.powsybl.commons.datasource.*;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;

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

    private static final Supplier<ExportConfig> CONFIG = Suppliers.memoize(ExportConfig::load);

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

    private static Exporter wrapExporter(ExportersLoader loader, Exporter exporter, ComputationManager computationManager, ExportConfig config) {
        Objects.requireNonNull(computationManager);
        Objects.requireNonNull(config);
        List<String> postProcessorNames = config.getPostProcessors();
        if (postProcessorNames != null && !postProcessorNames.isEmpty()) {
            return new Exporters.ExporterWrapper(loader, exporter, computationManager, postProcessorNames);
        }
        return exporter;
    }

    public static Collection<Exporter> list(ExportersLoader loader, ComputationManager computationManager, ExportConfig config) {
        Objects.requireNonNull(loader);
        return loader.loadExporters().stream()
            .map(exporter -> wrapExporter(loader, exporter, computationManager, config))
            .collect(Collectors.toList());
    }

    public static Collection<Exporter> list(ComputationManager computationManager, ExportConfig config) {
        return list(LOADER.get(), computationManager, config);
    }

    public static Collection<Exporter> list() {
        return list(LocalComputationManager.getDefault(), CONFIG.get());
    }

    /**
     * Get an exporter.
     *
     * @param format the export format
     * @return the exporter if one exists for the given format or
     * <code>null</code> otherwise
     */
    public static Exporter getExporter(ExportersLoader loader, String format, ComputationManager computationManager, ExportConfig config) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(loader);
        for (Exporter e : loader.loadExporters()) {
            if (format.equals(e.getFormat())) {
                return wrapExporter(loader, e, computationManager, config);
            }
        }
        return null;
    }

    public static Exporter getExporter(ExportersLoader loader, String format) {
        return getExporter(loader, format, LocalComputationManager.getDefault(), CONFIG.get());
    }

    public static Exporter getExporter(String format, ComputationManager computationManager, ExportConfig config) {
        return getExporter(LOADER.get(), format, computationManager, config);
    }

    public static Exporter getExporter(String format, ComputationManager computationManager) {
        return getExporter(format, computationManager, CONFIG.get());
    }

    public static Exporter getExporter(String format) {
        return getExporter(format, LocalComputationManager.getDefault());
    }

    public static Collection<String> getPostProcessorNames(ExportersLoader loader) {
        Objects.requireNonNull(loader);
        return loader.loadPostProcessors().stream().map(ExportPostProcessor::getName).collect(Collectors.toList());
    }

    public static Collection<String> getPostProcessorNames() {
        return getPostProcessorNames(LOADER.get());
    }

    private static class ExporterWrapper implements Exporter {

        private final Exporter exporter;

        private final ComputationManager computationManager;

        private final List<String> names;

        private final ExportersLoader loader;

        ExporterWrapper(ExportersLoader loader, Exporter exporter, ComputationManager computationManager, List<String> names) {
            this.loader = Objects.requireNonNull(loader);
            this.exporter = exporter;
            this.computationManager = computationManager;
            this.names = names;
        }

        public Exporter getExporter() {
            return exporter;
        }

        @Override
        public String getFormat() {
            return exporter.getFormat();
        }

        @Override
        public List<Parameter> getParameters() {
            return exporter.getParameters();
        }

        @Override
        public String getComment() {
            return exporter.getComment();
        }

        private static ExportPostProcessor getPostProcessor(ExportersLoader loader, String name) {
            for (ExportPostProcessor epp : loader.loadPostProcessors()) {
                if (epp.getName().equals(name)) {
                    return epp;
                }
            }
            throw new PowsyblException("Post processor " + name + " not found");
        }

        @Override
        public void export(Network network, Properties parameters, DataSource dataSource) {
            Object nativeDataModel = conversion(network, parameters);
            for (String name : names) {
                try {
                    getPostProcessor(loader, name).process(network, getFormat(), nativeDataModel, computationManager);
                } catch (Exception e) {
                    throw new PowsyblException(e);
                }
            }
            export(nativeDataModel, parameters, dataSource);
        }

        @Override
        public Object conversion(Network network, Properties parameters) {
            return exporter.conversion(network, parameters);
        }

        @Override
        public void export(Object nativeDataModel, Properties parameters, DataSource dataSource) {
            exporter.export(nativeDataModel, parameters, dataSource);
        }
    }

    public static Exporter addPostProcessors(ExportersLoader loader, Exporter exporter, ComputationManager computationManager, String... names) {
        return new Exporters.ExporterWrapper(loader, exporter, computationManager, Arrays.asList(names));
    }

    public static Exporter addPostProcessors(Exporter exporter, ComputationManager computationManager, String... names) {
        return addPostProcessors(LOADER.get(), exporter, computationManager, names);
    }

    public static Exporter addPostProcessors(Exporter exporter, String... names) {
        return addPostProcessors(exporter, LocalComputationManager.getDefault(), names);
    }

    public static Exporter setPostProcessors(ExportersLoader loader, Exporter exporter, ComputationManager computationManager, String... names) {
        Exporter exporter2 = removePostProcessors(exporter);
        return addPostProcessors(loader, exporter2, computationManager, names);
    }

    public static Exporter setPostProcessors(Exporter exporter, ComputationManager computationManager, String... names) {
        return setPostProcessors(LOADER.get(), exporter, computationManager, names);
    }

    public static Exporter setPostProcessors(Exporter exporter, String... names) {
        return setPostProcessors(exporter, LocalComputationManager.getDefault(), names);
    }

    public static Exporter removePostProcessors(Exporter exporter) {
        Objects.requireNonNull(exporter);
        if (exporter instanceof Exporters.ExporterWrapper) {
            return removePostProcessors(((Exporters.ExporterWrapper) exporter).getExporter());
        }
        return exporter;
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
     */
    public static void export(ExportersLoader loader, String format, Network network, Properties parameters, DataSource dataSource, ComputationManager computationManager, ExportConfig config) {
        Exporter exporter = getExporter(loader, format, computationManager, config);
        if (exporter == null) {
            throw new PowsyblException("Export format " + format + " not supported");
        }
        exporter.export(network, parameters, dataSource);
    }

    public static void export(ExportersLoader loader, String format, Network network, Properties parameters, DataSource dataSource) {
        export(loader, format, network, parameters, dataSource, LocalComputationManager.getDefault(), CONFIG.get());
    }

    public static void export(String format, Network network, Properties parameters, DataSource dataSource, ComputationManager computationManager) {
        export(LOADER.get(), format, network, parameters, dataSource, computationManager, CONFIG.get());
    }

    public static void export(String format, Network network, Properties parameters, DataSource dataSource) {
        export(format, network, parameters, dataSource, LocalComputationManager.getDefault());
    }

    /**
     * A convenient method to export a model to a given format.
     *
     * @param format the export format
     * @param network the model
     * @param parameters some properties to configure the export
     * @param file the network file
     */
    public static void export(ExportersLoader loader, String format, Network network, Properties parameters, Path file) {
        DataSource dataSource = createDataSource(file);
        export(loader, format, network, parameters, dataSource, LocalComputationManager.getDefault(), CONFIG.get());
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
     */
    public static void export(ExportersLoader loader, String format, Network network, Properties parameters, String directory, String baseName) {
        export(loader, format, network, parameters, new FileDataSource(Paths.get(directory), baseName), LocalComputationManager.getDefault(), CONFIG.get());
    }

    public static void export(String format, Network network, Properties parameters, String directory, String baseName) {
        export(LOADER.get(), format, network, parameters, directory, baseName);
    }

}
