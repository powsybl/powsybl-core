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

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class ActionBuilderTest {

    @Test
    void shuntCompensatorPositionActionBuilderTest() {
        ShuntCompensatorPositionActionBuilder shuntCompensatorPositionActionBuilder1 = new ShuntCompensatorPositionActionBuilder()
            .withId("actionId").withShuntCompensatorId("shuntCompensatorId");
        String message1 = assertThrows(IllegalArgumentException.class, shuntCompensatorPositionActionBuilder1::build)
            .getMessage();
        Assertions.assertEquals("sectionCount is undefined", message1);

        ShuntCompensatorPositionActionBuilder shuntCompensatorPositionActionBuilder2 = new ShuntCompensatorPositionActionBuilder()
            .withId("actionId").withShuntCompensatorId("shuntCompensatorId").withSectionCount(-1);
        String message2 = assertThrows(IllegalArgumentException.class, shuntCompensatorPositionActionBuilder2::build)
            .getMessage();
        Assertions.assertEquals("sectionCount should be positive for a shunt compensator", message2);
    }

    @Test
    void pctLoadActionBuilderShouldCheckPctNotAbove100() {
        PercentChangeLoadActionBuilder actionBuilder = new PercentChangeLoadActionBuilder()
                .withId("actionId")
                .withLoadId("myLoad")
                .withQStrategy(PercentChangeLoadAction.QModificationStrategy.CONSTANT_Q)
                .withPercentP0Change(-101);
        assertThrows(IllegalArgumentException.class, actionBuilder::build);
    }
}
