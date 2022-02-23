/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.scripting;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowProvider;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(LoadFlowProvider.class)
public class LFMock implements LoadFlowProvider {

    @Override
    public CompletableFuture<LoadFlowResult> run(Network network, ComputationManager computationManager, String workingStateId, LoadFlowParameters parameters) {
        return CompletableFuture.completedFuture(new LoadFlowResultImpl(true, Collections.emptyMap(), ""));
    }

    @Override
    public Optional<ExtensionJsonSerializer> getParametersExtensionSerializer() {
        return Optional.empty();
    }

    @Override
    public Optional<Extension<LoadFlowParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
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
