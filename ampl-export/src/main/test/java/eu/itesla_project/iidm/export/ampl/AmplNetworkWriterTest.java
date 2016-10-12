/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.export.ampl;

import com.google.common.io.CharStreams;
import eu.itesla_project.iidm.datasource.MemDataSource;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AmplNetworkWriterTest {

    private void assertEqualsToRef(MemDataSource dataSource, String suffix, String refFileName) throws IOException {
        assertEquals(new String(dataSource.getData(suffix, "txt"), StandardCharsets.UTF_8),
                CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/" + refFileName), StandardCharsets.UTF_8)));
    }

    @Test
    public void write() throws Exception {
        Network network = EurostagTutorialExample1Factory.create();

        MemDataSource dataSource = new MemDataSource();
        new AmplNetworkWriter(network, dataSource, new AmplExportConfig(AmplExportConfig.ExportScope.ALL, true))
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

}