/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.autocompletion;

import com.powsybl.commons.test.TestUtil;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class BashCompletionGeneratorTest {

    enum TypeOption {
        TYPE1,
        TYPE2
    }

    static Stream<Arguments> data() {
        return Stream.of(Arguments.of(new StringTemplateBashCompletionGenerator()));
    }

    private static String readResource(String path) throws IOException {
        try (InputStream is = BashCompletionGeneratorTest.class.getResourceAsStream(path)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    private void checkGeneratedScript(String referencePath, BashCompletionGenerator generator, BashCommand... commands) throws IOException {
        try (StringWriter sw = new StringWriter()) {
            generator.generateCommands("itools", Arrays.asList(commands), sw);
            String script = TestUtil.normalizeLineSeparator(sw.toString());
            String refScript = TestUtil.normalizeLineSeparator(readResource(referencePath));
            assertEquals(refScript, script);
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    void oneCommandOneNoArgOption(BashCompletionGenerator generator) throws IOException {
        BashOption option = new BashOption("--type");
        BashCommand command = new BashCommand("cmd", option);
        checkGeneratedScript("1-command-1-no-arg-option.sh", generator, command);
    }

    @ParameterizedTest
    @MethodSource("data")
    void oneCommandOneFile(BashCompletionGenerator generator) throws IOException {
        BashOption option = new BashOption("--case-file", "FILE", OptionType.FILE);
        BashCommand command = new BashCommand("cmd", option);
        checkGeneratedScript("1-command-1-file.sh", generator, command);
    }

    @ParameterizedTest
    @MethodSource("data")
    void oneCommandMultipleOptions(BashCompletionGenerator generator) throws IOException {
        BashOption file = new BashOption("--case-file", "FILE", OptionType.FILE);
        BashOption dir = new BashOption("--output-dir", "DIR", OptionType.DIRECTORY);
        BashOption host = new BashOption("--host", "HOST", OptionType.HOSTNAME);
        BashOption enumOption = new BashOption("--type", "TYPE", OptionType.enumeration(TypeOption.class));
        BashCommand command = new BashCommand("cmd", file, dir, host, enumOption);
        checkGeneratedScript("1-command-multiple-options.sh", generator, command);
    }

    @ParameterizedTest
    @MethodSource("data")
    void twoCommands(BashCompletionGenerator generator) throws IOException {
        checkGeneratedScript("2-commands.sh", generator, new BashCommand("cmd1"), new BashCommand("cmd2"));
    }

}
