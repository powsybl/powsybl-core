/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.AmplExporter;
import com.powsybl.ampl.converter.AmplNetworkReader;
import com.powsybl.ampl.converter.AmplUtil;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.computation.*;
import com.powsybl.iidm.network.Network;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

/**
 * This executionHandler will run an ampl model on a network.
 * <p>
 * It copies every file given by {@link IAmplModel#getAmplRunFiles()} in
 * the working directory. It exports the Network with
 * {@link AmplExporter#export}.
 * <p>
 * Then it runs the ampl model, and {@link AmplNetworkReader#read} is used to
 * apply modifications on the network.
 * <p>
 * The majority of the configuration is made by the {@link IAmplModel}
 * interface.
 */
public class AmplModelExecutionHandler extends AbstractExecutionHandler<AmplResults> {

    private static final String AMPL_BINARY = "ampl";
    private static final String COMMAND_ID = "AMPL_runner";
    private IAmplModel model;
    private Network network;
    private String networkVariant;
    private AmplConfig config;

    public AmplModelExecutionHandler(IAmplModel model, Network network, String networkVariant, AmplConfig config) {
        this.model = model;
        this.network = network;
        this.networkVariant = networkVariant;
        this.config = config;
    }

    public AmplModelExecutionHandler(IAmplModel model, Network network, String networkVariant) {
        this(model, network, networkVariant, AmplConfig.getConfig());
    }

    private void exportAmplModel(Path workingDir) throws IOException {
        for (Pair<String, InputStream> fileAndStream : model.getModelAsStream()) {
            Files.copy(fileAndStream.getRight(),
                    workingDir.resolve(fileAndStream.getLeft()),
                    StandardCopyOption.REPLACE_EXISTING);
            fileAndStream.getRight().close();
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

    protected static CommandExecution createAmplRunCommand(AmplConfig config, IAmplModel model) {
        Command cmd = new SimpleCommandBuilder()
                .id(COMMAND_ID)
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
        exportAmplModel(workingDir);
        return Collections.singletonList(createAmplRunCommand(this.config, this.model));
    }

    @Override
    public AmplResults after(Path workingDir, ExecutionReport report) throws IOException {
        super.after(workingDir.toAbsolutePath(), report);
        DataSource networkAmplResults = new FileDataSource(workingDir, this.model.getOutputFilePrefix());
        AmplNetworkReader reader = new AmplNetworkReader(networkAmplResults, this.network, this.model.getVariant(),
                AmplUtil.createMapper(this.network), this.model.getNetworkApplier(), this.model.getOutputFormat());
        // TODO read everything from the reader and use NetworkApplier.
        reader.readGenerators();
        return AmplResults.ok();
    }

}
