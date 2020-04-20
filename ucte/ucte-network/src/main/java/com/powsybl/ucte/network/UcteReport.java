/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteReport.class);

    private int nodeWithUndefinedActivePowerCount = 0;

    private int nodeWithUndefinedMinimumActivePowerCount = 0;

    private int nodeWithUndefinedMaximumActivePowerCount = 0;

    private int nodeWithFlatActiveLimitsCount = 0;

    private int nodeNotRegulatingVoltageWithUndefinedReactivePowerCount = 0;

    private int nodeWithUndefinedMinimumReactivePowerCount = 0;

    private int nodeWithUndefinedMaximumReactivePowerCount = 0;

    private int nodeWithInvertedReactivePowerLimitsCount = 0;

    private int nodeWithReactivePowerUnderMaximumPermissibleValueCount = 0;

    private int nodeWithReactivePowerAboveMinimumPermissibleValueCount = 0;

    private int nodeWithFlatReactiveLimitsCount = 0;

    public void addNodeWithUndefinedActivePower() {
        nodeWithUndefinedActivePowerCount++;
    }

    public void addNodeWithUndefinedMinimumActivePower() {
        nodeWithUndefinedMinimumActivePowerCount++;
    }

    public void addNodeWithUndefinedMaximumActivePower() {
        nodeWithUndefinedMaximumActivePowerCount++;
    }

    public void addNodeWithFlatActiveLimits() {
        nodeWithFlatActiveLimitsCount++;
    }

    public void addNodeNotRegulatingVoltageWithUndefinedReactivePowerCount() {
        nodeNotRegulatingVoltageWithUndefinedReactivePowerCount++;
    }

    public void addNodeWithUndefinedMinimumReactivePower() {
        nodeWithUndefinedMinimumReactivePowerCount++;
    }

    public void addNodeWithUndefinedMaximumReactivePower() {
        nodeWithUndefinedMaximumReactivePowerCount++;
    }

    public void addNodeWithInvertedReactivePowerLimits() {
        nodeWithInvertedReactivePowerLimitsCount++;
    }

    public void addNodeWithReactivePowerUnderMaximumPermissibleValue() {
        nodeWithReactivePowerUnderMaximumPermissibleValueCount++;
    }

    public void addNodeWithReactivePowerAboveMinimumPermissibleValue() {
        nodeWithReactivePowerAboveMinimumPermissibleValueCount++;
    }

    public void addNodeWithFlatReactiveLimits() {
        nodeWithFlatReactiveLimitsCount++;
    }

    public void log() {
        if (nodeWithUndefinedActivePowerCount > 0) {
            LOGGER.warn("{} nodes have an undefined active power", nodeWithUndefinedActivePowerCount);
        }

        if (nodeWithUndefinedMinimumActivePowerCount > 0) {
            LOGGER.warn("{} nodes have an undefined minimum active power", nodeWithUndefinedMinimumActivePowerCount);
        }

        if (nodeWithUndefinedMaximumActivePowerCount > 0) {
            LOGGER.warn("{} nodes have an undefined maximum active power", nodeWithUndefinedMaximumActivePowerCount);
        }

        if (nodeNotRegulatingVoltageWithUndefinedReactivePowerCount > 0) {
            LOGGER.warn("{} nodes not regulating voltage with an undefined reactive power", nodeNotRegulatingVoltageWithUndefinedReactivePowerCount);
        }

        if (nodeWithUndefinedMinimumReactivePowerCount > 0) {
            LOGGER.warn("{} nodes have an undefined minimum reactive power", nodeWithUndefinedMinimumReactivePowerCount);
        }

        if (nodeWithUndefinedMaximumReactivePowerCount > 0) {
            LOGGER.warn("{} nodes have an undefined maximum reactive power", nodeWithUndefinedMaximumReactivePowerCount);
        }

        if (nodeWithInvertedReactivePowerLimitsCount > 0) {
            LOGGER.warn("{} nodes have inverted reactive power limits", nodeWithInvertedReactivePowerLimitsCount);
        }

        if (nodeWithReactivePowerUnderMaximumPermissibleValueCount > 0) {
            LOGGER.warn("{} nodes have reactive power under maximum permissible value", nodeWithReactivePowerUnderMaximumPermissibleValueCount);
        }

        if (nodeWithReactivePowerAboveMinimumPermissibleValueCount > 0) {
            LOGGER.warn("{} nodes have reactive power above minimum permissible value", nodeWithReactivePowerAboveMinimumPermissibleValueCount);
        }

        if (nodeWithFlatActiveLimitsCount > 0) {
            LOGGER.warn("{} nodes have flat active limits", nodeWithFlatActiveLimitsCount);
        }

        if (nodeWithFlatReactiveLimitsCount > 0) {
            LOGGER.warn("{} nodes have flat reactive limits", nodeWithFlatReactiveLimitsCount);
        }
    }
}
