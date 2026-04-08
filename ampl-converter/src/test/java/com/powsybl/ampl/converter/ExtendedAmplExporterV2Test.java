/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Pierre ARVY {@literal <pierre.arvy at artelys.com>}
 */
class ExtendedAmplExporterV2Test extends AbstractAmplExporterTest {

    @Test
    void testNoModifiedExports() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();

        exporter.export(network, properties, dataSource);

        // no modification compared to ampl exporter v1.0
        assertEqualsToRef(dataSource, "_network_substations", "inputs/eurostag-tutorial-example1-substations.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "inputs/eurostag-tutorial-example1-rtc.txt");
        assertEqualsToRef(dataSource, "_network_ptc", "inputs/eurostag-tutorial-example1-ptc.txt");
        assertEqualsToRef(dataSource, "_network_loads", "inputs/eurostag-tutorial-example1-loads.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/eurostag-tutorial-example1-limits.txt");
    }

    @Test
    void testQ0UnitColumnBatteries() throws IOException {
        Network network = BatteryNetworkFactory.create();

        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_batteries", "inputs/extended_exporter_v2/battery-q0-unit-column.txt");
    }

    @Test
    void testIsCondenserExportGenerators() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        network.getVoltageLevel("VLGEN").newGenerator()
                .setId("GEN3")
                .setBus("NGEN")
                .setConnectableBus("NGEN")
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setRegulatingTerminal(network.getLoad("LOAD").getTerminal())
                .setTargetV(152.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .setCondenser(true)
                .add();

        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_generators",
                "inputs/extended_exporter_v2/eurostag-tutorial-example1-generators-is-condenser.txt");
    }

    @Test
    void testLccLoadTargetQ() throws IOException {
        Network network = HvdcTestNetwork.createLcc();

        exporter.export(network, properties, dataSource);

        // Check hvdc line has null parameter for ac emulation
        assertEqualsToRef(dataSource, "_network_hvdc", "inputs/extended_exporter_v2/hvdc-ac-emul-lcc-test-case.txt");

        // Check target Q has been added to LCC converter station table
        assertEqualsToRef(dataSource, "_network_lcc_converter_stations", "inputs/extended_exporter_v2/lcc-load-target-q-test-case.txt");
    }

    @Test
    void testHvdcNoAcEmulation() throws IOException {
        Network network = HvdcTestNetwork.createVsc();

        exporter.export(network, properties, dataSource);

        // Check that export is the same as for basic AMPL exporter
        assertEqualsToRef(dataSource, "_network_vsc_converter_stations", "inputs/vsc-test-case.txt");

        // Check hvdc line has null parameter for ac emulation
        assertEqualsToRef(dataSource, "_network_hvdc", "inputs/extended_exporter_v2/hvdc-vsc-test-case.txt");
    }

    @Test
    void testHvdcAcEmulation() throws IOException {
        Network network = HvdcTestNetwork.createVsc();
        network.getHvdcLine("L").newExtension(HvdcAngleDroopActivePowerControlAdder.class)
                .withP0(200.0f)
                .withDroop(0.9f)
                .withEnabled(true)
                .add();

        exporter.export(network, properties, dataSource);

        // Check that export is the same as for basic AMPL exporter
        assertEqualsToRef(dataSource, "_network_vsc_converter_stations", "inputs/vsc-test-case.txt");

        // Check ac emulation parameters of the hvdc line
        assertEqualsToRef(dataSource, "_network_hvdc", "inputs/extended_exporter_v2/hvdc-ac-emul-vsc-test-case.txt");
    }

    @Test
    void writeHeadersWithVersion12() throws IOException {
        Network network = Network.create("dummy_network", "test");
        exporter.export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_headers", "inputs/extended_exporter_v2/headers.txt");
    }

}
