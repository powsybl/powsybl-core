/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.mock;

import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author Quinary <itesla@quinary.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class LoadFlowMock implements LoadFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowMock.class);

    @Override
    public String getName() {
        return "LoadFlow Mock";
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public CompletableFuture<LoadFlowResult> run(String workingStateId, LoadFlowParameters parameters) {
        LOGGER.warn("Running loadflow mock");

        return CompletableFuture.completedFuture(new LoadFlowResultImpl(true, Collections.emptyMap(), ""));
    }
}
