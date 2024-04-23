/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.*;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class Importers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Importers.class);

    static final String UNSUPPORTED_FILE_FORMAT_OR_INVALID_FILE = "Unsupported file format or invalid file.";

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
     * @param reportNode the reportNode used for functional logs
     * @return the model
     */
    public static Network importData(ImportersLoader loader, String format, ReadOnlyDataSource dataSource, Properties parameters, ComputationManager computationManager, ImportConfig config, ReportNode reportNode) {
        Importer importer = Importer.find(loader, format, computationManager, config);
        if (importer == null) {
            throw new PowsyblException("Import format " + format + " not supported");
        }
        return importer.importData(dataSource, NetworkFactory.findDefault(), parameters, reportNode);
    }

    public static Network importData(ImportersLoader loader, String format, ReadOnlyDataSource dataSource, Properties parameters, ComputationManager computationManager, ImportConfig config) {
        return importData(loader, format, dataSource, parameters, computationManager, config, ReportNode.NO_OP);
    }

    public static Network importData(String format, ReadOnlyDataSource dataSource, Properties parameters, ComputationManager computationManager, ReportNode reportNode) {
        return importData(new ImportersServiceLoader(), format, dataSource, parameters, computationManager, ImportConfig.CACHE.get(), reportNode);
    }

    public static Network importData(String format, ReadOnlyDataSource dataSource, Properties parameters, ComputationManager computationManager) {
        return importData(new ImportersServiceLoader(), format, dataSource, parameters, computationManager, ImportConfig.CACHE.get(), ReportNode.NO_OP);
    }

    public static Network importData(String format, ReadOnlyDataSource dataSource, Properties parameters, ReportNode reportNode) {
        return importData(new ImportersServiceLoader(), format, dataSource, parameters, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), reportNode);
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
        return importData(format, DataSourceUtil.createDataSource(Paths.get(directory), "", baseName), parameters);
    }

    private static void doImport(ReadOnlyDataSource dataSource, Importer importer, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener, NetworkFactory networkFactory, ReportNode reportNode) {
        Objects.requireNonNull(consumer);
        try {
            if (listener != null) {
                listener.accept(dataSource);
            }
            Network network = importer.importData(dataSource, networkFactory, parameters, reportNode);
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

    public static void importAll(Path dir, Importer importer, boolean parallel, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener, ReportNode reportNode) throws IOException, InterruptedException, ExecutionException {
        importAll(dir, importer, parallel, parameters, consumer, listener, NetworkFactory.findDefault(), reportNode);
    }

    public static void importAll(Path dir, Importer importer, boolean parallel, Properties parameters, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener, NetworkFactory networkFactory, ReportNode reportNode) throws IOException, InterruptedException, ExecutionException {
        List<ReadOnlyDataSource> dataSources = new ArrayList<>();
        importAll(dir, importer, dataSources);
        if (parallel) {
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            try {
                List<Future<?>> futures = dataSources.stream()
                        .map(ds -> {
                            ReportNode child = createChildReportNode(reportNode, ds);
                            return executor.submit(() -> doImport(ds, importer, parameters, consumer, listener, networkFactory, child));
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
                doImport(dataSource, importer, parameters, consumer, listener, networkFactory, createChildReportNode(reportNode, dataSource));
            }
        }
    }

    private static ReportNode createChildReportNode(ReportNode reportNode, ReadOnlyDataSource ds) {
        return reportNode.newReportNode()
                .withMessageTemplate("importDataSource", "Import data source ${dataSource}")
                .withUntypedValue("dataSource", ds.getBaseName())
                .add();
    }

    public static void importAll(Path dir, Importer importer, boolean parallel, Consumer<Network> consumer, Consumer<ReadOnlyDataSource> listener) throws IOException, InterruptedException, ExecutionException {
        importAll(dir, importer, parallel, null, consumer, listener, ReportNode.NO_OP);
    }

    public static void importAll(Path dir, Importer importer, boolean parallel, Consumer<Network> consumer) throws IOException, InterruptedException, ExecutionException {
        importAll(dir, importer, parallel, consumer, null);
    }
}
