/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractSlackTerminalTest {

    static Network createBusBreakerNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
            .setId("S")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s.newVoltageLevel()
            .setId("VL")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vl.getBusBreakerView().newBus()
            .setId("B")
            .add();
        vl.newLoad()
            .setId("L")
            .setBus("B")
            .setConnectableBus("B")
            .setP0(100)
            .setQ0(50)
            .add();

        VoltageLevel vl1 = s.newVoltageLevel()
            .setId("VL1")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vl1.getBusBreakerView().newBus()
            .setId("B1")
            .add();
        vl1.newGenerator()
            .setId("GE")
            .setBus("B1")
            .setConnectableBus("B1")
            .setTargetP(100)
            .setMinP(0)
            .setMaxP(110)
            .setTargetV(380)
            .setVoltageRegulatorOn(true)
            .add();

        network.newLine()
            .setId("LI")
            .setR(0.05)
            .setX(1.)
            .setG1(0.)
            .setG2(0.)
            .setB1(0.)
            .setB2(0.)
            .setVoltageLevel1("VL")
            .setVoltageLevel2("VL1")
            .setBus1("B")
            .setBus2("B1")
            .add();

        return network;
    }

    @Test
    public void test() {
        Network network = createBusBreakerNetwork();
        VoltageLevel vl0 = network.getVoltageLevel("VL");
        SlackTerminalAdder adder = vl0.newExtension(SlackTerminalAdder.class);

        // error test
        try {
            adder.add();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Terminal needs to be set to create a SlackTerminal extension", e.getMessage());
        }

        // Defining the slackTerminal on the first Terminal
        String busBusBreakerId = "B";
        Terminal terminal = network.getBusBreakerView().getBus(busBusBreakerId).getConnectedTerminals().iterator().next();
        adder.withTerminal(terminal).add();

        SlackTerminal slackTerminal;
        for (VoltageLevel vl : network.getVoltageLevels()) {
            slackTerminal = vl.getExtension(SlackTerminal.class);
            if (slackTerminal != null) {
                assertEquals(busBusBreakerId, slackTerminal.getTerminal().getBusBreakerView().getBus().getId());
                assertEquals("VL_0", slackTerminal.getTerminal().getBusView().getBus().getId());
            }
        }
    }

    @Test
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        // Creates the extension
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vl = network.getVoltageLevel("VLLOAD");
        SlackTerminal.attach(network.getBusBreakerView().getBus("NLOAD"));
        SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
        assertNotNull(slackTerminal);
        final Terminal t0 = slackTerminal.getTerminal();

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertEquals(t0, slackTerminal.getTerminal());

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = new ArrayList<>();
        targetVariantIds.add(variant1);
        targetVariantIds.add(variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertEquals(t0, slackTerminal.getTerminal());
        variantManager.setWorkingVariant(variant3);
        assertEquals(t0, slackTerminal.getTerminal());

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            slackTerminal.getTerminal();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }

    @Test
    public void vlErrorTest() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vl = network.getVoltageLevel("VLHV1");

        // Adding a terminal in the wrong voltage level
        Terminal wrongTerminal = network.getBusBreakerView().getBus("NLOAD").getConnectedTerminals().iterator().next();
        SlackTerminalAdder slackTerminalAdder = vl.newExtension(SlackTerminalAdder.class).withTerminal(wrongTerminal);
        try {
            slackTerminalAdder.add();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Terminal given is not in the right VoltageLevel (VLLOAD instead of VLHV1)", e.getMessage());
        }

        // First adding a terminal in the right voltage level...
        Terminal terminal = network.getBusBreakerView().getBus("NHV1").getConnectedTerminals().iterator().next();
        SlackTerminal slackTerminal = slackTerminalAdder.withTerminal(terminal).add();
        assertNotNull(slackTerminal);

        // ... then setting a terminal in the wrong voltage level
        Terminal wrongTerminal2 = network.getBusBreakerView().getBus("NHV2").getConnectedTerminals().iterator().next();
        try {
            slackTerminal.setTerminal(wrongTerminal2, true);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Terminal given is not in the right VoltageLevel (VLHV2 instead of VLHV1)", e.getMessage());
        }
    }

    @Test
    public void variantsResetTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";

        // Creates 2 variants before creating the extension
        Network network = EurostagTutorialExample1Factory.create();
        VariantManager variantManager = network.getVariantManager();
        List<String> targetVariantIds = Arrays.asList(variant1, variant2);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant2);

        // Creates the extension
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
        SlackTerminal.attach(network.getBusBreakerView().getBus("NGEN"));
        SlackTerminal stGen = vlgen.getExtension(SlackTerminal.class);
        assertNotNull(stGen);
        final Terminal tGen = stGen.getTerminal();

        // Testing that only current variant was set
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertNull(stGen.getTerminal());
        stGen.setTerminal(tGen);

        variantManager.setWorkingVariant(variant1);
        assertNull(stGen.getTerminal());
        stGen.setTerminal(tGen);

        // Testing the empty property of the slackTerminal
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertFalse(stGen.setTerminal(null).isEmpty());

        variantManager.setWorkingVariant(variant2);
        assertFalse(stGen.setTerminal(null).isEmpty());

        variantManager.setWorkingVariant(variant1);
        assertTrue(stGen.setTerminal(null).isEmpty());
        assertFalse(stGen.setTerminal(tGen).isEmpty());

        // Testing the cleanIfEmpty boolean
        stGen.setTerminal(null, false);
        assertNotNull(vlgen.getExtension(SlackTerminal.class));
        stGen.setTerminal(null, true);
        assertNull(vlgen.getExtension(SlackTerminal.class));

        // Creates an extension on another voltageLevel
        VoltageLevel vlhv1 = network.getVoltageLevel("VLLOAD");
        SlackTerminal.attach(network.getBusBreakerView().getBus("NLOAD"));
        SlackTerminal stLoad = vlhv1.getExtension(SlackTerminal.class);
        assertNotNull(stLoad);
        assertEquals("NLOAD", stLoad.getTerminal().getBusBreakerView().getBus().getId());
        assertFalse(stLoad.isEmpty());

         // Reset the SlackTerminal of VLGEN voltageLevel to its previous value
        SlackTerminal.reset(vlgen, tGen);
        stGen = vlgen.getExtension(SlackTerminal.class);
        assertNotNull(stGen);
        assertEquals(tGen, stGen.getTerminal());
        variantManager.setWorkingVariant(variant2);
        assertNull(stGen.getTerminal());

        // Removes all SlackTerminals from network
        variantManager.setWorkingVariant(variant1);
        SlackTerminal.reset(network);
        assertNull(vlgen.getExtension(SlackTerminal.class));
        assertNull(vlhv1.getExtension(SlackTerminal.class));

    }

    @Test
    public void testWithSubnetwork() {
        Network network1 = createBusBreakerNetwork();
        SlackTerminal.attach(network1.getBusBreakerView().getBus("B"));
        Network network2 = EurostagTutorialExample1Factory.create();
        SlackTerminal.attach(network2.getBusBreakerView().getBus("NHV1"));

        Network merged = Network.merge(network1, network2);
        network1 = merged.getSubnetwork("test");
        network2 = merged.getSubnetwork("sim1");

        // still there after merge
        assertNotNull(merged.getVoltageLevel("VL").getExtension(SlackTerminal.class));
        assertNotNull(merged.getVoltageLevel("VLHV1").getExtension(SlackTerminal.class));

        // we can reset everything (including subnetworks)
        SlackTerminal.reset(merged);
        assertNull(merged.getVoltageLevel("VL").getExtension(SlackTerminal.class));
        assertNull(merged.getVoltageLevel("VLHV1").getExtension(SlackTerminal.class));

        // we can reset only a subnetwork
        SlackTerminal.attach(network1.getBusBreakerView().getBus("B"));
        SlackTerminal.attach(network2.getBusBreakerView().getBus("NHV1"));
        SlackTerminal.reset(network1);
        assertNull(merged.getVoltageLevel("VL").getExtension(SlackTerminal.class)); // reset
        assertNotNull(merged.getVoltageLevel("VLHV1").getExtension(SlackTerminal.class)); // untouched
    }
}
