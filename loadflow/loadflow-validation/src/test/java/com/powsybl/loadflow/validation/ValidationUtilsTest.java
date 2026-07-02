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
        assertFalse(ValidationUtils.isOutsideTolerance(10, 11, 1));
        assertTrue(ValidationUtils.isOutsideTolerance(10.0, 10.02, 0.01));
    }

    @Test
    void isOutsideOrAtToleranceShouldSucceed() {
        assertFalse(ValidationUtils.isOutsideOrAtTolerance(10.0, 10.001, 0.01));
        assertTrue(ValidationUtils.isOutsideOrAtTolerance(10, 11, 1));
        assertTrue(ValidationUtils.isOutsideOrAtTolerance(10.0, 10.02, 0.01));
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
}
