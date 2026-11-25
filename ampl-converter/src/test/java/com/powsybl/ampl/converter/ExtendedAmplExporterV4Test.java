/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;
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
    String getBatteriesTestRefFileName() {
        return "inputs/extended_exporter_v4/batteries.txt";
    }

    @Override
    String getHeadersTestRefFileName() {
        return "inputs/extended_exporter_v4/headers.txt";
    }

    @Override
    @Test
    void testVscExtendedReactiveCapabilityCurveData() throws IOException {
        Network network = HvdcTestNetwork.createVsc();
        exporter.export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_network_vsc_converter_stations", "inputs/extended_exporter_v4/vsc-test-case.txt");
    }

    @Test
    void testReactivePowerPerActivePowerRegulation() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.getGenerator("GEN").newVoltageRegulation()
                //.withMode(RegulationMode.REACTIVE_POWER_PER_ACTIVE_POWER)
                .withRegulating(true)
                .withSlope(0.25)
                .build();

        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_generators",
                "inputs/extended_exporter_v4/generator-qp-regulation.txt");
    }
}
