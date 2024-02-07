/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion.translation;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class DefaultNetworkElementTest {

    @Test
    void testBranch() {
        Substation substation1 = Mockito.mock(Substation.class);
        Mockito.when(substation1.getNullableCountry()).thenReturn(Country.FR);
        Substation substation2 = Mockito.mock(Substation.class);
        Mockito.when(substation2.getNullableCountry()).thenReturn(Country.AQ);

        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(225.);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(226.);

        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);
        Mockito.when(line.getId()).thenReturn("testLine");
        Mockito.when(line.getTerminal(TwoSides.ONE)).thenReturn(terminal1);
        Mockito.when(line.getTerminal(TwoSides.TWO)).thenReturn(terminal2);
        Mockito.when(line.getLimits(LimitType.CURRENT, TwoSides.ONE)).thenAnswer(x -> createLimits(120.));
        Mockito.when(line.getLimits(LimitType.CURRENT, TwoSides.TWO)).thenAnswer(x -> createLimits(250.));
        Mockito.when(line.getLimits(LimitType.APPARENT_POWER, TwoSides.ONE)).thenAnswer(x -> createLimits(654.));
        Mockito.when(line.getLimits(LimitType.ACTIVE_POWER, TwoSides.TWO)).thenAnswer(x -> createLimits(987.));

        DefaultNetworkElement networkElement = new DefaultNetworkElement(line);
        assertEquals("testLine", networkElement.getId());
        assertEquals(Country.FR, networkElement.getCountry());
        assertEquals(Country.FR, networkElement.getCountry1());
        assertEquals(Country.AQ, networkElement.getCountry2());
        assertEquals(225., networkElement.getNominalVoltage(), 0.01);
        assertEquals(225., networkElement.getNominalVoltage1(), 0.01);
        assertEquals(226., networkElement.getNominalVoltage2(), 0.01);
        assertThrows(PowsyblException.class, networkElement::getNominalVoltage3);

        assertEquals(120., getPermanentLimit(networkElement, LimitType.CURRENT, ThreeSides.ONE), 0.01);
        assertEquals(250., getPermanentLimit(networkElement, LimitType.CURRENT, ThreeSides.TWO), 0.01);
        assertThrows(PowsyblException.class, () -> getPermanentLimit(networkElement, LimitType.CURRENT, ThreeSides.THREE));
        assertEquals(654., getPermanentLimit(networkElement, LimitType.APPARENT_POWER, ThreeSides.ONE), 0.01);
        assertNull(getPermanentLimit(networkElement, LimitType.APPARENT_POWER, ThreeSides.TWO));
        assertNull(getPermanentLimit(networkElement, LimitType.ACTIVE_POWER, ThreeSides.ONE));
        assertEquals(987., getPermanentLimit(networkElement, LimitType.ACTIVE_POWER, ThreeSides.TWO), 0.01);
    }

    //TODO Add tests for the other kinds

    private Optional<LoadingLimits> createLimits(double permanentValue) {
        LoadingLimits limits = Mockito.mock(LoadingLimits.class);
        Mockito.when(limits.getPermanentLimit()).thenReturn(permanentValue);
        return Optional.of(limits);
    }

    private Double getPermanentLimit(DefaultNetworkElement networkElement, LimitType type, ThreeSides side) {
        Optional<LoadingLimits> limits = networkElement.getLimits(type, side);
        return limits.map(LoadingLimits::getPermanentLimit).orElse(null);
    }
}
