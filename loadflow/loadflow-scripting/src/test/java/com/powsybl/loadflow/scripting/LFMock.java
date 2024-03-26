/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.loadflow.scripting;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.*;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
@AutoService(LoadFlowProvider.class)
public class LFMock extends AbstractNoSpecificParametersLoadFlowProvider {

    @Override
    public CompletableFuture<LoadFlowResult> run(Network network, ComputationManager computationManager, String workingStateId, LoadFlowParameters parameters, ReportNode reportNode) {
        return CompletableFuture.completedFuture(new LoadFlowResultImpl(true, Collections.emptyMap(), ""));
    }

    @Override
    public String getName() {
        return "LFMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
