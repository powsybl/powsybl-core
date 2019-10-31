/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

action('full modification') {
    tasks {
        generatorModification('GEN') {
            minP 20.0
            maxP 60.0
            targetP 50.0
            targetV 10.0
            targetQ 25.0
            voltageRegulatorOn false
        }
    }
}

action('pDelta within boundaries') {
    tasks {
        generatorModification('GEN') {
            pDelta(-1.0)
        }
    }
}

action('pDelta lower boundary overflow') {
    tasks {
        generatorModification('GEN') {
            pDelta(-30000.0)
        }
    }
}

action('pDelta upper boundary overflow') {
    tasks {
        generatorModification('GEN') {
            pDelta 30000.0
        }
    }
}

action('targetP lower boundary overflow') {
    tasks {
        generatorModification('GEN') {
            targetP(-50000.0)
        }
    }
}

action('targetP upper boundary overflow') {
    tasks {
        generatorModification('GEN') {
            targetP 50000.0
        }
    }
}
