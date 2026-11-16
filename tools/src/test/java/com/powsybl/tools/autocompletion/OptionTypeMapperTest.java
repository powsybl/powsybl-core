/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.autocompletion;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class OptionTypeMapperTest {

    @Test
    void test() {
        OptionTypeMapper mapper = new OptionTypeMapper()
            .addArgNameMapping("FILE", OptionType.FILE)
            .addOptionNameMapping(".*file", OptionType.FILE)
            .addOptionNameMapping("file.*", OptionType.FILE);

        BashOption noArgOption = new BashOption("case-file");
        mapper.map(noArgOption);
        assertNull(noArgOption.getType());

        BashOption fileOption = new BashOption("case-file", "ARG");
        mapper.map(fileOption);
        assertSame(OptionType.FILE, fileOption.getType());

        BashOption argFileOption = new BashOption("case", "FILE");
        mapper.map(argFileOption);
        assertSame(OptionType.FILE, argFileOption.getType());

        BashOption nonFileOption = new BashOption("case", "ARG");
        mapper.map(nonFileOption);
        assertNull(nonFileOption.getType());
    }

    @Test
    void defaultType() {
        OptionTypeMapper mapper = new OptionTypeMapper()
                .setDefaultType(OptionType.DIRECTORY)
                .addArgNameMapping("FILE", OptionType.FILE);

        BashOption argFileOption = new BashOption("case", "FILE");
        BashOption nonFileOption = new BashOption("case", "ARG");

        BashCommand cmd1 = new BashCommand("cmd1", argFileOption);
        BashCommand cmd2 = new BashCommand("cmd2", nonFileOption);
        mapper.map(ImmutableList.of(cmd1, cmd2));

        assertSame(OptionType.FILE, argFileOption.getType());
        assertSame(OptionType.DIRECTORY, nonFileOption.getType());
    }

}
