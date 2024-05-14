/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.test.AbstractSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class ReportNodeTest extends AbstractSerDeTest {

    @Test
    void testInclude() throws IOException {
        ReportNode root = ReportNode.newRootReportNode()
                .withMessageTemplate("rootTemplate", "Root message")
                .build();

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> root.include(root));
        assertEquals("Cannot add a reportNode in itself", e1.getMessage());

        ReportNode otherRoot = ReportNode.newRootReportNode()
                .withMessageTemplate("includedRoot", "Included root message")
                .build();
        otherRoot.newReportNode()
                .withMessageTemplate("includedChild", "Included child message")
                .add();

        root.include(otherRoot);
        assertEquals(1, root.getChildren().size());
        assertEquals(otherRoot, root.getChildren().get(0));

        // Other root is not root anymore and can therefore not be added again
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> root.include(otherRoot));
        assertEquals("Cannot include non-root reportNode", e2.getMessage());

        ReportNode child = root.newReportNode()
                .withMessageTemplate("child", "Child message")
                .add();
        PowsyblException e3 = assertThrows(PowsyblException.class, () -> root.include(child));
        assertEquals("Cannot include non-root reportNode", e3.getMessage());

        roundTripTest(root, ReportNodeSerializer::write, ReportNodeDeserializer::read, "/testIncludeReportNode.json");
    }
}
