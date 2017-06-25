/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.tools;

import eu.itesla_project.commons.tools.AbstractToolTest;
import eu.itesla_project.commons.tools.Tool;

import java.util.Collections;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class ConversionToolTest extends AbstractToolTest {

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singletonList(new ConversionTool());
    }

    @Override
    public void assertCommand() {
        Tool tool = new ConversionTool();
        assertCommand(tool.getCommand(), "convert-network", 5, 3);
        assertOption(tool.getCommand().getOptions(), "input-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-format", true, true);
        assertOption(tool.getCommand().getOptions(), "import-parameters", false, true);
        assertOption(tool.getCommand().getOptions(), "export-parameters", false, true);
    }
}
