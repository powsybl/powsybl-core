/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.extensions.ObservabilityAreaAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.util.Networks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class ObservabilityAreaTest {

    @Test
    void testBb() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
        vlgen.getBusBreakerView().newBus().setId("TEST").add();
        vlgen.getBusBreakerView().newBus().setId("TEST2").add();
        vlgen.getBusBreakerView().newSwitch().setId("TEST_SW").setBus1("NGEN").setBus2("TEST").setOpen(false).add();
        vlgen.getBusBreakerView().newSwitch().setId("TEST2_SW").setBus1("TEST").setBus2("TEST2").setOpen(true).add();
        network.newLine().setId("TEST_L").setVoltageLevel1(vlgen.getId()).setBus1("TEST2").setVoltageLevel2("VLHV1").setBus2("NHV1")
                .setR(3).setX(33).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).add();

        ObservabilityAreaAdder adder = vlgen.newExtension(ObservabilityAreaAdder.class);
        int n = 1;
        for (Bus bus : vlgen.getBusView().getBuses()) {
            adder.withObservabilityAreaByBusViewBus(bus.getId(), n, getStatus(n));
            n++;
        }
        adder.add();

        ObservabilityArea ext = vlgen.getExtension(ObservabilityArea.class);
        Assertions.assertNotNull(ext);
        n = 1;
        for (Bus bus : vlgen.getBusView().getBuses()) {
            ObservabilityArea.AreaCharacteristics c = ext.getBusView().getObservabilityArea(bus.getId());
            Assertions.assertNotNull(c);
            Assertions.assertEquals(n, c.getAreaNumber());
            Assertions.assertEquals(getStatus(n), c.getStatus());
            Assertions.assertTrue(bus.getConnectedTerminalStream().allMatch(t -> c.getTerminals().contains(t)));
            Assertions.assertTrue(c.getTerminals().containsAll(bus.getConnectedTerminalStream().collect(Collectors.toSet())));
            for (Bus b : vlgen.getBusBreakerView().getBusesFromBusViewBusId(bus.getId())) {
                Assertions.assertNotNull(ext.getBusBreakerView().getObservabilityArea(b.getId()));
                Assertions.assertEquals(n, ext.getBusBreakerView().getObservabilityArea(b.getId()).getAreaNumber());
                Assertions.assertEquals(getStatus(n), ext.getBusBreakerView().getObservabilityArea(b.getId()).getStatus());
            }
            for (Terminal t : bus.getConnectedTerminals()) {
                Assertions.assertNotNull(ext.getObservabilityArea(t));
                Assertions.assertEquals(n, ext.getObservabilityArea(t).getAreaNumber());
                Assertions.assertEquals(getStatus(n), ext.getObservabilityArea(t).getStatus());
            }
            n++;
        }

        UnsupportedOperationException e1 = Assertions.assertThrows(UnsupportedOperationException.class, () -> ext.getNodeBreakerView().getObservabilityAreaByNode());
        Assertions.assertEquals("Not supported in a bus breaker topology", e1.getMessage());
        UnsupportedOperationException e2 = Assertions.assertThrows(UnsupportedOperationException.class, () -> ext.getNodeBreakerView().getObservabilityArea(0));
        Assertions.assertEquals("Not supported in a bus breaker topology", e2.getMessage());

        // Topology modification
        vlgen.getBusBreakerView().getSwitch("TEST2_SW").setOpen(false);
        PowsyblException e3 = Assertions.assertThrows(PowsyblException.class, () -> ext.getBusView().getObservabilityArea("VLGEN_0"));
        Assertions.assertEquals("Inconsistent observabilities areas: bus VLGEN_0 is associated to different area numbers and/or status", e3.getMessage());
        ObservabilityArea.AreaCharacteristics c1 = ext.getBusView().getObservabilityArea("VLGEN_0", false);
        Assertions.assertNotNull(c1);
        Assertions.assertTrue(Set.of(1, 2).contains(c1.getAreaNumber()));
        Assertions.assertTrue(Set.of(ObservabilityArea.ObservabilityStatus.BORDER, ObservabilityArea.ObservabilityStatus.NON_OBSERVABLE).contains(c1.getStatus()));

        // Structure modification
        vlgen.getBusBreakerView().removeSwitch("TEST_SW");
        vlgen.getBusBreakerView().removeSwitch("TEST2_SW");
        vlgen.getBusBreakerView().removeBus("TEST");
        PowsyblException e4 = Assertions.assertThrows(PowsyblException.class, () -> ext.getBusView().getObservabilityArea("VLGEN_0"));
        Assertions.assertEquals("Inconsistent observabilities areas: bus TEST does not exist anymore in bus-breaker view", e4.getMessage());
        ObservabilityArea.AreaCharacteristics c2 = ext.getBusView().getObservabilityArea("VLGEN_0", false);
        Assertions.assertNotNull(c2);
        Assertions.assertEquals(1, c2.getAreaNumber());
        Assertions.assertEquals(ObservabilityArea.ObservabilityStatus.BORDER, c2.getStatus());
    }

    @Test
    void testNb() {
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel vlC = network.getVoltageLevel("C");
        vlC.getNodeBreakerView().newBusbarSection().setId("TEST_BBS").setNode(9).add();
        vlC.getNodeBreakerView().newSwitch().setId("TEST_SW").setNode1(9).setNode2(10).setOpen(true).setKind(SwitchKind.DISCONNECTOR).add();
        network.newLine().setId("TEST_L").setVoltageLevel1(vlC.getId()).setNode1(10).setVoltageLevel2("N").setNode2(30)
                .setR(3).setX(33).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).add();

        ObservabilityAreaAdder adder = vlC.newExtension(ObservabilityAreaAdder.class);

        int n = 1;
        Map<String, Set<Integer>> nodesByBus = Networks.getNodesByBus(vlC);
        for (Bus bus : vlC.getBusView().getBuses()) {
            adder.withObservabilityAreaByNodes(nodesByBus.get(bus.getId()), n, getStatus(n));
            n++;
        }
        adder.add();

        ObservabilityArea ext = vlC.getExtension(ObservabilityArea.class);
        Assertions.assertNotNull(ext);
        n = 1;
        for (Bus bus : vlC.getBusView().getBuses()) {
            ObservabilityArea.AreaCharacteristics c = ext.getBusView().getObservabilityArea(bus.getId());
            Assertions.assertNotNull(c);
            Assertions.assertEquals(n, c.getAreaNumber());
            Assertions.assertEquals(getStatus(n), c.getStatus());
            Assertions.assertTrue(bus.getConnectedTerminalStream().allMatch(t -> c.getTerminals().contains(t)));
            Assertions.assertTrue(c.getTerminals().containsAll(bus.getConnectedTerminalStream().collect(Collectors.toSet())));
            for (Bus b : vlC.getBusBreakerView().getBusesFromBusViewBusId(bus.getId())) {
                Assertions.assertNotNull(ext.getBusBreakerView().getObservabilityArea(b.getId()));
                Assertions.assertEquals(n, ext.getBusBreakerView().getObservabilityArea(b.getId()).getAreaNumber());
                Assertions.assertEquals(getStatus(n), ext.getBusBreakerView().getObservabilityArea(b.getId()).getStatus());
            }
            for (int node : nodesByBus.get(bus.getId())) {
                Assertions.assertNotNull(ext.getNodeBreakerView().getObservabilityArea(node));
                Assertions.assertEquals(n, ext.getNodeBreakerView().getObservabilityArea(node).getAreaNumber());
                Assertions.assertEquals(getStatus(n), ext.getNodeBreakerView().getObservabilityArea(node).getStatus());
            }
            for (Terminal t : bus.getConnectedTerminals()) {
                Assertions.assertNotNull(ext.getObservabilityArea(t));
                Assertions.assertEquals(n, ext.getObservabilityArea(t).getAreaNumber());
                Assertions.assertEquals(getStatus(n), ext.getObservabilityArea(t).getStatus());
            }
            n++;
        }

        // Topology modification
        vlC.getNodeBreakerView().getSwitch("TEST_SW").setOpen(false);
        Assertions.assertNotNull(vlC.getBusView().getBus("C_9")); // new bus after topology modification
        Assertions.assertNull(ext.getBusView().getObservabilityArea("C_9"));

        // Structure modification
        vlC.getNodeBreakerView().newSwitch().setId("TEST2_SW").setNode1(0).setNode2(9).setOpen(false).setKind(SwitchKind.DISCONNECTOR).add();
        PowsyblException e = Assertions.assertThrows(PowsyblException.class, () -> ext.getBusView().getObservabilityArea("C_0"));
        Assertions.assertEquals("Inconsistent observability areas: only part of nodes of bus-view bus C_0 are defined", e.getMessage());
        ObservabilityArea.AreaCharacteristics c = ext.getBusView().getObservabilityArea("C_0", false);
        Assertions.assertNotNull(c);
        Assertions.assertEquals(1, c.getAreaNumber());
        Assertions.assertEquals(ObservabilityArea.ObservabilityStatus.BORDER, c.getStatus());
    }

    private static ObservabilityArea.ObservabilityStatus getStatus(int n) {
        if (n % 2 == 0) {
            return ObservabilityArea.ObservabilityStatus.NON_OBSERVABLE;
        } else if (n % 3 == 0) {
            return ObservabilityArea.ObservabilityStatus.OBSERVABLE;
        } else {
            return ObservabilityArea.ObservabilityStatus.BORDER;
        }
    }
}
