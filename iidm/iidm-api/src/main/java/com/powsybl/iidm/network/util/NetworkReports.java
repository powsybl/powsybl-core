/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportBundleBaseName;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

import java.util.Objects;

/**
 * @author Alice Caron {@literal <alice.caron at rte-france.com>}
 */
public final class NetworkReports {

    private static final String DANGLING_LINE_ID_1 = "danglingLineId1";
    private static final String DANGLING_LINE_ID_2 = "danglingLineId2";

    private NetworkReports() {
    }

    public static void alreadyConnectedIdentifiableTerminal(ReportNode reportNode, String identifiableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-alreadyConnectedTerminal", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("identifiable", identifiableId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void alreadyDisconnectedIdentifiableTerminal(ReportNode reportNode, String identifiableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-alreadyDisconnectedTerminal", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("identifiable", identifiableId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void parentHasBothRatioAndPhaseTapChanger(ReportNode reportNode, String parentMessage) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-validationWarningBothRatioPhase", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("parent", parentMessage)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void parentHasDuplicatePointForActivePower(ReportNode reportNode, String ownerMessage, Double p) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-validationWarningDuplicate", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("parent", ownerMessage)
                .withUntypedValue("p", p)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static ReportNode runIidmNetworkValidationCHecks(ReportNode reportNode, String networkId) {
        return Objects.requireNonNull(reportNode).newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-IIDMValidation", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("networkId", networkId)
                .add();
    }

    // DEBUG
    public static void inconsistentPropertyValues(ReportNode reportNode, String propertyName, String propertyValue1, String propertyValue2, String danglingLineId1, String danglingLineId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-InconsistentPropertyValues", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("propertyName", propertyName)
                .withUntypedValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withUntypedValue("propertyValue1", propertyValue1)
                .withUntypedValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withUntypedValue("propertyValue2", propertyValue2)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    public static void moveCommonAliases(ReportNode reportNode, String alias, String danglingLineId1, String danglingLineId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-MoveCommonAlias", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("alias", alias)
                .withUntypedValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withUntypedValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    public static void propertyOnlyOnOneSide(ReportNode reportNode, String propertyName, String propertyValue, int emptySide, String danglingLineId1, String danglingLineId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-PropertyOnlyOnOneSide", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("propertyName", propertyName)
                .withUntypedValue("side", emptySide)
                .withUntypedValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withUntypedValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withUntypedValue("propertyValue", propertyValue)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    // WARN
    public static void inconsistentAliasTypes(ReportNode reportNode, String alias, String type1, String type2, String danglingLineId1, String danglingLineId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-InconsistentAliasTypes", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("alias", alias)
                .withUntypedValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withUntypedValue("type1", type1)
                .withUntypedValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withUntypedValue("type2", type2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void inconsistentAliasValues(ReportNode reportNode, String alias1, String alias2, String type, String danglingLineId1, String danglingLineId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-InconsistentAliasValues", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("alias1", alias1)
                .withUntypedValue("alias2", alias2)
                .withUntypedValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withUntypedValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withUntypedValue("type", type)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static ReportNode createChildReportNode(ReportNode reportNode, ReadOnlyDataSource ds) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-importDataSource", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("dataSource", ds.getBaseName())
                .add();
    }

    public static void exportMock(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-export_test", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .add();
    }

    public static void testImportPostProcessor(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-network-testImportPostProcessor", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .add();
    }
}
