/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.google.common.io.CharStreams;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AmplNetworkWriterTest {

    private void assertEqualsToRef(MemDataSource dataSource, String suffix, String refFileName) throws IOException {
        assertEquals(CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/" + refFileName), StandardCharsets.UTF_8)),
                     new String(dataSource.getData(suffix, "txt"), StandardCharsets.UTF_8));
    }

    @Test
    public void writeEurostag() throws Exception {
        Network network = EurostagTutorialExample1Factory.create();

        MemDataSource dataSource = new MemDataSource();
        new AmplNetworkWriter(network, dataSource, new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true, AmplExportConfig.ExportActionType.CURATIVE))
                .write();

        assertEqualsToRef(dataSource, "_network_substations", "eurostag-tutorial-example1-substations.txt");
        assertEqualsToRef(dataSource, "_network_buses", "eurostag-tutorial-example1-buses.txt");
        assertEqualsToRef(dataSource, "_network_tct", "eurostag-tutorial-example1-tct.txt");
        assertEqualsToRef(dataSource, "_network_rtc", "eurostag-tutorial-example1-rtc.txt");
        assertEqualsToRef(dataSource, "_network_ptc", "eurostag-tutorial-example1-ptc.txt");
        assertEqualsToRef(dataSource, "_network_loads", "eurostag-tutorial-example1-loads.txt");
        assertEqualsToRef(dataSource, "_network_shunts", "eurostag-tutorial-example1-shunts.txt");
        assertEqualsToRef(dataSource, "_network_generators", "eurostag-tutorial-example1-generators.txt");
        assertEqualsToRef(dataSource, "_network_limits", "eurostag-tutorial-example1-limits.txt");
    }

    @Test
    public void writeSVC() throws Exception {
        Network network = SvcTestCaseFactory.create();

        MemDataSource dataSource = new MemDataSource();
        new AmplNetworkWriter(network, dataSource, new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true, AmplExportConfig.ExportActionType.CURATIVE))
                .write();

        assertEqualsToRef(dataSource, "_network_static_var_compensators", "svc-test-case.txt");
    }

    @Test
    public void writeLcc() throws Exception {
        Network network = HvdcTestNetwork.createLcc();

        MemDataSource dataSource = new MemDataSource();
        new AmplNetworkWriter(network, dataSource, new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true, AmplExportConfig.ExportActionType.CURATIVE))
                .write();

        assertEqualsToRef(dataSource, "_network_hvdc", "lcc-test-case.txt");
    }

    @Test
    public void writeVsc() throws Exception {
        Network network = HvdcTestNetwork.createVsc();

        MemDataSource dataSource = new MemDataSource();
        new AmplNetworkWriter(network, dataSource, new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true, AmplExportConfig.ExportActionType.CURATIVE))
                .write();

        assertEqualsToRef(dataSource, "_network_hvdc", "vsc-test-case.txt");
    }
}
