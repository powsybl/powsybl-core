/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.api

import com.google.auto.service.AutoService
import eu.itesla_project.commons.config.ComponentDefaultConfig
import eu.itesla_project.computation.ComputationManager
import eu.itesla_project.computation.script.GroovyExtension
import eu.itesla_project.iidm.network.Network
import eu.itesla_project.loadflow.api.mock.LoadFlowFactoryMock

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(GroovyExtension.class)
class LoadFlowGroovyExtension implements GroovyExtension {

    private final LoadFlowFactory loadFlowFactory;

    private final LoadFlowParameters parameters;

    LoadFlowGroovyExtension(LoadFlowFactory loadFlowFactory, LoadFlowParameters parameters) {
        assert loadFlowFactory
        assert parameters
        this.loadFlowFactory = loadFlowFactory
        this.parameters = parameters
    }

    LoadFlowGroovyExtension() {
        this(ComponentDefaultConfig.load().newFactoryImpl(LoadFlowFactory.class),
             LoadFlowParameters.load());
    }

    @Override
    void load(Binding binding, ComputationManager computationManager) {
        binding.runLoadFlow = { Network network, LoadFlowParameters parameters  = this.parameters ->
            LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, 0);
            loadFlow.run()
        }
    }
}
