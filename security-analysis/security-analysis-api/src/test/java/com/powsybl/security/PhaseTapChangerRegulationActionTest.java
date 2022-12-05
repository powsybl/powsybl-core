/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.security.action.PhaseTapChangerRegulationAction;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class PhaseTapChangerRegulationActionTest {

    @Test
    public void test() {
        String message = Assert.assertThrows(IllegalArgumentException.class, () -> new PhaseTapChangerRegulationAction("id17", "transformerId5", ThreeWindingsTransformer.Side.ONE,
                false, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)).getMessage();
        Assert.assertEquals("PhaseTapChangerRegulationAction can not have a regulation mode " +
                "if it is not regulating", message);
        PhaseTapChangerRegulationAction action = PhaseTapChangerRegulationAction.deactivateRegulation("id17", "transformerId5",
                ThreeWindingsTransformer.Side.ONE);
        Assert.assertFalse(action.isRegulating());
        Assert.assertTrue(action.getRegulationMode().isEmpty());
    }
}
