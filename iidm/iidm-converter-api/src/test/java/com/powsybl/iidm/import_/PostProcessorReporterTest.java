package com.powsybl.iidm.import_;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.commons.reporter.ReporterModelDeserializer;
import com.powsybl.commons.reporter.ReporterModelSerializer;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PostProcessorReporterTest extends AbstractConverterTest {
    @Test
    public void postProcessorWithReporter() throws IOException {
        Importer testImporter = new TestImporter();
        ImportPostProcessorMock importPostProcessorMock = new ImportPostProcessorMock();
        ImportersLoader loader = new ImportersLoaderList(Collections.singletonList(testImporter), Collections.singletonList(importPostProcessorMock));
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        Importer importer1 = Importers.addPostProcessors(loader, testImporter, computationManager, "testReporter");

        ReporterModel reporter = new ReporterModel("testPostProcessor", "Test importer post processor");
        Network network1 = importer1.importData(null, NetworkFactory.findDefault(), null, reporter);
        assertNotNull(network1);

        Optional<Report> report = reporter.getReports().stream().findFirst();
        assertTrue(report.isPresent());

        roundTripTest(reporter, ReporterModelSerializer::write, ReporterModelDeserializer::read, "/postProcessorReporterTest.json");
    }
}
