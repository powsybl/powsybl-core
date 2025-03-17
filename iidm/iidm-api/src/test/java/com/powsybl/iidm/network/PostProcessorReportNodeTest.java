/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.report.*;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import com.powsybl.computation.ComputationManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class PostProcessorReportNodeTest extends AbstractSerDeTest {
    private final Importer testImporter = new TestImporter();
    private final ImportPostProcessorMock importPostProcessorMock = new ImportPostProcessorMock();
    private final ImportersLoader loader = new ImportersLoaderList(Collections.singletonList(testImporter), Collections.singletonList(importPostProcessorMock));
    private final ComputationManager computationManager = Mockito.mock(ComputationManager.class);
    private final Importer importer1 = Importer.addPostProcessors(loader, testImporter, computationManager, "testReportNode");

    @Test
    void postProcessorWithReportNode() throws IOException {

        ReportNode reportRoot = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("testPostProcessor")
                .build();
        Network network1 = importer1.importData(null, new NetworkFactoryMock(), null, reportRoot);
        assertNotNull(network1);

        Optional<ReportNode> report = reportRoot.getChildren().stream().findFirst();
        assertTrue(report.isPresent());

        roundTripTest(reportRoot, ReportNodeSerializer::write, ReportNodeDeserializer::read, "/postProcessorReportNodeTest.json");
    }

    @Test
    void postProcessorWithoutReportNode() throws Exception {
        Network network1 = importer1.importData(null, new NetworkFactoryMock(), null);
        importPostProcessorMock.process(network1, computationManager);
        assertNotNull(network1);
        assertEquals(ZonedDateTime.of(2021, 12, 20, 0, 0, 0, 0, ZoneOffset.UTC), network1.getCaseDate());
    }
}
