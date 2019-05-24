/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.*;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class to work with IIDM importers.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Importers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Importers.class);

    private static final Supplier<ImportersLoader> LOADER = Suppliers.memoize(ImportersServiceLoader::new);

    private static final Supplier<ImportConfig> CONFIG = Suppliers.memoize(ImportConfig::load);

    private Importers() {
    }

    /**
     * Get all supported import formats.
     */
    public static Collection<String> getFormats(ImportersLoader loader) {
        Objects.requireNonNull(loader);
        return loader.loadImporters().stream().map(Importer::getFormat).collect(Collectors.toSet());
    }

    public static Collection<String> getFormats() {
        return getFormats(LOADER.get());
    }

    private static Importer wrapImporter(ImportersLoader loader, Importer importer, ComputationManager computationManager, ImportConfig config) {
        Objects.requireNonNull(computationManager);
        Objects.requireNonNull(config);
        List<String> postProcessorNames = config.getPostProcessors();
        if (postProcessorNames != null && !postProcessorNames.isEmpty()) {
            return new ImporterWrapper(loader, importer, computationManager, postProcessorNames);
        }
        return importer;
    }

    public static Collection<Importer> list(ImportersLoader loader, ComputationManager computationManager, ImportConfig config) {
        Objects.requireNonNull(loader);
        return loader.loadImporters().stream()
                .map(importer -> wrapImporter(loader, importer, computationManager, config))
                .collect(Collectors.toList());
    }

    public static Collection<Importer> list(ComputationManager computationManager, ImportConfig config) {
        return list(LOADER.get(), computationManager, config);
    }

    public static Collection<Importer> list() {
        return list(LocalComputationManager.getDefault(), CONFIG.get());
    }

    /**
     * Get an importer.
     *
     * @param format the import format
     * @return the importer if one exists for the given format or
     * <code>null</code> otherwise.
     */
    public static Importer getImporter(ImportersLoader loader, String format, ComputationManager computationManager, ImportConfig config) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(loader);
        for (Importer importer : loader.loadImporters()) {
            if (format.equals(importer.getFormat())) {
                return wrapImporter(loader, importer, computationManager, config);
            }
        }
        return null;
    }

    public static Importer getImporter(String format, ComputationManager computationManager, ImportConfig config) {
        return getImporter(LOADER.get(), format, computationManager, config);
    }

    public static Importer getImporter(String format, ComputationManager computationManager) {
        return getImporter(format, computationManager, CONFIG.get());
    }

    public static Importer getImporter(String format) {
        return getImporter(format, LocalComputationManager.getDefault());
    }

    public static Collection<String> getPostProcessorNames(ImportersLoader loader) {
        Objects.requireNonNull(loader);
        return loader.loadPostProcessors().stream().map(ImportPostProcessor::getName).collect(Collectors.toList());
    }

    public static Collection<String> getPostProcessorNames() {
        return getPostProcessorNames(LOADER.get());
    }

    private static class ImporterWrapper implements Importer {

        private final Importer importer;

        private final ComputationManager computationManager;

        private final List<String> names;

        private final ImportersLoader loader;

        ImporterWrapper(ImportersLoader loader, Importer importer, ComputationManager computationManager, List<String> names) {
            this.loader = Objects.requireNonNull(loader);
            this.importer = importer;
            this.computationManager = computationManager;
            this.names = names;
        }

        public Importer getImporter() {
            return importer;
        }

        @Override
        public String getFormat() {
            return importer.getFormat();
        }

        @Override
        public List<Parameter> getParameters() {
            return importer.getParameters();
        }

        @Override
        public String getComment() {
            return importer.getComment();
        }

        @Override
        public boolean exists(ReadOnlyDataSource dataSource) {
            return importer.exists(dataSource);
        }

        private static ImportPostProcessor getPostProcessor(ImportersLoader loader, String name) {
            for (ImportPostProcessor ipp : loader.loadPostProcessors()) {
                if (ipp.getName().equals(name)) {
                    return ipp;
                }
            }
            throw new PowsyblException("Post processor " + name + " not found");
        }

        @Override
        public Network importData(ReadOnlyDataSource dataSource, Properties parameters) {
            Network network = importer.importData(dataSource, parameters);
            for (String name : names) {
                try {
                    getPostProcessor(loader, name).process(network, computationManager);
                } catch (Exception e) {
                    throw new PowsyblException(e);
                }
            }
            return network;
        }

        @Override
        public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
            importer.copy(fromDataSource, toDataSource);
        }
    }

    public static Importer addPostProcessors(ImportersLoader loader, Importer importer, ComputationManager computationManager, String... names) {
        return new ImporterWrapper(loader, importer, computationManager, Arrays.asList(names));
    }

    public static Importer addPostProcessors(Importer importer, ComputationManager computationManager, String... names) {
        return addPostProcessors(LOADER.get(), importer, computationManager, names);
    }

    public static Importer addPostProcessors(Importer importer, String... names) {
        return addPostProcessors(importer, LocalComputationManager.getDefault(), names);
    }

    public static Importer setPostProcessors(ImportersLoader loader, Importer importer, ComputationManager computationManager, String... names) {
        Importer importer2 = removePostProcessors(importer);
        return addPostProcessors(loader, importer2, computationManager, names);
    }

    public static Importer setPostProcessors(Importer importer, ComputationManager computationManager, String... names) {
        return setPostProcessors(LOADER.get(), importer, computationManager, names);
    }

    public static Importer setPostProcessors(Importer importer, String... names) {
        return setPostProcessors(importer, LocalComputationManager.getDefault(), names);
    }

    public static Importer removePostProcessors(Importer importer) {
        Objects.requireNonNull(importer);
        if (importer instanceof ImporterWrapper) {
            return removePostProcessors(((ImporterWrapper) importer).getImporter());
        }
        return importer;
    }

    /**
     * A convenient method to create a model from data in a given format.
     *
     * @param format the import format
     * @param dataSource data source
     * @param parameters some properties to configure the import
     * @param computationManager computation manager to use for default post processors
     * @return the model
     */
    public static Network importData(ImportersLoader loader, String format, ReadOnlyDataSource dataSource, Properties parameters, ComputationManager computationManager, ImportConfig config) {
        Importer importer = getImporter(loader, format, computationManager, config);
        if (importer == null) {
            throw new PowsyblException("Import format " + format + " not supported");
        }
        return importer.importData(dataSource, parameters);
    }

    public static Network importData(String format, ReadOnlyDataSource dataSource, Properties parameters, ComputationManager computationManager) {
        return importData(LOADER.get(), format, dataSource, parameters, computationManager, CONFIG.get());
    }

    public static Network importData(String format, ReadOnlyDataSource dataSource, Properties parameters) {
        return importData(format, dataSource, parameters, LocalComputationManager.getDefault());
    }

    /**
     * A convenient method to create a model from data in a given format.
     *
     * @param format the import format
     * @param directory the directory where input files are
     * @param baseName a base name for all input files
     * @param parameters some properties to configure the import
     * @return the model
     */
    public static Network importData(String format, String directory, String baseName, Properties parameters) {
        return importData(format, new FileDataSource(Paths.get(directory), baseName), parameters);
    }

    private static void doImport(ReadOnlyDataSource dataSource, Importer importer, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) {
        Objects.requireNonNull(consumer);
        try {
            if (listener != null) {
                listener.accept(dataSource);
            }
            Network network = importer.importData(dataSource, parameters);
            consumer.accept(network);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private static void addDataSource(Path dir, Path file, Importer importer, List<ReadOnlyDataSource> dataSources) {
        Objects.requireNonNull(importer);
        String caseBaseName = DataSourceUtil.getBaseName(file);
        ReadOnlyDataSource ds = new GenericReadOnlyDataSource(dir, caseBaseName);
        if (importer.exists(ds)) {
            dataSources.add(ds);
        }
    }

    private static void importAll(Path parent, Importer importer, List<ReadOnlyDataSource> dataSources) throws IOException {
        if (Files.isDirectory(parent)) {
            try (Stream<Path> stream = Files.list(parent)) {
                stream.sorted().forEach(child -> {
                    if (Files.isDirectory(child)) {
                        try {
                            importAll(child, importer, dataSources);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    } else {
                        addDataSource(parent, child, importer, dataSources);
                    }
                });
            }
        } else {
            if (parent.getParent() != null) {
                addDataSource(parent.getParent(), parent, importer, dataSources);
            }
        }
    }

    public static void importAll(Path dir, Importer importer, boolean parallel, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        List<ReadOnlyDataSource> dataSources = new ArrayList<>();
        importAll(dir, importer, dataSources);
        if (parallel) {
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            try {
                List<Future<?>> futures = dataSources.stream()
                        .map(ds -> executor.submit(() -> doImport(ds, importer, parameters, consumer, listener)))
                        .collect(Collectors.toList());
                for (Future<?> future : futures) {
                    future.get();
                }
            } finally {
                executor.shutdownNow();
            }
        } else {
            for (ReadOnlyDataSource dataSource : dataSources) {
                doImport(dataSource, importer, parameters, consumer, listener);
            }
        }
    }

    public static void importAll(Path dir, Importer importer, boolean parallel, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        importAll(dir, importer, parallel, null, consumer, listener);
    }

    public static void importAll(Path dir, Importer importer, boolean parallel, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        importAll(dir, importer, parallel, consumer, null);
    }

    /**
     * @deprecated Use {@link ConversionParameters#readParameter(String, Properties, Parameter)} instead
     */
    @Deprecated
    public static Object readParameter(String format, Properties parameters, Parameter configuredParameter) {
        return ConversionParameters.readParameter(format, parameters, configuredParameter);
    }

    /**
     * @deprecated Use {@link ConversionParameters#readParameter(String, Properties, Parameter, ParameterDefaultValueConfig)} instead
     */
    @Deprecated
    public static Object readParameter(String format, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return ConversionParameters.readParameter(format, parameters, configuredParameter, defaultValueConfig);
    }

    public static DataSource createDataSource(Path file) {
        Objects.requireNonNull(file);
        if (!Files.isRegularFile(file)) {
            throw new PowsyblException("File " + file + " does not exist or is not a regular file");
        }
        Path absFile = file.toAbsolutePath();
        return createDataSource(absFile.getParent(), absFile.getFileName().toString());
    }

    public static DataSource createDataSource(Path directory, String fileNameOrBaseName) {
        return DataSourceUtil.createDataSource(directory, fileNameOrBaseName, null);
    }

    public static Importer findImporter(ReadOnlyDataSource dataSource, ImportersLoader loader, ComputationManager computationManager, ImportConfig config) {
        for (Importer importer : Importers.list(loader, computationManager, config)) {
            if (importer.exists(dataSource)) {
                return importer;
            }
        }
        return null;
    }

    public static Importer findImporter(ReadOnlyDataSource dataSource, ComputationManager computationManager) {
        return findImporter(dataSource, LOADER.get(), computationManager, CONFIG.get());
    }

    public static Network loadNetwork(Path file, ComputationManager computationManager, ImportConfig config, Properties parameters, ImportersLoader loader) {
        ReadOnlyDataSource dataSource = createDataSource(file);
        Importer importer = findImporter(dataSource, loader, computationManager, config);
        if (importer != null) {
            return importer.importData(dataSource, parameters);
        }
        throw new PowsyblException("Unsupported file format or invalid file.");
    }

    public static Network loadNetwork(Path file, ComputationManager computationManager, ImportConfig config, Properties parameters) {
        return loadNetwork(file, computationManager, config, parameters, LOADER.get());
    }

    public static Network loadNetwork(Path file) {
        return loadNetwork(file, LocalComputationManager.getDefault(), CONFIG.get(), null);
    }

    public static Network loadNetwork(String file) {
        return loadNetwork(Paths.get(file));
    }

    public static Network loadNetwork(String filename, InputStream data, ComputationManager computationManager, ImportConfig config, Properties parameters, ImportersLoader loader) {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource(DataSourceUtil.getBaseName(filename));
        dataSource.putData(filename, data);
        Importer importer = findImporter(dataSource, loader, computationManager, config);
        if (importer != null) {
            return importer.importData(dataSource, parameters);
        }
        throw new PowsyblException("Unsupported file format or invalid file.");
    }

    public static Network loadNetwork(String filename, InputStream data, ComputationManager computationManager, ImportConfig config, Properties parameters) {
        return loadNetwork(filename, data, computationManager, config, parameters, LOADER.get());
    }

    public static Network loadNetwork(String filename, InputStream data, ComputationManager computationManager) {
        return loadNetwork(filename, data, computationManager, CONFIG.get(), null);
    }

    public static Network loadNetwork(String filename, InputStream data) {
        return loadNetwork(filename, data, LocalComputationManager.getDefault());
    }

    public static void loadNetworks(Path dir, boolean parallel, ImportersLoader loader, ComputationManager computationManager, ImportConfig config, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        if (!Files.isDirectory(dir)) {
            throw new PowsyblException("Directory " + dir + " does not exist or is not a regular directory");
        }
        for (Importer importer : Importers.list(loader, computationManager, config)) {
            Importers.importAll(dir, importer, parallel, parameters, consumer, listener);
        }
    }

    public static void loadNetworks(Path dir, boolean parallel, ImportersLoader loader, ComputationManager computationManager, ImportConfig config, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, loader, computationManager, config, null, consumer, listener);
    }

    public static void loadNetworks(Path dir, boolean parallel, ComputationManager computationManager, ImportConfig config, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, LOADER.get(), computationManager, config, parameters, consumer, listener);
    }

    public static void loadNetworks(Path dir, boolean parallel, ComputationManager computationManager, ImportConfig config, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, LOADER.get(), computationManager, config, consumer, listener);
    }

    public static void loadNetworks(Path dir, boolean parallel, ComputationManager computationManager, ImportConfig config, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, computationManager, config, consumer, null);
    }

    public static void loadNetworks(Path dir, boolean parallel, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, LocalComputationManager.getDefault(), CONFIG.get(), consumer);
    }

    public static void loadNetworks(Path dir, boolean parallel, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, LocalComputationManager.getDefault(), CONFIG.get(), consumer, listener);
    }

    public static void loadNetworks(Path dir, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, false, LocalComputationManager.getDefault(), CONFIG.get(), consumer);
    }
}
