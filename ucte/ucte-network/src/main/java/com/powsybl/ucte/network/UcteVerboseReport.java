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
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(UcteReport.class)
public class UcteVerboseReport implements UcteReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteReport.class);

    @Override
    public String getName() {
        return "Verbose";
    }

    @Override
    public void log() {
        // Nothing to do
    }

    @Override
    public void onElementWithInvalidCurrentLimit(UcteElementId id, int currentLimit) {
        LOGGER.warn("Invalid current limit {} for element '{}'", currentLimit, id);
    }

    @Override
    public void onElementWithMissingCurrentLimit(UcteElementId id) {
        LOGGER.warn("Missing current limit for element '{}'", id);
    }

    @Override
    public void onElementWithSmallReactance(UcteElementId id, float oldReactance, float newReactance) {
        LOGGER.warn("Small reactance {} of element '{}' fixed to {}", oldReactance, id, newReactance);
    }

    @Override
    public void onNodeWithUndefinedActivePowerGeneration(UcteNodeCode id, float newActivePowerGeneration) {
        LOGGER.warn("Node {}: active power is undefined, set value to {}", id, newActivePowerGeneration);
    }

    @Override
    public void onNodeWithUndefinedMinimumActivePower(UcteNodeCode id, float newMinimumPermissibleActivePowerGeneration) {
        LOGGER.warn("Node {}: minimum active power is undefined, set value to {}", id, newMinimumPermissibleActivePowerGeneration);
    }

    @Override
    public void onNodeWithUndefinedMaximumActivePower(UcteNodeCode id, float newMaximumPermissibleActivePowerGeneration) {
        LOGGER.warn("Node {}: maximum active power is undefined, set value to {}", id, newMaximumPermissibleActivePowerGeneration);
    }

    @Override
    public void onNodeWithInvertedActivePowerLimits(UcteNodeCode id, float minimumPermissibleActivePowerGeneration, float maximumPermissibleActivePowerGeneration) {
        LOGGER.warn("Node {}: active power limits are inverted ({}, {}), swap values",
                id, minimumPermissibleActivePowerGeneration, maximumPermissibleActivePowerGeneration);
    }

    @Override
    public void onNodeWithActivePowerUnderMaximumPermissibleValue(UcteNodeCode id, float activePowerGeneration, float oldMaximumPermissibleActivePowerGeneration, float newMaximumPermissibleActivePowerGeneration) {
        LOGGER.warn("Node {}: active power {} under maximum permissible value {}, set maximum permissible value to {}",
                id, activePowerGeneration, oldMaximumPermissibleActivePowerGeneration, newMaximumPermissibleActivePowerGeneration);
    }

    @Override
    public void onNodeWithActivePowerAboveMinimumPermissibleValue(UcteNodeCode id, float activePowerGeneration, float oldMinimumPermissibleActivePowerGeneration, float newMinimumPermissibleActivePowerGeneration) {
        LOGGER.warn("Node {}: active power {} above minimum permissible value {}, set minimum permissible value to {}",
                id, activePowerGeneration, oldMinimumPermissibleActivePowerGeneration, newMinimumPermissibleActivePowerGeneration);
    }

    @Override
    public void onNodeWithFlatActiveLimits(UcteNodeCode id, float oldMinimumPermissibleActivePowerGeneration, float oldMaximumPermissibleActivePowerGeneration, float newMinimumPermissibleActivePowerGeneration, float newMaximumPermissibleActivePowerGeneration) {
        LOGGER.warn("Node {}: flat active limits [{}, {}], set values to [{}, {}]",
                id, oldMinimumPermissibleActivePowerGeneration, oldMaximumPermissibleActivePowerGeneration,
                newMinimumPermissibleActivePowerGeneration, newMaximumPermissibleActivePowerGeneration);
    }

    @Override
    public void onNodeRegulatingVoltageWithNullSetpoint(UcteNodeCode id, float voltageReference, UcteNodeTypeCode oldTypeCode, UcteNodeTypeCode newTypeCode) {
        LOGGER.warn("Node {}: voltage is regulated, but voltage setpoint is null ({}), switch type code from {} to {}",
                id, voltageReference, oldTypeCode, newTypeCode);
    }

    @Override
    public void onNodeNotRegulatingVoltageWithUndefinedReactivePowerCount(UcteNodeCode id, float newReactivePowerGeneration) {
        LOGGER.warn("Node {}: voltage is not regulated but reactive power is undefined, set value to {}", id, newReactivePowerGeneration);
    }

    @Override
    public void onNodeWithUndefinedMinimumReactivePower(UcteNodeCode id, float newMinimumPermissibleReactivePowerGeneration) {
        LOGGER.warn("Node {}: minimum reactive power is undefined, set value to {}", id, newMinimumPermissibleReactivePowerGeneration);
    }

    @Override
    public void onNodeWithUndefinedMaximumReactivePower(UcteNodeCode id, float newMaximumPermissibleReactivePowerGeneration) {
        LOGGER.warn("Node {}: maximum reactive power is undefined, set value to {}", id, newMaximumPermissibleReactivePowerGeneration);
    }

    @Override
    public void onNodeWithInvertedReactivePowerLimits(UcteNodeCode id, float minimumPermissibleReactivePowerGeneration, float maximumPermissibleReactivePowerGeneration) {
        LOGGER.warn("Node {}: reactive power limits are inverted ({}, {}), swap values",
                id, minimumPermissibleReactivePowerGeneration, maximumPermissibleReactivePowerGeneration);
    }

    @Override
    public void onNodeWithReactivePowerUnderMaximumPermissibleValue(UcteNodeCode id, float reactivePowerGeneration, float oldMaximumPermissibleReactivePowerGeneration, float newMaximumPermissibleReactivePowerGeneration) {
        LOGGER.warn("Node {}: reactive power {} under maximum permissible value {}, set maximum permissible value to {}",
                id, reactivePowerGeneration, oldMaximumPermissibleReactivePowerGeneration, newMaximumPermissibleReactivePowerGeneration);
    }

    @Override
    public void onNodeWithReactivePowerAboveMinimumPermissibleValue(UcteNodeCode id, float reactivePowerGeneration, float oldMinimumPermissibleReactivePowerGeneration, float newMinimumPermissibleReactivePowerGeneration) {
        LOGGER.warn("Node {}: reactive power {} above minimum permissible value {}, set minimum permissible value to {}",
                id, reactivePowerGeneration, oldMinimumPermissibleReactivePowerGeneration, newMinimumPermissibleReactivePowerGeneration);
    }

    @Override
    public void onNodeWithTooHighMinimumReactivePower(UcteNodeCode id, float oldMinimumPermissibleReactivePowerGeneration, float newMinimumPermissibleReactivePowerGeneration) {
        LOGGER.warn("Node {}: minimum reactive power is too high {}, set value to {}", id, oldMinimumPermissibleReactivePowerGeneration, newMinimumPermissibleReactivePowerGeneration);
    }

    @Override
    public void onNodeWithTooHighMaximumReactivePower(UcteNodeCode id, float oldMaximumPermissibleReactivePowerGeneration, float newMaximumPermissibleReactivePowerGeneration) {
        LOGGER.warn("Node {}: maximum reactive power is too high {}, set value to {}", id, oldMaximumPermissibleReactivePowerGeneration, newMaximumPermissibleReactivePowerGeneration);
    }

    @Override
    public void onNodeWithFlatReactiveLimits(UcteNodeCode id, float oldMinimumPermissibleReactivePowerGeneration, float oldMaximumPermissibleReactivePowerGeneration, float newMinimumPermissibleReactivePowerGeneration, float newMaximumPermissibleReactivePowerGeneration) {
        LOGGER.warn("Node {}: flat reactive limits [{}, {}), set values to [{}, {}]",
                id, oldMinimumPermissibleReactivePowerGeneration, oldMaximumPermissibleReactivePowerGeneration,
                newMinimumPermissibleReactivePowerGeneration, newMaximumPermissibleReactivePowerGeneration);
    }

    @Override
    public void onPhaseRegulationWithBadVoltageSetpoint(UcteElementId id, float oldVoltageSetpoint, float newVoltageSetpoint) {
        LOGGER.warn("Phase regulation of transformer '{}' has a bad target voltage {}, set to undefined", id, oldVoltageSetpoint);
    }

    @Override
    public void onIncompletePhaseRegulation(UcteElementId id) {
        LOGGER.warn("Phase regulation of transformer '{}' removed because incomplete", id);
    }

    @Override
    public void onIncompleteAngleRegulation(UcteElementId id) {
        LOGGER.warn("Angle regulation of transformer '{}' removed because incomplete", id);
    }

    @Override
    public void onAngleRegulationWithNoType(UcteElementId id, UcteAngleRegulationType newAngleRegulationType) {
        LOGGER.warn("Type is missing for angle regulation of transformer '{}', default to {}", id, newAngleRegulationType);
    }
}
