/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 * @author Pierre ARVY {@literal <pierre.arvy at artelys.com>}
 */
class ExtendedAmplExporterTest extends AbstractAmplExporterTest {

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        properties.put("iidm.export.ampl.export-version", "1.1");
    }

    @Test
    void testNoModifiedExports() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();

        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_substations", "inputs/eurostag-tutorial-example1-substations.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "inputs/eurostag-tutorial-example1-rtc.txt");
        assertEqualsToRef(dataSource, "_network_ptc", "inputs/eurostag-tutorial-example1-ptc.txt");
        assertEqualsToRef(dataSource, "_network_loads", "inputs/eurostag-tutorial-example1-loads.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/eurostag-tutorial-example1-limits.txt");
    }

    @Test
    void testSlackBusSynchronousComponentExport() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlGen = network.getVoltageLevel("VLLOAD");
        Bus bus = vlGen.getBusBreakerView().getBus("NLOAD");
        SlackTerminalAdder adder = vlGen.newExtension(SlackTerminalAdder.class);
        adder.withTerminal(bus.getConnectedTerminals().iterator().next()).add();

        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_buses",
            "inputs/extended_exporter/eurostag-tutorial-example1-buses.txt");
    }

    @Test
    void testThreeWindingTransformerExport() throws IOException {
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

        exporter.export(network, properties, dataSource);

        // Check that export is the same as for basic AMPL exporter
        assertEqualsToRef(dataSource, "_network_branches", "inputs/three-windings-transformers-branches.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "inputs/three-windings-transformers-rtc.txt");
        assertEqualsToRef(dataSource, "_network_substations", "inputs/three-windings-transformers-substations.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/three-windings-transformers-limits.txt");

        // Check that slack bus and sc have been added to buses file
        assertEqualsToRef(dataSource, "_network_buses",
                "inputs/extended_exporter/three-windings-transformers-buses.txt");

        // Check that bus 1 is in different sc if leg 1 is disconnected
        network.getThreeWindingsTransformer("3WT").getLeg1().getTerminal().disconnect();
        exporter.export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_network_buses", "inputs/extended_exporter/three-windings-transformers-1-leg-disconnected-buses.txt");

        // Check that bus 1 and 2 are in different sc if leg 1 and 2 are disconnected
        network.getThreeWindingsTransformer("3WT").getLeg2().getTerminal().disconnect();
        exporter.export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_network_buses", "inputs/extended_exporter/three-windings-transformers-2-legs-disconnected-buses.txt");

        // Check that all buses are in different sc if 3wt is disconnected
        network.getThreeWindingsTransformer("3WT").disconnect();
        exporter.export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_network_buses", "inputs/extended_exporter/three-windings-transformers-disconnected-buses.txt");
    }

    @Test
    void testDanglingLineExport() throws IOException {
        Network network = DanglingLineNetworkFactory.create();

        exporter.export(network, properties, dataSource);

        // Check that export is the same as for basic AMPL exporter
        assertEqualsToRef(dataSource, "_network_branches", "inputs/dangling-line-branches.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/dangling-line-limits.txt");
        assertEqualsToRef(dataSource, "_network_loads", "inputs/dangling-line-loads.txt");
        assertEqualsToRef(dataSource, "_network_substations", "inputs/dangling-line-substations.txt");

        // Check that slack bus and sc have been added to buses file
        assertEqualsToRef(dataSource, "_network_buses",
                "inputs/extended_exporter/dangling-line-buses.txt");

        // Check that middle bus in different sc if dangling line bus is disconnected
        network.getDanglingLine("DL").getTerminal().disconnect();
        exporter.export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_network_buses", "inputs/extended_exporter/dangling-line-disconnected-buses.txt");
    }

    @Test
    void testTieLineExport() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        for (DanglingLine danglingLine : network.getDanglingLines()) {
            danglingLine.newCurrentLimits()
                    .setPermanentLimit(100.0)
                    .beginTemporaryLimit().setName("20'").setValue(120.0).setAcceptableDuration(20 * 60).endTemporaryLimit()
                    .beginTemporaryLimit().setName("10'").setValue(140.0).setAcceptableDuration(10 * 60).endTemporaryLimit()
                    .add();
        }

        properties.put("iidm.export.ampl.with-xnodes", "true");
        exporter.export(network, properties, dataSource);

        // Check that export is the same as for basic AMPL exporter
        assertEqualsToRef(dataSource, "_network_substations", "inputs/eurostag-tutorial-example1-substations-tl.txt");
        assertEqualsToRef(dataSource, "_network_branches", "inputs/eurostag-tutorial-example1-branches-tl.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/eurostag-tutorial-example1-limits-tl.txt");

        // Check that slack bus and sc have been added to buses file
        assertEqualsToRef(dataSource, "_network_buses",
                "inputs/extended_exporter/eurostag-tutorial-example1-buses-tl.txt");

        // Check that sc num stays the same if dangling line 1 is disconnected, as middle bus stays connected to main sc
        network.getTieLine("NHV1_NHV2_1").getDanglingLine1().getTerminal().disconnect();
        exporter.export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_network_buses", "inputs/extended_exporter/eurostag-tutorial-example1-buses-tl.txt");

        // Check that sc num of middle bus is different if dangling lines 1 and 2 are disconnected
        network.getTieLine("NHV1_NHV2_1").getDanglingLine2().getTerminal().disconnect();
        exporter.export(network, properties, dataSource);
        assertEqualsToRef(dataSource, "_network_buses", "inputs/extended_exporter/eurostag-tutorial-example1-buses-tl-disconnected.txt");

    }

    @Test
    void testNewTapTwoWindingsTransformerExport() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        TwoWindingsTransformer transformer = network.getTwoWindingsTransformers().iterator().next();
        transformer.newRatioTapChanger();
        transformer.newPhaseTapChanger();
        exporter.export(network, properties, dataSource);
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

        exporter.export(network, properties, dataSource);

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

        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_generators",
            "inputs/extended_exporter/eurostag-tutorial-example1-generators-regulating-bus.txt");
    }

    @Test
    void testRegulatingBusIdExportSvc() throws IOException {
        Network network = SvcTestCaseFactory.createWithMoreSVCs();
        network.getStaticVarCompensator("SVC2").setRegulating(false);
        network.getVoltageLevel("VL1").newStaticVarCompensator()
                .setId("SVC1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setRegulating(true)
                .setVoltageSetpoint(390)
                .setRegulatingTerminal(network.getLoad("L2").getTerminal())
                .add();

        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_network_static_var_compensators",
                "inputs/extended_exporter/svc-test-case-regulating-bus.txt");
    }

    @Test
    void testTwoSynchronousComponentWithHvdcExport() throws IOException {
        Network network = HvdcTestNetwork.createVsc();

        exporter.export(network, properties, dataSource);

        // Check that export is the same as for basic AMPL exporter
        assertEqualsToRef(dataSource, "_network_hvdc", "inputs/hvdc-vsc-test-case.txt");
        assertEqualsToRef(dataSource, "_network_vsc_converter_stations", "inputs/vsc-test-case.txt");

        // Check that synchronous components are different due to HVDC line
        assertEqualsToRef(dataSource, "_network_buses", "inputs/extended_exporter/buses-vsc-test-case.txt");
    }

    @Test
    void writeTwoConnectedComponent() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMultipleConnectedComponents();

        exporter.export(network, properties, dataSource);

        // Check that synchronous components are different for the two connected component
        assertEqualsToRef(dataSource, "_network_buses", "inputs/extended_exporter/two-connected-components-buses.txt");
    }

    @Test
    void testVersion() throws IOException {
        Network network = Network.create("dummy_network", "test");
        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_headers", "inputs/extended_exporter/headers.txt");
    }
}
