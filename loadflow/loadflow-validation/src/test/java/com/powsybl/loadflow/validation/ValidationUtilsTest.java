/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class ValidationUtilsTest extends AbstractValidationTest {

    @Test
    void areNaN() {
        assertFalse(ValidationUtils.areNaN(looseConfig, 1.02f));
        assertFalse(ValidationUtils.areNaN(looseConfig, 1f, 3.5f));
        assertFalse(ValidationUtils.areNaN(looseConfig, 3.7f, 2f, .004f));
        assertTrue(ValidationUtils.areNaN(looseConfig, Float.NaN));
        assertTrue(ValidationUtils.areNaN(looseConfig, Float.NaN, 2f, .004f));
        assertTrue(ValidationUtils.areNaN(looseConfig, Float.NaN, 2f, Float.NaN));

        assertFalse(ValidationUtils.areNaN(looseConfig, 1.02d));
        assertFalse(ValidationUtils.areNaN(looseConfig, 1d, 3.5d));
        assertFalse(ValidationUtils.areNaN(looseConfig, 3.7d, 2d, .004d));
        assertTrue(ValidationUtils.areNaN(looseConfig, Double.NaN));
        assertTrue(ValidationUtils.areNaN(looseConfig, Double.NaN, 2d, .004d));
        assertTrue(ValidationUtils.areNaN(looseConfig, Double.NaN, 2d, Double.NaN));

        looseConfig.setOkMissingValues(true);
        assertFalse(ValidationUtils.areNaN(looseConfig, 3.7f, 2f, .004f));
        assertFalse(ValidationUtils.areNaN(looseConfig, Float.NaN));
        assertFalse(ValidationUtils.areNaN(looseConfig, Float.NaN, 2f, .004f));
        assertFalse(ValidationUtils.areNaN(looseConfig, Float.NaN, 2f, Float.NaN));
        assertFalse(ValidationUtils.areNaN(looseConfig, 3.7d, 2d, .004d));
        assertFalse(ValidationUtils.areNaN(looseConfig, Double.NaN));
        assertFalse(ValidationUtils.areNaN(looseConfig, Double.NaN, 2d, .004d));
        assertFalse(ValidationUtils.areNaN(looseConfig, Double.NaN, 2d, Double.NaN));
    }

    @Test
    void boundedWithin() {
        assertTrue(ValidationUtils.boundedWithin(0.0, 10.0, 5.0, 0.0));
        assertFalse(ValidationUtils.boundedWithin(0.0, 10.0, -5.0, 0.0));
        assertFalse(ValidationUtils.boundedWithin(0.0, 10.0, 15.0, 0.0));

        assertFalse(ValidationUtils.boundedWithin(0.0, 10.0, Double.NaN, 0.0));
        assertFalse(ValidationUtils.boundedWithin(Double.NaN, Double.NaN, 5.0, 0.0));

        assertTrue(ValidationUtils.boundedWithin(Double.NaN, 10.0, 5.0, 0.0));
        assertFalse(ValidationUtils.boundedWithin(Double.NaN, 10.0, 15.0, 0.0));

        assertTrue(ValidationUtils.boundedWithin(0.0, Double.NaN, 5.0, 0.0));
        assertFalse(ValidationUtils.boundedWithin(0.0, Double.NaN, -5.0, 0.0));
    }

    @Test
    void isMainComponentShouldSucceed() {
        assertTrue(ValidationUtils.isMainComponent(looseConfig, true));
        assertFalse(ValidationUtils.isMainComponent(looseConfig, false));

        looseConfig.setCheckMainComponentOnly(false);
        assertTrue(ValidationUtils.isMainComponent(looseConfig, true));
        assertTrue(ValidationUtils.isMainComponent(looseConfig, false));
    }

    @Test
    void isUndefinedOrZeroShouldSucceed() {
        assertTrue(ValidationUtils.isUndefinedOrZero(Double.NaN, 0.01));
        assertTrue(ValidationUtils.isUndefinedOrZero(0.0, 0.01));
        assertTrue(ValidationUtils.isUndefinedOrZero(0.01, 0.02));
        assertFalse(ValidationUtils.isUndefinedOrZero(0.02, 0.01));
    }

    @Test
    void isOutsideToleranceShouldSucceed() {
        assertFalse(ValidationUtils.isOutsideTolerance(10.0, 10.001, 0.01));
        assertTrue(ValidationUtils.isOutsideTolerance(10.0, 10.02, 0.01));
    }

    @Test
    void isConnectedAndValidatedShouldSucceed() {
        // Given (config parameter CheckMainComponentOnly true)
        // config parameter CheckMainComponentOnly true
        // When Then
        assertTrue(ValidationUtils.isConnectedAndMainComponent(true, true, looseConfig));
        assertFalse(ValidationUtils.isConnectedAndMainComponent(true, false, looseConfig));
        assertFalse(ValidationUtils.isConnectedAndMainComponent(false, true, looseConfig));
        //Given (config parameter CheckMainComponentOnly false)
        looseConfig.setCheckMainComponentOnly(false);
        // When Then
        assertTrue(ValidationUtils.isConnectedAndMainComponent(true, false, looseConfig));
        assertFalse(ValidationUtils.isConnectedAndMainComponent(false, false, looseConfig));
    }

    @Test
    void computeShuntExpectedQShouldSucceed() {
        assertEquals(18.0, ValidationUtils.computeShuntExpectedQ(-1.0, 2, 3.0));
        assertEquals(-18.0, ValidationUtils.computeShuntExpectedQ(1.0, 2, 3.0));
    }

    @Test
    void isActivePowerKoShouldSucceed() {
        // Given (parameter OkMissingValues false, Threshold: 0.01)
        double threshold = strictConfig.getThreshold();
        assertFalse(ValidationUtils.isActivePowerKo(-100.0, 100.0, strictConfig, threshold));
        assertTrue(ValidationUtils.isActivePowerKo(-100.02, 100.0, strictConfig, threshold));
        assertTrue(ValidationUtils.isActivePowerKo(0.0, Double.NaN, strictConfig, threshold));
        // Given (config parameter authorized missingValues true)
        strictConfig.setOkMissingValues(true);
        assertFalse(ValidationUtils.isActivePowerKo(0.0, Double.NaN, strictConfig, threshold));
    }

    @Test
    void isReactivePowerKoShouldSucceed() {
        double threshold = strictConfig.getThreshold(); // 0.01
        assertFalse(ValidationUtils.isReactivePowerKo(-50.0, 50.0, threshold));
        assertTrue(ValidationUtils.isReactivePowerKo(-50.02, 50.0, threshold));
    }

    @Test
    void isVoltageRegulationKoShouldSucceed() {
        double threshold = strictConfig.getThreshold();
        double minQ = -10.0;
        double maxQ = 20.0;
        double targetV = 400.0;
        // Case: V > targetV + threshold => qGen ~ minQ
        assertFalse(ValidationUtils.isVoltageRegulationKo(minQ, 401.0, targetV, minQ, maxQ, threshold));
        assertTrue(ValidationUtils.isVoltageRegulationKo(minQ + 0.02, 401.0, targetV, minQ, maxQ, threshold));
        // Case: V < targetV - threshold => qGen ~ maxQ
        assertFalse(ValidationUtils.isVoltageRegulationKo(maxQ, 399.0, targetV, minQ, maxQ, threshold));
        assertTrue(ValidationUtils.isVoltageRegulationKo(maxQ - 0.02, 399.0, targetV, minQ, maxQ, threshold));
        // Case: |V - targetV| <= threshold => qGen ~ [minQ, maxQ] [-10.0, 20.0]
        assertFalse(ValidationUtils.isVoltageRegulationKo(0.0, 400.0, targetV, minQ, maxQ, threshold));
        assertTrue(ValidationUtils.isVoltageRegulationKo(25.0, 400.0, targetV, minQ, maxQ, threshold));
    }

    @Test
    void isReactiveBoundInvertedShouldSucceed() {
        double threshold = 0.01;
        // Inverted ok + enabled => true
        assertTrue(ValidationUtils.isReactiveBoundInverted(10.0, 9.0, threshold, true));
        // Inverted ok + disabled => false
        assertFalse(ValidationUtils.isReactiveBoundInverted(10.0, 9.0, threshold, false));
        // Not inverted => false
        assertFalse(ValidationUtils.isReactiveBoundInverted(10.0, 10.0, threshold, true));
    }

    @Test
    void isSetpointOutsidePowerBoundsShouldSucceed() {
        double threshold = 0.01;
        // Outside lower + enabled => true
        assertTrue(ValidationUtils.isSetpointOutsidePowerBounds(9.98, 10.0, 20.0, threshold, true));
        // Outside upper + enabled => true
        assertTrue(ValidationUtils.isSetpointOutsidePowerBounds(20.02, 10.0, 20.0, threshold, true));
        // Outside + disabled => false
        assertFalse(ValidationUtils.isSetpointOutsidePowerBounds(20.02, 10.0, 20.0, threshold, false));
        // Not outside bounds => false
        assertFalse(ValidationUtils.isSetpointOutsidePowerBounds(15.0, 10.0, 20.0, threshold, true));
    }

}
