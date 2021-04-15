/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools.autocompletion;

import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Tool;
import org.junit.Before;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class BashCompletionToolTest extends AbstractToolTest {

    private Tool tool;

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
}
