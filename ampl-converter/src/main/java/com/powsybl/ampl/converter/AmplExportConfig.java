/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.ampl.converter.version.AmplExportVersion;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class AmplExportConfig {

    public enum ExportScope {
        ALL,
        ONLY_MAIN_CC,
        ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS,
        ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS_AND_ALL_LOADS,
    }

    public enum ExportActionType {
        CURATIVE("curative"),
        PREVENTIVE("preventive");

        private final String label;

        ExportActionType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private ExportScope exportScope;

    private AmplExportVersion version;

    private boolean exportXNodes;

    private ExportActionType actionType;

    private boolean exportRatioTapChangerVoltageTarget;

    private boolean twtSplitShuntAdmittance;

    private boolean exportSorted;

    public AmplExportConfig(ExportScope exportScope, boolean exportXNodes, ExportActionType actionType) {
        this(exportScope, exportXNodes, actionType, false, false, AmplExportVersion.defaultVersion(), false);
    }

    public AmplExportConfig(ExportScope exportScope, boolean exportXNodes, ExportActionType actionType,
                            boolean exportRatioTapChangerVoltageTarget, boolean twtSplitShuntAdmittance) {
        this(exportScope, exportXNodes, actionType, exportRatioTapChangerVoltageTarget, twtSplitShuntAdmittance,
            AmplExportVersion.defaultVersion(), false);
    }

    public AmplExportConfig(ExportScope exportScope, boolean exportXNodes, ExportActionType actionType,
                            boolean exportRatioTapChangerVoltageTarget, boolean twtSplitShuntAdmittance,
                            AmplExportVersion version) {
        this(exportScope, exportXNodes, actionType, exportRatioTapChangerVoltageTarget, twtSplitShuntAdmittance, version, false);
    }

    public AmplExportConfig(ExportScope exportScope, boolean exportXNodes, ExportActionType actionType, boolean exportRatioTapChangerVoltageTarget, boolean twtSplitShuntAdmittance, AmplExportVersion version, boolean exportSorted) {
        this.exportScope = Objects.requireNonNull(exportScope);
        this.exportXNodes = exportXNodes;
        this.actionType = Objects.requireNonNull(actionType);
        this.exportRatioTapChangerVoltageTarget = exportRatioTapChangerVoltageTarget;
        this.twtSplitShuntAdmittance = twtSplitShuntAdmittance;
        this.version = Objects.requireNonNull(version);
        this.exportSorted = exportSorted;
    }

    public ExportScope getExportScope() {
        return exportScope;
    }

    public AmplExportConfig setExportScope(ExportScope exportScope) {
        this.exportScope = Objects.requireNonNull(exportScope);
        return this;
    }

    public boolean isExportXNodes() {
        return exportXNodes;
    }

    public AmplExportConfig setExportXNodes(boolean exportXNodes) {
        this.exportXNodes = exportXNodes;
        return this;
    }

    public ExportActionType getActionType() {
        return actionType;
    }

    public AmplExportConfig setActionType(ExportActionType actionType) {
        this.actionType = Objects.requireNonNull(actionType);
        return this;
    }

    public boolean isExportRatioTapChangerVoltageTarget() {
        return exportRatioTapChangerVoltageTarget;
    }

    public AmplExportConfig setExportRatioTapChangerVoltageTarget(boolean exportRatioTapChangerVoltageTarget) {
        this.exportRatioTapChangerVoltageTarget = exportRatioTapChangerVoltageTarget;
        return this;
    }

    public boolean isTwtSplitShuntAdmittance() {
        return twtSplitShuntAdmittance;
    }

    public AmplExportConfig setTwtSplitShuntAdmittance(boolean twtSplitShuntAdmittance) {
        this.twtSplitShuntAdmittance = twtSplitShuntAdmittance;
        return this;
    }

    public AmplExportVersion getVersion() {
        return version;
    }

    public AmplExportConfig setVersion(AmplExportVersion version) {
        this.version = Objects.requireNonNull(version);
        return this;
    }

    public boolean isExportSorted() {
        return exportSorted;
    }

    public AmplExportConfig setExportSorted(boolean exportSorted) {
        this.exportSorted = exportSorted;
        return this;
    }

}
