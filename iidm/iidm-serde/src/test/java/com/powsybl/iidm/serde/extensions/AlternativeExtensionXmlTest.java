/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.iidm.network.impl.extensions.OperatingStatusImpl;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.ExportOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class AlternativeExtensionXmlTest extends AbstractIidmSerDeTest {

    // This test is the same as OperatingStatusXmlTest::test, but the extension is exported/imported
    // using the defined alternative (which is not versioned).
    @Test
    void test() throws IOException {
        Network network = OperatingStatusXmlTest.createTestNetwork();

        // extend line
        Line line = network.getLine("L");
        assertNotNull(line);
        OperatingStatus<Line> lineOperatingStatus = new OperatingStatusImpl<>(line,
                OperatingStatus.Status.PLANNED_OUTAGE);
        line.addExtension(OperatingStatus.class, lineOperatingStatus);

        var exportOptions = new ExportOptions().addExtensionVersion(OperatingStatus.NAME, "alternative");
        Network network2 = allFormatsRoundTripTest(network, "/alternativeOperatingStatusRef.xml", exportOptions);

        Line line2 = network2.getLine("L");
        assertNotNull(line2);
        OperatingStatus<Line> lineOperatingStatus2 = line2.getExtension(OperatingStatus.class);
        assertNotNull(lineOperatingStatus2);
        assertEquals(lineOperatingStatus.getStatus(), lineOperatingStatus2.getStatus());

        lineOperatingStatus2.setStatus(OperatingStatus.Status.IN_OPERATION);
        assertEquals(OperatingStatus.Status.IN_OPERATION, lineOperatingStatus2.getStatus());
    }

}
