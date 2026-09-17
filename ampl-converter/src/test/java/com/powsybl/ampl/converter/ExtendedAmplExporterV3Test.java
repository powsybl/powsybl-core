/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.VoltageRegulationAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import static com.powsybl.iidm.network.test.BatteryNetworkFactory.VLBAT;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class ExtendedAmplExporterV3Test extends ExtendedAmplExporterV2Test {

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        properties.remove("iidm.export.ampl.export-version");
    }

    @Override
    Network getBatteriesTestNetwork() {
        Network network = BatteryNetworkFactory.create();
        Battery battery3 = addNewBattery(network, "BAT3");
        battery3.newExtension(VoltageRegulationAdder.class)
                .withVoltageRegulatorOn(false)
                .withTargetV(123.4)
                .withRegulatingTerminal(battery3.getTerminal())
                .add();
        Battery battery4 = addNewBattery(network, "BAT4");
        battery4.newExtension(VoltageRegulationAdder.class)
                .withVoltageRegulatorOn(true)
                .withTargetV(234.5)
                .withRegulatingTerminal(network.getGenerator("GEN").getTerminal())
                .add();
        return network;
    }

    Battery addNewBattery(Network network, String id) {
        VoltageLevel vlbat = network.getVoltageLevel(VLBAT);
        Bus nbat = vlbat.getBusBreakerView().getBus("NBAT");
        Battery battery2 = network.getBattery("BAT2");
        Battery battery = vlbat.newBattery()
                .setId(id)
                .setBus(nbat.getId())
                .setConnectableBus(nbat.getId())
                .setTargetP(100)
                .setTargetQ(200)
                .setMinP(-200)
                .setMaxP(200)
                .add();
        battery.newReactiveCapabilityCurve(battery2.getReactiveLimits(ReactiveCapabilityCurve.class)).add();
        battery.getTerminal().setP(-605).setQ(-225);
        return battery;
    }

    @Override
    String getBatteriesTestRefFileName() {
        return "inputs/extended_exporter_v3/batteries.txt";
    }

    @Override
    String getHeadersTestRefFileName() {
        return "inputs/extended_exporter_v3/headers.txt";
    }
}
