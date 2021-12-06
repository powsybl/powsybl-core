package com.powsybl.iidm.import_;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

public class ImporterTestWithPostProcessor implements Importer  {

    ImportPostProcessor postProcessor;
    ComputationManager computationManager;

    public ImporterTestWithPostProcessor(ImportPostProcessor postProcessor, ComputationManager computationManager) {
        this.postProcessor = postProcessor;
        this.computationManager = computationManager;
    }

    @Override
    public String getFormat() {
        return "TST";
    }

    @Override
    public String getComment() {
        return "Dummy importer to test Importers";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            return dataSource == null || dataSource.exists(null, "tst");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, Reporter reporter) {
        Network network = EurostagTutorialExample1Factory.create();
        try {
            postProcessor.process(network, computationManager, reporter);
        } catch (Exception e) {
            throw new PowsyblException(e);
        }
        return network;

    }

}
