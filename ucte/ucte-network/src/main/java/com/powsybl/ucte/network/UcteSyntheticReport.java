/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(UcteReport.class)
public class UcteSyntheticReport implements UcteReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteSyntheticReport.class);

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

    @Override
    public String getName() {
        return "Synthetic";
    }

    @Override
    public void onElementWithInvalidCurrentLimit(UcteElementId id, int currentLimit) {
        elementWithInvalidCurrentLimitCount++;
    }

    @Override
    public void onElementWithMissingCurrentLimit(UcteElementId id) {
        elementWithMissingCurrentLimitCount++;
    }

    @Override
    public void onElementWithSmallReactance(UcteElementId id, float oldReactance, float newReactance) {
        elementWithSmallReactanceCount++;
    }

    @Override
    public void onNodeWithUndefinedActivePowerGeneration(UcteNodeCode id, float newActivePowerGeneration) {
        nodeWithUndefinedActivePowerCount++;
    }

    @Override
    public void onNodeWithUndefinedMinimumActivePower(UcteNodeCode id, float newMinimumPermissibleActivePowerGeneration) {
        nodeWithUndefinedMinimumActivePowerCount++;
    }

    @Override
    public void onNodeWithUndefinedMaximumActivePower(UcteNodeCode id, float newMaximumPermissibleActivePowerGeneration) {
        nodeWithUndefinedMaximumActivePowerCount++;
    }

    @Override
    public void onNodeWithInvertedActivePowerLimits(UcteNodeCode id, float minimumPermissibleActivePowerGeneration, float maximumPermissibleActivePowerGeneration) {
        nodeWithInvertedActivePowerLimitsCount++;
    }

    @Override
    public void onNodeWithActivePowerUnderMaximumPermissibleValue(UcteNodeCode id, float activePowerGeneration, float oldMaximumPermissibleActivePowerGeneration, float newMaximumPermissibleActivePowerGeneration) {
        nodeWithActivePowerUnderMaximumPermissibleValueCount++;
    }

    @Override
    public void onNodeWithActivePowerAboveMinimumPermissibleValue(UcteNodeCode id, float activePowerGeneration, float oldMinimumPermissibleActivePowerGeneration, float newMinimumPermissibleActivePowerGeneration) {
        nodeWithActivePowerAboveMinimumPermissibleValueCount++;
    }

    @Override
    public void onNodeWithFlatActiveLimits(UcteNodeCode id, float oldMinimumPermissibleActivePowerGeneration, float oldMaximumPermissibleActivePowerGeneration, float newMinimumPermissibleActivePowerGeneration, float newMaximumPermissibleActivePowerGeneration) {
        nodeWithFlatActiveLimitsCount++;
    }

    @Override
    public void onNodeRegulatingVoltageWithNullSetpoint(UcteNodeCode id, float voltageReference, UcteNodeTypeCode oldTypeCode, UcteNodeTypeCode newTypeCode) {
        nodeRegulatingVoltageWithNullSetpointCount++;
    }

    @Override
    public void onNodeNotRegulatingVoltageWithUndefinedReactivePowerCount(UcteNodeCode id, float newReactivePowerGeneration) {
        nodeNotRegulatingVoltageWithUndefinedReactivePowerCount++;
    }

    @Override
    public void onNodeWithUndefinedMinimumReactivePower(UcteNodeCode id, float newMinimumPermissibleReactivePowerGeneration) {
        nodeWithUndefinedMinimumReactivePowerCount++;
    }

    @Override
    public void onNodeWithUndefinedMaximumReactivePower(UcteNodeCode id, float newMaximumPermissibleReactivePowerGeneration) {
        nodeWithUndefinedMaximumReactivePowerCount++;
    }

    @Override
    public void onNodeWithInvertedReactivePowerLimits(UcteNodeCode id, float minimumPermissibleReactivePowerGeneration, float maximumPermissibleReactivePowerGeneration) {
        nodeWithInvertedReactivePowerLimitsCount++;
    }

    @Override
    public void onNodeWithReactivePowerUnderMaximumPermissibleValue(UcteNodeCode id, float reactivePowerGeneration, float oldMaximumPermissibleReactivePowerGeneration, float newMaximumPermissibleReactivePowerGeneration) {
        nodeWithReactivePowerUnderMaximumPermissibleValueCount++;
    }

    @Override
    public void onNodeWithReactivePowerAboveMinimumPermissibleValue(UcteNodeCode id, float reactivePowerGeneration, float oldMinimumPermissibleReactivePowerGeneration, float newMinimumPermissibleReactivePowerGeneration) {
        nodeWithReactivePowerAboveMinimumPermissibleValueCount++;
    }

    @Override
    public void onNodeWithTooHighMinimumReactivePower(UcteNodeCode id, float oldMinimumPermissibleReactivePowerGeneration, float newMinimumPermissibleReactivePowerGeneration) {
        nodeWithTooHighMinimumReactivePowerCount++;
    }

    @Override
    public void onNodeWithTooHighMaximumReactivePower(UcteNodeCode id, float oldMaximumPermissibleReactivePowerGeneration, float newMaximumPermissibleReactivePowerGeneration) {
        nodeWithTooHighMaximumReactivePowerCount++;
    }

    @Override
    public void onNodeWithFlatReactiveLimits(UcteNodeCode id, float oldMinimumPermissibleReactivePowerGeneration, float oldMaximumPermissibleReactivePowerGeneration, float newMinimumPermissibleReactivePowerGeneration, float newMaximumPermissibleReactivePowerGeneration) {
        nodeWithFlatReactiveLimitsCount++;
    }

    @Override
    public void onPhaseRegulationWithBadVoltageSetpoint(UcteElementId id, float oldVoltageSetpoint, float newVoltageSetpoint) {
        phaseRegulationWithBadTargetVoltageCount++;
    }

    @Override
    public void onIncompletePhaseRegulation(UcteElementId id) {
        incompletePhaseRegulationCount++;
    }

    @Override
    public void onIncompleteAngleRegulation(UcteElementId id) {
        incompleteAngleRegulationCount++;
    }

    @Override
    public void onAngleRegulationWithNoType(UcteElementId id, UcteAngleRegulationType newAngleRegulationType) {
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
