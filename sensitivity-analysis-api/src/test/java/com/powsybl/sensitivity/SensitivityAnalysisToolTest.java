/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.contingency.*;
import com.powsybl.contingency.json.ContingencyJsonModule;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.sensitivity.json.SensitivityJsonModule;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.Tool;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class SensitivityAnalysisToolTest extends AbstractToolTest {

    private static final String COMMAND_NAME = "sensitivity-analysis";

    private final SensitivityAnalysisTool tool = new SensitivityAnalysisTool();

    private ObjectMapper objectMapper;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // create network
        Network network = EurostagTutorialExample1Factory.create();
        NetworkXml.write(network, fileSystem.getPath("network.xiidm"));

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SensitivityJsonModule())
                .registerModule(new ContingencyJsonModule());

        // create factors
        List<SensitivityFactor> factors = List.of(new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER, "NHV1_NHV2_1", SensitivityVariableType.INJECTION_ACTIVE_POWER, "GEN", false, ContingencyContext.all()));
        try (Writer writer = Files.newBufferedWriter(fileSystem.getPath("factors.json"), StandardCharsets.UTF_8)) {
            objectMapper.writeValue(writer, factors);
        }

        // create contingencies
        ContingencyList contingencyList = new DefaultContingencyList("one contingency list", List.of(new Contingency("NHV1_NHV2_2", new BranchContingency("NHV1_NHV2_2"))));
        try (Writer writer = Files.newBufferedWriter(fileSystem.getPath("contingencies.json"), StandardCharsets.UTF_8)) {
            objectMapper.writeValue(writer, contingencyList);
        }

        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        try (Writer writer = Files.newBufferedWriter(fileSystem.getPath("parameters.json"), StandardCharsets.UTF_8)) {
            objectMapper.writeValue(writer, parameters);
        }
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    public void assertCommand() {
        assertCommand(tool.getCommand(), COMMAND_NAME, 8, 3);
        assertOption(tool.getCommand().getOptions(), "case-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-file", true, true);
        assertOption(tool.getCommand().getOptions(), "factors-file", true, true);
        assertOption(tool.getCommand().getOptions(), "contingencies-file", false, true);
        assertOption(tool.getCommand().getOptions(), "variable-sets-file", false, true);
        assertOption(tool.getCommand().getOptions(), "parameters-file", false, true);
    }

    @Test
    public void runJsonOutput() throws IOException {
        String expectedOut = "Loading network 'network.xiidm'" + System.lineSeparator() +
                "Running analysis..." + System.lineSeparator();
        assertCommand(new String[] {COMMAND_NAME,
            "--case-file", "network.xiidm",
            "--factors-file", "factors.json",
            "--contingencies-file", "contingencies.json",
            "--parameters-file", "parameters.json",
            "--output-file", "output.json"},
                CommandLineTools.COMMAND_OK_STATUS, expectedOut, "");

        assertTrue(Files.exists(fileSystem.getPath("output.json")));
        List<SensitivityValue> values;
        try (Reader reader = Files.newBufferedReader(fileSystem.getPath("output.json"))) {
            values = objectMapper.readValue(reader, new TypeReference<>() {
            });
        }
        assertEquals(1, values.size());
        SensitivityValue value = values.get(0);
//        assertEquals("", value.getFactor().getFunctionId());
    }

    @Test
    public void runCsvOutput() throws IOException {
        String expectedOut = "Loading network 'network.xiidm'" + System.lineSeparator() +
                "Running analysis..." + System.lineSeparator();
        assertCommand(new String[] {COMMAND_NAME,
            "--case-file", "network.xiidm",
            "--factors-file", "factors.json",
            "--contingencies-file", "contingencies.json",
            "--parameters-file", "parameters.json",
            "--output-file", "output.csv"},
                CommandLineTools.COMMAND_OK_STATUS, expectedOut, "");

        Path outputCsvFile = fileSystem.getPath("output.csv");
        assertTrue(Files.exists(outputCsvFile));
        String outputCsvRef = "Contingency ID,Variable ID,Function ID,Function ref value,Sensitivity value" + System.lineSeparator() +
                ",GEN,NHV1_NHV2_1,0.00000,0.00000" + System.lineSeparator();
        assertEquals(outputCsvRef, Files.readString(outputCsvFile));
    }

//
//    @Test
//    public void checkFailsWhenNetworkFileNotFound() throws IOException {
//        assertCommand(new String[] {COMMAND_NAME, "--case-file", "wrongFile.uct", "--factors-file", "test.csv", "--output-file", "output.csv"}, 3, null, "com.powsybl.commons.PowsyblException: File wrongFile.uct does not exist or is not a regular file");
//    }
//
//    @Test
//    public void checkFailsWhenFactorsFileNotFound() throws IOException {
//        assertCommand(new String[] {COMMAND_NAME, "--case-file", "test.uct", "--factors-file", "wrongFile.csv", "--output-file", "output.csv"}, 3, null, "PowsyblException: Unsupported file format or invalid file");
//    }
//
//    @Test
//    public void checkThrowsWhenOutputFileAndNoFormat() throws IOException {
//        assertCommand(new String[] {COMMAND_NAME, "--case-file", "test.uct", "--factors-file", "test.csv", "--output-file", "out.txt"}, 3, "", "Unsupported output format: out.txt");
//    }

    @Test
    public void checkCommand() {
        assertEquals("sensitivity-analysis", tool.getCommand().getName());
        assertEquals("Computation", tool.getCommand().getTheme());
        assertEquals("Run sensitivity analysis", tool.getCommand().getDescription());
    }
}
