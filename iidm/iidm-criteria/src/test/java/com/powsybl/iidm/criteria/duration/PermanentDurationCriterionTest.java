/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class PermanentDurationCriterionTest {
    @Test
    void getTypeTest() {
        PermanentDurationCriterion criterion = new PermanentDurationCriterion();
        assertEquals(LimitDurationCriterion.LimitDurationType.PERMANENT, criterion.getType());
    }
}
