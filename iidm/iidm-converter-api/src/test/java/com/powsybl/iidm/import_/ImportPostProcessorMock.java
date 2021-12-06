package com.powsybl.iidm.import_;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

public class ImportPostProcessorMock implements ImportPostProcessor {

    public ImportPostProcessorMock() {
    }

    @Override
    public String getName() {
        return "testReporter";
    }

    @Override
    public void process(Network network, ComputationManager computationManager, Reporter reporter) throws Exception {
        reporter.report(Report.builder()
            .withKey("testImportPostProcessor")
            .withDefaultMessage("testing import post processor")
            .build());
    }
}
