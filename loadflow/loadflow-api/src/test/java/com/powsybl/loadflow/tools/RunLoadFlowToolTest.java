/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.tools;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class RunLoadFlowToolTest extends AbstractSerDeTest {

    @Test
    void printLoadFlowResultTest() throws IOException {
        LoadFlowResult result = new LoadFlowResultImpl(true, Collections.emptyMap(), "",
                List.of(
                    new LoadFlowResultImpl.ComponentResultImpl(0, 0,
                            LoadFlowResult.ComponentResult.Status.CONVERGED, "Success", Collections.emptyMap(), 8,
                            "myRefBus", List.of(new LoadFlowResultImpl.SlackBusResultImpl("mySlack", 0.01)),
                            300.45),
                    new LoadFlowResultImpl.ComponentResultImpl(1, 1,
                            LoadFlowResult.ComponentResult.Status.MAX_ITERATION_REACHED, "Newton-Raphson Iteration Limit",
                            Collections.emptyMap(), 40,
                            "myOtherRefBus", List.of(
                                new LoadFlowResultImpl.SlackBusResultImpl("mySlack1", 12.34),
                                new LoadFlowResultImpl.SlackBusResultImpl("mySlack2", 45.67)
                            ),
                            2678.22)
                )
        );
        try (StringWriter writer = new StringWriter()) {
            RunLoadFlowTool.printLoadFlowResult(result, writer, new AsciiTableFormatterFactory(), new TableFormatterConfig(Locale.US, "inv"));
            writer.flush();
            assertTxtEquals(getClass().getResourceAsStream("/LoadFlowResultResult.txt"), writer.toString());
        }
    }
}
