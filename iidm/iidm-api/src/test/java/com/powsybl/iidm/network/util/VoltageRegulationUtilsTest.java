/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Set;
import java.util.stream.Stream;

import static com.powsybl.iidm.network.regulation.RegulationMode.REACTIVE_POWER;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE_PER_REACTIVE_POWER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
// TODO MSA complete the missing tests
class VoltageRegulationUtilsTest {

    static Stream<Arguments> provideParametersForGetAllowedRegulationModes() {
        return Stream.of(
            Arguments.of(Battery.class, true, true, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(Battery.class, false, true, Set.of(VOLTAGE)),
            Arguments.of(Battery.class, true, false, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(Battery.class, false, false, Set.of(VOLTAGE, REACTIVE_POWER)),

            Arguments.of(Generator.class, true, true, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(Generator.class, false, true, Set.of(VOLTAGE)),
            Arguments.of(Generator.class, true, false, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(Generator.class, false, false, Set.of(VOLTAGE, REACTIVE_POWER)),

            Arguments.of(RatioTapChanger.class, true, true, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(RatioTapChanger.class, false, true, Set.of()),
            Arguments.of(RatioTapChanger.class, true, false, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(RatioTapChanger.class, false, false, Set.of(VOLTAGE, REACTIVE_POWER)),

            Arguments.of(ShuntCompensator.class, true, true, Set.of(VOLTAGE)),
            Arguments.of(ShuntCompensator.class, false, true, Set.of(VOLTAGE)),
            Arguments.of(ShuntCompensator.class, true, false, Set.of(VOLTAGE)),
            Arguments.of(ShuntCompensator.class, false, false, Set.of(VOLTAGE)),

            Arguments.of(StaticVarCompensator.class, true, true, Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER)),
            Arguments.of(StaticVarCompensator.class, false, true, Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER)),
            Arguments.of(StaticVarCompensator.class, true, false, Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER)),
            Arguments.of(StaticVarCompensator.class, false, false, Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER)),

            Arguments.of(VscConverterStation.class, true, true, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(VscConverterStation.class, false, true, Set.of(VOLTAGE)),
            Arguments.of(VscConverterStation.class, true, false, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(VscConverterStation.class, false, false, Set.of(VOLTAGE, REACTIVE_POWER)),

            Arguments.of(VoltageSourceConverter.class, true, true, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(VoltageSourceConverter.class, false, true, Set.of(VOLTAGE)),
            Arguments.of(VoltageSourceConverter.class, true, false, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(VoltageSourceConverter.class, false, false, Set.of(VOLTAGE, REACTIVE_POWER))

        );
    }

    @ParameterizedTest
    @MethodSource("provideParametersForGetAllowedRegulationModes")
    void testGetAllowedRegulationModes(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolderClass,
                                       boolean isRemoteRegulating, boolean isRegulating, Set<RegulationMode> expectedModes) {
        Set<RegulationMode> allowedModes = VoltageRegulationUtils.getSettableRegulationModes(voltageRegulationHolderClass, isRemoteRegulating, isRegulating);
        assertEquals(expectedModes, allowedModes, "Allowed regulation modes do not match for class " + voltageRegulationHolderClass.getSimpleName() +
                " with isRemoteRegulating=" + isRemoteRegulating + " and isRegulating=" + isRegulating);
    }

    @Test
    void testGetAllowedRegulationModesForUnsupportedClass() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> VoltageRegulationUtils.getSettableRegulationModes(VoltageRegulationUtilsTest.UnsupportedHolder.class, true, true));
        assertEquals("UnsupportedHolder class cannot be used with VoltageRegulation", exception.getMessage());
    }

    private interface UnsupportedHolder extends VoltageRegulationHolder<VoltageRegulationUtilsTest.UnsupportedHolder> {
    }

    static Stream<Arguments> provideParametersForBuildVoltageRegulation() {
        return Stream.of(
            Arguments.of(true, true, true),
            Arguments.of(true, false, false),
            Arguments.of(false, true, true),
            Arguments.of(false, false, true)
        );
    }

    @ParameterizedTest
    @Disabled // TODO MSA
    @MethodSource("provideParametersForBuildVoltageRegulation")
    void testBuildVoltageRegulation(boolean isLocalTerminal,
                                    boolean isRegulatingOn,
                                    boolean expectedVoltageRegulationCreated) {
        VoltageRegulationHolder<?> holder = Mockito.mock(VoltageRegulationHolder.class);
        Terminal regulatingTerminal = Mockito.mock(Terminal.class);
        double targetV = 225.0;

        VoltageRegulationUtils.buildVoltageRegulation(holder, isLocalTerminal, targetV, regulatingTerminal, isRegulatingOn);

        if (isLocalTerminal) {
            assertEquals(targetV, holder.getLocalTargetV(), 0.0);
        }

        VoltageRegulation voltageRegulation = holder.getVoltageRegulation();
        if (!expectedVoltageRegulationCreated) {
            assertNull(voltageRegulation);
            assertFalse(holder.isRegulating());
            return;
        }

        assertNotNull(voltageRegulation);
        assertEquals(VOLTAGE, voltageRegulation.getMode());
        assertEquals(isRegulatingOn, voltageRegulation.isRegulating());

        if (isLocalTerminal) {
            assertNull(voltageRegulation.getTerminal());
            assertTrue(Double.isNaN(voltageRegulation.getTargetValue()));
        } else {
            assertSame(regulatingTerminal, voltageRegulation.getTerminal());
            assertEquals(targetV, voltageRegulation.getTargetValue(), 0.0);
        }
    }

    static Stream<Arguments> provideParametersForBuildVoltageRegulationData() {
        return Stream.of(
            Arguments.of(null, 225.0, Double.NaN, VOLTAGE),
            Arguments.of(null, Double.NaN, 50.0, null),
            Arguments.of(null, Double.NaN, Double.NaN, VOLTAGE),
            Arguments.of(true, 225.0, 50.0, VOLTAGE),
            Arguments.of(true, Double.NaN, Double.NaN, VOLTAGE),
            Arguments.of(false, 225.0, 50.0, null),
            Arguments.of(false, Double.NaN, Double.NaN, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideParametersForBuildVoltageRegulationData")
    void testBuildVoltageRegulationData(Boolean voltageRegulatorOn,
                                        Double voltageSetpoint,
                                        Double reactivePowerSetpoint,
                                        RegulationMode expectedRegulationMode) {
        // WHEN
        VoltageRegulationUtils.VoltageRegulationData voltageRegulationData = VoltageRegulationUtils.buildVoltageRegulationData(
            voltageRegulatorOn,
            voltageSetpoint,
            reactivePowerSetpoint
        );
        // THEN
        assertEquals(expectedRegulationMode, voltageRegulationData.regulationMode());
        assertEquals(voltageSetpoint, voltageRegulationData.targetV(), 0.0);
        assertEquals(reactivePowerSetpoint, voltageRegulationData.targetQ(), 0.0);
        assertTrue(Double.isNaN(voltageRegulationData.targetValue()));
    }

}
