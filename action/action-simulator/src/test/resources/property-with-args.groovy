/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

rule('rule1') {
    when line('NHV1_NHV2_2').terminal1.i >= line('NHV1_NHV2_2').currentLimits1.permanentLimit && line('NHV1_NHV2_2').terminal1.i < line('NHV1_NHV2_2').currentLimits1.getTemporaryLimitValue(1200)
    apply 'action1'
}

action('action1') {
    tasks {
    }
}
