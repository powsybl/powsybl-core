/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

rule('golden') {
    description 'a golden rule'
    when true
    life 1
    apply 'respect'
}

action('respect') {
    tasks {
        script {
            load('LOAD').p0 += 1
        }
    }
}