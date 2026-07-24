package com.powsybl.iidm.network.regulation.mode.getter;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static com.powsybl.iidm.network.regulation.RegulationMode.REACTIVE_POWER;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE_PER_REACTIVE_POWER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for RegulationModeValidators, focusing on the getAllowedRegulationModes method.
 * This method determines the set of allowed RegulationModes for a given VoltageRegulationHolder
 * class (e.g., Battery, Generator, etc.) based on whether remote regulation is enabled.
 */
class RegulationModeGettersTest {

    static Stream<Arguments> provideParametersForGetAllowedRegulationModes() {
        return Stream.of(
            Arguments.of(Battery.class, true, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(Battery.class, false, Set.of(VOLTAGE)),

            Arguments.of(Generator.class, true, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(Generator.class, false, Set.of(VOLTAGE)),

            Arguments.of(RatioTapChanger.class, true, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(RatioTapChanger.class, false, Set.of()),

            Arguments.of(ShuntCompensator.class, true, Set.of(VOLTAGE)),
            Arguments.of(ShuntCompensator.class, false, Set.of(VOLTAGE)),

            Arguments.of(StaticVarCompensator.class, true, Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER)),
            Arguments.of(StaticVarCompensator.class, false, Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER)),

            Arguments.of(VscConverterStation.class, true, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(VscConverterStation.class, false, Set.of(VOLTAGE)),

            Arguments.of(VoltageSourceConverter.class, true, Set.of(VOLTAGE, REACTIVE_POWER)),
            Arguments.of(VoltageSourceConverter.class, false, Set.of(VOLTAGE))

        );
    }

    @ParameterizedTest
    @MethodSource("provideParametersForGetAllowedRegulationModes")
    void testGetAllowedRegulationModes(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolderClass, boolean isRemoteRegulating, Set<RegulationMode> expectedModes) {
        Set<RegulationMode> allowedModes = RegulationModeGetters.getAllowedRegulationModes(voltageRegulationHolderClass, isRemoteRegulating);
        assertEquals(expectedModes, allowedModes, "Allowed regulation modes do not match for class " + voltageRegulationHolderClass.getSimpleName() + " with isRemoteRegulating=" + isRemoteRegulating);
    }

    @Test
    void testGetAllowedRegulationModesForUnsupportedClass() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RegulationModeGetters.getAllowedRegulationModes(UnsupportedHolder.class, true));
        assertEquals("UnsupportedHolder class cannot be used with VoltageRegulation", exception.getMessage());
    }

    private interface UnsupportedHolder extends VoltageRegulationHolder<UnsupportedHolder> {
    }
}
