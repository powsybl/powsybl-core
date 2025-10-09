/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public abstract class AbstractVoltagePerReactivePowerControlTest {

    private StaticVarCompensator svc;

    @BeforeEach
    public void setUp() {
        Network network = SvcTestCaseFactory.create();
        svc = network.getStaticVarCompensator("SVC2");
    }

    @Test
    public void test() {
        VoltagePerReactivePowerControl control = svc.newExtension(VoltagePerReactivePowerControlAdder.class)
                .withSlope(0.2)
                .add();
        assertEquals(0.2, control.getSlope(), 0.0);
        control.setSlope(0.5);
        assertEquals(0.5, control.getSlope(), 0.0);
        assertEquals("SVC2", control.getExtendable().getId());
    }

    @Test
    public void testUndefined() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> svc.newExtension(VoltagePerReactivePowerControlAdder.class)
                .withSlope(Double.NaN)
                .add());
        assertTrue(e.getMessage().contains("Undefined value for slope"));
    }
}
