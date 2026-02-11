/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public abstract class AbstractRemoteReactivePowerControlTest {

    public Network createNetwork() {
        Network network = Network.create("test", "test");
        Bus b1 = createBus(network, "b1_s", "b1", 1);
        Bus b2 = createBus(network, "b2_s", "b2", 1);
        Bus b3 = createBus(network, "b3_s", "b3", 1);
        Bus b4 = createBus(network, "b4_s", "b4", 1);
        createGenerator(b1, "g1", 2, 1);
        createGenerator(b4, "g4", 1, 1);
        createLoad(b2, "d2", 1, 1);
        createLoad(b3, "d3", 4, 1);
        createLine(network, b1, b4, "l14", 0.1f);
        createLine(network, b1, b2, "l12", 0.1f);
        createLine(network, b2, b3, "l23", 0.1f);
        createLine(network, b3, b4, "l34", 0.1f);
        createLine(network, b1, b3, "l13", 0.1f);
        return network;
    }

    //nominal v = 1
    protected static Bus createBus(Network network, String substationId, String id, double nominalV) {
        Substation s = network.getSubstation(substationId);
        if (s == null) {
            s = network.newSubstation()
                       .setId(substationId)
                       .setCountry(Country.FR)
                       .add();
        }
        VoltageLevel vl = s.newVoltageLevel()
                           .setId(id + "_vl")
                           .setNominalV(nominalV)
                           .setTopologyKind(TopologyKind.BUS_BREAKER)
                           .add();
        return vl.getBusBreakerView().newBus()
                 .setId(id)
                 .add();
    }

    //v=1
    protected static Generator createGenerator(Bus b, String id, double p, double v) {
        Generator g = b.getVoltageLevel()
                       .newGenerator()
                       .setId(id)
                       .setBus(b.getId())
                       .setConnectableBus(b.getId())
                       .setEnergySource(EnergySource.OTHER)
                       .setMinP(0)
                       .setMaxP(p)
                       .setTargetP(p)
                       .setTargetV(v)
                       .newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withTargetValue(v).add()
                       .add();
        g.getTerminal().setP(-p).setQ(0);
        return g;
    }

    protected static Load createLoad(Bus b, String id, double p, double q) {
        Load l = b.getVoltageLevel().newLoad()
                  .setId(id)
                  .setBus(b.getId())
                  .setConnectableBus(b.getId())
                  .setP0(p)
                  .setQ0(q)
                  .add();
        l.getTerminal().setP(p).setQ(q);
        return l;
    }

    protected static Line createLine(Network network, Bus b1, Bus b2, String id, double x) {
        return network.newLine()
                      .setId(id)
                      .setVoltageLevel1(b1.getVoltageLevel().getId())
                      .setBus1(b1.getId())
                      .setConnectableBus1(b1.getId())
                      .setVoltageLevel2(b2.getVoltageLevel().getId())
                      .setBus2(b2.getId())
                      .setConnectableBus2(b2.getId())
                      .setR(0)
                      .setX(x)
                      .setG1(0)
                      .setG2(0)
                      .setB1(0)
                      .setB2(0)
                      .add();
    }

    @Test
    public void test() {

        Network network = createNetwork();
        Generator g = network.getGenerator("g4");
        Line l = network.getLine("l34");
        VoltageRegulation voltageRegulation = g.newVoltageRegulation()
            .withTargetValue(200.0)
            .withTerminal(l.getTerminal(TwoSides.ONE))
            .withMode(RegulationMode.REACTIVE_POWER)
            .build();
        assertEquals(200.0, voltageRegulation.getTargetValue(), 0.0);
        assertEquals(l.getTerminal(TwoSides.ONE), voltageRegulation.getTerminal());
        assertTrue(voltageRegulation.isRegulating());
    }

    @Test
    public void variantsCloneTest() {
        Network network = createNetwork();
        Generator g = network.getGenerator("g4");
        Line l = network.getLine("l34");
        VoltageRegulation voltageRegulation = g.newVoltageRegulation()
            .withTargetValue(200.0)
            .withTerminal(l.getTerminal(TwoSides.ONE))
            .withMode(RegulationMode.REACTIVE_POWER)
            .build();

        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertEquals(200.0, voltageRegulation.getTargetValue(), 0);
        assertTrue(voltageRegulation.isRegulating());

        // Testing setting different values in the cloned variant and going back to the initial one
        voltageRegulation.setTargetValue(210.0);
        voltageRegulation.setRegulating(false);
        assertFalse(voltageRegulation.isRegulating());
        assertEquals(210.0, voltageRegulation.getTargetValue(), 0f);

        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(200.0f, voltageRegulation.getTargetValue(), 0f);
        assertTrue(voltageRegulation.isRegulating());

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertEquals(200.0f, voltageRegulation.getTargetValue(), 0f);
        assertTrue(voltageRegulation.isRegulating());
        variantManager.setWorkingVariant(variant3);
        assertEquals(200.0f, voltageRegulation.getTargetValue(), 0f);
        assertTrue(voltageRegulation.isRegulating());

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            voltageRegulation.getTargetValue();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }

    @Test
    @Disabled("TODO MSA add validation")
    public void adderTest() {
        Network network = createNetwork();
        Generator g = network.getGenerator("g4");
        Line l = network.getLine("l34");
        var adder = g.newVoltageRegulation()
                .withTargetValue(200.0);
        var e = assertThrows(PowsyblException.class, adder::build);
        assertEquals("Regulating terminal must be set", e.getMessage());
        adder = g.newVoltageRegulation()
                .withTerminal(l.getTerminal(TwoSides.ONE));
        e = assertThrows(PowsyblException.class, adder::build);
        assertEquals("Reactive power target must be set", e.getMessage());
        var voltageRegulation = g.newVoltageRegulation()
                .withTargetValue(200.0)
                .withTerminal(l.getTerminal(TwoSides.ONE))
                .build();
        assertTrue(voltageRegulation.isRegulating());
    }

    @Test
    public void terminalRemoveTest() {
        Network network = createNetwork();
        Generator g = network.getGenerator("g4");
        Line l = network.getLine("l34");
        g.newVoltageRegulation()
            .withTargetValue(200.0)
            .withMode(RegulationMode.REACTIVE_POWER)
            .withTerminal(l.getTerminal(TwoSides.ONE))
            .build();
        assertNotNull(g.getVoltageRegulation());
        l.remove();
        // remote terminal has been removed because it is invalid
        assertNull(g.getVoltageRegulation().getTerminal());
    }

    @Test
    public void terminalReplacementTest() {
        Network network = createNetwork();
        Generator g = network.getGenerator("g4");
        Line l = network.getLine("l34");
        Line l2 = network.getLine("l12");
        VoltageRegulation voltageRegulation = g.newVoltageRegulation()
            .withTargetValue(200.0)
            .withTerminal(l.getTerminal(TwoSides.ONE))
            .withMode(RegulationMode.REACTIVE_POWER)
            .build();
        assertNotNull(g.getVoltageRegulation());
        voltageRegulation.setTerminal(l2.getTerminal(TwoSides.ONE));
        l.remove();
        // voltageRegulation should not be removed
        assertNotNull(g.getVoltageRegulation());
        assertNotNull(g.getVoltageRegulation().getTerminal());
        l2.remove();
        // voltageRegulation should not be removed
        assertNotNull(g.getVoltageRegulation());
        assertNull(g.getVoltageRegulation().getTerminal());
    }

    @Test
    void replacementTest() {
        Network network = createNetwork();
        Generator g = network.getGenerator("g4");
        Line l = network.getLine("l34");
        Terminal lTerminal = l.getTerminal(TwoSides.ONE);
        VoltageRegulation voltageRegulation = g.newVoltageRegulation()
            .withTargetValue(200.0)
            .withMode(RegulationMode.REACTIVE_POWER)
            .withTerminal(lTerminal)
            .build();

        assertEquals(lTerminal, voltageRegulation.getTerminal());
        assertEquals("b3", voltageRegulation.getTerminal().getBusBreakerView().getBus().getId());

        // Replacement
        Terminal.BusBreakerView bbView = lTerminal.getBusBreakerView();
        bbView.moveConnectable("b2", true);
        assertEquals("b2", voltageRegulation.getTerminal().getBusBreakerView().getBus().getId());

        // Remote terminal should be removed
        l.remove();
        assertNotNull(g.getVoltageRegulation());
        assertNull(g.getVoltageRegulation().getTerminal());
    }
}
