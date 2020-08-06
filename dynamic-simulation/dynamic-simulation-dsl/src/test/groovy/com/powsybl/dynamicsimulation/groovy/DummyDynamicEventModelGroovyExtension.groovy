/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.groovy

import java.util.function.Consumer

import com.google.auto.service.AutoService
import com.powsybl.dsl.DslException
import com.powsybl.dynamicsimulation.DynamicEventModel

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
@AutoService(DynamicEventModelGroovyExtension.class)
class DummyDynamicEventModelGroovyExtension implements DynamicEventModelGroovyExtension {

    static class DummyDynamicEventModelSpec {
        String id
        
        void id(String id) {
            this.id = id
        }
    }

    void load(Binding binding, Consumer<DynamicEventModel> consumer) {
        binding.dummyDynamicEventModel = { Closure<Void> closure ->
            def cloned = closure.clone()

            DummyDynamicEventModelSpec dynamicEventModelSpec = new DummyDynamicEventModelSpec()

            cloned.delegate = dynamicEventModelSpec
            cloned()
            if (!dynamicEventModelSpec.id) {
                throw new DslException("'id' field is not set")
            }

            consumer.accept(new DummyDynamicEventModel(dynamicEventModelSpec.id))
        }
    }
}
