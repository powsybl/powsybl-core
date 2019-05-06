/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.computation.CommandExecution;
import com.powsybl.computation.DefaultExecutionReport;
import com.powsybl.computation.ExecutionHandler;
import com.powsybl.computation.SimpleCommand;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.*;
import com.powsybl.security.converter.JsonSecurityAnalysisResultExporter;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;
import org.apache.commons.lang3.SystemUtils;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisExecutionHandlersTest {

    private FileSystem fileSystem;
    private Path workingDir;

    @Before
    public void createFileSystem() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        workingDir = fileSystem.getPath("/work");
    }

    @After
    public void closeFileSystem() throws IOException {
        fileSystem.close();
    }

    @Test
    public void forwardedBeforeWithPartialInput() throws IOException {
        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput();
        input.setParameters(new SecurityAnalysisParameters());
        input.setNetworkVariant(EurostagTutorialExample1Factory.create(), VariantManagerConstants.INITIAL_VARIANT_ID);

        ExecutionHandler<SecurityAnalysisResult> handler = SecurityAnalysisExecutionHandlers.forwarded(input);

        List<CommandExecution> commandExecutions = handler.before(workingDir);

        assertEquals(1, commandExecutions.size());
        CommandExecution commandExecution = commandExecutions.get(0);
        assertEquals(1, commandExecution.getExecutionCount());
        SimpleCommand command = (SimpleCommand) commandExecution.getCommand();
        assertNotNull(command);

        String expectedDefaultProgram = SystemUtils.IS_OS_WINDOWS ? "itools.bat" : "itools";
        assertEquals(expectedDefaultProgram, command.getProgram());
        List<String> args = command.getArgs(0);
        Assertions.assertThat(args).first().isEqualTo("security-analysis");
        Assertions.assertThat(args.subList(1, args.size()))
                .containsExactlyInAnyOrder("--case-file=/work/network.xiidm",
                        "--parameters-file=/work/parameters.json",
                        "--output-file=/work/result.json",
                        "--output-format=JSON");

        Assertions.assertThat(workingDir.resolve("network.xiidm")).exists();
        Assertions.assertThat(workingDir.resolve("parameters.json")).exists();
    }

    @Test
    public void forwardedAfter() throws IOException {

        try (Writer writer = Files.newBufferedWriter(workingDir.resolve("result.json"))) {
            new JsonSecurityAnalysisResultExporter().export(SecurityAnalysisResult.empty(), writer);
        }

        ExecutionHandler<SecurityAnalysisResult> handler = SecurityAnalysisExecutionHandlers.forwarded(new SecurityAnalysisExecutionInput());
        SecurityAnalysisResult result = handler.after(workingDir, DefaultExecutionReport.ok());

        assertNotNull(result);
        assertTrue(result.getPreContingencyResult().isComputationOk());
        assertTrue(result.getPreContingencyResult().getLimitViolations().isEmpty());
        assertTrue(result.getPostContingencyResults().isEmpty());
    }

    @Test
    public void forwardedBeforeWithCompleteInput() throws IOException {
        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput()
                .setParameters(new SecurityAnalysisParameters())
                .setNetworkVariant(EurostagTutorialExample1Factory.create(), VariantManagerConstants.INITIAL_VARIANT_ID)
                .setContingenciesSource(ByteSource.wrap("contingencies definition".getBytes(StandardCharsets.UTF_8)))
                .addResultExtensions(ImmutableList.of("ext1", "ext2"))
                .addViolationTypes(ImmutableList.of(LimitViolationType.CURRENT));
        ExecutionHandler<SecurityAnalysisResult> handler = SecurityAnalysisExecutionHandlers.forwarded(input, 12);

        Path workingDir = fileSystem.getPath("/work");
        List<CommandExecution> commandExecutions = handler.before(workingDir);
        SimpleCommand command = (SimpleCommand) commandExecutions.get(0).getCommand();
        List<String> args = command.getArgs(0);
        Assertions.assertThat(args.subList(1, args.size()))
                .containsExactlyInAnyOrder("--case-file=/work/network.xiidm",
                        "--parameters-file=/work/parameters.json",
                        "--output-file=/work/result.json",
                        "--output-format=JSON",
                        "--contingencies-file=/work/contingencies.groovy",
                        "--with-extensions=ext1,ext2",
                        "--limit-types=CURRENT",
                        "--task-count=12");

        Assertions.assertThat(workingDir.resolve("network.xiidm")).exists();
        Assertions.assertThat(workingDir.resolve("parameters.json")).exists();
        Assertions.assertThat(workingDir.resolve("contingencies.groovy")).exists();
    }

    @Test
    public void distributedBefore() throws IOException {
        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput()
                .setParameters(new SecurityAnalysisParameters())
                .setNetworkVariant(EurostagTutorialExample1Factory.create(), VariantManagerConstants.INITIAL_VARIANT_ID)
                .setContingenciesSource(ByteSource.wrap("contingencies definition".getBytes(StandardCharsets.UTF_8)))
                .addResultExtensions(ImmutableList.of("ext1", "ext2"))
                .addViolationTypes(ImmutableList.of(LimitViolationType.CURRENT));
        ExecutionHandler<SecurityAnalysisResult> handler = SecurityAnalysisExecutionHandlers.distributed(input, 3);

        List<CommandExecution> commandExecutions = handler.before(workingDir);
        SimpleCommand command = (SimpleCommand) commandExecutions.get(0).getCommand();
        List<String> args = command.getArgs(0);
        Assertions.assertThat(command.getArgs(0).subList(1, args.size()))
                .containsExactlyInAnyOrder("--case-file=/work/network.xiidm",
                        "--parameters-file=/work/parameters.json",
                        "--output-file=/work/task_0_result.json",
                        "--output-format=JSON",
                        "--contingencies-file=/work/contingencies.groovy",
                        "--with-extensions=ext1,ext2",
                        "--limit-types=CURRENT",
                        "--task=1/3");

        Assertions.assertThat(command.getArgs(1).subList(1, args.size()))
                .containsExactlyInAnyOrder("--case-file=/work/network.xiidm",
                        "--parameters-file=/work/parameters.json",
                        "--output-file=/work/task_1_result.json",
                        "--output-format=JSON",
                        "--contingencies-file=/work/contingencies.groovy",
                        "--with-extensions=ext1,ext2",
                        "--limit-types=CURRENT",
                        "--task=2/3");
    }

    @Test
    public void distributedBeforeWithLog() throws IOException {
        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput()
                .setParameters(new SecurityAnalysisParameters())
                .setNetworkVariant(EurostagTutorialExample1Factory.create(), VariantManagerConstants.INITIAL_VARIANT_ID)
                .setContingenciesSource(ByteSource.wrap("contingencies definition".getBytes(StandardCharsets.UTF_8)));
        ExecutionHandler<SecurityAnalysisResultWithLog> handler = SecurityAnalysisExecutionHandlers.distributedWithLog(input, 3);

        List<CommandExecution> commandExecutions = handler.before(workingDir);
        SimpleCommand command = (SimpleCommand) commandExecutions.get(0).getCommand();
        List<String> args = command.getArgs(0);
        Assertions.assertThat(command.getArgs(0).subList(1, args.size()))
                .containsExactlyInAnyOrder("--case-file=/work/network.xiidm",
                        "--parameters-file=/work/parameters.json",
                        "--output-file=/work/task_0_result.json",
                        "--output-format=JSON",
                        "--contingencies-file=/work/contingencies.groovy",
                        "--task=1/3",
                        "--log-file=/work/logs_0.zip");

        Assertions.assertThat(command.getArgs(1).subList(1, args.size()))
                .containsExactlyInAnyOrder("--case-file=/work/network.xiidm",
                        "--parameters-file=/work/parameters.json",
                        "--output-file=/work/task_1_result.json",
                        "--output-format=JSON",
                        "--contingencies-file=/work/contingencies.groovy",
                        "--task=2/3",
                        "--log-file=/work/logs_1.zip");
    }

    @Test
    public void forwardedBeforeWithLog() throws IOException {
        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput()
                .setParameters(new SecurityAnalysisParameters())
                .setNetworkVariant(EurostagTutorialExample1Factory.create(), VariantManagerConstants.INITIAL_VARIANT_ID)
                .setContingenciesSource(ByteSource.wrap("contingencies definition".getBytes(StandardCharsets.UTF_8)));
        ExecutionHandler<SecurityAnalysisResultWithLog> handler = SecurityAnalysisExecutionHandlers.forwardedWithLogs(input);

        List<CommandExecution> commandExecutions = handler.before(workingDir);
        SimpleCommand command = (SimpleCommand) commandExecutions.get(0).getCommand();
        List<String> args = command.getArgs(0);
        Assertions.assertThat(command.getArgs(0).subList(1, args.size()))
                .containsExactlyInAnyOrder("--case-file=/work/network.xiidm",
                        "--parameters-file=/work/parameters.json",
                        "--output-file=/work/result.json",
                        "--output-format=JSON",
                        "--contingencies-file=/work/contingencies.groovy",
                        "--log-file=/work/logs.zip");
    }

    private static SecurityAnalysisResult resultForContingency(String id) {
        return new SecurityAnalysisResult(LimitViolationsResult.empty(),
                Collections.singletonList(new PostContingencyResult(new Contingency(id),
                        LimitViolationsResult.empty())));
    }

    @Test
    public void distributedAfter() throws IOException {
        JsonSecurityAnalysisResultExporter exporter = new JsonSecurityAnalysisResultExporter();
        try (Writer writer = Files.newBufferedWriter(workingDir.resolve("task_0_result.json"))) {
            exporter.export(resultForContingency("c1"), writer);
        }

        try (Writer writer = Files.newBufferedWriter(workingDir.resolve("task_1_result.json"))) {
            exporter.export(resultForContingency("c2"), writer);
        }

        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput();
        ExecutionHandler<SecurityAnalysisResult> handler = SecurityAnalysisExecutionHandlers.distributed(input, 2);

        SecurityAnalysisResult result = handler.after(workingDir, new DefaultExecutionReport());

        assertNotNull(result);
        assertTrue(result.getPreContingencyResult().isComputationOk());
        assertTrue(result.getPreContingencyResult().getLimitViolations().isEmpty());
        assertEquals(2, result.getPostContingencyResults().size());
        assertEquals("c1", result.getPostContingencyResults().get(0).getContingency().getId());
        assertEquals("c2", result.getPostContingencyResults().get(1).getContingency().getId());
    }

    private static Set<String> getFileNamesFromZip(byte[] bytes) throws IOException {
        Set<String> foundNames = new HashSet<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {

            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                foundNames.add(entry.getName());
                entry = zip.getNextEntry();
            }
        }
        return foundNames;
    }

    @Test
    public void distributedAfterWithLogs() throws IOException {
        JsonSecurityAnalysisResultExporter exporter = new JsonSecurityAnalysisResultExporter();
        try (Writer writer = Files.newBufferedWriter(workingDir.resolve("task_0_result.json"))) {
            exporter.export(resultForContingency("c1"), writer);
        }

        try (Writer writer = Files.newBufferedWriter(workingDir.resolve("task_1_result.json"))) {
            exporter.export(resultForContingency("c2"), writer);
        }

        Set<String> expectedLogs = ImmutableSet.of("logs_0.zip",
                "security-analysis-task_0.out",
                "security-analysis-task_0.err",
                "logs_1.zip",
                "security-analysis-task_1.out",
                "security-analysis-task_1.err");
        for (String logFileName : expectedLogs) {
            Files.write(workingDir.resolve(logFileName), "logs".getBytes(StandardCharsets.UTF_8));
        }

        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput();
        ExecutionHandler<SecurityAnalysisResultWithLog> handler = SecurityAnalysisExecutionHandlers.distributedWithLog(input, 2);

        SecurityAnalysisResultWithLog resultWithLog = handler.after(workingDir, new DefaultExecutionReport());
        SecurityAnalysisResult result = resultWithLog.getResult();

        assertNotNull(result);
        assertTrue(result.getPreContingencyResult().isComputationOk());
        assertTrue(result.getPreContingencyResult().getLimitViolations().isEmpty());
        assertEquals(2, result.getPostContingencyResults().size());
        assertEquals("c1", result.getPostContingencyResults().get(0).getContingency().getId());
        assertEquals("c2", result.getPostContingencyResults().get(1).getContingency().getId());

        byte[] logBytes = resultWithLog.getLogBytes()
                .orElseThrow(AssertionError::new);
        Set<String> foundNames = getFileNamesFromZip(logBytes);
        assertEquals(expectedLogs, foundNames);
    }

    @Test
    public void forwardedAfterWithLogs() throws IOException {
        JsonSecurityAnalysisResultExporter exporter = new JsonSecurityAnalysisResultExporter();
        try (Writer writer = Files.newBufferedWriter(workingDir.resolve("result.json"))) {
            exporter.export(resultForContingency("c1"), writer);
        }

        Set<String> expectedLogs = ImmutableSet.of("logs.zip",
                "security-analysis.out",
                "security-analysis.err");

        for (String logFileName : expectedLogs) {
            Files.write(workingDir.resolve(logFileName), "logs".getBytes(StandardCharsets.UTF_8));
        }

        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput();
        ExecutionHandler<SecurityAnalysisResultWithLog> handler = SecurityAnalysisExecutionHandlers.forwardedWithLogs(input, 2);

        SecurityAnalysisResultWithLog resultWithLog = handler.after(workingDir, new DefaultExecutionReport());
        SecurityAnalysisResult result = resultWithLog.getResult();

        assertNotNull(result);
        assertTrue(result.getPreContingencyResult().isComputationOk());
        assertTrue(result.getPreContingencyResult().getLimitViolations().isEmpty());
        assertEquals(1, result.getPostContingencyResults().size());
        assertEquals("c1", result.getPostContingencyResults().get(0).getContingency().getId());

        assertTrue(resultWithLog.getLogBytes().isPresent());

        byte[] logBytes = resultWithLog.getLogBytes()
                .orElseThrow(AssertionError::new);
        Set<String> foundNames = getFileNamesFromZip(logBytes);
        assertEquals(expectedLogs, foundNames);
    }

}
