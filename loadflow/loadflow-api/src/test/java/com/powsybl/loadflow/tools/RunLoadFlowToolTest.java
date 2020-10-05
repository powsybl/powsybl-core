/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.tools;

import com.google.common.io.CharStreams;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RunLoadFlowToolTest {

    @Test
    public void printLoadFlowResultTest() throws IOException {
        LoadFlowResult result = new LoadFlowResultImpl(true, Collections.emptyMap(), "",
                Collections.singletonList(new LoadFlowResultImpl.ComponentResultImpl(0, "CONVERGE", 8, "mySlack", 0.01)));
        try (StringWriter writer = new StringWriter()) {
            RunLoadFlowTool.printLoadFlowResult(result, writer, new AsciiTableFormatterFactory(), new TableFormatterConfig());
            writer.flush();
            String ref = CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/LoadFlowResultResult.txt")));
            assertEquals(ref, writer.toString());
        }
    }
}
