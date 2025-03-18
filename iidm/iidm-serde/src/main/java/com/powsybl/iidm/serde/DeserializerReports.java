/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

import java.util.Set;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public final class DeserializerReports {

    private DeserializerReports() {
    }

    // INFO
    public static void importedNetworkReport(ReportNode reportNode, String networkId, String format) {
        if (reportNode != null) {
            reportNode.newReportNode()
                    .withMessageTemplate("core.iidm.serde.importedNetwork")
                    .withUntypedValue("networkId", networkId)
                    .withUntypedValue("format", format)
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .add();
        }
    }

    // INFO
    public static void importedExtension(ReportNode reportNode, Set<String> extensionsNameImported) {
        if (reportNode != null) {
            extensionsNameImported.forEach(extensionName ->
                reportNode.newReportNode()
                        .withMessageTemplate("core.iidm.serde.importedExtension")
                        .withUntypedValue("extensionName", extensionName)
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .add()
            );
        }
    }

    public static void extensionNotFound(ReportNode reportNode, Set<String> extensionsNotFoundName) {
        if (reportNode != null) {
            extensionsNotFoundName.forEach(extensionName ->
                reportNode.newReportNode()
                        .withMessageTemplate("core.iidm.serde.extensionNotFound")
                        .withUntypedValue("extensionName", extensionName)
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .add()
            );
        }
    }

    public static ReportNode doneImportingXiidm(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.iidm.serde.xiidmImportDone")
                .add();
    }

    public static ReportNode readWarningValidationPart(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.iidm.serde.validationWarnings")
                .add();
    }

    public static ReportNode importedExtensions(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.iidm.serde.importedExtensions")
                .add();
    }

    public static ReportNode notFoundExtensions(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.iidm.serde.extensionsNotFound")
                .add();
    }
}
