/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractNetworkRemoveTest {

    private static final String NEW_BUS = "NEW_BUS";
    private static final String NLOAD = "NLOAD";
    private static final String NHV2_NLOAD = "NHV2_NLOAD";
    private static final String VLLOAD = "VLLOAD";
    private Network network;

    @BeforeEach
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
    }

    @AfterEach
    public void tearDown() {
        network = null;
    }

    @Test
    public void removeLineTest() {
        String id = "NHV1_NHV2_1";
        Line l = network.getLine(id);
        assertEquals(2, network.getLineCount());
        l.remove();
        assertEquals(1, network.getLineCount());
        assertNull(network.getLine(id));
    }

    @Test
    public void moveLoadToNewBus() {
        VoltageLevel vl = network.getVoltageLevel(VLLOAD);
        vl.getBusBreakerView().newBus()
                .setId(NEW_BUS)
                .add();
        Load l = network.getLoad("LOAD");
        assertEquals(NLOAD, l.getTerminal().getBusBreakerView().getBus().getId());
        assertTrue(l.getTerminal().isConnected());
        l.getTerminal().getBusBreakerView().setConnectableBus(NEW_BUS);
        assertEquals(NEW_BUS, l.getTerminal().getBusBreakerView().getBus().getId());
        assertTrue(l.getTerminal().isConnected());
    }

    private void extend(Network n) {
        VoltageLevel vl = n.getVoltageLevel(VLLOAD);
        vl.getBusBreakerView().newBus()
                .setId(NEW_BUS)
                .add();
        vl.getBusBreakerView().newSwitch()
                .setId("COUPL")
                .setBus1(NLOAD)
                .setBus2(NEW_BUS)
                .setOpen(false)
                .add();
    }

    @Test
    public void removeAll() {
        extend(network);
        VoltageLevel vl = network.getVoltageLevel(VLLOAD);
        vl.getBusBreakerView().removeAllSwitches();
        network.getLoad("LOAD").remove();
        network.getTwoWindingsTransformer(NHV2_NLOAD).remove();
        vl.getBusBreakerView().removeAllBuses();
        assertEquals(0, Iterables.size(vl.getBusBreakerView().getBuses()));
        assertEquals(0, Iterables.size(vl.getBusBreakerView().getSwitches()));
    }

    @Test
    public void removeBusFailure() {
        VoltageLevel vl = network.getVoltageLevel(VLLOAD);
        try {
            vl.getBusBreakerView().removeBus(NLOAD);
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    @Test
    public void removeBus() {
        network.getLoad("LOAD").remove();
        network.getTwoWindingsTransformer(NHV2_NLOAD).remove();
        VoltageLevel vl = network.getVoltageLevel(VLLOAD);
        vl.getBusBreakerView().removeBus(NLOAD);
        assertEquals(0, Iterables.size(vl.getBusBreakerView().getBuses()));
    }

    @Test
    public void removeBusFailureBecauseOfSwitch() {
        extend(network);
        network.getLoad("LOAD").remove();
        network.getTwoWindingsTransformer(NHV2_NLOAD).remove();
        VoltageLevel vl = network.getVoltageLevel(VLLOAD);
        try {
            vl.getBusBreakerView().removeBus(NLOAD);
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    @Test
    public void removeSwitchFailure() {
        extend(network);
        VoltageLevel vl = network.getVoltageLevel(VLLOAD);
        try {
            vl.getBusBreakerView().removeSwitch("XXX");
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    @Test
    public void removeSwitch() {
        extend(network);
        VoltageLevel vl = network.getVoltageLevel(VLLOAD);
        vl.getBusBreakerView().removeSwitch("COUPL");
    }
}
