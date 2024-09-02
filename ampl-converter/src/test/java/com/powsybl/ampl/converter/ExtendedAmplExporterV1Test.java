/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.ampl.converter.version.AmplExportVersion;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
class ExtendedAmplExporterV1Test extends AbstractSerDeTest {

    private AmplExportConfig v2Config;

    private void assertEqualsToRef(MemDataSource dataSource, String suffix, String refFileName) throws IOException {
        try (InputStream actual = new ByteArrayInputStream(dataSource.getData(suffix, "txt"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + refFileName), actual);
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        v2Config = new AmplExportConfig(AmplExportConfig.ExportScope.ALL, false,
            AmplExportConfig.ExportActionType.CURATIVE, false, false,
            AmplExportVersion.V1_1);
    }

    @Test
    void testSlackBusExport() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlGen = network.getVoltageLevel("VLLOAD");
        Bus bus = vlGen.getBusBreakerView().getBus("NLOAD");
        SlackTerminalAdder adder = vlGen.newExtension(SlackTerminalAdder.class);
        adder.withTerminal(bus.getConnectedTerminals().iterator().next()).add();

        MemDataSource dataSource = new MemDataSource();
        new AmplNetworkWriter(network, dataSource, v2Config).write();

        assertEqualsToRef(dataSource, "_network_buses",
            "eurostag-tutorial-example1-buses.txt");
    }

    @Test
    void testNewTapExport() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        TwoWindingsTransformer transformer = network.getTwoWindingsTransformers().iterator().next();
        transformer.newRatioTapChanger();
        transformer.newPhaseTapChanger();
        MemDataSource dataSource = new MemDataSource();
        new AmplNetworkWriter(network, dataSource, v2Config).write();
        assertEqualsToRef(dataSource, "_network_tct",
            "eurostag-tutorial-example1-tct.txt");

    }

    @Test
    void testRegulatingBusIdExportGenerators() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        network.getGenerator("GEN").setVoltageRegulatorOn(false);

        MemDataSource dataSource = new MemDataSource();
        new AmplNetworkWriter(network, dataSource, v2Config).write();

        assertEqualsToRef(dataSource, "_network_generators",
            "eurostag-tutorial-example1-generators-regulating-bus.txt");
    }

    @Test
    void testRegulatingBusIdExportSvc() throws IOException {
        Network network = SvcTestCaseFactory.createWithMoreSVCs();
        network.getStaticVarCompensator("SVC2").setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
        MemDataSource dataSource = new MemDataSource();
        new AmplNetworkWriter(network, dataSource, v2Config).write();

        assertEqualsToRef(dataSource, "_network_static_var_compensators", "svc-test-case-regulating-bus.txt");
    }

    @Test
    void testVersion() throws IOException {
        Network network = Network.create("dummy_network", "test");
        MemDataSource dataSource = new MemDataSource();
        new AmplNetworkWriter(network, dataSource, v2Config).write();

        assertEqualsToRef(dataSource, "_headers", "headers.txt");
    }
}
