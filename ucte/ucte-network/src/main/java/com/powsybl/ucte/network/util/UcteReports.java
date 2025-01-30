/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network.util;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.ucte.network.UcteLine;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class UcteReports {

    public static final String LINE_ID_KEY = "lineId";

    private static ResourceBundle bundle(Locale locale) {
        return ResourceBundle.getBundle("com.powsybl.commons.reports", locale);
    }

    private UcteReports() {
    }

    public static ReportNode fixUcteTransformers(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("fixUcteTransformers", bundle(reportNode.getTreeContext().getLocale()).getString("fixUcteTransformers"))
                .add();
    }

    public static ReportNode fixUcteRegulations(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("fixUcteRegulations", bundle(reportNode.getTreeContext().getLocale()).getString("fixUcteRegulations"))
                .add();
    }

    public static void negativeLineResistance(UcteLine line, ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withMessageTemplate("negativeLineResistance", bundle(reportNode.getTreeContext().getLocale()).getString("negativeLineResistance"))
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("resistance", line.getResistance(), TypedValue.RESISTANCE)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

}
