/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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

    private boolean exportXNodes;

    private ExportActionType actionType;

    private boolean exportRatioTapChangerVoltageTarget;

    public AmplExportConfig(ExportScope exportScope, boolean exportXNodes, ExportActionType actionType) {
        this(exportScope, exportXNodes, actionType, false);
    }

    public AmplExportConfig(ExportScope exportScope, boolean exportXNodes, ExportActionType actionType, boolean exportRatioTapChangerVoltageTarget) {
        this.exportScope = exportScope;
        this.exportXNodes = exportXNodes;
        this.actionType = actionType;
        this.exportRatioTapChangerVoltageTarget = exportRatioTapChangerVoltageTarget;
    }

    public ExportScope getExportScope() {
        return exportScope;
    }

    public void setExportScope(ExportScope exportScope) {
        this.exportScope = exportScope;
    }

    public boolean isExportXNodes() {
        return exportXNodes;
    }

    public void setExportXNodes(boolean exportXNodes) {
        this.exportXNodes = exportXNodes;
    }

    public ExportActionType getActionType() {
        return actionType;
    }

    public boolean isExportRatioTapChangerVoltageTarget() {
        return exportRatioTapChangerVoltageTarget;
    }

    public void setExportRatioTapChangerVoltageTarget(boolean exportRatioTapChangerVoltageTarget) {
        this.exportRatioTapChangerVoltageTarget = exportRatioTapChangerVoltageTarget;
    }

}
