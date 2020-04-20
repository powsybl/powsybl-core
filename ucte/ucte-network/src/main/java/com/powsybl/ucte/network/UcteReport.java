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

    private int nodeWithInvertedActivePowerLimitsCount = 0;

    private int nodeWithActivePowerUnderMaximumPermissibleValueCount = 0;

    private int nodeWithActivePowerAboveMinimumPermissibleValueCount = 0;

    private int nodeWithFlatActiveLimitsCount = 0;

    private int nodeRegulatingVoltageWithNullSetpointCount = 0;

    private int nodeNotRegulatingVoltageWithUndefinedReactivePowerCount = 0;

    private int nodeWithUndefinedMinimumReactivePowerCount = 0;

    private int nodeWithUndefinedMaximumReactivePowerCount = 0;

    private int nodeWithInvertedReactivePowerLimitsCount = 0;

    private int nodeWithReactivePowerUnderMaximumPermissibleValueCount = 0;

    private int nodeWithReactivePowerAboveMinimumPermissibleValueCount = 0;

    private int nodeWithTooHighMinimumReactivePowerCount = 0;

    private int nodeWithTooHighMaximumReactivePowerCount = 0;

    private int nodeWithFlatReactiveLimitsCount = 0;

    private int elementWithSmallReactanceCount = 0;

    private int elementWithMissingCurrentLimitCount = 0;

    private int elementWithInvalidCurrentLimitCount = 0;

    private int phaseRegulationWithBadTargetVoltageCount = 0;

    private int incompletePhaseRegulationCount = 0;

    private int incompleteAngleRegulationCount = 0;

    private int angleRegulationWithNoTypeCount = 0;

    public void addNodeWithUndefinedActivePower() {
        nodeWithUndefinedActivePowerCount++;
    }

    public void addNodeWithUndefinedMinimumActivePower() {
        nodeWithUndefinedMinimumActivePowerCount++;
    }

    public void addNodeWithUndefinedMaximumActivePower() {
        nodeWithUndefinedMaximumActivePowerCount++;
    }

    public void addNodeWithInvertedActivePowerLimits() {
        nodeWithInvertedActivePowerLimitsCount++;
    }

    public void addNodeWithActivePowerUnderMaximumPermissibleValue() {
        nodeWithActivePowerUnderMaximumPermissibleValueCount++;
    }

    public void addNodeWithActivePowerAboveMinimumPermissibleValue() {
        nodeWithActivePowerAboveMinimumPermissibleValueCount++;
    }

    public void addNodeWithFlatActiveLimits() {
        nodeWithFlatActiveLimitsCount++;
    }

    public void addNodeRegulatingVoltageWithNullSetpoint() {
        nodeRegulatingVoltageWithNullSetpointCount++;
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

    public void addNodeWithTooHighMinimumReactivePower() {
        nodeWithTooHighMinimumReactivePowerCount++;
    }

    public void addNodeWithTooHighMaximumReactivePower() {
        nodeWithTooHighMaximumReactivePowerCount++;
    }

    public void addNodeWithFlatReactiveLimits() {
        nodeWithFlatReactiveLimitsCount++;
    }

    public void addElementWithSmallReactance() {
        elementWithSmallReactanceCount++;
    }

    public void addElementWithMissingCurrentLimit() {
        elementWithMissingCurrentLimitCount++;
    }

    public void addElementWithInvalidCurrentLimit() {
        elementWithInvalidCurrentLimitCount++;
    }

    public void addPhaseRegulationWithBadTargetVoltage() {
        phaseRegulationWithBadTargetVoltageCount++;
    }

    public void addIncompletePhaseRegulation() {
        incompletePhaseRegulationCount++;
    }

    public void addIncompleteAngleRegulation() {
        incompleteAngleRegulationCount++;
    }

    public void addAngleRegulationWithNoType() {
        angleRegulationWithNoTypeCount++;
    }

    private void logNodeActivePower() {
        if (nodeWithUndefinedActivePowerCount > 0) {
            LOGGER.warn("{} nodes have an undefined active power", nodeWithUndefinedActivePowerCount);
        }

        if (nodeWithUndefinedMinimumActivePowerCount > 0) {
            LOGGER.warn("{} nodes have an undefined minimum active power", nodeWithUndefinedMinimumActivePowerCount);
        }

        if (nodeWithUndefinedMaximumActivePowerCount > 0) {
            LOGGER.warn("{} nodes have an undefined maximum active power", nodeWithUndefinedMaximumActivePowerCount);
        }

        if (nodeWithInvertedActivePowerLimitsCount > 0) {
            LOGGER.warn("{} nodes have inverted active power limits", nodeWithInvertedActivePowerLimitsCount);
        }

        if (nodeWithActivePowerUnderMaximumPermissibleValueCount > 0) {
            LOGGER.warn("{} nodes have active power under maximum permissible value", nodeWithActivePowerUnderMaximumPermissibleValueCount);
        }

        if (nodeWithActivePowerAboveMinimumPermissibleValueCount > 0) {
            LOGGER.warn("{} nodes have active power above minimum permissible value", nodeWithActivePowerAboveMinimumPermissibleValueCount);
        }

        if (nodeWithFlatActiveLimitsCount > 0) {
            LOGGER.warn("{} nodes have flat active limits", nodeWithFlatActiveLimitsCount);
        }
    }

    private void logNodeVoltage() {
        if (nodeRegulatingVoltageWithNullSetpointCount > 0) {
            LOGGER.warn("{} nodes have voltage regulation with a null setpoint", nodeRegulatingVoltageWithNullSetpointCount);
        }
    }

    private void logNodeReactivePower() {
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

        if (nodeWithTooHighMinimumReactivePowerCount > 0) {
            LOGGER.warn("{} nodes have a too high minimum reactive power", nodeWithTooHighMinimumReactivePowerCount);
        }

        if (nodeWithTooHighMaximumReactivePowerCount > 0) {
            LOGGER.warn("{} nodes have a too high maximum reactive power", nodeWithTooHighMaximumReactivePowerCount);
        }

        if (nodeWithFlatReactiveLimitsCount > 0) {
            LOGGER.warn("{} nodes have flat reactive limits", nodeWithFlatReactiveLimitsCount);
        }
    }

    private void logElement() {
        if (elementWithSmallReactanceCount > 0) {
            LOGGER.warn("{} elements have a small reactance", elementWithSmallReactanceCount);
        }

        if (elementWithMissingCurrentLimitCount > 0) {
            LOGGER.warn("{} elements have a missing current limit", elementWithMissingCurrentLimitCount);
        }

        if (elementWithInvalidCurrentLimitCount > 0) {
            LOGGER.warn("{} elements have an invalid current limit", elementWithInvalidCurrentLimitCount);
        }
    }

    private void logRegulation() {
        if (phaseRegulationWithBadTargetVoltageCount > 0) {
            LOGGER.warn("{} phase regulations have a bad target voltage", phaseRegulationWithBadTargetVoltageCount);
        }

        if (incompletePhaseRegulationCount > 0) {
            LOGGER.warn("{} phase regulations are incomplete", incompletePhaseRegulationCount);
        }

        if (incompleteAngleRegulationCount > 0) {
            LOGGER.warn("{} angle regulations are incomplete", incompleteAngleRegulationCount);
        }

        if (angleRegulationWithNoTypeCount > 0) {
            LOGGER.warn("{} angle regulations do not have a type", angleRegulationWithNoTypeCount);
        }
    }

    public void log() {
        logNodeActivePower();
        logNodeVoltage();
        logNodeReactivePower();
        logElement();
        logRegulation();
    }
}
