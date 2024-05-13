/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.scripting

import com.google.auto.service.AutoService
import com.powsybl.computation.ComputationManager
import com.powsybl.iidm.network.Network
import com.powsybl.loadflow.LoadFlow
import com.powsybl.loadflow.LoadFlowParameters
import com.powsybl.scripting.groovy.GroovyScriptExtension

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(GroovyScriptExtension.class)
class LoadFlowGroovyScriptExtension implements GroovyScriptExtension {

    private final LoadFlowParameters parameters

    LoadFlowGroovyScriptExtension(LoadFlowParameters parameters) {
        assert parameters
        this.parameters = parameters
    }

    LoadFlowGroovyScriptExtension() {
        this(LoadFlowParameters.load())
    }

    @Override
    void load(Binding binding, ComputationManager computationManager) {
        binding.loadFlow = { Network network, LoadFlowParameters parameters = this.parameters ->
            LoadFlow.run(network, network.getVariantManager().getWorkingVariantId(), computationManager, parameters)
        }
        binding.loadflow = { Network network, LoadFlowParameters parameters = this.parameters ->
            LoadFlow.run(network, network.getVariantManager().getWorkingVariantId(), computationManager, parameters)
        }
    }

    @Override
    void unload() {
    }
}
