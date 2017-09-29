/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkRemoveTest {

    private Network network;

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
    }

    @After
    public void tearDown() {
        network = null;
    }

    @Test
    public void removeLineTest() {
        String id = "NHV1_NHV2_1";
        Line l = network.getLine(id);
        assertTrue(network.getLineCount() == 2);
        l.remove();
        assertTrue(network.getLineCount() == 1);
        assertTrue(network.getLine(id) == null);
    }

    @Test
    public void moveLoadToNewBus() {
        VoltageLevel vl = network.getVoltageLevel("VLLOAD");
        vl.getBusBreakerView().newBus()
                .setId("NEW_BUS")
                .add();
        Load l = network.getLoad("LOAD");
        assertTrue(l.getTerminal().getBusBreakerView().getBus().getId().equals("NLOAD"));
        assertTrue(l.getTerminal().isConnected());
        l.getTerminal().getBusBreakerView().setConnectableBus("NEW_BUS");
        assertTrue(l.getTerminal().getBusBreakerView().getBus().getId().equals("NEW_BUS"));
        assertTrue(l.getTerminal().isConnected());
    }

    private void extend(Network n) {
        VoltageLevel vl = network.getVoltageLevel("VLLOAD");
        vl.getBusBreakerView().newBus()
                .setId("NEW_BUS")
                .add();
        vl.getBusBreakerView().newSwitch()
                .setId("COUPL")
                .setBus1("NLOAD")
                .setBus2("NEW_BUS")
                .setOpen(false)
                .add();
    }

    @Test
    public void removeAll() {
        extend(network);
        VoltageLevel vl = network.getVoltageLevel("VLLOAD");
        vl.getBusBreakerView().removeAllSwitches();
        network.getLoad("LOAD").remove();
        network.getTwoWindingsTransformer("NHV2_NLOAD").remove();
        vl.getBusBreakerView().removeAllBuses();
        assertTrue(Iterables.size(vl.getBusBreakerView().getBuses()) == 0);
        assertTrue(Iterables.size(vl.getBusBreakerView().getSwitches()) == 0);
    }

    @Test
    public void removeBusFailure() {
        VoltageLevel vl = network.getVoltageLevel("VLLOAD");
        try {
            vl.getBusBreakerView().removeBus("NLOAD");
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void removeBus() {
        network.getLoad("LOAD").remove();
        network.getTwoWindingsTransformer("NHV2_NLOAD").remove();
        VoltageLevel vl = network.getVoltageLevel("VLLOAD");
        vl.getBusBreakerView().removeBus("NLOAD");
        assertTrue(Iterables.size(vl.getBusBreakerView().getBuses()) == 0);
    }

    @Test
    public void removeBusFailureBecauseOfSwitch() {
        extend(network);
        network.getLoad("LOAD").remove();
        network.getTwoWindingsTransformer("NHV2_NLOAD").remove();
        VoltageLevel vl = network.getVoltageLevel("VLLOAD");
        try {
            vl.getBusBreakerView().removeBus("NLOAD");
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void removeSwitchFailure() {
        extend(network);
        VoltageLevel vl = network.getVoltageLevel("VLLOAD");
        try {
            vl.getBusBreakerView().removeSwitch("XXX");
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void removeSwitch() {
        extend(network);
        VoltageLevel vl = network.getVoltageLevel("VLLOAD");
        vl.getBusBreakerView().removeSwitch("COUPL");
    }
}
