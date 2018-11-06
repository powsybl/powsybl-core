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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AmplNetworkWriterTest extends AbstractConverterTest {

    private void assertEqualsToRef(MemDataSource dataSource, String suffix, String resource) throws IOException {
        try (InputStream actual = dataSource.newInputStream(dataSource.getMainFileName() + suffix)) {
            compareTxt(getClass().getResourceAsStream("/" + resource), actual);
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

        MemDataSource dataSource = new MemDataSource("test");
        AmplExporter exporter = new AmplExporter();
        exporter.export(network, new Properties(), dataSource);

        assertEqualsToRef(dataSource, "_network_substations.txt", "inputs/eurostag-tutorial-example1-substations.txt");
        assertEqualsToRef(dataSource, "_network_buses.txt", "inputs/eurostag-tutorial-example1-buses.txt");
        assertEqualsToRef(dataSource, "_network_tct.txt", "inputs/eurostag-tutorial-example1-tct.txt");
        assertEqualsToRef(dataSource, "_network_rtc.txt", "inputs/eurostag-tutorial-example1-rtc.txt");
        assertEqualsToRef(dataSource, "_network_ptc.txt", "inputs/eurostag-tutorial-example1-ptc.txt");
        assertEqualsToRef(dataSource, "_network_loads.txt", "inputs/eurostag-tutorial-example1-loads.txt");
        assertEqualsToRef(dataSource, "_network_shunts.txt", "inputs/eurostag-tutorial-example1-shunts.txt");
        assertEqualsToRef(dataSource, "_network_generators.txt", "inputs/eurostag-tutorial-example1-generators.txt");
        assertEqualsToRef(dataSource, "_network_limits.txt", "inputs/eurostag-tutorial-example1-limits.txt");
    }

    @Test
    public void writeLcc() throws IOException {
        Network network = HvdcTestNetwork.createLcc();

        MemDataSource dataSource = new MemDataSource("test");
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_hvdc.txt", "inputs/hvdc-lcc-test-case.txt");
        assertEqualsToRef(dataSource, "_network_lcc_converter_stations.txt", "inputs/lcc-test-case.txt");

    }

    @Test
    public void writePhaseTapChanger() throws IOException {
        Network network = PhaseShifterTestCaseFactory.create();

        MemDataSource dataSource = new MemDataSource("test");
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_ptc.txt", "inputs/ptc-test-case.txt");
    }

    @Test
    public void writeSVC() throws IOException {
        Network network = SvcTestCaseFactory.create();

        MemDataSource dataSource = new MemDataSource("test");
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_static_var_compensators.txt", "inputs/svc-test-case.txt");
    }

    @Test
    public void writeThreeWindingsTransformer() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits();

        MemDataSource dataSource = new MemDataSource("test");
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_branches.txt", "inputs/three-windings-transformers-branches.txt");
        assertEqualsToRef(dataSource, "_network_buses.txt", "inputs/three-windings-transformers-buses.txt");
        assertEqualsToRef(dataSource, "_network_rtc.txt", "inputs/three-windings-transformers-rtc.txt");
        assertEqualsToRef(dataSource, "_network_substations.txt", "inputs/three-windings-transformers-substations.txt");
        assertEqualsToRef(dataSource, "_network_tct.txt", "inputs/three-windings-transformers-tct.txt");
        assertEqualsToRef(dataSource, "_network_limits.txt", "inputs/three-windings-transformers-limits.txt");
    }

    @Test
    public void writeVsc() throws IOException {
        Network network = HvdcTestNetwork.createVsc();

        MemDataSource dataSource = new MemDataSource("test");
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_hvdc.txt", "inputs/hvdc-vsc-test-case.txt");
        assertEqualsToRef(dataSource, "_network_vsc_converter_stations.txt", "inputs/vsc-test-case.txt");

    }

    @Test
    public void writeCurrentLimits() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithCurrentLimits();

        MemDataSource dataSource = new MemDataSource("test");
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_limits.txt", "inputs/current-limits-test-case.txt");
    }

    @Test
    public void writeDanglingLines() throws IOException {
        Network network = DanglingLineNetworkFactory.create();

        MemDataSource dataSource = new MemDataSource("test");
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_network_branches.txt", "inputs/dangling-line-branches.txt");
        assertEqualsToRef(dataSource, "_network_buses.txt", "inputs/dangling-line-buses.txt");
        assertEqualsToRef(dataSource, "_network_limits.txt", "inputs/dangling-line-limits.txt");
        assertEqualsToRef(dataSource, "_network_loads.txt", "inputs/dangling-line-loads.txt");
        assertEqualsToRef(dataSource, "_network_substations.txt", "inputs/dangling-line-substations.txt");
    }

    @Test
    public void writeExtensions() throws IOException {
        Network network = HvdcTestNetwork.createLcc();
        HvdcLine l = network.getHvdcLine("L");
        l.addExtension(FooExtension.class, new FooExtension());
        MemDataSource dataSource = new MemDataSource("test");
        export(network, dataSource);

        assertEqualsToRef(dataSource, "_foo-extension.txt", "inputs/foo-extension.txt");
    }

    private static void export(Network network, DataSource dataSource) {
        AmplExporter exporter = new AmplExporter();
        exporter.export(network, new Properties(), dataSource);
    }
}
