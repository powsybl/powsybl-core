/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.groovy

import com.google.auto.service.AutoService
import com.powsybl.commons.report.ReportNode
import com.powsybl.dsl.DslException
import com.powsybl.dynamicsimulation.OutputVariable

import java.util.function.Consumer
/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
@AutoService(OutputVariableGroovyExtension.class)
class DummyOutputVariableGroovyExtension implements OutputVariableGroovyExtension {

    static class DummyOutputVariableSpec {
        String id
        String variable

        void id(String id) {
            this.id = id
        }

        void variable(String variable) {
            this.variable = variable
        }
    }

    private static void checkOutputVariableSpec(DummyOutputVariableSpec variableSpec) {
        if (!variableSpec.id) {
            throw new DslException("'id' field is not set")
        }
        if (!variableSpec.variable) {
            throw new DslException("'variable' field is not set")
        }
    }

    void load(Binding binding, Consumer<OutputVariable> consumer, ReportNode reportNode) {

        binding.dummyCurve = { Closure<Void> closure ->
            def cloned = closure.clone()
            DummyOutputVariableSpec variableSpec = new DummyOutputVariableSpec()
            cloned.delegate = variableSpec
            cloned()
            checkOutputVariableSpec(variableSpec)
            consumer.accept(new DummyOutputVariable(variableSpec.id, variableSpec.variable, OutputVariable.OutputType.CURVE))
        }
        binding.dummyFsv = { Closure<Void> closure ->
            def cloned = closure.clone()
            DummyOutputVariableSpec variableSpec = new DummyOutputVariableSpec()
            cloned.delegate = variableSpec
            cloned()
            checkOutputVariableSpec(variableSpec)
            consumer.accept(new DummyOutputVariable(variableSpec.id, variableSpec.variable, OutputVariable.OutputType.FINAL_STATE))
        }
    }
}
