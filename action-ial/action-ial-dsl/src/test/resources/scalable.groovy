/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
import static com.powsybl.iidm.modification.scalable.Scalable.*

action('actionScale') {
    modifications {
        script {
            variationValue = 15000
            variation = stack('GEN', 'GEN2', 'GEN3')
            variation.reset(network)
            variation.scale(network, variationValue)
        }
    }
}

action('testCompatible') {
    modifications {
        script {
            variationValue = 15000
            variation = stack(onGenerator('GEN'), onGenerator('GEN2'), onGenerator('GEN3'))
            variation.reset(network)
            variation.scale(network, variationValue)
        }
    }
}

action('testProportional') {
    modifications {
        script {
            variationValue = 15000
            gens = scalables('GEN', 'GEN2', 'GEN3')
            variation = proportional([50.0d,20.0d,30.0d], gens)
            variation.reset(network)
            variation.scale(network, variationValue)
        }
    }
}
