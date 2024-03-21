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
import com.powsybl.dynamicsimulation.DynamicModel

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
@AutoService(DynamicModelGroovyExtension.class)
class DummyDynamicModelGroovyExtension implements DynamicModelGroovyExtension {

    static class DummyDynamicModelSpec {
        String id
        String parameterSetId
        
        void id(String id) {
            this.id = id
        }

        void parameterSetId(String parameterSetId) {
            this.parameterSetId = parameterSetId
        }
    }

    void load(Binding binding, Consumer<DynamicModel> consumer, ReportNode reportNode) {
        binding.dummyDynamicModel = { Closure<Void> closure ->
            def cloned = closure.clone()

            DummyDynamicModelSpec dynamicModelSpec = new DummyDynamicModelSpec()

            cloned.delegate = dynamicModelSpec
            cloned()
            if (!dynamicModelSpec.id) {
                throw new DslException("'id' field is not set")
            }
            if (!dynamicModelSpec.parameterSetId) {
                throw new DslException("'parameterSetId' field is not set")
            }

            consumer.accept(new DummyDynamicModel(dynamicModelSpec.id, dynamicModelSpec.parameterSetId))
        }
    }

    @Override
    List<String> getModelNames() {
        List.of(DummyDynamicModel.class.simpleName)
    }
}
