/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

/**
 * @author Alice Caron {@literal <alice.caron at rte-france.com>}
 */
public final class CgmesModelReports {

    private CgmesModelReports() {
    }

    public static void readFile(ReportNode reportNode, String name) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.model.CGMESFileRead")
                .withTypedValue("instanceFile", name, TypedValue.FILENAME)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }
}
