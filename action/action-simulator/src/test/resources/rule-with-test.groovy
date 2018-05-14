/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
contingency('contingency1') {
    equipments 'NHV1_NHV2_1'
}

rule('rule1') {
    when !contingencyOccurred('contingency1')
    life 3
    apply 'action3'
}

rule('rule2') {
    when !contingencyOccurred('contingency1')
    test 'action1','action2'
}

action('action1') {
    tasks {
        script {
            println "hi action1"
            load('LOAD').p0 += 1
        }
    }
}

action('action2') {
    tasks {
        script {
            println "hi action2"
            load('LOAD').p0 += 1
        }
    }
}

action('action3') {
    tasks {
        script {
            println "hi action3"
            generator('GEN').targetP += 1
        }
    }
}
