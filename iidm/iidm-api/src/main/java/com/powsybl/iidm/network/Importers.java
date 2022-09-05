/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.*;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
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

    private static final String UNSUPPORTED_FILE_FORMAT_OR_INVALID_FILE = "Unsupported file format or invalid file.";

    private Importers() {
    }

    /**
     * A convenient method to create a model from data in a given format.
     *
     * @param format the import format
     * @param dataSource data source
     * @param parameters some properties to configure the import
     * @param computationManager computation manager to use for default post processors
     * @param config the import configuration
     * @param reporter the reporter used for functional logs
     * @return the model
     */
    public static Network importData(ImportersLoader loader, String format, ReadOnlyDataSource dataSource, Properties parameters, ComputationManager computationManager, ImportConfig config, Reporter reporter) {
        Importer importer = Importer.find(loader, format, computationManager, config);
        if (importer == null) {
            throw new PowsyblException("Import format " + format + " not supported");
        }
        return importer.importData(dataSource, NetworkFactory.findDefault(), parameters, reporter);
    }

    public static Network importData(ImportersLoader loader, String format, ReadOnlyDataSource dataSource, Properties parameters, ComputationManager computationManager, ImportConfig config) {
        return importData(loader, format, dataSource, parameters, computationManager, config, Reporter.NO_OP);
    }

    public static Network importData(String format, ReadOnlyDataSource dataSource, Properties parameters, ComputationManager computationManager, Reporter reporter) {
        return importData(new ImportersServiceLoader(), format, dataSource, parameters, computationManager, ImportConfig.CACHE.get(), reporter);
    }

    public static Network importData(String format, ReadOnlyDataSource dataSource, Properties parameters, ComputationManager computationManager) {
        return importData(new ImportersServiceLoader(), format, dataSource, parameters, computationManager, ImportConfig.CACHE.get(), Reporter.NO_OP);
    }

    public static Network importData(String format, ReadOnlyDataSource dataSource, Properties parameters, Reporter reporter) {
        return importData(new ImportersServiceLoader(), format, dataSource, parameters, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), reporter);
    }

    public static Network importData(String format, ReadOnlyDataSource dataSource, Properties parameters) {
        return importData(format, dataSource, parameters, LocalComputationManager.getDefault());
    }

    /**
     * A convenient method to create a model from data in a given format.
     *
     * @param format     the import format
     * @param directory  the directory where input files are
     * @param baseName   a base name for all input files
     * @param parameters some properties to configure the import
     * @return           the model
     */
    public static Network importData(String format, String directory, String baseName, Properties parameters) {
        return importData(format, new FileDataSource(Paths.get(directory), baseName), parameters);
    }

    private static void doImport(ReadOnlyDataSource dataSource, Importer importer, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener, Reporter reporter) {
        Objects.requireNonNull(consumer);
        try {
            if (listener != null) {
                listener.accept(dataSource);
            }
            Network network = importer.importData(dataSource, NetworkFactory.findDefault(), parameters, reporter);
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

    public static void importAll(Path dir, Importer importer, boolean parallel, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener, Reporter reporter) throws IOException, InterruptedException, ExecutionException {
        List<ReadOnlyDataSource> dataSources = new ArrayList<>();
        importAll(dir, importer, dataSources);
        if (parallel) {
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            try {
                List<Future<?>> futures = dataSources.stream()
                        .map(ds -> {
                            Reporter child = createSubReporter(reporter, ds);
                            return executor.submit(() -> doImport(ds, importer, parameters, consumer, listener, child));
                        })
                        .collect(Collectors.toList());
                for (Future<?> future : futures) {
                    future.get();
                }
            } finally {
                executor.shutdownNow();
            }
        } else {
            for (ReadOnlyDataSource dataSource : dataSources) {
                doImport(dataSource, importer, parameters, consumer, listener, createSubReporter(reporter, dataSource));
            }
        }
    }

    private static Reporter createSubReporter(Reporter reporter, ReadOnlyDataSource ds) {
        return reporter.createSubReporter("importDataSource", "Import data source ${dataSource}", "dataSource", ds.getBaseName());
    }

    public static void importAll(Path dir, Importer importer, boolean parallel, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        importAll(dir, importer, parallel, null, consumer, listener, Reporter.NO_OP);
    }

    public static void importAll(Path dir, Importer importer, boolean parallel, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        importAll(dir, importer, parallel, consumer, null);
    }

    /**
     * @deprecated Use {@link Parameter#read(String, Properties, Parameter)} instead
     */
    @Deprecated
    public static Object readParameter(String format, Properties parameters, Parameter configuredParameter) {
        return Parameter.read(format, parameters, configuredParameter);
    }

    /**
     * @deprecated Use {@link Parameter#read(String, Properties, Parameter, ParameterDefaultValueConfig)} instead
     */
    @Deprecated
    public static Object readParameter(String format, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return Parameter.read(format, parameters, configuredParameter, defaultValueConfig);
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

    /**
     * Loads a network from the specified file, trying to guess its format.
     *
     * @param file               The file to be loaded.
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @param networkFactory     Network factory
     * @param loader             Provides the list of available importers and post-processors
     * @param reporter           The reporter used for functional logs
     * @return                   The loaded network
     */
    public static Network loadNetwork(Path file, ComputationManager computationManager, ImportConfig config, Properties parameters, NetworkFactory networkFactory,
                                      ImportersLoader loader, Reporter reporter) {
        ReadOnlyDataSource dataSource = createDataSource(file);
        Importer importer = Importer.find(dataSource, loader, computationManager, config);
        if (importer != null) {
            return importer.importData(dataSource, networkFactory, parameters, reporter);
        }
        throw new PowsyblException(UNSUPPORTED_FILE_FORMAT_OR_INVALID_FILE);
    }

    public static Network loadNetwork(Path file, ComputationManager computationManager, ImportConfig config, Properties parameters,
                                      ImportersLoader loader, Reporter reporter) {
        return loadNetwork(file, computationManager, config, parameters, NetworkFactory.findDefault(), loader, reporter);
    }

    /**
     * Loads a network from the specified file, trying to guess its format.
     *
     * @param file               The file to be loaded.
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @param loader             Provides the list of available importers and post-processors
     * @return                   The loaded network
     */
    public static Network loadNetwork(Path file, ComputationManager computationManager, ImportConfig config, Properties parameters, ImportersLoader loader) {
        return loadNetwork(file, computationManager, config, parameters, loader, Reporter.NO_OP);
    }

    /**
     * Loads a network from the specified file, trying to guess its format,
     * and using importers and post processors defined as services.
     *
     * @param file               The file to be loaded.
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @return                   The loaded network
     */
    public static Network loadNetwork(Path file, ComputationManager computationManager, ImportConfig config, Properties parameters) {
        return loadNetwork(file, computationManager, config, parameters, new ImportersServiceLoader());
    }

    /**
     * Loads a network from the specified file, trying to guess its format,
     * and using importers and post processors defined as services.
     * Import will be performed using import configuration defined in default platform config,
     * and with no importer-specific parameters.
     * Post processors will use the default {@link LocalComputationManager}, as defined in
     * default platform config.
     *
     * @param file               The file to be loaded.
     * @return                   The loaded network
     */
    public static Network loadNetwork(Path file) {
        return loadNetwork(file, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), null);
    }

    /**
     * Loads a network from the specified file path, see {@link #loadNetwork(Path)}.
     *
     * @param file               The file to be loaded.
     * @return                   The loaded network
     */
    public static Network loadNetwork(String file) {
        return loadNetwork(Paths.get(file));
    }

    /**
     * Loads a network from a raw input stream, trying to guess the format from the specified filename.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @param loader             Provides the list of available importers and post-processors
     * @param reporter           The reporter used for functional logs
     * @return                   The loaded network
     */
    public static Network loadNetwork(String filename, InputStream data, ComputationManager computationManager, ImportConfig config, Properties parameters, ImportersLoader loader, Reporter reporter) {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource(DataSourceUtil.getBaseName(filename));
        dataSource.putData(filename, data);
        Importer importer = Importer.find(dataSource, loader, computationManager, config);
        if (importer != null) {
            return importer.importData(dataSource, NetworkFactory.findDefault(), parameters, reporter);
        }
        throw new PowsyblException(UNSUPPORTED_FILE_FORMAT_OR_INVALID_FILE);
    }

    /**
     * Loads a network from a raw input stream, trying to guess the format from the specified filename.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @param loader             Provides the list of available importers and post-processors
     * @return                   The loaded network
     */
    public static Network loadNetwork(String filename, InputStream data, ComputationManager computationManager, ImportConfig config, Properties parameters, ImportersLoader loader) {
        return loadNetwork(filename, data, computationManager, config, parameters, loader, Reporter.NO_OP);
    }

    /**
     * Loads a network from a raw input stream, trying to guess the format from the specified filename,
     * and using importers and post processors defined as services.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param computationManager A computation manager which may be used by import post-processors
     * @param config             The import config, in particular definition of post processors
     * @param parameters         Import-specific parameters
     * @return                   The loaded network
     */
    public static Network loadNetwork(String filename, InputStream data, ComputationManager computationManager, ImportConfig config, Properties parameters) {
        return loadNetwork(filename, data, computationManager, config, parameters, new ImportersServiceLoader());
    }

    /**
     * Loads a network from a raw input stream, trying to guess the format from the specified filename,
     * and using importers and post processors defined as services.
     * Import will be performed using import configuration defined in default platform config,
     * and with no importer-specific parameters.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param computationManager A computation manager which may be used by import post-processors
     * @return                   The loaded network
     */
    public static Network loadNetwork(String filename, InputStream data, ComputationManager computationManager) {
        return loadNetwork(filename, data, computationManager, ImportConfig.CACHE.get(), null);
    }

    /**
     * Loads a network from a raw input stream, trying to guess the format from the specified filename,
     * and using importers and post processors defined as services.
     * Import will be performed using import configuration defined in default platform config,
     * and with no importer-specific parameters.
     * Post processors will use the default {@link LocalComputationManager}, as defined in
     * default platform config.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @return                   The loaded network
     */
    public static Network loadNetwork(String filename, InputStream data) {
        return loadNetwork(filename, data, LocalComputationManager.getDefault());
    }

    /**
     * Loads a network from a raw input stream, trying to guess the format from the specified filename,
     * and using importers and post processors defined as services.
     * Import will be performed using import configuration defined in default platform config,
     * and with no importer-specific parameters.
     * Post processors will use the default {@link LocalComputationManager}, as defined in
     * default platform config.
     * Please note that the input stream must be from a simple file, not a zipped one.
     *
     * @param filename           The name of the file to be imported.
     * @param data               The raw data from which the network should be loaded
     * @param reporter           The reporter used for functional logs
     * @return                   The loaded network
     */
    public static Network loadNetwork(String filename, InputStream data, Reporter reporter) {
        return loadNetwork(filename, data, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), null, new ImportersServiceLoader(), reporter);
    }

    public static Network loadNetwork(ReadOnlyDataSource dataSource) {
        return loadNetwork(dataSource, null);
    }

    public static Network loadNetwork(ReadOnlyDataSource dataSource, Properties properties) {
        return loadNetwork(dataSource, properties, Reporter.NO_OP);
    }

    public static Network loadNetwork(ReadOnlyDataSource dataSource, Properties properties, Reporter reporter) {
        Importer importer = Importer.find(dataSource);
        if (importer != null) {
            return importer.importData(dataSource, NetworkFactory.findDefault(), properties, reporter);
        }
        throw new PowsyblException(UNSUPPORTED_FILE_FORMAT_OR_INVALID_FILE);
    }

    public static void loadNetworks(Path dir, boolean parallel, ImportersLoader loader, ComputationManager computationManager, ImportConfig config, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener, Reporter reporter) throws IOException, InterruptedException, ExecutionException {
        if (!Files.isDirectory(dir)) {
            throw new PowsyblException("Directory " + dir + " does not exist or is not a regular directory");
        }
        for (Importer importer : Importer.list(loader, computationManager, config)) {
            Importers.importAll(dir, importer, parallel, parameters, consumer, listener, reporter);
        }
    }

    public static void loadNetworks(Path dir, boolean parallel, ImportersLoader loader, ComputationManager computationManager, ImportConfig config, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, loader, computationManager, config, parameters, consumer, listener, Reporter.NO_OP);
    }

    public static void loadNetworks(Path dir, boolean parallel, ImportersLoader loader, ComputationManager computationManager, ImportConfig config, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, loader, computationManager, config, null, consumer, listener);
    }

    public static void loadNetworks(Path dir, boolean parallel, ComputationManager computationManager, ImportConfig config, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, new ImportersServiceLoader(), computationManager, config, parameters, consumer, listener);
    }

    public static void loadNetworks(Path dir, boolean parallel, ComputationManager computationManager, ImportConfig config, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, new ImportersServiceLoader(), computationManager, config, consumer, listener);
    }

    public static void loadNetworks(Path dir, boolean parallel, ComputationManager computationManager, ImportConfig config, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, computationManager, config, consumer, null);
    }

    public static void loadNetworks(Path dir, boolean parallel, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), consumer);
    }

    public static void loadNetworks(Path dir, boolean parallel, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, parallel, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), consumer, listener);
    }

    public static void loadNetworks(Path dir, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        loadNetworks(dir, false, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), consumer);
    }
}
