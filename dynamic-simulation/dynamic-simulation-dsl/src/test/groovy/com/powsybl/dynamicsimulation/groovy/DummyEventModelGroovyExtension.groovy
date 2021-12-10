/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.groovy

import com.google.auto.service.AutoService
import com.powsybl.dsl.DslException
import com.powsybl.dynamicsimulation.EventModel

import java.util.function.Consumer

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
@AutoService(EventModelGroovyExtension.class)
class DummyEventModelGroovyExtension implements EventModelGroovyExtension {

    static class DummyEventModelSpec {
        String id
        
        void id(String id) {
            this.id = id
        }
    }

    void load(Binding binding, Consumer<EventModel> consumer) {
        binding.dummyEventModel = { Closure<Void> closure ->
            def cloned = closure.clone()

            DummyEventModelSpec eventModelSpec = new DummyEventModelSpec()

            cloned.delegate = eventModelSpec
            cloned()
            if (!eventModelSpec.id) {
                throw new DslException("'id' field is not set")
            }

            consumer.accept(new DummyEventModel(eventModelSpec.id))
        }
    }
}
