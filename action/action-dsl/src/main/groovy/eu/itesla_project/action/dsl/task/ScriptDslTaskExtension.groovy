/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.dsl.task

import com.google.auto.service.AutoService
import eu.itesla_project.action.dsl.DslConstants
import eu.itesla_project.action.dsl.spi.DslTaskExtension
import eu.itesla_project.computation.ComputationManager
import eu.itesla_project.contingency.tasks.ModificationTask
import eu.itesla_project.iidm.network.Network

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(DslTaskExtension.class)
class ScriptDslTaskExtension implements DslTaskExtension, DslConstants {
    @Override
    void addToSpec(MetaClass tasksSpecMetaClass, List<ModificationTask> tasks, Binding binding) {
        tasksSpecMetaClass.script = { Closure<Void> closure ->
            tasks.add(new ScriptTask({ Network network, ComputationManager computationManager ->
                Network oldNetwork = binding.getVariable("network")
                binding.setVariable("network", network)
                binding.setVariable("computationManager", computationManager)
                binding.setVariable(SCRIPT_IS_RUNNING, true)
                try {
                    closure.call()
                } finally {
                    binding.setVariable(SCRIPT_IS_RUNNING, null)
                    binding.setVariable("network", oldNetwork)
                    binding.setVariable("computationManager", null)
                }
            }))
        }
    }
}
