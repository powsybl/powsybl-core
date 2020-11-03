/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import java.util.Collections;

import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CompareSecurityAnalysisResultsToolTest extends AbstractToolTest {

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new CompareSecurityAnalysisResultsTool());
    }

    @Override
    public void assertCommand() {
        CompareSecurityAnalysisResultsTool tool = new CompareSecurityAnalysisResultsTool();
        Command command = tool.getCommand();

        assertCommand(command, "compare-security-analysis-results", 4, 3);
        assertOption(command.getOptions(), "result1-file", true, true);
        assertOption(command.getOptions(), "result2-file", true, true);
        assertOption(command.getOptions(), "output-file", true, true);
        assertOption(command.getOptions(), "threshold", false, true);
    }

}
