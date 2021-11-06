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

import static com.powsybl.sensitivity.SensitivityFunctionType.*;
import static com.powsybl.sensitivity.SensitivityVariableType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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
        List<SensitivityFactor> factors = List.of(new SensitivityFactor(BRANCH_ACTIVE_POWER, "NHV1_NHV2_1", INJECTION_ACTIVE_POWER, "GEN", false, ContingencyContext.all()),
                                                  new SensitivityFactor(BRANCH_ACTIVE_POWER, "NHV1_NHV2_1", INJECTION_ACTIVE_POWER, "glsk", true, ContingencyContext.specificContingency("NHV1_NHV2_2")));
        try (Writer writer = Files.newBufferedWriter(fileSystem.getPath("factors.json"), StandardCharsets.UTF_8)) {
            objectMapper.writeValue(writer, factors);
        }

        // create contingencies
        ContingencyList contingencyList = new DefaultContingencyList("one contingency list", List.of(new Contingency("NHV1_NHV2_2", new BranchContingency("NHV1_NHV2_2"))));
        try (Writer writer = Files.newBufferedWriter(fileSystem.getPath("contingencies.json"), StandardCharsets.UTF_8)) {
            objectMapper.writeValue(writer, contingencyList);
        }

        List<SensitivityVariableSet> variableSets = Collections.emptyList();
        try (Writer writer = Files.newBufferedWriter(fileSystem.getPath("variableSets.json"), StandardCharsets.UTF_8)) {
            objectMapper.writeValue(writer, variableSets);
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
            "--variable-sets-file", "variableSets.json",
            "--parameters-file", "parameters.json",
            "--output-file", "output.json"},
                CommandLineTools.COMMAND_OK_STATUS, expectedOut, "");

        assertTrue(Files.exists(fileSystem.getPath("output.json")));
        List<SensitivityValue> values;
        try (Reader reader = Files.newBufferedReader(fileSystem.getPath("output.json"))) {
            values = objectMapper.readValue(reader, new TypeReference<>() {
            });
        }
        assertEquals(2, values.size());
        SensitivityValue value0 = values.get(0);
        assertEquals(0, value0.getFactorIndex());
        assertEquals(0, value0.getContingencyIndex());
        SensitivityValue value1 = values.get(1);
        assertEquals(1, value1.getFactorIndex());
        assertEquals(0, value1.getContingencyIndex());
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
        String outputCsvRef = String.join(System.lineSeparator(),
                "Contingency ID,Factor index,Function ref value,Sensitivity value",
                "NHV1_NHV2_2,0,0.00000,0.00000",
                "NHV1_NHV2_2,1,0.00000,0.00000")
                + System.lineSeparator();
        assertEquals(outputCsvRef, Files.readString(outputCsvFile));
    }

    @Test
    public void checkFailsWhenNetworkFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME,
            "--case-file", "wrongFile.xiidm",
            "--factors-file", "factors.json",
            "--output-file", "output.csv"},
                3, null, "com.powsybl.commons.PowsyblException: File wrongFile.xiidm does not exist or is not a regular file");
    }

    @Test
    public void checkFailsWhenFactorsFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME,
            "--case-file", "network.xiidm",
            "--factors-file", "wrongFile.json",
            "--output-file", "output.csv"},
                3, null, "java.nio.file.NoSuchFileException: wrongFile.json");
    }

    @Test
    public void checkThrowsWhenOutputFileAndNoFormat() throws IOException {
        assertCommand(new String[] {COMMAND_NAME,
            "--case-file", "network.xiidm",
            "--factors-file", "factors.json",
            "--output-file", "output.txt"},
                3, "", "Unsupported output format: output.txt");
    }

    @Test
    public void checkCommand() {
        assertEquals("sensitivity-analysis", tool.getCommand().getName());
        assertEquals("Computation", tool.getCommand().getTheme());
        assertEquals("Run sensitivity analysis", tool.getCommand().getDescription());
    }
}
