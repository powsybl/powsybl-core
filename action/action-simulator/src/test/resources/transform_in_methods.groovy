/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
def isOverloaded(lineIds) {
    println( 'in overloaded')
    lineIds.collect({lineId -> line(lineId).terminal1.i > line(lineId).currentLimits1.permanentLimit})
            .inject({a, b -> a && b})
}

rule('rule1') {
    when isOverloaded(['NHV1_NHV2_2'])
    life 2
    apply 'action1'

}

action('action1') {
    description 'hello'
    tasks {
        script {
            load('LOAD').p0 += 1
        }
    }
}