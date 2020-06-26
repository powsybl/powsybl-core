/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShuntCompensatorAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("ShuntCompensatorAdapterTest", "iidm");
    }

    @Test
    public void testSetterGetter() {
        Network networkRef = HvdcTestNetwork.createLcc();
        mergingView.merge(networkRef);

        final ShuntCompensator shuntCExpected = networkRef.getShuntCompensator("C1_Filter1");
        final ShuntCompensator shuntCActual   = mergingView.getShuntCompensator("C1_Filter1");
        assertNotNull(shuntCActual);
        assertTrue(shuntCActual instanceof ShuntCompensatorAdapter);
        assertSame(mergingView, shuntCActual.getNetwork());

        assertEquals(shuntCExpected.getType(), shuntCActual.getType());
        assertTrue(shuntCActual.getTerminal() instanceof TerminalAdapter);
        shuntCActual.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        int maxCount = shuntCExpected.getMaximumSectionCount();
        shuntCActual.getModel(ShuntCompensatorLinearModel.class).setMaximumSectionCount(++maxCount);
        int currentCount = shuntCExpected.getSectionCount();
        assertEquals(currentCount, shuntCActual.getSectionCount());
        assertTrue(shuntCActual.setSectionCount(++currentCount) instanceof ShuntCompensatorAdapter);
        assertEquals(currentCount, shuntCActual.getSectionCount());

        double b = shuntCExpected.getModel(ShuntCompensatorLinearModel.class).getBPerSection();
        assertEquals(b, shuntCActual.getModel(ShuntCompensatorLinearModel.class).getBPerSection(), 0.0d);
        shuntCActual.getModel(ShuntCompensatorLinearModel.class).setBPerSection(++b);
        assertEquals(shuntCExpected.getModel(ShuntCompensatorLinearModel.class).getBPerSection(), shuntCActual.getModel(ShuntCompensatorLinearModel.class).getBPerSection(), 0.0d);

        double currentB = shuntCExpected.getB();
        assertEquals(currentB, shuntCActual.getB(), 0.0d);
        shuntCActual.getModel(ShuntCompensatorLinearModel.class).setBPerSection(++currentB);
        assertEquals(shuntCExpected.getB(), shuntCActual.getB(), 0.0d);

        Terminal terminal = mergingView.getLccConverterStation("C1").getTerminal();
        assertTrue(shuntCActual.setRegulatingTerminal(terminal) instanceof ShuntCompensatorAdapter);
        assertSame(terminal, shuntCActual.getRegulatingTerminal());

        double targetV = shuntCExpected.getTargetV();
        assertEquals(targetV, shuntCActual.getTargetV(), 0.0d);
        assertTrue(shuntCActual.setTargetV(400) instanceof ShuntCompensatorAdapter);
        assertEquals(shuntCExpected.getTargetV(), shuntCActual.getTargetV(), 0.0d);

        double targetDeadband = shuntCExpected.getTargetDeadband();
        assertEquals(targetDeadband, shuntCActual.getTargetDeadband(), 0.0d);
        assertTrue(shuntCActual.setTargetDeadband(20) instanceof ShuntCompensatorAdapter);
        assertEquals(shuntCExpected.getTargetDeadband(), shuntCActual.getTargetDeadband(), 0.0d);

        boolean voltageRegulatorOn = shuntCExpected.isVoltageRegulatorOn();
        assertEquals(voltageRegulatorOn, shuntCActual.isVoltageRegulatorOn());
        assertTrue(shuntCActual.setVoltageRegulatorOn(!voltageRegulatorOn) instanceof ShuntCompensatorAdapter);
        assertEquals(shuntCExpected.isVoltageRegulatorOn(), shuntCActual.isVoltageRegulatorOn());

        // Not implemented yet !
        TestUtil.notImplemented(shuntCActual::remove);
    }

    @Test
    public void testCreateLinear() {
        createNetwork();

        // Linear shunt
        ShuntCompensatorAdder adder = mergingView.getVoltageLevel("VLLOAD")
                .newShuntCompensator()
                    .setId("linear")
                    .setConnectableBus("NLOAD")
                    .setSectionCount(0)
                    .newLinearModel()
                        .setBPerSection(1.0)
                        .setMaximumSectionCount(2)
                    .add();
        assertTrue(adder instanceof ShuntCompensatorAdderAdapter);
        assertTrue(adder.add() instanceof ShuntCompensatorAdapter);

        ShuntCompensator shunt = mergingView.getShuntCompensator("linear");
        assertEquals(0, shunt.getSectionCount());
        assertEquals(2, shunt.getMaximumSectionCount());
        assertEquals(0, shunt.getB(), 0.0);
        assertEquals(0.0, shunt.getG(), 0.0);
        assertEquals(0, shunt.getB(0), 0.0);
        assertEquals(1.0, shunt.getB(1), 0.0);
        assertEquals(2.0, shunt.getB(2), 0.0);
        assertEquals(0.0, shunt.getG(0), 0.0);
        assertEquals(0.0, shunt.getG(1), 0.0);
        assertEquals(0.0, shunt.getG(2), 0.0);

        ShuntCompensatorLinearModel model = shunt.getModel(ShuntCompensatorLinearModel.class);
        assertEquals(1.0, model.getBPerSection(), 0.0);
        assertTrue(Double.isNaN(model.getGPerSection()));
    }

    @Test
    public void testCreateNonLinear() {
        createNetwork();

        // Non linear shunt
        ShuntCompensatorAdder adder = mergingView.getVoltageLevel("VLLOAD")
                .newShuntCompensator()
                .setId("nonLinear")
                .setConnectableBus("NLOAD")
                .setSectionCount(1)
                .newNonLinearModel()
                    .beginSection()
                        .setB(1.0)
                    .endSection()
                    .beginSection()
                        .setB(2.0)
                    .endSection()
                .add();
        assertTrue(adder instanceof ShuntCompensatorAdderAdapter);
        assertTrue(adder.add() instanceof ShuntCompensatorAdapter);

        ShuntCompensator shunt = mergingView.getShuntCompensator("nonLinear");
        assertEquals(1, shunt.getSectionCount());
        assertEquals(2, shunt.getMaximumSectionCount());

        ShuntCompensatorNonLinearModel model = shunt.getModel(ShuntCompensatorNonLinearModel.class);
        assertEquals(2, model.getAllSections().size());
    }

    private void createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        network.getLoad("LOAD").remove();
        mergingView.merge(network);
    }
}
    //pour les méthodes setComponentNumber de BusExt je peux les remonter dans Bus
