/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.tools;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Properties;

import static com.powsybl.iidm.tools.ConversionToolConstants.*;
import static com.powsybl.tools.AbstractToolTest.assertOption;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class DefaultConversionOptionTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private FileSystem fs;
    private DefaultConversionOption option;
    private CommandLine line;
    private ToolRunningContext context;

    @Before
    public void setUp() throws IOException {
        fs = Jimfs.newFileSystem(Configuration.unix());
        try (BufferedWriter writer = Files.newBufferedWriter(fs.getPath("/input.txt"))) {
            writer.write("");
        }

        option = new DefaultConversionOption() {
            @Override
            public ImportConfig createImportConfig(CommandLine line) {
                return Mockito.mock(ImportConfig.class);
            }
        };

        line = Mockito.mock(CommandLine.class);
        Mockito.when(line.getOptionValue(INPUT_FILE)).thenReturn("/input.txt");
        Mockito.when(line.getOptionValue(ConversionToolUtils.OptionType.IMPORT.getLongOpt(), null)).thenReturn(null);
        Mockito.when(line.getOptionValue(OUTPUT_FORMAT)).thenReturn("OUT");
        Mockito.when(line.getOptionValue(OUTPUT_FILE)).thenReturn("/output");

        context = Mockito.mock(ToolRunningContext.class);
        Mockito.when(context.getFileSystem()).thenReturn(fs);
        Mockito.when(context.getShortTimeExecutionComputationManager()).thenReturn(Mockito.mock(ComputationManager.class));
        Mockito.when(context.getOutputStream()).thenReturn(Mockito.mock(PrintStream.class));
    }

    @After
    public void tearDown() throws IOException {
        fs.close();
    }

    @Test
    public void create() {
        assertEquals(INPUT_FILE, option.getInputFileOption());
        assertEquals(OUTPUT_FORMAT, option.getOutputFormatOption());
        assertEquals(OUTPUT_FILE, option.getOutputFileOption());

        DefaultConversionOption option2 = new DefaultConversionOption("INPUT_FILE_2");
        assertEquals("INPUT_FILE_2", option2.getInputFileOption());
        assertEquals(OUTPUT_FORMAT, option2.getOutputFormatOption());
        assertEquals(OUTPUT_FILE, option2.getOutputFileOption());

        DefaultConversionOption option3 = new DefaultConversionOption("INPUT_FILE_3",
                "OUTPUT_FILE_3", "OUTPUT_FORMAT_3");
        assertEquals("INPUT_FILE_3", option3.getInputFileOption());
        assertEquals("OUTPUT_FORMAT_3", option3.getOutputFormatOption());
        assertEquals("OUTPUT_FILE_3", option3.getOutputFileOption());
    }

    @Test
    public void addImportOptions() {
        Options options = new Options();
        option.addImportOptions(options);
        assertEquals(4, options.getOptions().size());
        assertEquals(1, options.getRequiredOptions().size());
        assertOption(options, INPUT_FILE, true, true);
        assertOption(options, IMPORT_PARAMETERS, false, true);
        assertOption(options, "I", false, true);
        assertOption(options, SKIP_POSTPROC, false, false);
    }

    @Test
    public void addExportOptions() {
        Options options = new Options();
        option.addExportOptions(options, true);
        assertEquals(4, options.getOptions().size());
        assertEquals(2, options.getRequiredOptions().size());
        assertOption(options, OUTPUT_FILE, true, true);
        assertOption(options, OUTPUT_FORMAT, true, true);
        assertOption(options, EXPORT_PARAMETERS, false, true);
        assertOption(options, "E", false, true);
    }

    @Test
    public void read() throws IOException {
        Properties parameters = new Properties();
        parameters.put("param1", "value1");
        parameters.put("import.parameter", "value");
        Mockito.when(line.getOptionProperties(any())).thenReturn(parameters);

        Network network = option.read(line, context);
        assertEquals(2, network.getLineCount());
    }

    @Test
    public void readNullNetwork() throws IOException {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Network /input.txt not found");

        Properties parameters = new Properties();
        parameters.put("null", "true");
        Mockito.when(line.getOptionProperties(any())).thenReturn(parameters);

        option.read(line, context);
    }

    @Test
    public void write() throws IOException {
        Properties parameters = new Properties();
        parameters.put("param2", "value2");
        parameters.put("export.parameter", "value");
        Mockito.when(line.getOptionProperties(any())).thenReturn(parameters);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PrintStream printStream = new PrintStream(os);
            Mockito.when(context.getOutputStream()).thenReturn(printStream);

            option.write(Mockito.mock(Network.class), line, context);

            assertEquals("Generating file /output-OUT.txt..." + System.lineSeparator(), os.toString());
        }

    }

    @Test
    public void writeNullExporter() throws IOException {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Export format UNSUPPORTED not supported");
        Mockito.when(line.getOptionValue(OUTPUT_FORMAT)).thenReturn("UNSUPPORTED");
        Mockito.when(line.getOptionProperties(any())).thenReturn(new Properties());
        option.write(Mockito.mock(Network.class), line, context);
    }
}
