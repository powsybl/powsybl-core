/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Importers.class, SecurityAnalysisFactories.class})
public class SecurityAnalysisToolTest extends AbstractToolTest {

    private SecurityAnalysisTool tool;

    @Override
    @Before
    public void setUp() throws Exception {
        tool = new SecurityAnalysisTool();
        super.setUp();
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
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);
        PrintStream err = new PrintStream(berr);
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
        ComputationManager cm = mock(ComputationManager.class);
        when(context.getShortTimeExecutionComputationManager()).thenReturn(cm);
        when(context.getLongTimeExecutionComputationManager()).thenReturn(cm);
        mockStatic(Importers.class);
        ImportConfig config = ImportConfig.load();
        Network network = mock(Network.class);
        when(network.getVariantManager()).thenReturn(mock(VariantManager.class));
        BDDMockito.given(Importers.loadNetwork(any(Path.class), any(ComputationManager.class), any(ImportConfig.class), any(Properties.class))).willReturn(network);
        mockStatic(SecurityAnalysisFactories.class);
        SecurityAnalysisFactory saFactory = mock(SecurityAnalysisFactory.class);
        SecurityAnalysis sa = mock(SecurityAnalysis.class);
        SecurityAnalysisResult sar = mock(SecurityAnalysisResult.class);
        LimitViolationsResult preResult = mock(LimitViolationsResult.class);
        when(sar.getPreContingencyResult()).thenReturn(preResult);
        SecurityAnalysisResultWithLog sarl = new SecurityAnalysisResultWithLog(sar, "hi".getBytes());
        when(saFactory.create(any(), any(), any(), any(), anyInt())).thenReturn(sa);
        BDDMockito.when(SecurityAnalysisFactories.newDefaultFactory()).thenReturn(saFactory);
        CompletableFuture<SecurityAnalysisResultWithLog> cfSarl = mock(CompletableFuture.class);
        when(cfSarl.join()).thenReturn(sarl);
        when(sa.runWithLog(any(), any(), any())).thenReturn(cfSarl);

        // execute
        tool.run(cl, context);

        // verify that runWithLog() called instead of run();
        verify(sa, never()).run(any(), any(), any());
        verify(sa, times(1)).runWithLog(any(), any(), any());

        // close
        bout.close();
        berr.close();
        out.close();
        err.close();
        cm.close();
    }
}
