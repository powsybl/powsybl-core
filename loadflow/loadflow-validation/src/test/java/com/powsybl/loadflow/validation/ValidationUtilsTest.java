/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static java.lang.Double.NaN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        assertTrue(ValidationUtils.areNaN(looseConfig, NaN));
        assertTrue(ValidationUtils.areNaN(looseConfig, NaN, 2d, .004d));
        assertTrue(ValidationUtils.areNaN(looseConfig, NaN, 2d, NaN));

        looseConfig.setOkMissingValues(true);
        assertFalse(ValidationUtils.areNaN(looseConfig, 3.7f, 2f, .004f));
        assertFalse(ValidationUtils.areNaN(looseConfig, Float.NaN));
        assertFalse(ValidationUtils.areNaN(looseConfig, Float.NaN, 2f, .004f));
        assertFalse(ValidationUtils.areNaN(looseConfig, Float.NaN, 2f, Float.NaN));
        assertFalse(ValidationUtils.areNaN(looseConfig, 3.7d, 2d, .004d));
        assertFalse(ValidationUtils.areNaN(looseConfig, NaN));
        assertFalse(ValidationUtils.areNaN(looseConfig, NaN, 2d, .004d));
        assertFalse(ValidationUtils.areNaN(looseConfig, NaN, 2d, NaN));
    }

    @Test
    void boundedWithin() {
        assertTrue(ValidationUtils.boundedWithin(0.0, 10.0, 5.0, 0.0));
        assertFalse(ValidationUtils.boundedWithin(0.0, 10.0, -5.0, 0.0));
        assertFalse(ValidationUtils.boundedWithin(0.0, 10.0, 15.0, 0.0));

        assertFalse(ValidationUtils.boundedWithin(0.0, 10.0, NaN, 0.0));
        assertFalse(ValidationUtils.boundedWithin(NaN, NaN, 5.0, 0.0));

        assertTrue(ValidationUtils.boundedWithin(NaN, 10.0, 5.0, 0.0));
        assertFalse(ValidationUtils.boundedWithin(NaN, 10.0, 15.0, 0.0));

        assertTrue(ValidationUtils.boundedWithin(0.0, NaN, 5.0, 0.0));
        assertFalse(ValidationUtils.boundedWithin(0.0, NaN, -5.0, 0.0));
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
        assertTrue(ValidationUtils.isUndefinedOrZero(NaN, 0.01));
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
        // Given (config parameter CheckMainComponentOnly false)
        looseConfig.setCheckMainComponentOnly(false);
        // When Then
        assertTrue(ValidationUtils.isConnectedAndMainComponent(true, false, looseConfig));
        assertFalse(ValidationUtils.isConnectedAndMainComponent(false, false, looseConfig));
    }

    @ParameterizedTest
    @MethodSource("provideTerminalStateArgument")
    void getTerminalStateTest(Bus bus, Bus connectableBus, double expectedV, boolean expectedConnected, boolean expectedIsMainComponent) {
        // Given
        Terminal terminal = mock(Terminal.class);
        Terminal.BusView busView = mock(Terminal.BusView.class);
        when(busView.getBus()).thenReturn(bus);
        when(busView.getConnectableBus()).thenReturn(connectableBus);
        when(terminal.getBusView()).thenReturn(busView);
        // When
        ValidationUtils.TerminalState terminalState = ValidationUtils.getTerminalState(terminal);
        // Then
        assertEquals(expectedV, terminalState.v());
        assertEquals(expectedConnected, terminalState.connected());
        assertEquals(expectedIsMainComponent, terminalState.mainComponent());
    }

    static Stream<Arguments> provideTerminalStateArgument() {
        Bus bus1 = mock(Bus.class);
        Bus bus2 = mock(Bus.class);
        when(bus2.isInMainConnectedComponent()).thenReturn(true);
        return Stream.of(
                Arguments.of(null, null, NaN, false, false),
                Arguments.of(bus1, null, 0.0, true, false),
                Arguments.of(bus2, null, 0.0, true, true),
                Arguments.of(null, bus1, NaN, false, false),
                Arguments.of(null, bus2, NaN, false, true));
    }

}
