/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class ExtendedAmplExporterV4Test extends ExtendedAmplExporterV3Test {

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        properties.remove("iidm.export.ampl.export-version");
    }

    @Override
    String getHeadersTestRefFileName() {
        return "inputs/extended_exporter_v4/headers.txt";
    }

    @Override
    String getIsCondenserTestRefFileName() {
        return "inputs/extended_exporter_v4/generator-is-condenser.txt";
    }

    @Override
    String getBatteriesTestRefFileName() {
        return "inputs/extended_exporter_v4/batteries-default.txt";
    }

    @Override
    String getVscTestRefFileName() {
        return "inputs/extended_exporter_v4/vsc.txt";
    }

    @Test
    void testGeneratorExport() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        Generator generator = network.getGenerator("GEN");
        generator.newVoltageRegulation()
                .withMode(RegulationMode.REACTIVE_POWER_PER_ACTIVE_POWER)
                .withRegulating(true)
                .withSlope(0.25)
                .build();
        String busId = generator.getTerminal().getVoltageLevel().getBusBreakerView().getBus("NGEN").getId();
        Generator generator1 = network.getVoltageLevel("VLGEN").newGenerator()
                .setId("GENERATOR1")
                .setBus(busId)
                .setConnectableBus(busId)
                .setMinP(-500)
                .setMaxP(500)
                .newVoltageRegulation().withMode(RegulationMode.VOLTAGE).add()
                .setLocalTargetV(24.5)
                .setTargetP(607.0)
                .setLocalTargetQ(301.0)
                .add();
        generator1.newReactiveCapabilityCurve()
                .beginPoint()
                    .setP(-500)
                    .setMinQ(-30)
                    .setMaxQ(30)
                .endPoint()
                .beginPoint()
                    .setP(0)
                    .setMinQ(-60)
                    .setMaxQ(60)
                .endPoint()
                .beginPoint()
                    .setP(500)
                    .setMinQ(-20)
                    .setMaxQ(20)
                .endPoint()
                .add();
        Generator generator2 = network.getVoltageLevel("VLGEN").newGenerator()
                .setId("GENERATOR2")
                .setBus(busId)
                .setConnectableBus(busId)
                .setMinP(-100)
                .setMaxP(700)
                .newVoltageRegulation().withMode(RegulationMode.VOLTAGE).add()
                .setLocalTargetV(24.5)
                .setTargetP(607.0)
                .setLocalTargetQ(301.0)
                .add();
        generator2.newMinMaxReactiveLimits()
                .setMinQ(-50)
                .setMaxQ(400)
                .add();

        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_generators",
                "inputs/extended_exporter_v4/generators.txt");
    }

    @Test
    void testBatteryExport() throws IOException {
        Network network = BatteryNetworkFactory.create();
        network.getBattery("BAT").newVoltageRegulation()
                .withMode(RegulationMode.REACTIVE_POWER_PER_ACTIVE_POWER)
                .withRegulating(true)
                .withSlope(0.25)
                .build();

        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_batteries",
                "inputs/extended_exporter_v4/batteries.txt");
    }

    @Test
    void testVscExport() throws IOException {
        Network network = HvdcTestNetwork.createVsc();

        exporter.export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_network_vsc_converter_stations",
                "inputs/extended_exporter_v4/vsc.txt");
    }
}
