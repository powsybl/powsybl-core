/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.autocompletion;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class BashCommandTest {

    @Test
    void testConversion() {
        Options inputOptions = new Options();
        inputOptions.addOption(Option.builder().longOpt("file")
                .hasArg()
                .argName("FILE")
                .build());
        inputOptions.addOption(Option.builder("E").build());
        Map<String, Options> inputCommands = ImmutableMap.of("cmd", inputOptions);
        List<BashCommand> commands = BashCommand.convert(inputCommands);

        assertEquals(1, commands.size());
        BashCommand command = commands.get(0);
        assertEquals("cmd", command.getName());
        List<BashOption> options = command.getOptions();
        assertEquals(2, options.size());

        BashOption option1 = options.get(0);
        assertEquals("--file", option1.getName());
        assertEquals("FILE", option1.getArgName().orElseThrow(IllegalStateException::new));
        assertTrue(option1.hasArg());
        assertNull(option1.getType());
        assertNull(option1.getPossibleValues());

        BashOption option2 = options.get(1);
        assertEquals("-E", option2.getName());
        assertTrue(option2.getArgName().isEmpty());
        assertFalse(option2.hasArg());
        assertNull(option2.getType());
        assertNull(option2.getPossibleValues());
    }
}
