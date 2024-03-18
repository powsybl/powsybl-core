/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.action.PhaseTapChangerRegulationAction;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class PhaseTapChangerRegulationActionTest {

    @Test
    void test() {
        String message = assertThrows(IllegalArgumentException.class, () -> new PhaseTapChangerRegulationAction("id17", "transformerId5", ThreeSides.ONE,
                false, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, 10.0)).getMessage();
        assertEquals("PhaseTapChangerRegulationAction can not have a regulation mode " +
                "if it is not regulating", message);
        PhaseTapChangerRegulationAction action = PhaseTapChangerRegulationAction.deactivateRegulation("id17", "transformerId5",
                ThreeSides.ONE);
        assertFalse(action.isRegulating());
        assertTrue(action.getRegulationMode().isEmpty());
    }
}
