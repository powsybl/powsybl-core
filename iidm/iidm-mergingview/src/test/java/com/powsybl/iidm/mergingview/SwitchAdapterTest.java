/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class SwitchAdapterTest {
    private MergingView mergingView;

    @BeforeEach
    void setup() {
        mergingView = MergingView.create("SwitchAdapterTest", "iidm");
    }

    @Test
    void testSetterGetter() {
        final Network networkRef = NetworkTest1Factory.create();
        mergingView.merge(networkRef);

        final Switch swExpected = networkRef.getSwitch("load1Disconnector1");
        final Switch swActual = mergingView.getSwitch("load1Disconnector1");
        assertNotNull(swActual);
        assertTrue(swActual instanceof SwitchAdapter);
        assertSame(mergingView, swActual.getNetwork());

        assertEquals(swExpected.isOpen(), swActual.isOpen());
        swActual.setOpen(true);
        assertTrue(swActual.isOpen());

        assertEquals(swExpected.isRetained(), swActual.isRetained());
        swActual.setRetained(true);
        assertTrue(swActual.isRetained());

        assertEquals(swExpected.isFictitious(), swActual.isFictitious());
        swActual.setFictitious(true);
        assertTrue(swActual.isFictitious());

        assertEquals(swExpected.getKind(), swActual.getKind());

        assertTrue(swActual.getVoltageLevel() instanceof NodeBreakerVoltageLevelAdapter);
        assertEquals(swExpected.getVoltageLevel().getId(), swActual.getVoltageLevel().getId());
    }
}
