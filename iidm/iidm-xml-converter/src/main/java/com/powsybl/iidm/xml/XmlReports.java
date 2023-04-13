/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;

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
                    .withDefaultMessage("XML network ${networkId} is imported.")
                    .withValue("networkId", networkId)
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .build());
        }
    }

    // INFO
    public static void importedExtension(Reporter reporter, String extensionName) {
        if (reporter != null) {
            reporter.report(Report.builder()
                    .withKey("importedExtension")
                    .withDefaultMessage("Extension ${extensionName} is imported.")
                    .withValue("extensionName", extensionName)
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .build());
        }
    }

}
