/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
contingency('contingency1') {
    equipments 'NHV1_NHV2_1'
}

rule('rule') {
    when contingencyOccurred('contingency1')
    apply 'action'
}

rule('rule2') {
    when contingencyOccurred('contingency2')
    apply 'action2'
}
