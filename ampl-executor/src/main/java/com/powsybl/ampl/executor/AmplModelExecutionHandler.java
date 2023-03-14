/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.*;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.computation.*;
import com.powsybl.iidm.network.Network;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

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
 * @author Nicolas Pierre <nicolas.pierre@artelys.com>
 */
public class AmplModelExecutionHandler extends AbstractExecutionHandler<AmplResults> {

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
        for (AmplInputFile param : parameters.getInputParameters()) {
            try (InputStream paramStream = param.getParameterFileAsStream(this.mapper)) {
                Files.copy(paramStream, workingDir.resolve(param.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void exportNetworkAsAmpl(Path workingDir) throws IOException {
        DataSource networkExportPath = new FileDataSource(workingDir, this.model.getNetworkDataPrefix());
        // AmplExporter throws UncheckedIOException
        // but we are in a context where IOException are handled
        // thus we rethrow a simple IOException
        try {
            new AmplExporter().export(network, null, networkExportPath);
        } catch (UncheckedIOException rethrow) {
            throw new IOException(rethrow);
        }
    }

    private void doAfterSuccess(Path workingDir, AmplNetworkReader reader) throws IOException {
        readNetworkElements(reader);
        readCustomFiles(workingDir);
    }

    private void readCustomFiles(Path workingDir) throws IOException {
        for (AmplOutputFile amplOutputFile : parameters.getOutputParameters()) {
            Path outputPath = workingDir.resolve(amplOutputFile.getFileName());
            amplOutputFile.read(outputPath, this.mapper);
        }
    }

    private void readNetworkElements(AmplNetworkReader reader) throws IOException {
        for (AmplReadableElement element : this.model.getAmplReadableElement()) {
            element.readElement(reader);
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
        DataSource networkAmplResults = new FileDataSource(workingDir, this.model.getOutputFilePrefix());
        AmplNetworkReader reader = new AmplNetworkReader(networkAmplResults, this.network, this.model.getVariant(),
                mapper, this.model.getNetworkApplierFactory(), this.model.getOutputFormat());
        doAfterSuccess(workingDir, reader);
        return AmplResults.ok();
    }

}
