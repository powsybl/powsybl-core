/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest.DummySerializer;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest.DummyExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(LoadFlowProvider.class)
public class LoadFlowProviderMock implements LoadFlowProvider {

    @Override
    public CompletableFuture<LoadFlowResult> run(Network network, ComputationManager computationManager, String workingStateId, LoadFlowParameters parameters) {
        return CompletableFuture.completedFuture(new LoadFlowResultImpl(true, Collections.emptyMap(), ""));
    }

    @Override
    public Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.of(new DummySerializer());
    }

    @Override
    public Optional<Extension<LoadFlowParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.of(new DummyExtension());
    }

    @Override
    public String getName() {
        return "LoadFlowMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public Optional<Extension<LoadFlowParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.of(new DummyExtension());
    }

    @Override
    public List<String> getSpecificParametersNames() {
        return Collections.singletonList("dummy-extension");
    }
}
