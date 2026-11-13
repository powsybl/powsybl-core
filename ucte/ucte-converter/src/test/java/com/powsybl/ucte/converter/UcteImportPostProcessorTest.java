/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.ucte.converter.UcteExporterReportTest.checkReportNode;

/**
 * @author Antoine Limouzin {@literal <antoine.limouzin at rte.com>}
 */
class UcteImportPostProcessorTest {

    @Test
    void shouldAddNodeGeoNameInBus() {
        final ReportNode reportNode = ReportNode.newRootReportNode()
                         .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME,
                                 PowsyblCoreReportResourceBundle.BASE_NAME)
                         .withMessageTemplate("testImportReportNode")
                         .build();
        final Properties properties = new Properties();
        properties.put(UcteImporter.POST_PROCESSORS, "test-import");
        final ReadOnlyDataSource dataSource = new ResourceDataSource("importPostProcessor", new ResourceSet("/", "importPostProcessor.uct"));
        final Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), properties, reportNode);
        Assertions.assertEquals("Italia A3", network.getBusBreakerView().getBus("IA____21").getProperty("ucteName"));
        checkReportNode("""
                + Test importing UCTE network
                    + Reading UCTE network file
                        Fix UCTE nodes
                        Fix UCTE lines
                        Fix UCTE transformers
                        Fix UCTE regulations
                    + Post-processor test-import
                        Geographical name of IA____21 is Italia A3
                """, reportNode);
    }

}
