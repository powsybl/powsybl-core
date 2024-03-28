/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class NoModificationsImplTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        LimitsComputer<Identifiable<?>, LoadingLimits> computer = LimitsComputer.NO_MODIFICATIONS;

        Optional<LimitsContainer<LoadingLimits>> optLimits = computer.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE, false);
        assertTrue(optLimits.isPresent());
        assertEquals(500, optLimits.get().getLimits().getPermanentLimit(), 0.01);
        assertEquals(500, optLimits.get().getOriginalLimits().getPermanentLimit(), 0.01);
        assertFalse(optLimits.get().hasChanged());

        optLimits = computer.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO, true);
        assertTrue(optLimits.isPresent());
        checkLimitsOnSide2(optLimits.get().getLimits());
        checkLimitsOnSide2(optLimits.get().getOriginalLimits());
        assertFalse(optLimits.get().hasChanged());
    }

    private void checkLimitsOnSide2(LoadingLimits limits) {
        assertEquals(1100, limits.getPermanentLimit(), 0.01);
        assertEquals(1200, limits.getTemporaryLimitValue(10 * 60), 0.01);
        assertEquals(1500, limits.getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, limits.getTemporaryLimitValue(0), 0.01);
    }
}
