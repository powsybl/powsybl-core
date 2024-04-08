/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.powsybl.loadflow.LoadFlowResult.ComponentResult.Status.CONVERGED;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(LoadFlowProvider.class)
public class LoadFlowProviderMock extends AbstractNoSpecificParametersLoadFlowProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowProviderMock.class);

    @Override
    public String getName() {
        return "LoadFlowMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public CompletableFuture<LoadFlowResult> run(Network network, ComputationManager computationManager, String workingStateId, LoadFlowParameters parameters, ReportNode reportNode) {
        LOGGER.warn("Running loadflow mock");

        // Add a converged ComponentResult
        List<LoadFlowResult.ComponentResult> componentResults = new ArrayList<>();
        componentResults.add(new LoadFlowResultImpl.ComponentResultImpl(1, 2, CONVERGED, 3, "id", 4d, 5d));

        return CompletableFuture.completedFuture(new LoadFlowResultImpl(true, Collections.emptyMap(), "", componentResults));
    }
}
