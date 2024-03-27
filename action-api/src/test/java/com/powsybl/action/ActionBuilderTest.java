/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class ActionBuilderTest {

    @Test
    void shuntCompensatorPositionActionBuilderTest() {
        ShuntCompensatorPositionActionBuilder shuntCompensatorPositionActionBuilder1 = new ShuntCompensatorPositionActionBuilder()
            .withId("actionId").withShuntCompensatorId("shuntCompensatorId");
        String message1 = Assertions.assertThrows(IllegalArgumentException.class, shuntCompensatorPositionActionBuilder1::build)
            .getMessage();
        Assertions.assertEquals("sectionCount is undefined", message1);

        ShuntCompensatorPositionActionBuilder shuntCompensatorPositionActionBuilder2 = new ShuntCompensatorPositionActionBuilder()
            .withId("actionId").withShuntCompensatorId("shuntCompensatorId").withSectionCount(-1);
        String message2 = Assertions.assertThrows(IllegalArgumentException.class, shuntCompensatorPositionActionBuilder2::build)
            .getMessage();
        Assertions.assertEquals("sectionCount should be positive for a shunt compensator", message2);
    }
}
