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
