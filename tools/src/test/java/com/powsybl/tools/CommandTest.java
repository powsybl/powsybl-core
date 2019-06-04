/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import org.apache.commons.cli.Options;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CommandTest {

    private static final Options OPTIONS = new Options();

    private static final Command COMMAND = new AbstractCommand("name",
            "theme",
            "description") {
        @Override
        public Options getOptions() {
            return OPTIONS;
        }
    };

    @Test
    public void test() {
        assertEquals("name", COMMAND.getName());
        assertEquals("theme", COMMAND.getTheme());
        assertEquals("description", COMMAND.getDescription());
        assertSame(OPTIONS, COMMAND.getOptions());
        assertNull(COMMAND.getUsageFooter());
        assertFalse(COMMAND.isHidden());
    }

}
