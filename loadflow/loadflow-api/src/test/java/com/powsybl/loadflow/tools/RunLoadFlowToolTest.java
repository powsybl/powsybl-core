/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.tools;

import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.test.AbstractToolTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class RunLoadFlowToolTest extends AbstractToolTest {

    private final RunLoadFlowTool tool = new RunLoadFlowTool();

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new RunLoadFlowTool());
    }

    @Test
    void printLoadFlowResultTest() throws IOException {
        LoadFlowResult result = new LoadFlowResultImpl(true, Collections.emptyMap(), "",
                List.of(
                    new LoadFlowResultImpl.ComponentResultImpl(0, 0,
                            LoadFlowResult.ComponentResult.Status.CONVERGED, "Success", Collections.emptyMap(), 8,
                            "myRefBus", List.of(new LoadFlowResultImpl.SlackBusResultImpl("mySlack", 0.01)),
                            300.45),
                    new LoadFlowResultImpl.ComponentResultImpl(1, 1,
                            LoadFlowResult.ComponentResult.Status.MAX_ITERATION_REACHED, "Newton-Raphson Iteration Limit",
                            Collections.emptyMap(), 40,
                            "myOtherRefBus", List.of(
                                new LoadFlowResultImpl.SlackBusResultImpl("mySlack1", 12.34),
                                new LoadFlowResultImpl.SlackBusResultImpl("mySlack2", 45.67)
                            ),
                            2678.22)
                )
        );
        try (StringWriter writer = new StringWriter()) {
            RunLoadFlowTool.printLoadFlowResult(result, writer, new AsciiTableFormatterFactory(), new TableFormatterConfig(Locale.US, "inv"));
            writer.flush();
            assertTxtEquals(getClass().getResourceAsStream("/LoadFlowResultResult.txt"), writer.toString());
        }
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
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/SupportedLoadFlowParameters.json")),
                fileSystem.getPath("supportedParameters.json"));
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/UnsupportedLoadFlowParameters.json")),
                fileSystem.getPath("unsupportedParameters.json"));
    }

    @Test
    void testLoadflow() {
        String expectedOut = String.join(System.lineSeparator(),
                "Loading network 'network.xiidm'",
                "+ Loadflow tool",
                "   testLoadflow",
                "Loadflow results:",
                "+------+--------+---------+",
                "| Ok   | Status | Metrics |",
                "+------+--------+---------+",
                "| true | FAILED | {}      |",
                "+------+--------+---------+" + System.lineSeparator());
        assertCommandSuccessful(new String[]{"loadflow", "--case-file", "network.xiidm", "--parameters-file", "supportedParameters.json"}, expectedOut);
    }

    @Test
    void testLoadflowWithOutputFile() throws IOException {
        String expectedOut = String.join(System.lineSeparator(),
                "Loading network 'network.xiidm'",
                "+ Loadflow tool",
                "   testLoadflow",
                "Writing results to 'outputTest.json'" + System.lineSeparator());
        String expectedOutputFile = """
                        {
                          "version" : "1.4",
                          "isOK" : true,
                          "metrics" : { }
                        }""";
        assertCommandSuccessful(new String[]{"loadflow", "--case-file", "network.xiidm", "--parameters-file", "supportedParameters.json", "--output-file", "outputTest.json", "--output-format", "JSON"}, expectedOut);
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
        String expectedOutputFile = "+ Loadflow tool\n   testLoadflow\n";
        assertCommandSuccessful(new String[]{"loadflow", "--case-file", "network.xiidm", "--parameters-file", "supportedParameters.json", "--output-log-file", "outputTest.log"}, expectedOut);
        ComparisonUtils.assertTxtEquals(expectedOutputFile, Files.newInputStream(fileSystem.getPath("outputTest.log")));
    }

    @Test
    void testUnsupportedParametersLoadflow() {
        String expectedOut = String.join(System.lineSeparator(),
                "Loading network 'network.xiidm'",
                "+ Loadflow tool",
                "   testParametersNotSupported",
                "Unsupported parameters found, LoadFlowMock cannot launch the loadflow" + System.lineSeparator());
        assertCommandSuccessful(new String[]{"loadflow", "--case-file", "network.xiidm", "--parameters-file", "unsupportedParameters.json"}, expectedOut);
    }

    @Test
    void testUnsupportedParametersLoadflowWithOutputLogFile() throws IOException {
        String expectedOut = String.join(System.lineSeparator(),
                "Loading network 'network.xiidm'",
                "Writing logs to 'outputTest.log'",
                "Unsupported parameters found, LoadFlowMock cannot launch the loadflow" + System.lineSeparator());
        String expectedOutputFile = "+ Loadflow tool\n   testParametersNotSupported\n";
        assertCommandSuccessful(new String[]{"loadflow", "--case-file", "network.xiidm", "--parameters-file", "unsupportedParameters.json", "--output-log-file", "outputTest.log"}, expectedOut);
        ComparisonUtils.assertTxtEquals(expectedOutputFile, Files.newInputStream(fileSystem.getPath("outputTest.log")));
    }
}
