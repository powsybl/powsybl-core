/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
contingency('contingency1') {
    equipments 'NHV1_NHV2_1'
}

contingency('contingency2') {
    equipments 'GEN'
}

rule('rule1') {
    when generator('GEN').terminal.connected
    apply 'action'
}

rule('rule1') {
    when generator('GEN').terminal.connected
    apply 'action'
}

action('action') {
    tasks {

    }
}
