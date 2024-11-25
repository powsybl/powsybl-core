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
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
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
                       .setVoltageRegulatorOn(true)
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
        RemoteReactivePowerControl control = g.newExtension(RemoteReactivePowerControlAdder.class).withTargetQ(200.0).withRegulatingTerminal(l.getTerminal(TwoSides.ONE)).withEnabled(true).add();
        assertEquals(200.0, control.getTargetQ(), 0.0);
        assertEquals(l.getTerminal(TwoSides.ONE), control.getRegulatingTerminal());
        assertTrue(control.isEnabled());
    }

    @Test
    public void variantsCloneTest() {
        Network network = createNetwork();
        Generator g = network.getGenerator("g4");
        Line l = network.getLine("l34");
        RemoteReactivePowerControl control = g.newExtension(RemoteReactivePowerControlAdder.class)
                .withTargetQ(200.0)
                .withRegulatingTerminal(l.getTerminal(TwoSides.ONE))
                .withEnabled(true)
                .add();

        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertEquals(200.0, control.getTargetQ(), 0);
        assertTrue(control.isEnabled());

        // Testing setting different values in the cloned variant and going back to the initial one
        control.setTargetQ(210.0);
        control.setEnabled(false);
        assertFalse(control.isEnabled());
        assertEquals(210.0, control.getTargetQ(), 0f);

        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(200.0f, control.getTargetQ(), 0f);
        assertTrue(control.isEnabled());

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertEquals(200.0f, control.getTargetQ(), 0f);
        assertTrue(control.isEnabled());
        variantManager.setWorkingVariant(variant3);
        assertEquals(200.0f, control.getTargetQ(), 0f);
        assertTrue(control.isEnabled());

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            control.getTargetQ();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }

    @Test
    public void adderTest() {
        Network network = createNetwork();
        Generator g = network.getGenerator("g4");
        Line l = network.getLine("l34");
        var adder = g.newExtension(RemoteReactivePowerControlAdder.class)
                .withTargetQ(200.0)
                .withEnabled(true);
        var e = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Regulating terminal must be set", e.getMessage());
        adder = g.newExtension(RemoteReactivePowerControlAdder.class)
                .withRegulatingTerminal(l.getTerminal(TwoSides.ONE))
                .withEnabled(true);
        e = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Reactive power target must be set", e.getMessage());
        var extension = g.newExtension(RemoteReactivePowerControlAdder.class)
                .withTargetQ(200.0)
                .withRegulatingTerminal(l.getTerminal(TwoSides.ONE))
                .add();
        assertTrue(extension.isEnabled());
    }

    @Test
    public void terminalRemoveTest() {
        Network network = createNetwork();
        Generator g = network.getGenerator("g4");
        Line l = network.getLine("l34");
        g.newExtension(RemoteReactivePowerControlAdder.class)
                .withTargetQ(200.0)
                .withRegulatingTerminal(l.getTerminal(TwoSides.ONE))
                .withEnabled(true)
                .add();
        assertNotNull(g.getExtension(RemoteReactivePowerControl.class));
        l.remove();
        // extension has been removed because regulating terminal is invalid
        assertNull(g.getExtension(RemoteReactivePowerControl.class));
    }
}
