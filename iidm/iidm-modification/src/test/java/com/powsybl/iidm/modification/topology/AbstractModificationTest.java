/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.NetworkSerDe;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractModificationTest extends AbstractSerDeTest {

    protected void writeXmlTest(Network network, String refXmlFile) throws IOException {
        writeXmlTest(network, NetworkSerDe::write, refXmlFile);
    }

    protected void testReportNode(ReportNode reportNode, String reportsFile) throws IOException {
        Optional<ReportNode> report = reportNode.getChildren().stream().findFirst();
        assertTrue(report.isPresent());

        StringWriter sw = new StringWriter();
        reportNode.print(sw);

        InputStream refStream = TopologyTestUtils.class.getResourceAsStream(reportsFile);
        ComparisonUtils.assertTxtEquals(refStream, sw.toString());
    }
}
