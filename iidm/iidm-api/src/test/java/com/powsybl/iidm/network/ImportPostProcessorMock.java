/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import org.joda.time.DateTime;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ImportPostProcessorMock implements ImportPostProcessor {

    public ImportPostProcessorMock() {
    }

    @Override
    public String getName() {
        return "testReporter";
    }

    @Override
    public void process(Network network, ComputationManager computationManager, Reporter reporter) throws Exception {
        network.setCaseDate(new DateTime(2021, 12, 20, 0, 0, 0));
        reporter.report(Report.builder()
            .withKey("testImportPostProcessor")
            .withDefaultMessage("testing import post processor")
            .build());
    }
}
