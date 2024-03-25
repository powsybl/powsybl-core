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
import com.powsybl.dynamicsimulation.Curve

import java.util.function.Consumer
/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
@AutoService(CurveGroovyExtension.class)
class DummyCurveGroovyExtension implements CurveGroovyExtension {

    static class DummyCurveSpec {
        String id
        String variable

        void id(String id) {
            this.id = id
        }

        void variable(String variable) {
            this.variable = variable
        }
    }

    void load(Binding binding, Consumer<Curve> consumer, ReportNode reportNode) {
        binding.dummyCurve = { Closure<Void> closure ->
            def cloned = closure.clone()

            DummyCurveSpec curveSpec = new DummyCurveSpec()

            cloned.delegate = curveSpec
            cloned()
            if (!curveSpec.id) {
                throw new DslException("'id' field is not set")
            }
            if (!curveSpec.variable) {
                throw new DslException("'variable' field is not set")
            }

            consumer.accept(new DummyCurve(curveSpec.id, curveSpec.variable))
        }
    }
}
