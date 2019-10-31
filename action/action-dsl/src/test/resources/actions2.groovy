/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
rule ('Memoriser_Prise_Init_TD_Boutre') {
    when !contingencyOccurred()
    life 1
    apply 'someAction'
}

action ('someAction') {
    description 'asdf'
    tasks {
        script {
            transformer('NGEN_NHV1').r = 3
            closeSwitch('switchId')
        }
    }
}

action ('missingMethod') {
    description 'asdf'
    tasks {
        script {
            unknownMethod("foo")
        }
    }
}

action ('anotherAction') {
    tasks {
        closeSwitch('switchId')
    }
}

action ('fixedTap') {
    tasks {
        phaseShifterFixedTap('NGEN_NHV1', 1)
    }
}

action ('deltaTap0') {
    tasks {
        phaseShifterDeltaTap('NGEN_NHV1', 0)
    }
}

action ('deltaTap1') {
    tasks {
        phaseShifterDeltaTap('NGEN_NHV1', 1)
    }
}

action ('deltaTap2') {
    tasks {
        phaseShifterDeltaTap('NGEN_NHV1', 2)
    }
}

action ('deltaTap3') {
    tasks {
        phaseShifterDeltaTap('NGEN_NHV1', 3)
    }
}

action ('deltaTap10') {
    tasks {
        phaseShifterDeltaTap('NGEN_NHV1', 10)
    }
}

action ('deltaTapMinus1') {
    tasks {
        phaseShifterDeltaTap('NGEN_NHV1', -1)
    }
}

action ('deltaTapMinus2') {
    tasks {
        phaseShifterDeltaTap('NGEN_NHV1', -2)
    }
}

action ('deltaTapMinus10') {
    tasks {
        phaseShifterDeltaTap('NGEN_NHV1', -10)
    }
}
