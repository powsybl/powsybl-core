/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.task

import com.google.auto.service.AutoService
import com.powsybl.action.dsl.ActionDslException
import com.powsybl.action.dsl.spi.DslTaskExtension
import com.powsybl.computation.ComputationManager
import com.powsybl.contingency.tasks.ModificationTask
import com.powsybl.iidm.network.Network

import static com.powsybl.action.dsl.GroovyDslConstants.SCRIPT_IS_RUNNING

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(DslTaskExtension.class)
class ScriptDslTaskExtension implements DslTaskExtension {
    @Override
    void addToSpec(MetaClass tasksSpecMetaClass, List<ModificationTask> tasks, Binding binding) {
        tasksSpecMetaClass.script = { Closure<Void> closure ->
            tasks.add(new ScriptTask({ Network network, ComputationManager computationManager ->
                Network oldNetwork = binding.getVariable("network")
                binding.setVariable("network", network)
                binding.setVariable("computationManager", computationManager)
                binding.setVariable(SCRIPT_IS_RUNNING, true)
                MetaClass delegateMetaClass = closure.owner.delegate.metaClass;
                try {
                    closure.owner.delegate.metaClass = null
                    closure.resolveStrategy = Closure.OWNER_ONLY
                    closure.call()
                } catch (MissingMethodException e) {
                    if (delegateMetaClass.respondsTo(closure, e.getMethod(), String)) {
                        throw new ActionDslException("Dsl extension task(" + e.getMethod() + ") is forbidden in task script")
                    } else {
                        throw e
                    }
                } finally {
                    binding.setVariable(SCRIPT_IS_RUNNING, null)
                    binding.setVariable("network", oldNetwork)
                    binding.setVariable("computationManager", null)
                }
            }))
        }
    }
}
