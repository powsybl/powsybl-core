/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools.autocompletion;

import com.google.common.collect.ImmutableList;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class BashCompletionToolTest extends AbstractToolTest {

    private BashCompletionTool tool;

    @Before
    @Override
    public void setUp() throws Exception {
        tool = new BashCompletionTool();
        super.setUp();
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    public void assertCommand() {
        assertEquals("Misc", tool.getCommand().getTheme());
        assertFalse(tool.getCommand().isHidden());

        assertCommand(tool.getCommand(), "generate-completion-script", 1, 1);
        assertOption(tool.getCommand().getOptions(), "output-file", true, true);
    }

    private String expectedScript() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("tool-output.sh")) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    @Test
    public void testBehaviour() throws IOException {

        Options options1 = new Options();
        options1.addOption(Option.builder("I").hasArg().build());
        options1.addOption(Option.builder().longOpt("case-file").hasArg().argName("FILE").build());
        Tool tool1 = createTool("tool1", options1);

        Options options2 = new Options();
        options2.addOption(Option.builder().longOpt("hostname").hasArg().argName("HOST").build());
        options2.addOption(Option.builder().longOpt("dir").hasArg().argName("DIR").build());
        Tool tool2 = createTool("tool2", options2);

        tool.generateCompletionScript(ImmutableList.of(tool1, tool2), fileSystem.getPath("/output.sh"));

        String outputScript = Files.readString(fileSystem.getPath("/output.sh"), StandardCharsets.UTF_8);
        assertEquals(expectedScript(), outputScript);
    }

    private static Tool createTool(String name, Options options) {
        return new Tool() {
            @Override
            public Command getCommand() {
                return new Command() {
                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public String getTheme() {
                        return null;
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }

                    @Override
                    public Options getOptions() {
                        return options;
                    }

                    @Override
                    public String getUsageFooter() {
                        return null;
                    }
                };
            }

            @Override
            public void run(CommandLine line, ToolRunningContext context) throws Exception {

            }
        };
    }
}
