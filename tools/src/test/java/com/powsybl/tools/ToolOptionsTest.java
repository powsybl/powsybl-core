/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import com.google.common.collect.Lists;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.commons.cli.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class ToolOptionsTest {

    private ToolRunningContext context;
    private FileSystem fileSystem;

    private enum EnumOption {
        VALUE
    }

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        context = Mockito.mock(ToolRunningContext.class);
        Mockito.when(context.getFileSystem()).thenReturn(fileSystem);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void test() throws ParseException {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(Option.builder().longOpt("value").hasArg().build())
                .addOption(Option.builder().longOpt("int").hasArg().build())
                .addOption(Option.builder().longOpt("float").hasArg().build())
                .addOption(Option.builder().longOpt("enum").hasArg().build())
                .addOption(Option.builder().longOpt("list").hasArg().build())
                .addOption(Option.builder().longOpt("path").hasArg().build())
                .addOption(Option.builder().longOpt("flag").build());

        // Check for user value
        String[] args = {"--value", "valueString",
                         "--int", "5",
                         "--float", "3.2",
                         "--enum", "VALUE",
                         "--list", "str1,str2",
                         "--path", "/test/path",
                         "--flag"};
        CommandLine line = parser.parse(options, args);
        ToolOptions opts = new ToolOptions(line, context);

        assertEquals("valueString", opts.getValue("value").orElseThrow(AssertionError::new));
        assertEquals(5, (long) opts.getInt("int").orElseThrow(AssertionError::new));
        assertEquals(3.2f, opts.getFloat("float").orElseThrow(AssertionError::new), 0f);
        assertEquals(EnumOption.VALUE, opts.getEnum("enum", EnumOption.class).orElseThrow(AssertionError::new));
        assertEquals(Lists.newArrayList("str1", "str2"), opts.getValues("list").orElseThrow(AssertionError::new));
        assertEquals(fileSystem.getPath("/test/path"), opts.getPath("path").orElseThrow(AssertionError::new));
        assertTrue(opts.hasOption("flag"));
        assertFalse(opts.hasOption("flag2"));
    }

}
