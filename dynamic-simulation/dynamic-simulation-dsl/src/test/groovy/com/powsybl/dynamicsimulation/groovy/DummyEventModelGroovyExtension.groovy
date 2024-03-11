/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.groovy


import com.powsybl.commons.report.ReportNode

import java.util.function.Consumer

import com.google.auto.service.AutoService
import com.powsybl.dsl.DslException
import com.powsybl.dynamicsimulation.EventModel

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
@AutoService(EventModelGroovyExtension.class)
class DummyEventModelGroovyExtension implements EventModelGroovyExtension {

    static class DummyEventModelSpec {
        String id
        double startTime
        
        void id(String id) {
            this.id = id
        }

        void startTime(double startTime) {
            this.startTime = startTime
        }
    }

    void load(Binding binding, Consumer<EventModel> consumer, ReportNode reportNode) {
        binding.dummyEventModel = { Closure<Void> closure ->
            def cloned = closure.clone()

            DummyEventModelSpec eventModelSpec = new DummyEventModelSpec()

            cloned.delegate = eventModelSpec
            cloned()
            if (!eventModelSpec.id) {
                throw new DslException("'id' field is not set")
            }
            if (!eventModelSpec.startTime) {
                throw new DslException("'startTime' field is not set")
            }

            consumer.accept(new DummyEventModel(eventModelSpec.id, eventModelSpec.startTime))
        }
    }

    @Override
    List<String> getModelNames() {
        List.of(DummyEventModel.class.simpleName)
    }
}
