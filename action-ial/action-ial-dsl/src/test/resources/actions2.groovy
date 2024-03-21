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


action ('backwardCompatibility') {
    description 'backward compatibility'
    tasks {
        phaseShifterFixedTap('NGEN_NHV1', 2)
    }
}

action ('someAction') {
    description 'asdf'
    modifications {
        script {
            transformer('NGEN_NHV1').r = 3
            closeSwitch('switchId')
        }
    }
}

action ('missingMethod') {
    description 'asdf'
    modifications {
        script {
            unknownMethod("foo")
        }
    }
}

action ('anotherAction') {
    modifications {
        closeSwitch('switchId')
    }
}

action ('fixedTap') {
    modifications {
        phaseShifterFixedTap('NGEN_NHV1', 1)
    }
}

action ('deltaTap0') {
    modifications {
        phaseShifterTap('NGEN_NHV1', 0)
    }
}

action ('deltaTap1') {
    modifications {
        phaseShifterTap('NGEN_NHV1', 1)
    }
}

action ('deltaTap2') {
    modifications {
        phaseShifterTap('NGEN_NHV1', 2)
    }
}

action ('deltaTap3') {
    modifications {
        phaseShifterTap('NGEN_NHV1', 3)
    }
}

action ('deltaTap10') {
    modifications {
        phaseShifterTap('NGEN_NHV1', 10)
    }
}

action ('deltaTapMinus1') {
    modifications {
        phaseShifterTap('NGEN_NHV1', -1)
    }
}

action ('deltaTapMinus2') {
    modifications {
        phaseShifterTap('NGEN_NHV1', -2)
    }
}

action ('deltaTapMinus10') {
    modifications {
        phaseShifterTap('NGEN_NHV1', -10)
    }
}

action ('InvalidTransformerId') {
    modifications {
        phaseShifterTap('NHV1_NHV2_1', -10)
    }
}

action ('TransformerWithoutPhaseShifter') {
    modifications {
        phaseShifterTap('NGEN_NHV1', -10)
    }
}
