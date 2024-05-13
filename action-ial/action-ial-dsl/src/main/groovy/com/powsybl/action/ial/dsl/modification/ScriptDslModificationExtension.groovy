/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl.modification

import com.google.auto.service.AutoService
import com.powsybl.action.ial.dsl.ActionDslException
import com.powsybl.action.ial.dsl.spi.DslModificationExtension
import com.powsybl.computation.ComputationManager
import com.powsybl.iidm.network.Network
import com.powsybl.iidm.modification.NetworkModification

import static com.powsybl.dsl.GroovyDslConstants.SCRIPT_IS_RUNNING

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(DslModificationExtension.class)
class ScriptDslModificationExtension implements DslModificationExtension {
    @Override
    void addToSpec(MetaClass modificationsSpecMetaClass, List<NetworkModification> modifications, Binding binding) {
        modificationsSpecMetaClass.script = { Closure<Void> closure ->
            modifications.add(new ScriptNetworkModification({ Network network, ComputationManager computationManager ->
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
