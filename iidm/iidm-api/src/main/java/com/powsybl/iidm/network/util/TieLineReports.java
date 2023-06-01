/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
final class TieLineReports {

    private static final String DANGLING_LINE_ID_1 = "boundaryLineId1";
    private static final String DANGLING_LINE_ID_2 = "boundaryLineId2";

    // DEBUG
    static void inconsistentPropertyValues(Reporter reporter, String propertyName, String propertyValue1, String propertyValue2, String boundaryLineId1, String boundaryLineId2) {
        reporter.report(Report.builder()
                .withKey("InconsistentPropertyValues")
                .withDefaultMessage("Inconsistencies of property ${propertyName} between ${boundaryLineId1} (value=${propertyValue1}) and ${boundaryLineId2} (value=${propertyValue2}). Property is not added to merged line")
                .withValue("propertyName", propertyName)
                .withValue(DANGLING_LINE_ID_1, boundaryLineId1)
                .withValue("propertyValue1", propertyValue1)
                .withValue(DANGLING_LINE_ID_2, boundaryLineId2)
                .withValue("propertyValue2", propertyValue2)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .build());
    }

    static void moveCommonAliases(Reporter reporter, String alias, String boundaryLineId1, String boundaryLineId2) {
        reporter.report(Report.builder()
                .withKey("MoveCommonAlias")
                .withDefaultMessage("Alias ${alias} found in boundary lines ${boundaryLineId1} and ${boundaryLineId2} is moved to their merged line.")
                .withValue("alias", alias)
                .withValue(DANGLING_LINE_ID_1, boundaryLineId1)
                .withValue(DANGLING_LINE_ID_2, boundaryLineId2)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .build());
    }

    static void propertyOnlyOnOneSide(Reporter reporter, String propertyName, String propertyValue, int emptySide, String boundaryLineId1, String boundaryLineId2) {
        reporter.report(Report.builder()
                .withKey("PropertyOnlyOnOneSide")
                .withDefaultMessage("Inconsistencies of property ${propertyName} between both sides (${boundaryLineId1) on side 1 and ${boundaryLineId2} on side2) of merged line. " +
                        "Side ${side} has no value. Value on other side is kept.")
                .withValue("propertyName", propertyName)
                .withValue("side", emptySide)
                .withValue(DANGLING_LINE_ID_1, boundaryLineId1)
                .withValue(DANGLING_LINE_ID_2, boundaryLineId2)
                .withValue("propertyValue", propertyValue)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .build());
    }

    // WARN
    static void inconsistentAliasTypes(Reporter reporter, String alias, String type1, String type2, String boundaryLineId1, String boundaryLineId2) {
        reporter.report(Report.builder()
                .withKey("InconsistentAliasTypes")
                .withDefaultMessage("Inconsistencies of types for alias ${alias} type in boundary lines ${boundaryLineId1} (type=${type1}) and ${boundaryLineId2} (type=${type2}). Type is lost.")
                .withValue("alias", alias)
                .withValue(DANGLING_LINE_ID_1, boundaryLineId1)
                .withValue("type1", type1)
                .withValue(DANGLING_LINE_ID_2, boundaryLineId2)
                .withValue("type2", type2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    static void inconsistentAliasValues(Reporter reporter, String alias1, String alias2, String type, String boundaryLineId1, String boundaryLineId2) {
        reporter.report(Report.builder()
                .withKey("InconsistentAliasValues")
                .withDefaultMessage("Inconsistencies found for alias type '${type}'('${alias1}' for '${boundaryLineId1}' and '${alias2}' for '${boundaryLineId2}'). " +
                        "Types are respectively renamed as '${type}_1' and '${type}_2'.")
                .withValue("alias1", alias1)
                .withValue("alias2", alias2)
                .withValue(DANGLING_LINE_ID_1, boundaryLineId1)
                .withValue(DANGLING_LINE_ID_2, boundaryLineId2)
                .withValue("type", type)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    private TieLineReports() {
    }
}
