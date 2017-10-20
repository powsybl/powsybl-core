/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
import static com.powsybl.action.util.Scalable.*

action('actionScale') {
    tasks {
        script {
            variationValue = 15000
            variation = stack('GEN', 'GEN2', 'GEN3')
            variation.reset(network)
            variation.scale(network, variationValue)
        }
    }
}

action('testCompatible') {
    tasks {
        script {
            variationValue = 15000
            variation = stack(gen('GEN'), gen('GEN2'), gen('GEN3'))
            variation.reset(network)
            variation.scale(network, variationValue)
        }
    }
}

action('testProportional') {
    tasks {
        script {
            variationValue = 15000
            gens = scalables('GEN', 'GEN2', 'GEN3')
            variation = proportional([50.0f,20.0f,30.0f], gens)
            variation.reset(network)
            variation.scale(network, variationValue)
        }
    }
}