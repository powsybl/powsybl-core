/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.ucte.network;

import com.powsybl.commons.config.PlatformConfigNamedProvider;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public interface UcteReport extends PlatformConfigNamedProvider {

    void log();

    // - Element notifications ------------------------------------------------
    void onElementWithInvalidCurrentLimit(UcteElementId id, int currentLimit);

    void onElementWithMissingCurrentLimit(UcteElementId id);

    void onElementWithSmallReactance(UcteElementId id, float oldReactance, float newReactance);

    // - Node notifications ---------------------------------------------------
    void onNodeWithUndefinedActivePowerGeneration(UcteNodeCode id, float newActivePowerGeneration);

    void onNodeWithUndefinedMinimumActivePower(UcteNodeCode id, float newMinimumPermissibleActivePowerGeneration);

    void onNodeWithUndefinedMaximumActivePower(UcteNodeCode id, float newMaximumPermissibleActivePowerGeneration);

    void onNodeWithInvertedActivePowerLimits(UcteNodeCode id, float minimumPermissibleActivePowerGeneration, float maximumPermissibleActivePowerGeneration);

    void onNodeWithActivePowerUnderMaximumPermissibleValue(UcteNodeCode id, float activePowerGeneration, float oldMaximumPermissibleActivePowerGeneration, float newMaximumPermissibleActivePowerGeneration);

    void onNodeWithActivePowerAboveMinimumPermissibleValue(UcteNodeCode id, float activePowerGeneration, float oldMinimumPermissibleActivePowerGeneration, float newMinimumPermissibleActivePowerGeneration);

    void onNodeWithFlatActiveLimits(UcteNodeCode id, float oldMinimumPermissibleActivePowerGeneration, float oldMaximumPermissibleActivePowerGeneration, float newMinimumPermissibleActivePowerGeneration, float newMaximumPermissibleActivePowerGeneration);

    void onNodeRegulatingVoltageWithNullSetpoint(UcteNodeCode id, float voltageReference, UcteNodeTypeCode oldTypeCode, UcteNodeTypeCode newTypeCode);

    void onNodeNotRegulatingVoltageWithUndefinedReactivePowerCount(UcteNodeCode id, float newReactivePowerGeneration);

    void onNodeWithUndefinedMinimumReactivePower(UcteNodeCode id, float newMinimumPermissibleReactivePowerGeneration);

    void onNodeWithUndefinedMaximumReactivePower(UcteNodeCode id, float newMaximumPermissibleReactivePowerGeneration);

    void onNodeWithInvertedReactivePowerLimits(UcteNodeCode id, float minimumPermissibleReactivePowerGeneration, float maximumPermissibleReactivePowerGeneration);

    void onNodeWithReactivePowerUnderMaximumPermissibleValue(UcteNodeCode id, float reactivePowerGeneration, float oldMaximumPermissibleReactivePowerGeneration, float newMaximumPermissibleReactivePowerGeneration);

    void onNodeWithReactivePowerAboveMinimumPermissibleValue(UcteNodeCode id, float reactivePowerGeneration, float oldMinimumPermissibleReactivePowerGeneration, float newMinimumPermissibleReactivePowerGeneration);

    void onNodeWithTooHighMinimumReactivePower(UcteNodeCode id, float oldMinimumPermissibleReactivePowerGeneration, float newMinimumPermissibleReactivePowerGeneration);

    void onNodeWithTooHighMaximumReactivePower(UcteNodeCode id, float oldMaximumPermissibleReactivePowerGeneration, float newMaximumPermissibleReactivePowerGeneration);

    void onNodeWithFlatReactiveLimits(UcteNodeCode id, float oldMinimumPermissibleReactivePowerGeneration, float oldMaximumPermissibleReactivePowerGeneration, float newMinimumPermissibleReactivePowerGeneration, float newMaximumPermissibleReactivePowerGeneration);

    // - Phase regulation notifications ---------------------------------------
    void onPhaseRegulationWithBadVoltageSetpoint(UcteElementId id, float oldVoltageSetpoint, float newVoltageSetpoint);

    void onIncompletePhaseRegulation(UcteElementId id);

    void onIncompleteAngleRegulation(UcteElementId id);

    void onAngleRegulationWithNoType(UcteElementId id, UcteAngleRegulationType newAngleRegulationType);

}
