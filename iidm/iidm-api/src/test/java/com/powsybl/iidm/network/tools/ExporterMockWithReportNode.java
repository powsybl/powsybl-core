/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Exporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.NetworkReports;

import java.util.Properties;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
@AutoService(Exporter.class)
public class ExporterMockWithReportNode implements Exporter {

    public ExporterMockWithReportNode() {
    }

    @Override
    public String getFormat() {
        return "OUT";
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource, ReportNode reportNode) {
        NetworkReports.exportMock(reportNode);
    }
}
