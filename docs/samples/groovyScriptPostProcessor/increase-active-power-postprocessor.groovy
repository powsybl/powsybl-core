/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import com.powsybl.loadflow.LoadFlowFactory
import com.powsybl.loadflow.LoadFlowParameters
import com.powsybl.commons.config.ComponentDefaultConfig

println "Network " + network.getId() +
    "\tGenerators: " + network.getGeneratorCount() +
    "\tLines: " + network.getLineCount() +
    "\tLoads: " + network.getLoadCount()

println "\nDump Loads"
println "id | p | p+1%"

// change the network
def percent = 1.01

network.getLoads().each { load ->
    if (load.getTerminal != null) {
        def currentValue = load.getTerminal().getP()
        load.getTerminal().setP(currentValue * percent)
        def newVal = load.getTerminal().getP()
        println load.getId() + " | " + currentValue + " | " + newVal
    }
}

// execute a LF
println "\nExecute a load-flow"

def defaultConfig = ComponentDefaultConfig.load()
loadFlowFactory = defaultConfig.newFactoryImpl(LoadFlowFactory.class)
loadFlowParameters = new LoadFlowParameters(LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES)
loadFlow = loadFlowFactory.create(network, computationManager, 0)
result = loadFlow.run(network.getStateManager().getWorkingStateId(),loadFlowParameters).join()

println "LF results - converge: " + result.ok + " ; metrics: " + result.getMetrics()
