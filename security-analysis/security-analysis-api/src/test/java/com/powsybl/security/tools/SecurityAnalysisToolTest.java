/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.tools;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.ComputationException;
import com.powsybl.computation.ComputationExceptionBuilder;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.import_.ImportersLoaderList;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.execution.SecurityAnalysisExecutionBuilder;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessor;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessorFactory;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class SecurityAnalysisToolTest extends AbstractToolTest {

    private static final String OUTPUT_LOG_FILENAME = "out.zip";

    private SecurityAnalysisTool tool;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        tool = new SecurityAnalysisTool();
        Files.createFile(fileSystem.getPath("network.xml"));
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    public void assertCommand() {
        assertCommand(tool.getCommand(), "security-analysis", 13, 1);
        assertOption(tool.getCommand().getOptions(), "case-file", true, true);
        assertOption(tool.getCommand().getOptions(), "parameters-file", false, true);
        assertOption(tool.getCommand().getOptions(), "limit-types", false, true);
        assertOption(tool.getCommand().getOptions(), "output-file", false, true);
        assertOption(tool.getCommand().getOptions(), "output-format", false, true);
        assertOption(tool.getCommand().getOptions(), "contingencies-file", false, true);
        assertOption(tool.getCommand().getOptions(), "with-extensions", false, true);
        assertOption(tool.getCommand().getOptions(), "task-count", false, true);
        assertOption(tool.getCommand().getOptions(), "task", false, true);
        assertOption(tool.getCommand().getOptions(), "external", false, false);
        assertOption(tool.getCommand().getOptions(), "log-file", false, true);
    }

    @Test
    public void test() {
        assertCommand();
    }

    private static CommandLine mockCommandLine(Map<String, String> options, Set<String> flags) {
        CommandLine cli =  mock(CommandLine.class);
        when(cli.hasOption(any())).thenReturn(false);
        when(cli.getOptionValue(any())).thenReturn(null);
        options.forEach((k, v) -> {
            when(cli.getOptionValue(k)).thenReturn(v);
            when(cli.hasOption(k)).thenReturn(true);
        });
        flags.forEach(f -> when(cli.hasOption(f)).thenReturn(true));
        when(cli.getOptionProperties(any())).thenReturn(new Properties());
        return cli;
    }

    private ToolOptions emptyOptions() {
        return mockOptions(Collections.emptyMap());
    }

    private ToolOptions mockOptions(Map<String, String> options) {
        return mockOptions(options, Collections.emptySet());
    }

    private ToolOptions mockOptions(Map<String, String> options, Set<String> flags) {
        return new ToolOptions(mockCommandLine(options, flags), fileSystem);
    }

    @Test
    public void parseInputs() throws IOException {
        ToolOptions options = emptyOptions();

        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput();

        SecurityAnalysisTool.updateInput(options, input);
        assertThat(input.getViolationTypes()).isEmpty();
        assertThat(input.getResultExtensions()).isEmpty();
        assertThat(input.getContingenciesSource()).isNotPresent();

        options = mockOptions(ImmutableMap.of(SecurityAnalysisToolConstants.LIMIT_TYPES_OPTION, "HIGH_VOLTAGE,CURRENT"));
        SecurityAnalysisTool.updateInput(options, input);
        assertThat(input.getViolationTypes()).containsExactly(LimitViolationType.CURRENT, LimitViolationType.HIGH_VOLTAGE);

        options = mockOptions(ImmutableMap.of(SecurityAnalysisToolConstants.WITH_EXTENSIONS_OPTION, "ext1,ext2"));
        SecurityAnalysisTool.updateInput(options, input);
        assertThat(input.getResultExtensions()).containsExactly("ext1", "ext2");

        ToolOptions invalidOptions = mockOptions(ImmutableMap.of(SecurityAnalysisToolConstants.CONTINGENCIES_FILE_OPTION, "contingencies"));
        assertThatIllegalArgumentException().isThrownBy(() -> SecurityAnalysisTool.updateInput(invalidOptions, input));

        Files.write(fileSystem.getPath("contingencies"), "test".getBytes());
        options = mockOptions(ImmutableMap.of(SecurityAnalysisToolConstants.CONTINGENCIES_FILE_OPTION, "contingencies"));
        SecurityAnalysisTool.updateInput(options, input);
        assertThat(input.getContingenciesSource()).isPresent();
        if (input.getContingenciesSource().isPresent()) {
            assertEquals("test", new String(input.getContingenciesSource().get().read()));
        } else {
            fail();
        }
    }

    @Test
    public void buildPreprocessedInput() {
        SecurityAnalysisExecutionInput executionInput = new SecurityAnalysisExecutionInput()
                .setNetworkVariant(mock(Network.class), "")
                .setParameters(new SecurityAnalysisParameters());

        SecurityAnalysisPreprocessor preprocessor = mock(SecurityAnalysisPreprocessor.class);
        SecurityAnalysisPreprocessorFactory factory = mock(SecurityAnalysisPreprocessorFactory.class);
        when(factory.newPreprocessor(any())).thenReturn(preprocessor);

        SecurityAnalysisInput input = SecurityAnalysisTool.buildPreprocessedInput(executionInput, LimitViolationFilter::new, factory);

        assertSame(executionInput.getParameters(), input.getParameters());
        assertSame(executionInput.getNetworkVariant(), input.getNetworkVariant());

        verify(factory, times(0)).newPreprocessor(any());

        executionInput.setContingenciesSource(ByteSource.empty());
        SecurityAnalysisTool.buildPreprocessedInput(executionInput, LimitViolationFilter::new, factory);

        verify(factory, times(1)).newPreprocessor(any());
        verify(preprocessor, times(1)).preprocess(any());
    }

    @Test
    public void readNetwork() throws IOException {
        ToolRunningContext context = new ToolRunningContext(mock(PrintStream.class), mock(PrintStream.class), fileSystem,
                mock(ComputationManager.class), mock(ComputationManager.class));

        CommandLine cli = mockCommandLine(ImmutableMap.of("case-file", "network.xml"), Collections.emptySet());
        SecurityAnalysisTool.readNetwork(cli, context, new ImportersLoaderList(new NetworkImporterMock()));
    }

    @Test
    public void testRunWithLog() throws Exception {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ByteArrayOutputStream berr = new ByteArrayOutputStream();
             PrintStream out = new PrintStream(bout);
             PrintStream err = new PrintStream(berr);
             ComputationManager cm = mock(ComputationManager.class)) {
            CommandLine cl = mockCommandLine(ImmutableMap.of("case-file", "network.xml",
                    SecurityAnalysisToolConstants.OUTPUT_LOG_OPTION, OUTPUT_LOG_FILENAME), ImmutableSet.of("skip-postproc"));

            ToolRunningContext context = new ToolRunningContext(out, err, fileSystem, cm, cm);

            SecurityAnalysisExecutionBuilder builderRunWithLog = new SecurityAnalysisExecutionBuilder(ExternalSecurityAnalysisConfig::new,
                    "SecurityAnalysisToolProviderMock",
                executionInput -> new SecurityAnalysisInput(executionInput.getNetworkVariant().getNetwork(), "runWithLog"));

            // Invoked methods run() & runWithLog() now are controlled by variantId
            // run() should run only when variantId 'run'
            // runWithLog() should run only when variantId 'runWithLog'

            // Check runWithLog execution
            tool.run(cl, context, builderRunWithLog,
                    SecurityAnalysisParameters::new,
                    new ImportersLoaderList(new NetworkImporterMock()),
                    TableFormatterConfig::new);
            // Check log-file creation
            Path logPath = context.getFileSystem().getPath(OUTPUT_LOG_FILENAME);
            assertTrue(Files.exists(logPath));
            // Need to clean for next test
            Files.delete(logPath);

            // Check run execution
            when(cl.hasOption("log-file")).thenReturn(false);
            SecurityAnalysisExecutionBuilder builderRun = new SecurityAnalysisExecutionBuilder(ExternalSecurityAnalysisConfig::new,
                "SecurityAnalysisToolProviderMock",
                executionInput -> new SecurityAnalysisInput(executionInput.getNetworkVariant()));

            tool.run(cl, context, builderRun,
                    SecurityAnalysisParameters::new,
                    new ImportersLoaderList(new NetworkImporterMock()),
                    TableFormatterConfig::new);

            // Check no log-file creation
            assertFalse(Files.exists(logPath));

            // exception happens
            SecurityAnalysisExecutionBuilder builderException = new SecurityAnalysisExecutionBuilder(ExternalSecurityAnalysisConfig::new,
                "SecurityAnalysisToolExceptionProviderMock",
                executionInput -> new SecurityAnalysisInput(executionInput.getNetworkVariant()));
            try {
                tool.run(cl, context, builderException,
                        SecurityAnalysisParameters::new,
                        new ImportersLoaderList(new NetworkImporterMock()),
                        TableFormatterConfig::new);
                fail();
            } catch (CompletionException exception) {
                assertTrue(exception.getCause() instanceof ComputationException);
                assertEquals("outLog", ((ComputationException) exception.getCause()).getOutLogs().get("out"));
                assertEquals("errLog", ((ComputationException) exception.getCause()).getErrLogs().get("err"));
            }
        }
    }

    @AutoService(SecurityAnalysisProvider.class)
    public static class SecurityAnalysisProviderMock implements SecurityAnalysisProvider {
        @Override
        public CompletableFuture<SecurityAnalysisResult> run(Network network, String workingVariantId, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors) {
            CompletableFuture<SecurityAnalysisResult> cfSar = mock(CompletableFuture.class);
            SecurityAnalysisResult result = mock(SecurityAnalysisResult.class);
            when(result.getPreContingencyResult()).thenReturn(mock(LimitViolationsResult.class));
            when(cfSar.join()).thenReturn(result);
            return cfSar;
        }

        @Override
        public CompletableFuture<SecurityAnalysisResult> runWithLog(Network network, String workingVariantId, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors) {
            CompletableFuture<SecurityAnalysisResult> cfSar = mock(CompletableFuture.class);
            SecurityAnalysisResult result = mock(SecurityAnalysisResult.class);
            when(result.getPreContingencyResult()).thenReturn(mock(LimitViolationsResult.class));
            when(result.getLogBytes()).thenReturn(Optional.of("Hello world".getBytes()));
            when(cfSar.join()).thenReturn(result);
            return cfSar;
        }

        @Override
        public String getName() {
            return "SecurityAnalysisToolProviderMock";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }
    }

    @AutoService(SecurityAnalysisProvider.class)
    public static class SecurityAnalysisExceptionProviderMock implements SecurityAnalysisProvider {
        @Override
        public CompletableFuture<SecurityAnalysisResult> run(Network network, String workingVariantId, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors) {
            ComputationExceptionBuilder ceb = new ComputationExceptionBuilder(new RuntimeException("test"));
            ceb.addOutLog("out", "outLog")
                    .addErrLog("err", "errLog");
            ComputationException computationException = ceb.build();
            throw new CompletionException(computationException);
        }

        @Override
        public String getName() {
            return "SecurityAnalysisToolExceptionProviderMock";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }
    }
}
