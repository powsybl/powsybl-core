/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class ImportPostProcessorMock implements ImportPostProcessor {

    public ImportPostProcessorMock() {
    }

    @Override
    public String getName() {
        return "testReportNode";
    }

    @Override
    public void process(Network network, ComputationManager computationManager, ReportNode reportNode) throws Exception {
        network.setCaseDate(ZonedDateTime.of(2021, 12, 20, 0, 0, 0, 0, ZoneOffset.UTC));
        reportNode.newReportNode()
            .withMessageTemplate("testImportPostProcessor", "testing import post processor")
            .add();
    }
}
