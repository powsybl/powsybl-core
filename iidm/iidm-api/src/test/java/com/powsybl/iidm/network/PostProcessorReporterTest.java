/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.commons.reporter.ReporterModelDeserializer;
import com.powsybl.commons.reporter.ReporterModelSerializer;
import com.powsybl.computation.ComputationManager;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
class PostProcessorReporterTest extends AbstractConverterTest {
    private final Importer testImporter = new TestImporter();
    private final ImportPostProcessorMock importPostProcessorMock = new ImportPostProcessorMock();
    private final ImportersLoader loader = new ImportersLoaderList(Collections.singletonList(testImporter), Collections.singletonList(importPostProcessorMock));
    private final ComputationManager computationManager = Mockito.mock(ComputationManager.class);
    private final Importer importer1 = Importer.addPostProcessors(loader, testImporter, computationManager, "testReporter");

    @Test
    void postProcessorWithReporter() throws IOException {

        ReporterModel reporter = new ReporterModel("testPostProcessor", "Test importer post processor");
        Network network1 = importer1.importData(null, new NetworkFactoryMock(), null, reporter);
        assertNotNull(network1);

        Optional<Report> report = reporter.getReports().stream().findFirst();
        assertTrue(report.isPresent());

        roundTripTest(reporter, ReporterModelSerializer::write, ReporterModelDeserializer::read, "/postProcessorReporterTest.json");
    }

    @Test
    void postProcessorWithoutReporter() throws Exception {
        Network network1 = importer1.importData(null, new NetworkFactoryMock(), null);
        importPostProcessorMock.process(network1, computationManager);
        assertNotNull(network1);
        assertEquals(new DateTime(2021, 12, 20, 0, 0, 0), network1.getCaseDate());
    }
}
