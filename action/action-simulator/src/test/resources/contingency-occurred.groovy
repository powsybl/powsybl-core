/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

contingency('contingency1') {
    equipments 'NHV1_NHV2_1'
}

rule('rule1') {
    when !contingencyOccurred('contingency1')
    apply 'action1'
}

rule('rule2') {
    when contingencyOccurred('contingency1') && contingencyOccurred()
    apply 'action2'
}

action('action1') {
    tasks {
    }
}

action('action2') {
    tasks {
    }
}
