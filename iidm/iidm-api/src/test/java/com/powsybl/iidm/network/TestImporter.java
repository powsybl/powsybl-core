/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportBundleBaseName;
import com.powsybl.commons.report.ReportNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Properties;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(Importer.class)
public class TestImporter implements Importer {

    @Override
    public String getFormat() {
        return "TST";
    }

    @Override
    public List<String> getSupportedExtensions() {
        return List.of("tst");
    }

    @Override
    public String getComment() {
        return "Dummy importer to test Importers";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            return dataSource == null || dataSource.isDataExtension("tst") && dataSource.exists(null, "tst");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, ReportNode reportNode) {
        if (reportNode != null) {
            reportNode.newReportNode()
                    .withLocaleMessageTemplate("testImportModel", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                    .withUntypedValue("model", "eurostagTutorialExample1")
                    .add();
        }
        return networkFactory.createNetwork("mock", "test");
    }
}
