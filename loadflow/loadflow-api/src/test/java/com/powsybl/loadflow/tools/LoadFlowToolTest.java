/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.tools;

import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.test.AbstractToolTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class LoadFlowToolTest extends AbstractToolTest {

    private final RunLoadFlowTool tool = new RunLoadFlowTool();

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new RunLoadFlowTool());
    }

    @Override
    @Test
    public void assertCommand() {
        Command command = tool.getCommand();

        assertCommand(command, "loadflow", 11, 1);
        assertEquals("Computation", command.getTheme());
        assertEquals("Run loadflow", command.getDescription());
        assertNull(command.getUsageFooter());
        assertOption(command.getOptions(), "case-file", true, true);
        assertOption(command.getOptions(), "parameters-file", false, true);
        assertOption(command.getOptions(), "output-file", false, true);
        assertOption(command.getOptions(), "output-format", false, true);
        assertOption(command.getOptions(), "output-case-format", false, true);
        assertOption(command.getOptions(), "output-case-file", false, true);
        assertOption(command.getOptions(), "output-log-file", false, true);
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // create network
        Network network = EurostagTutorialExample1Factory.create();
        NetworkSerDe.write(network, fileSystem.getPath("network.xiidm"));
    }

    @Test
    void testLoadflow() {
        String expectedOut = String.join(System.lineSeparator(),
                "Loading network 'network.xiidm'",
                "Loadflow tool",
                "Loadflow results:",
                "+------+--------+---------+",
                "| Ok   | Status | Metrics |",
                "+------+--------+---------+",
                "| true | FAILED | {}      |",
                "+------+--------+---------+" + System.lineSeparator());
        assertCommandSuccessful(new String[]{"loadflow", "--case-file", "network.xiidm"}, expectedOut);
    }

    @Test
    void testLoadflowWithOutputFile() throws IOException {
        String expectedOut = String.join(System.lineSeparator(),
                "Loading network 'network.xiidm'",
                "Loadflow tool",
                "Writing results to 'outputTest.json'" + System.lineSeparator());
        String expectedOutputFile = """
                        {
                          "version" : "1.4",
                          "isOK" : true,
                          "metrics" : { }
                        }""";
        assertCommandSuccessful(new String[]{"loadflow", "--case-file", "network.xiidm", "--output-file", "outputTest.json",
        "--output-format", "JSON"}, expectedOut);
        ComparisonUtils.assertTxtEquals(expectedOutputFile, Files.newInputStream(fileSystem.getPath("outputTest.json")));
    }

    @Test
    void testLoadflowWithOutputLogFile() throws IOException {
        String expectedOut = String.join(System.lineSeparator(),
                "Loading network 'network.xiidm'",
                "Writing logs to 'outputTest.log'",
                "Loadflow results:",
                "+------+--------+---------+",
                "| Ok   | Status | Metrics |",
                "+------+--------+---------+",
                "| true | FAILED | {}      |",
                "+------+--------+---------+" + System.lineSeparator());
        String expectedOutputFile = "Loadflow tool\n";
        assertCommandSuccessful(new String[]{"loadflow", "--case-file", "network.xiidm", "--output-log-file", "outputTest.log"}, expectedOut);
        ComparisonUtils.assertTxtEquals(expectedOutputFile, Files.newInputStream(fileSystem.getPath("outputTest.log")));
    }
}
