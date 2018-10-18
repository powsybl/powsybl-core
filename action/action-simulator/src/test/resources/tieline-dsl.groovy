import com.powsybl.iidm.network.TieLine

/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

rule('rule1') {
    when true
    life 1
    apply 'action1'
}

action('action1') {
    description 'hello'
    tasks {
        script {
            tieline = (TieLine) line("tie");
            TieLine tie = (TieLine) line("tie");
            tie.ucteXnodeCode
            load('LOAD').p0 = tieline.half1.getX()
            load('LOAD').q0 = tie.getR()
        }
    }
}