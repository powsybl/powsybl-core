/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Properties;

/**
 * @author Luma Zamarreno {@literal <zamarrenolm at aia.es>}
 */
@AutoService(Importer.class)
public class TestImporterWithoutUpdate implements Importer {

    @Override
    public String getFormat() {
        return "TST-without-update";
    }

    @Override
    public List<String> getSupportedExtensions() {
        return List.of("tst-extension-no-updates");
    }

    @Override
    public String getComment() {
        return "Dummy importer to test Importers";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            return dataSource == null || dataSource.isDataExtension("tst-extension-no-updates") && dataSource.exists(null, "tst-extension-no-updates");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, ReportNode reportNode) {
        if (reportNode != null) {
            reportNode.newReportNode()
                    .withMessageTemplate("testImportModelWithoutUpdate")
                    .withUntypedValue("model", "empty-network")
                    .add();
        }
        return networkFactory.createNetwork("mock", "test");
    }

    // We intentionally do not implement the update method defined in the Importer interface
}
