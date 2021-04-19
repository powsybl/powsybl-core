/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools.autocompletion;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@RunWith(Parameterized.class)
public class BashCompletionGeneratorTest {

    enum TypeOption {
        TYPE1,
        TYPE2
    }

    @Parameterized.Parameter
    public BashCompletionGenerator generator;

    @Parameterized.Parameters
    public static Iterable<? extends Object> data() {
        return Arrays.asList(
                //new FreemarkerBashCompletionGenerator(),
                new StringTemplateBashCompletionGenerator()
        );
    }

    private static String readResource(String path) throws IOException {
        try (InputStream is = BashCompletionGeneratorTest.class.getResourceAsStream(path)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    private void checkGeneratedScript(String referencePath, BashCommand... commands) throws IOException {
        try (StringWriter sw = new StringWriter()) {
            generator.generateCommands("itools", Arrays.asList(commands), sw);
            String script = sw.toString();
            assertEquals(readResource(referencePath), script);
        }
    }

    @Test
    public void oneCommandOneNoArgOption() throws IOException {
        BashOption option = new BashOption("--type");
        BashCommand command = new BashCommand("cmd", option);
        checkGeneratedScript("1-command-1-no-arg-option.sh", command);
    }

    @Test
    public void oneCommandOneFile() throws IOException {
        BashOption option = new BashOption("--case-file", "FILE", OptionType.FILE);
        BashCommand command = new BashCommand("cmd", option);
        checkGeneratedScript("1-command-1-file.sh", command);
    }

    @Test
    public void oneCommandMultipleOptions() throws IOException {
        BashOption file = new BashOption("--case-file", "FILE", OptionType.FILE);
        BashOption dir = new BashOption("--output-dir", "DIR", OptionType.DIRECTORY);
        BashOption host = new BashOption("--host", "HOST", OptionType.HOSTNAME);
        BashOption enumOption = new BashOption("--type", "TYPE", OptionType.enumeration(TypeOption.class));
        BashCommand command = new BashCommand("cmd", file, dir, host, enumOption);
        checkGeneratedScript("1-command-multiple-options.sh", command);
    }

    @Test
    public void twoCommands() throws IOException {
        checkGeneratedScript("2-commands.sh", new BashCommand("cmd1"), new BashCommand("cmd2"));
    }

}
