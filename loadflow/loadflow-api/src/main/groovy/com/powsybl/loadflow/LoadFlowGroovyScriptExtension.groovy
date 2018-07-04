/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow

import com.google.auto.service.AutoService
import com.google.common.base.Supplier
import com.google.common.base.Suppliers
import com.powsybl.commons.config.ComponentDefaultConfig
import com.powsybl.computation.ComputationManager
import com.powsybl.iidm.network.Network
import com.powsybl.scripting.groovy.GroovyScriptExtension

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(GroovyScriptExtension.class)
class LoadFlowGroovyScriptExtension implements GroovyScriptExtension {

    private final Supplier<LoadFlowFactory> loadFlowFactorySupplier

    private final LoadFlowParameters parameters

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
             LoadFlowParameters.load())
    }

    @Override
    void load(Binding binding, ComputationManager computationManager) {
        binding.loadFlow = { Network network, LoadFlowParameters parameters = this.parameters ->
            LoadFlowFactory loadFlowFactory = loadFlowFactorySupplier.get()
            LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, 0)
            loadFlow.run(network.getStateManager().getWorkingStateId(), parameters).join()
        }
    }
}
