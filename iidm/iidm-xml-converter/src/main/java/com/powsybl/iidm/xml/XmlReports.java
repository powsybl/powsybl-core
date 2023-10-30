/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;

import java.util.Set;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public final class XmlReports {

    private XmlReports() {
    }

    // INFO
    public static void importedXmlNetworkReport(Reporter reporter, String networkId) {
        if (reporter != null) {
            reporter.report(Report.builder()
                    .withKey("importedXmlNetwork")
                    .withDefaultMessage("XML network \"${networkId}\" is imported.")
                    .withValue("networkId", networkId)
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .build());
        }
    }

    // INFO
    public static void importedExtension(Reporter reporter, Set<String> extensionsNameImported) {
        if (reporter != null) {
            extensionsNameImported.forEach(extensionName ->
                reporter.report(Report.builder()
                        .withKey("importedExtension")
                        .withDefaultMessage("Extension ${extensionName} imported.")
                        .withValue("extensionName", extensionName)
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .build())
            );
        }
    }

    public static void extensionNotFound(Reporter reporter, Set<String> extensionsNotFoundName) {
        if (reporter != null) {
            extensionsNotFoundName.forEach(extensionName ->
                reporter.report(Report.builder()
                        .withKey("extensionNotFound")
                        .withDefaultMessage("Extension ${extensionName} not found.")
                        .withValue("extensionName", extensionName)
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .build())
            );
        }
    }

}
