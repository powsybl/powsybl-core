/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;

public class AmplExporterV2Test {

    Properties properties;
    MemDataSource dataSource;
    AmplExporter exporter;

    @BeforeEach
    void setUp() {
        properties = new Properties();
        properties.put("iidm.export.ampl.export-version", "2.0");
        dataSource = new MemDataSource();
        exporter = new AmplExporter();
    }

    @Test
    void testEurostagV2() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();

        exporter.export(network, properties, dataSource);

        // verify export is the same as for basic ampl exporter
        assertEqualsToRef(dataSource, "_network_substations", "inputs/eurostag-tutorial-example1-substations.txt");
        assertEqualsToRef(dataSource, "_network_tct", "inputs/eurostag-tutorial-example1-tct.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "inputs/eurostag-tutorial-example1-rtc.txt");
        assertEqualsToRef(dataSource, "_network_ptc", "inputs/eurostag-tutorial-example1-ptc.txt");
        assertEqualsToRef(dataSource, "_network_loads", "inputs/eurostag-tutorial-example1-loads.txt");
        assertEqualsToRef(dataSource, "_network_generators", "inputs/eurostag-tutorial-example1-generators.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/eurostag-tutorial-example1-limits.txt");

        // verify synchronous component has been added to buses file
        assertEqualsToRef(dataSource, "_network_buses", "inputs/v2/eurostag-tutorial-example1-buses.txt");
    }

    @Test
    void writeThreeWindingsTransformerV2() throws IOException {
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

        // verify export is the same as for basic ampl exporter
        assertEqualsToRef(dataSource, "_network_branches", "inputs/three-windings-transformers-branches.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "inputs/three-windings-transformers-rtc.txt");
        assertEqualsToRef(dataSource, "_network_substations", "inputs/three-windings-transformers-substations.txt");
        assertEqualsToRef(dataSource, "_network_tct", "inputs/three-windings-transformers-tct.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/three-windings-transformers-limits.txt");

        // verify synchronous component has been added to buses file
        assertEqualsToRef(dataSource, "_network_buses", "inputs/v2/three-windings-transformers-buses.txt");
    }

    @Test
    void writeTieLine() throws IOException {
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

        // verify export is the same as for basic ampl exporter
        assertEqualsToRef(dataSource, "_network_substations", "inputs/eurostag-tutorial-example1-substations-tl.txt");
        assertEqualsToRef(dataSource, "_network_branches", "inputs/eurostag-tutorial-example1-branches-tl.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/eurostag-tutorial-example1-limits-tl.txt");

        // verify synchronous component has been added to buses file
        assertEqualsToRef(dataSource, "_network_buses", "inputs/v2/eurostag-tutorial-example1-buses-tl.txt");
    }

    @Test
    void writeDanglingLines() throws IOException {
        Network network = DanglingLineNetworkFactory.create();

        exporter.export(network, properties, dataSource);

        // verify export is the same as for basic ampl exporter
        assertEqualsToRef(dataSource, "_network_branches", "inputs/dangling-line-branches.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/dangling-line-limits.txt");
        assertEqualsToRef(dataSource, "_network_loads", "inputs/dangling-line-loads.txt");
        assertEqualsToRef(dataSource, "_network_substations", "inputs/dangling-line-substations.txt");

        // verify synchronous component has been added to buses file
        assertEqualsToRef(dataSource, "_network_buses", "inputs/v2/dangling-line-buses.txt");
    }

    @Test
    void writeTwoSynchronousComponentWithHVDC() throws IOException {
        Network network = HvdcTestNetwork.createVsc();

        exporter.export(network, properties, dataSource);

        // verify export is the same as for basic ampl exporter
        assertEqualsToRef(dataSource, "_network_hvdc", "inputs/hvdc-vsc-test-case.txt");
        assertEqualsToRef(dataSource, "_network_vsc_converter_stations", "inputs/vsc-test-case.txt");

        // verify synchronous component are different for the two buses of the network
        assertEqualsToRef(dataSource, "_network_buses", "inputs/v2/buses-vsc-test-case.txt");
    }

    @Test
    void writeTwoConnectedComponent() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMultipleConnectedComponents();

        exporter.export(network, properties, dataSource);

        // verify synchronous component are different for the two connected component
        assertEqualsToRef(dataSource, "_network_buses", "inputs/v2/two-connected-components-buses.txt");
    }

    @Test
    void writeHeadersWithVersion2() throws IOException {
        Network network = Network.create("dummy_network", "test");

        exporter.export(network, properties, dataSource);

        assertEqualsToRef(dataSource, "_headers", "inputs/v2/headers.txt");
    }

    private void assertEqualsToRef(MemDataSource dataSource, String suffix, String refFileName) throws IOException {
        try (InputStream actual = new ByteArrayInputStream(dataSource.getData(suffix, "txt"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + refFileName), actual);
        }
    }
}
