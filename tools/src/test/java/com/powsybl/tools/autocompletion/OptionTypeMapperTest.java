/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools.autocompletion;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class OptionTypeMapperTest {

    @Test
    public void test() {
        OptionTypeMapper mapper = new OptionTypeMapper();
        mapper.addArgNameMapping("FILE", OptionType.FILE);
        mapper.addOptionNameMapping(".*file", OptionType.FILE);
        mapper.addOptionNameMapping("file.*", OptionType.FILE);

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
        mapper.map(fileOption);
        assertNull(nonFileOption.getType());
    }

}
