/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.reporter.ReportNodeImpl;
import com.powsybl.commons.reporter.ReportNode;
import com.powsybl.commons.reporter.TypedValue;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
final class TieLineReports {

    private static final String DANGLING_LINE_ID_1 = "danglingLineId1";
    private static final String DANGLING_LINE_ID_2 = "danglingLineId2";

    // DEBUG
    static void inconsistentPropertyValues(ReportNode reportNode, String propertyName, String propertyValue1, String propertyValue2, String danglingLineId1, String danglingLineId2) {
        reportNode.addChild(ReportNodeImpl.builder()
                .withKey("InconsistentPropertyValues")
                .withDefaultMessage("Inconsistencies of property ${propertyName} between ${danglingLineId1} (value=${propertyValue1}) and ${danglingLineId2} (value=${propertyValue2}). Property is not added to merged line")
                .withValue("propertyName", propertyName)
                .withValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withValue("propertyValue1", propertyValue1)
                .withValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withValue("propertyValue2", propertyValue2)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .build());
    }

    static void moveCommonAliases(ReportNode reportNode, String alias, String danglingLineId1, String danglingLineId2) {
        reportNode.addChild(ReportNodeImpl.builder()
                .withKey("MoveCommonAlias")
                .withDefaultMessage("Alias ${alias} found in dangling lines ${danglingLineId1} and ${danglingLineId2} is moved to their merged line.")
                .withValue("alias", alias)
                .withValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .build());
    }

    static void propertyOnlyOnOneSide(ReportNode reportNode, String propertyName, String propertyValue, int emptySide, String danglingLineId1, String danglingLineId2) {
        reportNode.addChild(ReportNodeImpl.builder()
                .withKey("PropertyOnlyOnOneSide")
                .withDefaultMessage("Inconsistencies of property ${propertyName} between both sides (${danglingLineId1) on side 1 and ${danglingLineId2} on side2) of merged line. " +
                        "Side ${side} has no value. Value on other side is kept.")
                .withValue("propertyName", propertyName)
                .withValue("side", emptySide)
                .withValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withValue("propertyValue", propertyValue)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .build());
    }

    // WARN
    static void inconsistentAliasTypes(ReportNode reportNode, String alias, String type1, String type2, String danglingLineId1, String danglingLineId2) {
        reportNode.addChild(ReportNodeImpl.builder()
                .withKey("InconsistentAliasTypes")
                .withDefaultMessage("Inconsistencies of types for alias ${alias} type in dangling lines ${danglingLineId1} (type=${type1}) and ${danglingLineId2} (type=${type2}). Type is lost.")
                .withValue("alias", alias)
                .withValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withValue("type1", type1)
                .withValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withValue("type2", type2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    static void inconsistentAliasValues(ReportNode reportNode, String alias1, String alias2, String type, String danglingLineId1, String danglingLineId2) {
        reportNode.addChild(ReportNodeImpl.builder()
                .withKey("InconsistentAliasValues")
                .withDefaultMessage("Inconsistencies found for alias type '${type}'('${alias1}' for '${danglingLineId1}' and '${alias2}' for '${danglingLineId2}'). " +
                        "Types are respectively renamed as '${type}_1' and '${type}_2'.")
                .withValue("alias1", alias1)
                .withValue("alias2", alias2)
                .withValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withValue("type", type)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    private TieLineReports() {
    }
}
