/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.reporter.LoggerTreeReporter;
import com.powsybl.commons.reporter.LoggerTreeReporterDeserializer;
import com.powsybl.commons.reporter.LoggerTreeReporterSerializer;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class UcteImporterReporterTest extends AbstractConverterTest {

    protected static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n").replace("\r", "\n");
    }

    @Test
    public void testReportVoltageRegulatingXnode() throws Exception {
        ReadOnlyDataSource dataSource = new ResourceDataSource("frVoltageRegulatingXnode", new ResourceSet("/", "frVoltageRegulatingXnode.uct"));

        LoggerTreeReporter reporter = new LoggerTreeReporter();
        new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null, reporter);

        StringWriter sw = new StringWriter();
        reporter.export(sw);

        InputStream refStream = getClass().getResourceAsStream("/frVoltageRegulatingXnodeReport.txt");
        String refLogExport = normalizeLineSeparator(new String(ByteStreams.toByteArray(refStream), StandardCharsets.UTF_8));
        String logExport = normalizeLineSeparator(sw.toString());
        assertEquals(refLogExport, logExport);
    }

    @Test
    public void roundTripReporterJsonTest() throws Exception {
        ReadOnlyDataSource dataSource = new ResourceDataSource("frVoltageRegulatingXnode", new ResourceSet("/", "frVoltageRegulatingXnode.uct"));

        LoggerTreeReporter reporter = new LoggerTreeReporter();
        new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null, reporter);

        roundTripTest(reporter, LoggerTreeReporterSerializer::write, LoggerTreeReporterDeserializer::read, "/frVoltageRegulatingXnodeReport.json");
    }

}

