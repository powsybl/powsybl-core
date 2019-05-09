/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation;

import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import org.junit.Test;

import java.util.Collections;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ImpactAnalysisToolTest extends AbstractToolTest {

    private final ImpactAnalysisTool tool = new ImpactAnalysisTool();

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Test
    public void assertCommand() {
        Command command = tool.getCommand();

        assertCommand(command, "run-impact-analysis", 6, 1);
        assertOption(command.getOptions(), "case-file", true, true);
        assertOption(command.getOptions(), "contingencies", false, true);
        assertOption(command.getOptions(), "output-csv-file", false, true);
        assertOption(command.getOptions(), "skip-postproc", false, false);
        assertOption(command.getOptions(), "import-parameters", false, true);
        assertOption(command.getOptions(), "I", false, true);
    }
}
