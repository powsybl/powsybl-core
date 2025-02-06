/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class ModificationLogs {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModificationLogs.class);

    private ModificationLogs() {
    }

    public static void logOrThrow(boolean throwException, String message) {
        if (throwException) {
            throw new PowsyblException(message);
        } else {
            LOGGER.warn("Error while applying modification : {}", message);
        }
    }

    public static void busOrBbsDoesNotExist(String bbsId, ReportNode reportNode, boolean throwException) {
        LOGGER.error("Bus or busbar section {} not found.", bbsId);
        ModificationReports.notFoundBusOrBusbarSectionReport(reportNode, bbsId);
        logOrThrow(throwException, String.format("Bus or busbar section %s not found", bbsId));
        return;
    }
}
