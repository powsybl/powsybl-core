/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.Assert.*;

/**
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
public class SlackTerminalTest {

    static Network createBusBreakerNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));
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

        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.FR)
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

    private static Terminal getFirstTerminal(Network network, String busBusBreaker) {
        return network.getBusBreakerView().getBus(busBusBreaker).getConnectedTerminals().iterator().next();
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

        // extends voltage level
        String busBusBreakerId = "B";
        adder.withTerminal(getFirstTerminal(network, busBusBreakerId)).add();

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
        VoltageLevel vl = network.getVoltageLevel("VLHV1");
        vl.newExtension(SlackTerminalAdder.class)
            .withTerminal(getFirstTerminal(network, "NLOAD"))
            .add();
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
    public void variantsResetTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";

        // Creates 2 variants before creating the extension
        Network network = EurostagTutorialExample1Factory.create();
        VariantManager variantManager = network.getVariantManager();
        List<String> targetVariantIds = new ArrayList<>();
        targetVariantIds.add(variant1);
        targetVariantIds.add(variant2);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant2);

        // Creates the extension
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
        vlgen.newExtension(SlackTerminalAdder.class)
                .withTerminal(getFirstTerminal(network, "NGEN"))
                .add();
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

        // Testing the cleanable property of the slackTerminal
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertFalse(stGen.setTerminal(null).isCleanable());

        variantManager.setWorkingVariant(variant2);
        assertFalse(stGen.setTerminal(null).isCleanable());

        variantManager.setWorkingVariant(variant1);
        assertTrue(stGen.setTerminal(null).isCleanable());
        assertFalse(stGen.setTerminal(tGen).isCleanable());

        // Testing the cleanIfCleanable boolean
        stGen.setTerminal(null, false);
        assertNotNull(vlgen.getExtension(SlackTerminal.class));
        stGen.setTerminal(null, true);
        assertNull(vlgen.getExtension(SlackTerminal.class));

        // Creates an extension on another voltageLevel
        VoltageLevel vlhv1 = network.getVoltageLevel("VLHV1");
        vlhv1.newExtension(SlackTerminalAdder.class)
                .withTerminal(getFirstTerminal(network, "NLOAD"))
                .add();
        SlackTerminal stLoad = vlhv1.getExtension(SlackTerminal.class);
        assertNotNull(stLoad);
        assertEquals("NLOAD", stLoad.getTerminal().getBusBreakerView().getBus().getId());
        assertFalse(stLoad.isCleanable());

         // Reset the SlackTerminal of VLGEN voltageLevel to its previous value
        SlackTerminal.reset(vlgen, tGen);
        stGen = vlgen.getExtension(SlackTerminal.class);
        assertNotNull(stGen);
        assertEquals(tGen, stGen.getTerminal());
        variantManager.setWorkingVariant(variant2);
        assertNull(stGen.getTerminal());

        // Removes all SlackTerminals from network
        SlackTerminal.removeAllFrom(network);
        assertNull(vlgen.getExtension(SlackTerminal.class));
        assertNull(vlhv1.getExtension(SlackTerminal.class));

    }
}
