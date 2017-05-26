/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.api

import com.google.auto.service.AutoService
import com.google.common.base.Supplier
import com.google.common.base.Suppliers
import eu.itesla_project.commons.config.ComponentDefaultConfig
import eu.itesla_project.computation.ComputationManager
import eu.itesla_project.computation.script.GroovyScriptExtension
import eu.itesla_project.iidm.network.Network

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(GroovyScriptExtension.class)
class LoadFlowGroovyScriptExtension implements GroovyScriptExtension {

    private final Supplier<LoadFlowFactory> loadFlowFactorySupplier;

    private final LoadFlowParameters parameters;

    private LoadFlowGroovyScriptExtension(Supplier<LoadFlowFactory> loadFlowFactorySupplier, LoadFlowParameters parameters) {
        assert loadFlowFactorySupplier
        assert parameters
        this.loadFlowFactorySupplier = loadFlowFactorySupplier
        this.parameters = parameters
    }

    LoadFlowGroovyScriptExtension(LoadFlowFactory loadFlowFactory, LoadFlowParameters parameters) {
        assert loadFlowFactory
        assert parameters
        this.loadFlowFactorySupplier = { loadFlowFactory }
        this.parameters = parameters
    }

    LoadFlowGroovyScriptExtension() {
        this(Suppliers.memoize({ ComponentDefaultConfig.load().newFactoryImpl(LoadFlowFactory.class) }),
             LoadFlowParameters.load());
    }

    @Override
    void load(Binding binding, ComputationManager computationManager) {
        binding.runLoadFlow = { Network network, LoadFlowParameters parameters  = this.parameters ->
            LoadFlowFactory loadFlowFactory = loadFlowFactorySupplier.get()
            LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, 0);
            loadFlow.run()
        }
    }
}
