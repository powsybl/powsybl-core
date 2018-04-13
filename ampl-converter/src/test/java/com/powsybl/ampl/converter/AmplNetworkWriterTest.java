/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.*;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AmplNetworkWriterTest extends AbstractConverterTest {

    private void assertEqualsToRef(MemDataSource dataSource, String suffix, String refFileName) throws IOException {
        try (InputStream actual = new ByteArrayInputStream(dataSource.getData(suffix, "txt"))) {
            compareTxt(getClass().getResourceAsStream("/" + refFileName), actual);
        }
    }

    @Test
    public void test() {
        AmplExporter exporter = new AmplExporter();
        Assert.assertEquals("AMPL", exporter.getFormat());
    }

    @Test
    public void writeEurostag() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        MemDataSource dataSource = new MemDataSource();
        AmplExporter exporter = new AmplExporter();
        exporter.export(network, new Properties(), dataSource);

        assertEqualsToRef(dataSource, "_network_substations", "inputs/eurostag-tutorial-example1-substations.txt");
        assertEqualsToRef(dataSource, "_network_buses", "inputs/eurostag-tutorial-example1-buses.txt");
        assertEqualsToRef(dataSource, "_network_tct", "inputs/eurostag-tutorial-example1-tct.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "inputs/eurostag-tutorial-example1-rtc.txt");
        assertEqualsToRef(dataSource, "_network_ptc", "inputs/eurostag-tutorial-example1-ptc.txt");
        assertEqualsToRef(dataSource, "_network_loads", "inputs/eurostag-tutorial-example1-loads.txt");
        assertEqualsToRef(dataSource, "_network_shunts", "inputs/eurostag-tutorial-example1-shunts.txt");
        assertEqualsToRef(dataSource, "_network_generators", "inputs/eurostag-tutorial-example1-generators.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/eurostag-tutorial-example1-limits.txt");
    }

    @Test
    public void writeLcc() throws IOException {
        Network network = HvdcTestNetwork.createLcc();

        MemDataSource dataSource = new MemDataSource();
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_hvdc", "inputs/hvdc-lcc-test-case.txt");
        assertEqualsToRef(dataSource, "_network_lcc_converter_stations", "inputs/lcc-test-case.txt");

    }

    @Test
    public void writePhaseTapChanger() throws IOException {
        Network network = PhaseShifterTestCaseFactory.create();

        MemDataSource dataSource = new MemDataSource();
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_ptc", "inputs/ptc-test-case.txt");
    }

    @Test
    public void writeSVC() throws IOException {
        Network network = SvcTestCaseFactory.create();

        MemDataSource dataSource = new MemDataSource();
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_static_var_compensators", "inputs/svc-test-case.txt");
    }

    @Test
    public void writeThreeWindingsTransformer() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits();

        MemDataSource dataSource = new MemDataSource();
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_branches", "inputs/three-windings-transformers-branches.txt");
        assertEqualsToRef(dataSource, "_network_buses", "inputs/three-windings-transformers-buses.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "inputs/three-windings-transformers-rtc.txt");
        assertEqualsToRef(dataSource, "_network_substations", "inputs/three-windings-transformers-substations.txt");
        assertEqualsToRef(dataSource, "_network_tct", "inputs/three-windings-transformers-tct.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/three-windings-transformers-limits.txt");
    }

    @Test
    public void writeVsc() throws IOException {
        Network network = HvdcTestNetwork.createVsc();

        MemDataSource dataSource = new MemDataSource();
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_hvdc", "inputs/hvdc-vsc-test-case.txt");
        assertEqualsToRef(dataSource, "_network_vsc_converter_stations", "inputs/vsc-test-case.txt");

    }

    @Test
    public void writeCurrentLimits() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithCurrentLimits();

        MemDataSource dataSource = new MemDataSource();
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_limits", "inputs/current-limits-test-case.txt");
    }

    @Test
    public void writeDanglingLines() throws IOException {
        Network network = DanglingLineNetworkFactory.create();

        MemDataSource dataSource = new MemDataSource();
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_branches", "inputs/dangling-line-branches.txt");
        assertEqualsToRef(dataSource, "_network_buses", "inputs/dangling-line-buses.txt");
        assertEqualsToRef(dataSource, "_network_limits", "inputs/dangling-line-limits.txt");
        assertEqualsToRef(dataSource, "_network_loads", "inputs/dangling-line-loads.txt");
        assertEqualsToRef(dataSource, "_network_substations", "inputs/dangling-line-substations.txt");
    }

    @Test
    public void writeExtensions() throws IOException {
        Network network = HvdcTestNetwork.createLcc();
        HvdcLine l = network.getHvdcLine("L");
        l.addExtension(FooExtension.class, new FooExtension());
        MemDataSource dataSource = new MemDataSource();
        export(network, dataSource);

        assertEqualsToRef(dataSource, "foo-extension", "inputs/foo-extension.txt");
    }

    private static void export(Network network, DataSource dataSource) {
        AmplExporter exporter = new AmplExporter();
        exporter.export(network, new Properties(), dataSource);
    }
}
