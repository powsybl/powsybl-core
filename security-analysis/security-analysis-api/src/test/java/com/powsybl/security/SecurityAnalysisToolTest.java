/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProviderFactory;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class SecurityAnalysisToolTest extends AbstractToolTest {

    private SecurityAnalysisTool tool;
    private MapModuleConfig moduleConfig;
    private ComponentDefaultConfig config;

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

    @Test
    public void testRunWithLog() throws Exception {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ByteArrayOutputStream berr = new ByteArrayOutputStream();
             PrintStream out = new PrintStream(bout);
             PrintStream err = new PrintStream(berr);
             ComputationManager cm = mock(ComputationManager.class)) {
            CommandLine cl = mock(CommandLine.class);
            ToolRunningContext context = mock(ToolRunningContext.class);
            when(context.getFileSystem()).thenReturn(fileSystem);
            when(context.getOutputStream()).thenReturn(out);
            when(context.getErrorStream()).thenReturn(err);
            when(cl.getOptionValue("case-file")).thenReturn("network.xml");
            when(cl.hasOption("limit-types")).thenReturn(false);
            when(cl.getOptionProperties(any())).thenReturn(new Properties());
            // tigger runWithLog()
            when(cl.hasOption("log-file")).thenReturn(true);
            when(cl.getOptionValue("log-file")).thenReturn("out.zip");

            when(context.getShortTimeExecutionComputationManager()).thenReturn(cm);
            when(context.getLongTimeExecutionComputationManager()).thenReturn(cm);
            ImportConfig config = new ImportConfig();

            SecurityAnalysisFactory saFactory = new SecurityAnalysisMockFactory();
            SecurityAnalysis sa = saFactory.create(null, cm, 1);

            // execute
            tool.run(cl, context, mock(ContingenciesProviderFactory.class), saFactory);

            // verify that runWithLog() called instead of run();
            verify(sa, never()).run(any(), any(), any());
            verify(sa, times(1)).runWithLog(any(), any(), any());

            when(cl.hasOption("log-file")).thenReturn(false);
            // execute
            tool.run(cl, context, mock(ContingenciesProviderFactory.class), saFactory);
            verify(sa, times(1)).run(any(), any(), any());
        }
    }

    private static class ImporterMock implements Importer {

        static final String FORMAT = "net";

        @Override
        public String getFormat() {
            return FORMAT;
        }

        @Override
        public String getComment() {
            return "";
        }

        @Override
        public boolean exists(ReadOnlyDataSource dataSource) {
            return true;
        }

        @Override
        public Network importData(ReadOnlyDataSource dataSource, Properties parameters) {
            Network network = Mockito.mock(Network.class);
            VariantManager variantManager = Mockito.mock(VariantManager.class);
            Mockito.when(variantManager.getWorkingVariantId()).thenReturn("s1");
            Mockito.when(network.getVariantManager()).thenReturn(variantManager);
            return network;
        }

        @Override
        public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        }
    }
}
