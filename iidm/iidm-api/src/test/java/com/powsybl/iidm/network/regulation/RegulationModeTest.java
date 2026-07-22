/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
class RegulationModeTest {

    @ParameterizedTest
    @EnumSource(RegulationMode.class)
    void fromIndexValidTest(RegulationMode regulationMode) {
        assertEquals(regulationMode, RegulationMode.fromIndex(regulationMode.getIndex()));
    }

    @Test
    void fromIndexInvalidTest() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RegulationMode.fromIndex(-1));
        assertEquals("Unknown or unsupported regulation mode index: -1. Allowed values are: [1, 2, 3]", exception.getMessage());
    }

    @Test
    void getIndexTest() {
        assertEquals(1, RegulationMode.getIndexFromMode(RegulationMode.VOLTAGE));
        assertNull(RegulationMode.getIndexFromMode(null));
    }

    @ParameterizedTest
    @MethodSource("holderTestCases")
    void getAllowedRegulationModesTest(boolean isRemote, Class<? extends VoltageRegulationHolder<?>> holderClass, Set<RegulationMode> expectedRemoteModes, Set<RegulationMode> expectedLocalModes) {
        Set<RegulationMode> expected = isRemote ? expectedRemoteModes : expectedLocalModes;
        assertEquals(expected, RegulationMode.getAllowedRegulationModes(isRemote, holderClass));
    }

    static Stream<Arguments> holderTestCases() {
        return Stream.of(
            Arguments.of(true, Battery.class, Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER), Set.of(RegulationMode.VOLTAGE)),
            Arguments.of(false, Battery.class, Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER), Set.of(RegulationMode.VOLTAGE)),
            Arguments.of(true, Generator.class, Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER), Set.of(RegulationMode.VOLTAGE)),
            Arguments.of(false, Generator.class, Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER), Set.of(RegulationMode.VOLTAGE)),
            Arguments.of(true, RatioTapChanger.class, Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER), Set.of()),
            Arguments.of(false, RatioTapChanger.class, Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER), Set.of()),
            Arguments.of(true, ShuntCompensator.class, Set.of(RegulationMode.VOLTAGE), Set.of(RegulationMode.VOLTAGE)),
            Arguments.of(false, ShuntCompensator.class, Set.of(RegulationMode.VOLTAGE), Set.of(RegulationMode.VOLTAGE)),
            Arguments.of(true, StaticVarCompensator.class,
                Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER, RegulationMode.VOLTAGE_PER_REACTIVE_POWER),
                Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER, RegulationMode.VOLTAGE_PER_REACTIVE_POWER)),
            Arguments.of(false, StaticVarCompensator.class,
                Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER, RegulationMode.VOLTAGE_PER_REACTIVE_POWER),
                Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER, RegulationMode.VOLTAGE_PER_REACTIVE_POWER)),
            Arguments.of(true, VscConverterStation.class, Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER), Set.of(RegulationMode.VOLTAGE)),
            Arguments.of(false, VscConverterStation.class, Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER), Set.of(RegulationMode.VOLTAGE)),
            Arguments.of(true, VoltageSourceConverter.class, Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER), Set.of(RegulationMode.VOLTAGE)),
            Arguments.of(false, VoltageSourceConverter.class, Set.of(RegulationMode.VOLTAGE, RegulationMode.REACTIVE_POWER), Set.of(RegulationMode.VOLTAGE))
        );
    }

    @Test
    void unsupportedHolderTest() {
        IllegalArgumentException exRemote = assertThrows(IllegalArgumentException.class, () ->
            RegulationMode.getAllowedRegulationModes(true, UnsupportedHolder.class)
        );
        assertEquals("UnsupportedHolder class cannot be used with VoltageRegulation", exRemote.getMessage());

        IllegalArgumentException exLocal = assertThrows(IllegalArgumentException.class, () ->
            RegulationMode.getAllowedRegulationModes(false, UnsupportedHolder.class)
        );
        assertEquals("UnsupportedHolder class cannot be used with VoltageRegulation", exLocal.getMessage());
    }

    private interface UnsupportedHolder extends VoltageRegulationHolder<UnsupportedHolder> {
    }
}
