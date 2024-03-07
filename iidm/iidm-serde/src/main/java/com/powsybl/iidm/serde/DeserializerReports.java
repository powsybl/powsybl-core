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
                    .withMessageTemplate("importedNetwork", "Network \"${networkId}\" is imported from ${format} format.")
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
                        .withMessageTemplate("importedExtension", "Extension ${extensionName} imported.")
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
                        .withMessageTemplate("extensionNotFound", "Extension ${extensionName} not found.")
                        .withUntypedValue("extensionName", extensionName)
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .add()
            );
        }
    }

}
