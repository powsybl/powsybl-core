/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.tck;

import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractLineTest;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineTest extends AbstractLineTest {

    @Test
    void shouldReportNodeWhenLineConnectVeryDifferentNominalVoltages() {
        // Given VL 200 KV / 440 KV
        Network network = NoEquipmentNetworkFactory.create();
        VoltageLevel voltageLevelA = network.getVoltageLevel("vl1");
        VoltageLevel voltageLevelB = network.getVoltageLevel("vl2");
        assertEquals(440.0, voltageLevelA.getNominalV());
        assertEquals(200.0, voltageLevelB.getNominalV());
        ReportNode reportRoot = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("test")
                .build();
        network.getReportNodeContext().pushReportNode(reportRoot);
        //When
        network.newLine()
                .setId("lineId")
                .setName("lineName")
                .setR(1.0)
                .setX(2.0)
                .setG1(3.0)
                .setG2(3.5)
                .setB1(4.0)
                .setB2(4.5)
                .setVoltageLevel1(voltageLevelA.getId())
                .setVoltageLevel2(voltageLevelB.getId())
                .setBus1("busA")
                .setBus2("busB")
                .setConnectableBus1("busA")
                .setConnectableBus2("busB")
                .add();
        // Then
        assertTrue(reportRoot.getChildren()
                .stream()
                .map(ReportNode::getMessageKey)
                .toList()
                .contains("core.iidm.network.lineNominalVoltageDifferent"));
    }
}

