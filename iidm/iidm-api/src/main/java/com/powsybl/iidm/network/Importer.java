/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.commons.parameters.Parameter;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is the base class for all IIDM importers.
 *
 * <p><code>Importer</code> lookup is based on the <code>ServiceLoader</code>
 * architecture so do not forget to create a
 * <code>META-INF/services/com.powsybl.iidm.network.Importer</code> file
 * with the fully qualified name of your <code>Importer</code> implementation.
 *
 * @see java.util.ServiceLoader
 * @see Importers
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface Importer {

    class ImporterWrapper implements Importer {

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
        public List<String> getSupportedExtensions() {
            return importer.getSupportedExtensions();
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
        public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, ReportNode reportNode) {
            Network network = importer.importData(dataSource, networkFactory, parameters, reportNode);
            for (String name : names) {
                try {
                    getPostProcessor(loader, name).process(network, computationManager, reportNode);
                } catch (Exception e) {
                    throw new PowsyblException(e);
                }
            }
            return network;
        }

        @Override
        public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
            return importData(dataSource, networkFactory, parameters, ReportNode.NO_OP);
        }

        @Override
        public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
            importer.copy(fromDataSource, toDataSource);
        }
    }

    /**
     * Get all supported import formats.
     */
    static Collection<String> getFormats(ImportersLoader loader) {
        Objects.requireNonNull(loader);
        return loader.loadImporters().stream().map(Importer::getFormat).collect(Collectors.toSet());
    }

    static Collection<String> getFormats() {
        return getFormats(new ImportersServiceLoader());
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

    static Collection<Importer> list(ImportersLoader loader, ComputationManager computationManager, ImportConfig config) {
        Objects.requireNonNull(loader);
        return loader.loadImporters().stream()
                .map(importer -> wrapImporter(loader, importer, computationManager, config))
                .collect(Collectors.toList());
    }

    static Collection<Importer> list(ComputationManager computationManager, ImportConfig config) {
        return list(new ImportersServiceLoader(), computationManager, config);
    }

    static Collection<Importer> list() {
        return list(LocalComputationManager.getDefault(), ImportConfig.CACHE.get());
    }

    /**
     * Find an importer for the specified format name. The returned importer will apply configured
     * {@link ImportPostProcessor}s on imported networks.
     *
     * @param loader             the loader responsible for providing the list of available importers and post processors
     * @param format             the import format
     * @param computationManager a computation manager which may be used by configured {@link ImportPostProcessor}s
     * @param config             the import configuration
     * @return the importer if one exists for the given format or <code>null</code> otherwise.
     */
    static Importer find(ImportersLoader loader, String format, @Nullable ComputationManager computationManager, ImportConfig config) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(loader);
        for (Importer importer : loader.loadImporters()) {
            if (format.equals(importer.getFormat())) {
                return wrapImporter(loader, importer, computationManager, config);
            }
        }
        return null;
    }

    /**
     * Find an importer for the specified format name. The returned importer will apply configured
     * {@link ImportPostProcessor}s on imported networks.
     *
     * <p>All declared services implementing the {@link Importer} interface are available.
     *
     * @param format             the import format
     * @param computationManager a computation manager which may be used by configured {@link ImportPostProcessor}s
     * @param config             the import configuration
     * @return the importer if one exists for the given format or <code>null</code> otherwise.
     */
    static Importer find(String format, @Nullable ComputationManager computationManager, ImportConfig config) {
        return find(new ImportersServiceLoader(), format, computationManager, config);
    }

    /**
     * Find an importer for the specified format name. The returned importer will apply configured
     * {@link ImportPostProcessor}s on imported networks.
     *
     * <p>All declared services implementing the {@link Importer} interface are available.
     * The import configuration is loaded from default platform config.
     *
     * @param format             the import format
     * @param computationManager a computation manager which may be used by configured {@link ImportPostProcessor}s
     * @return the importer if one exists for the given format or <code>null</code> otherwise.
     */
    static Importer find(String format, @Nullable ComputationManager computationManager) {
        return find(format, computationManager, ImportConfig.CACHE.get());
    }

    /**
     * Find an importer for the specified format name. The returned importer will apply configured
     * {@link ImportPostProcessor}s on imported networks.
     *
     * <p>All declared services implementing the {@link Importer} interface are available.
     * The import configuration is loaded from default platform config.
     * Import post processors will use the default instance of {@link LocalComputationManager},
     * as configured in default platform config.
     *
     * @param format             the import format
     * @return the importer if one exists for the given format or <code>null</code> otherwise.
     */
    static Importer find(String format) {
        return find(format, LocalComputationManager.getDefault());
    }

    static Collection<String> getPostProcessorNames(ImportersLoader loader) {
        Objects.requireNonNull(loader);
        return loader.loadPostProcessors().stream().map(ImportPostProcessor::getName).collect(Collectors.toList());
    }

    static Collection<String> getPostProcessorNames() {
        return getPostProcessorNames(new ImportersServiceLoader());
    }

    static Importer addPostProcessors(ImportersLoader loader, Importer importer, ComputationManager computationManager, String... names) {
        return new ImporterWrapper(loader, importer, computationManager, Arrays.asList(names));
    }

    static Importer addPostProcessors(Importer importer, ComputationManager computationManager, String... names) {
        return addPostProcessors(new ImportersServiceLoader(), importer, computationManager, names);
    }

    static Importer addPostProcessors(Importer importer, String... names) {
        return addPostProcessors(importer, LocalComputationManager.getDefault(), names);
    }

    static Importer setPostProcessors(ImportersLoader loader, Importer importer, ComputationManager computationManager, String... names) {
        Importer importer2 = removePostProcessors(importer);
        return addPostProcessors(loader, importer2, computationManager, names);
    }

    static Importer setPostProcessors(Importer importer, ComputationManager computationManager, String... names) {
        return setPostProcessors(new ImportersServiceLoader(), importer, computationManager, names);
    }

    static Importer setPostProcessors(Importer importer, String... names) {
        return setPostProcessors(importer, LocalComputationManager.getDefault(), names);
    }

    static Importer removePostProcessors(Importer importer) {
        Objects.requireNonNull(importer);
        if (importer instanceof ImporterWrapper importerWrapper) {
            return removePostProcessors(importerWrapper.getImporter());
        }
        return importer;
    }

    static Importer find(ReadOnlyDataSource dataSource, ImportersLoader loader, ComputationManager computationManager, ImportConfig config) {
        for (Importer importer : list(loader, computationManager, config)) {
            if (importer.exists(dataSource)) {
                return importer;
            }
        }
        return null;
    }

    static Importer find(ReadOnlyDataSource dataSource, ComputationManager computationManager) {
        return find(dataSource, new ImportersServiceLoader(), computationManager, ImportConfig.CACHE.get());
    }

    static Importer find(ReadOnlyDataSource dataSource) {
        return find(dataSource, LocalComputationManager.getDefault());
    }

    /**
     * Get a unique identifier of the format.
     */
    String getFormat();

    default List<String> getSupportedExtensions() {
        return Collections.emptyList();
    }

    /**
     * Get a description of import parameters
     * @return
     */
    default List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    /**
     * Get some information about this importer.
     */
    String getComment();

    /**
     * Check if the data source is importable
     * @param dataSource the data source
     * @return true if the data source is importable, false otherwise
     */
    boolean exists(ReadOnlyDataSource dataSource);

    /**
     * @deprecated Use {@link Importer#importData(ReadOnlyDataSource, NetworkFactory, Properties)} instead.
     */
    @Deprecated(since = "2.6.0")
    default Network importData(ReadOnlyDataSource dataSource, Properties parameters) {
        return importData(dataSource, NetworkFactory.findDefault(), parameters);
    }

    /**
     * Create a model.
     *
     * @param dataSource data source
     * @param networkFactory network factory
     * @param parameters some properties to configure the import
     * @return the model
     */
    default Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        return importData(dataSource, networkFactory, parameters, ReportNode.NO_OP);
    }

    /**
     * Create a model.
     *
     * @param dataSource data source
     * @param networkFactory network factory
     * @param parameters some properties to configure the import
     * @param reportNode the reportNode used for functional logs
     * @return the model
     */
    default Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, ReportNode reportNode) {
        return importData(dataSource, networkFactory, parameters);
    }

    /**
     * Update a model with additional data.
     *
     * @param dataSource data source
     * @param parameters some properties to configure the import
     */
    default void importData(ReadOnlyDataSource dataSource, Network network, Properties parameters) {
        importData(dataSource, network, parameters, ReportNode.NO_OP);
    }

    /**
     * Update a model with additional data.
     *
     * @param dataSource data source
     * @param parameters some properties to configure the import
     * @param reportNode the reportNode used for functional logs
     */
    default void importData(ReadOnlyDataSource dataSource, Network network, Properties parameters, ReportNode reportNode) {
        throw new UnsupportedOperationException("Import data over existing network not supported");
    }

    /**
     * Copy data from one data source to another.
     * @param fromDataSource from data source
     * @param toDataSource destination data source
     */
    default void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        throw new UnsupportedOperationException("Copy not implemented");
    }
}
