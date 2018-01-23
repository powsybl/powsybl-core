/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import org.apache.commons.cli.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class CommandLineUtilTest {

    private enum TestEnum {
        VALUE1,
        VALUE2
    }

    @Test
    public void test() throws ParseException {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(Option.builder().longOpt("value")
            .hasArg()
            .build());

        // Check for user value
        String[] args = {"--value", "VALUE2"};
        CommandLine line = parser.parse(options, args);
        assertEquals(TestEnum.VALUE2, CommandLineUtil.getOptionValue(line, "value", TestEnum.class, TestEnum.VALUE1));

        // Check for default value
        args = new String[0];
        line = parser.parse(options, args);
        assertEquals(TestEnum.VALUE1, CommandLineUtil.getOptionValue(line, "value", TestEnum.class, TestEnum.VALUE1));
    }
}
