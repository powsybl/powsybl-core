/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.*;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.computation.*;
import com.powsybl.iidm.network.Network;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * This executionHandler will run an ampl model on a network.
 * <p>
 * It copies every file given by {@link AmplModel#getAmplRunFiles()} in
 * the working directory. It exports the Network with
 * {@link AmplExporter#export}.
 * <p>
 * Then it runs the ampl model, and {@link AmplReadableElement#readElement} is used to
 * apply modifications on the network.
 * <p>
 * The majority of the configuration is made by the {@link AmplModel}
 * interface.
 *
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
public class AmplModelExecutionHandler extends AbstractExecutionHandler<AmplResults> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplModelExecutionHandler.class);

    private static final String AMPL_BINARY = "ampl";
    private static final String COMMAND_ID = "AMPL_runner";
    private final AmplParameters parameters;
    private final AmplModel model;
    private final Network network;
    private final String networkVariant;
    private final AmplConfig config;
    private final StringToIntMapper<AmplSubset> mapper;

    public AmplModelExecutionHandler(AmplModel model, Network network, String networkVariant, AmplConfig config,
                                     AmplParameters parameters) {
        this.model = model;
        this.network = network;
        this.networkVariant = networkVariant;
        this.config = config;
        this.parameters = parameters;
        this.mapper = AmplUtil.createMapper(this.network);
    }

    /**
     * This method will write ampl model files (.run, .dat and .mod)
     *
     * @param workingDir the directory where to write the files
     * @throws IOException rethrow {@link Files#copy(InputStream, Path, CopyOption...)}
     */
    private void exportAmplModel(Path workingDir) throws IOException {
        for (Pair<String, InputStream> fileAndStream : model.getModelAsStream()) {
            try (InputStream modelStream = fileAndStream.getRight()) {
                Files.copy(modelStream, workingDir.resolve(fileAndStream.getLeft()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * This method will write parameters files.
     *
     * @param workingDir the directory where to write the files
     * @throws IOException rethrow {@link Files#copy(InputStream, Path, CopyOption...)}
     */
    private void exportAmplParameters(Path workingDir) throws IOException {
        for (AmplInputFile amplInputFile : parameters.getInputParameters()) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(
                workingDir.resolve(amplInputFile.getFileName()),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING)) {
                amplInputFile.write(bufferedWriter, mapper);
            }
        }
    }

    private void exportNetworkAsAmpl(Path workingDir) {
        DataSource networkExportDataSource = DataSourceUtil.createDataSource(workingDir, this.model.getNetworkDataPrefix(), "");
        if (parameters.getAmplExportConfig() != null) {
            new AmplExporter().export(network, parameters.getAmplExportConfig(), networkExportDataSource);
        } else {
            new AmplExporter().export(network, new Properties(), networkExportDataSource);
        }
    }

    /**
     * This function will do all the output file readings,
     * including ones injected by {@link AmplParameters#getOutputParameters}.
     * If an exception happens during a read, we won't process next files.
     *
     * @param hasModelConverged if <code>true</code>, network files are read
     */
    private void postProcess(Path workingDir, AmplNetworkReader reader, boolean hasModelConverged) {
        if (hasModelConverged) {
            readNetworkElements(reader);
        }
        readCustomFiles(workingDir, hasModelConverged);
    }

    private Map<String, String> readIndicators(AmplNetworkReader reader) {
        Map<String, String> metrics = new HashMap<>();
        try {
            reader.readMetrics(metrics);
        } catch (IOException e) {
            throw new PowsyblException("Failed to parse ampl metrics.", e);
        }
        return metrics;
    }

    private void readCustomFiles(Path workingDir, boolean hasModelConverged) {
        for (AmplOutputFile amplOutputFile : parameters.getOutputParameters(hasModelConverged)) {
            Path customFilePath = workingDir.resolve(amplOutputFile.getFileName());
            if (Files.isRegularFile(customFilePath)) {
                try (BufferedReader reader = Files.newBufferedReader(customFilePath, StandardCharsets.UTF_8)) {
                    amplOutputFile.read(reader, mapper);
                } catch (IOException e) {
                    LOGGER.error("Failed to read custom output file : " + customFilePath.toAbsolutePath(), e);
                    throw new UncheckedIOException(e);
                }
            } else if (amplOutputFile.throwOnMissingFile()) {
                throw new PowsyblException("Custom output file '" + customFilePath + "' not found");
            }
        }
    }

    private void readNetworkElements(AmplNetworkReader reader) {
        for (AmplReadableElement element : this.model.getAmplReadableElement()) {
            try {
                element.readElement(reader);
            } catch (IOException e) {
                LOGGER.error("Failed to read network element output : " + element.name(), e);
                throw new UncheckedIOException(e);
            }
        }
    }

    protected static CommandExecution createAmplRunCommand(AmplConfig config, AmplModel model) {
        Command cmd = new SimpleCommandBuilder().id(COMMAND_ID)
                                                .program(getAmplBinPath(config))
                                                .args(model.getAmplRunFiles())
                                                .build();
        return new CommandExecution(cmd, 1, 0);
    }

    protected static String getAmplBinPath(AmplConfig cfg) {
        return cfg.getAmplHome() + File.separator + AMPL_BINARY;
    }

    @Override
    public List<CommandExecution> before(Path workingDir) throws IOException {
        network.getVariantManager().setWorkingVariant(this.networkVariant);
        exportNetworkAsAmpl(workingDir);
        exportAmplParameters(workingDir);
        exportAmplModel(workingDir);
        return Collections.singletonList(createAmplRunCommand(this.config, this.model));
    }

    @Override
    public AmplResults after(Path workingDir, ExecutionReport report) throws IOException {
        super.after(workingDir.toAbsolutePath(), report);
        DataSource networkAmplResults = DataSourceUtil.createDataSource(workingDir, this.model.getOutputFilePrefix(), "");
        AmplNetworkReader reader = new AmplNetworkReader(networkAmplResults, this.network, this.model.getVariant(),
                mapper, this.model.getNetworkUpdaterFactory(), this.model.getOutputFormat());
        Map<String, String> indicators = readIndicators(reader);
        boolean hasModelConverged = model.checkModelConvergence(indicators);
        postProcess(workingDir, reader, hasModelConverged);
        return new AmplResults(hasModelConverged, indicators);
    }

}
