/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.extensions.ObservabilityAreaAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.util.Networks;

import java.util.Map;
import java.util.Set;

final class ObservabilityAreaTestSupport {

    private ObservabilityAreaTestSupport() {
    }

    static Network createBusBreakerNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
        vlgen.getBusBreakerView().newBus().setId("TEST").add();
        vlgen.getBusBreakerView().newBus().setId("TEST2").add();
        vlgen.getBusBreakerView().newSwitch().setId("TEST_SW").setBus1("NGEN").setBus2("TEST").setOpen(false).add();
        vlgen.getBusBreakerView().newSwitch().setId("TEST2_SW").setBus1("TEST").setBus2("TEST2").setOpen(true).add();
        network.newLine().setId("TEST_L").setVoltageLevel1(vlgen.getId()).setBus1("TEST2").setVoltageLevel2("VLHV1").setBus2("NHV1")
                .setR(3).setX(33).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).add();
        return network;
    }

    static Network createNodeBreakerNetwork() {
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel vlC = network.getVoltageLevel("C");
        vlC.getNodeBreakerView().newBusbarSection().setId("TEST_BBS").setNode(9).add();
        vlC.getNodeBreakerView().newSwitch().setId("TEST_SW").setNode1(9).setNode2(10).setOpen(true).setKind(SwitchKind.DISCONNECTOR).add();
        network.newLine().setId("TEST_L").setVoltageLevel1(vlC.getId()).setNode1(10).setVoltageLevel2("N").setNode2(30)
                .setR(3).setX(33).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).add();
        return network;
    }

    static ObservabilityArea populateBusBreakerAreaFromBusView(VoltageLevel voltageLevel) {
        ObservabilityAreaAdder adder = voltageLevel.newExtension(ObservabilityAreaAdder.class);
        int n = 1;
        for (Bus bus : voltageLevel.getBusView().getBuses()) {
            adder.withObservabilityAreaByBusViewBus(bus.getId(), n, getStatus(n));
            n++;
        }
        return adder.add();
    }

    static ObservabilityArea populateNodeBreakerAreaFromNodes(VoltageLevel voltageLevel) {
        ObservabilityAreaAdder adder = voltageLevel.newExtension(ObservabilityAreaAdder.class);
        Map<String, Set<Integer>> nodesByBus = Networks.getNodesByBus(voltageLevel);
        int n = 1;
        for (Bus bus : voltageLevel.getBusView().getBuses()) {
            adder.withObservabilityAreaByNodes(nodesByBus.get(bus.getId()), n, getStatus(n));
            n++;
        }
        return adder.add();
    }

    static ObservabilityArea.ObservabilityStatus getStatus(int n) {
        if (n % 2 == 0) {
            return ObservabilityArea.ObservabilityStatus.NON_OBSERVABLE;
        } else if (n % 3 == 0) {
            return ObservabilityArea.ObservabilityStatus.OBSERVABLE;
        } else {
            return ObservabilityArea.ObservabilityStatus.BORDER;
        }
    }
}
