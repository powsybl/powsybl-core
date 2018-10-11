/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

contingency('HV_line_1') {
    equipments 'NHV1_NHV2_1'
}

contingency('HV_line_2') {
    equipments 'NHV1_NHV2_2'
}


rule('apply_shedding_for_line_1') {
    description 'Test load sheddings when line 1 is overloaded'
    life 8
    when isOverloaded(['NHV1_NHV2_1'])
    apply 'load_shed_100'
}

rule('apply_shedding_for_line_2') {
    description 'Test load sheddings when line 2 is overloaded'
    life 8
    when isOverloaded(['NHV1_NHV2_2'])
    apply 'load_shed_100'
}

action('load_shed_100') {
    description 'load shedding 100 MW'
    tasks {
        script {
            load('LOAD').p0 -= 100
        }
    }
}
