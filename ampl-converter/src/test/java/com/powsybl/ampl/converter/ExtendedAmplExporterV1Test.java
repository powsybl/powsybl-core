/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 * @author Pierre ARVY {@literal <pierre.arvy at artelys.com>}
 */
class ExtendedAmplExporterV1Test extends AbstractSerDeTest {

    MemDataSource dataSource;
    AmplExporter exporter;

    private void assertEqualsToRef(MemDataSource dataSource, String suffix, String refFileName) throws IOException {
        try (InputStream actual = new ByteArrayInputStream(dataSource.getData(suffix, "txt"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + refFileName), actual);
        }
    }

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        dataSource = new MemDataSource();
        exporter = new AmplExporter();
    }

    @Test
    void testNoModifiedExports() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();

        exporter.export(network, new Properties(), dataSource);

        assertEqualsToRef(dataSource, "_network_substations", "inputs/eurostag-tutorial-example1-substations.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "inputs/eurostag-tutorial-example1-rtc.txt");
        assertEqualsToRef(dataSource, "_network_ptc", "inputs/eurostag-tutorial-example1-ptc.txt");
        assertEqualsToRef(dataSource, "_network_loads", "inputs/eurostag-tutorial-example1-loads.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/eurostag-tutorial-example1-limits.txt");
    }

    @Test
    void testSlackBusExport() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlGen = network.getVoltageLevel("VLLOAD");
        Bus bus = vlGen.getBusBreakerView().getBus("NLOAD");
        SlackTerminalAdder adder = vlGen.newExtension(SlackTerminalAdder.class);
        adder.withTerminal(bus.getConnectedTerminals().iterator().next()).add();

        exporter.export(network, new Properties(), dataSource);

        assertEqualsToRef(dataSource, "_network_buses",
            "inputs/extended_exporter/eurostag-tutorial-example1-buses.txt");
    }

    @Test
    void testSlackBusValue3wtMiddleBusExport() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits();

        exporter.export(network, new Properties(), dataSource);

        // verify slack bus has been added to buses file
        assertEqualsToRef(dataSource, "_network_buses",
                "inputs/extended_exporter/three-windings-transformers-buses.txt");
    }

    @Test
    void testSlackBusValueDanglingLineMiddleBusExport() throws IOException {
        Network network = DanglingLineNetworkFactory.create();

        exporter.export(network, new Properties(), dataSource);

        // verify slack bus has been added to buses file
        assertEqualsToRef(dataSource, "_network_buses",
                "inputs/extended_exporter/dangling-line-buses.txt");
    }

    @Test
    void testSlackBusValueTieLineMiddleBusExport() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();

        Properties properties = new Properties();
        properties.put("iidm.export.ampl.with-xnodes", "true");
        exporter.export(network, properties, dataSource);

        // verify slack bus has been added to buses file
        assertEqualsToRef(dataSource, "_network_buses",
                "inputs/extended_exporter/eurostag-tutorial-example1-buses-tl.txt");
    }

    @Test
    void testNewTapTwoWindingsTransformerExport() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        TwoWindingsTransformer transformer = network.getTwoWindingsTransformers().iterator().next();
        transformer.newRatioTapChanger();
        transformer.newPhaseTapChanger();
        exporter.export(network, new Properties(), dataSource);
        // verify r, g and b values have been added to tap changer file
        assertEqualsToRef(dataSource, "_network_tct",
            "inputs/extended_exporter/eurostag-tutorial-example1-tct.txt");
    }

    @Test
    void testNewTapThreeWindingsTransformerExport() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits();
        network.getThreeWindingsTransformer("3WT").getLeg1()
                .newPhaseTapChanger()
                .beginStep()
                .setRho(1)
                .setR(0.1)
                .setX(1.)
                .setB(0.)
                .setG(0.)
                .setAlpha(0)
                .endStep()
                .setTapPosition(0)
                .setLowTapPosition(0)
                .add();

        exporter.export(network, new Properties(), dataSource);

        // verify r, g and b values have been added to tap changer file
        assertEqualsToRef(dataSource, "_network_tct",
                "inputs/extended_exporter/three-windings-transformers-tct.txt");
    }

    @Test
    void testRegulatingBusIdExportGenerators() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        network.getGenerator("GEN").setVoltageRegulatorOn(false);
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
                .add();

        exporter.export(network, new Properties(), dataSource);

        assertEqualsToRef(dataSource, "_network_generators",
            "inputs/extended_exporter/eurostag-tutorial-example1-generators-regulating-bus.txt");
    }

    @Test
    void testRegulatingBusIdExportSvc() throws IOException {
        Network network = SvcTestCaseFactory.createWithMoreSVCs();
        network.getStaticVarCompensator("SVC2").setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
        network.getVoltageLevel("VL1").newStaticVarCompensator()
                .setId("SVC1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(390)
                .setRegulatingTerminal(network.getLoad("L2").getTerminal())
                .add();

        exporter.export(network, new Properties(), dataSource);

        assertEqualsToRef(dataSource, "_network_static_var_compensators",
                "inputs/extended_exporter/svc-test-case-regulating-bus.txt");
    }

    @Test
    void testVersion() throws IOException {
        Network network = Network.create("dummy_network", "test");
        exporter.export(network, new Properties(), dataSource);

        assertEqualsToRef(dataSource, "_headers", "inputs/extended_exporter/headers.txt");
    }
}
