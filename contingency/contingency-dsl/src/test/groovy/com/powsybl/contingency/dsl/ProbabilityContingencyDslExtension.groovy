/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.powsybl.contingency.dsl

import com.google.auto.service.AutoService
import com.powsybl.commons.extensions.Extension
import com.powsybl.contingency.Contingency
import com.powsybl.dsl.ExtendableDslExtension

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
@AutoService(ContingencyDslExtension.class)
class ProbabilityContingencyDslExtension implements ContingencyDslExtension {
    @Override
    Class<Contingency> getExtendableClass() {
        return Contingency.class
    }

    @Override
    void addToSpec(MetaClass extSpecMetaClass, List<Extension<Contingency>> contingencyExtensions, Binding binding) {
        extSpecMetaClass.probability = { Closure<Void> closure ->
            def cloned = closure.clone()
            ProbabilitySpec spec = new ProbabilitySpec()
            cloned.delegate = spec
            cloned()

            contingencyExtensions.add(new ProbabilityContingencyExtension(spec.base, spec.tsName))
        }
    }

    static class ProbabilitySpec {
        Double base
        String tsName

        void base(Double base) {
            this.base = base
        }

        void tsName(String tsName) {
            this.tsName = tsName
        }
    }
}
