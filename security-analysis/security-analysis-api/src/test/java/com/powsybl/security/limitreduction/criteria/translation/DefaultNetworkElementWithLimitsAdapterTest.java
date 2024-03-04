/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction.criteria.translation;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class DefaultNetworkElementWithLimitsAdapterTest {
    //TODO Add tieLine test
    //TODO Use real network instead of mocked object
    @Test
    void testLine() {
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

        DefaultNetworkElementWithLimitsAdapter networkElement = new DefaultNetworkElementWithLimitsAdapter(line);
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

        assertTrue(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.TIE_LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER));
        assertFalse(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER));
        assertTrue(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.IDENTIFIERS));
    }

    @Test
    void testTwoWindingsTransformer() {
        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getNullableCountry()).thenReturn(Country.FR);

        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(225.);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(90.);

        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        TwoWindingsTransformer twt = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twt.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(twt.getId()).thenReturn("testTwoWindingsTransformer");
        Mockito.when(twt.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(twt.getTerminal(TwoSides.ONE)).thenReturn(terminal1);
        Mockito.when(twt.getTerminal(TwoSides.TWO)).thenReturn(terminal2);
        Mockito.when(twt.getLimits(LimitType.CURRENT, TwoSides.ONE)).thenAnswer(x -> createLimits(120.));
        Mockito.when(twt.getLimits(LimitType.CURRENT, TwoSides.TWO)).thenAnswer(x -> createLimits(250.));
        Mockito.when(twt.getLimits(LimitType.APPARENT_POWER, TwoSides.ONE)).thenAnswer(x -> createLimits(654.));
        Mockito.when(twt.getLimits(LimitType.ACTIVE_POWER, TwoSides.TWO)).thenAnswer(x -> createLimits(987.));

        DefaultNetworkElementWithLimitsAdapter networkElement = new DefaultNetworkElementWithLimitsAdapter(twt);
        assertEquals("testTwoWindingsTransformer", networkElement.getId());
        assertEquals(Country.FR, networkElement.getCountry());
        assertEquals(225., networkElement.getNominalVoltage(), 0.01);
        assertEquals(225., networkElement.getNominalVoltage1(), 0.01);
        assertEquals(90., networkElement.getNominalVoltage2(), 0.01);
        assertThrows(PowsyblException.class, networkElement::getNominalVoltage3);

        assertEquals(120., getPermanentLimit(networkElement, LimitType.CURRENT, ThreeSides.ONE), 0.01);
        assertEquals(250., getPermanentLimit(networkElement, LimitType.CURRENT, ThreeSides.TWO), 0.01);
        assertThrows(PowsyblException.class, () -> getPermanentLimit(networkElement, LimitType.CURRENT, ThreeSides.THREE));
        assertEquals(654., getPermanentLimit(networkElement, LimitType.APPARENT_POWER, ThreeSides.ONE), 0.01);
        assertNull(getPermanentLimit(networkElement, LimitType.APPARENT_POWER, ThreeSides.TWO));
        assertNull(getPermanentLimit(networkElement, LimitType.ACTIVE_POWER, ThreeSides.ONE));
        assertEquals(987., getPermanentLimit(networkElement, LimitType.ACTIVE_POWER, ThreeSides.TWO), 0.01);

        assertFalse(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.TIE_LINE));
        assertTrue(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER));
        assertFalse(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER));
        assertTrue(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.IDENTIFIERS));
    }

    @Test
    void testThreeWindingsTransformer() {
        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getNullableCountry()).thenReturn(Country.FR);

        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(400.);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(225.);
        VoltageLevel voltageLevel3 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel3.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(voltageLevel3.getNominalV()).thenReturn(90.);

        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Terminal terminal3 = Mockito.mock(Terminal.class);
        Mockito.when(terminal3.getVoltageLevel()).thenReturn(voltageLevel3);

        ThreeWindingsTransformer twt = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(twt.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        Mockito.when(twt.getId()).thenReturn("testThreeWindingsTransformer");
        Mockito.when(twt.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(twt.getTerminal(ThreeSides.ONE)).thenReturn(terminal1);
        Mockito.when(twt.getTerminal(ThreeSides.TWO)).thenReturn(terminal2);
        Mockito.when(twt.getTerminal(ThreeSides.THREE)).thenReturn(terminal3);

        ThreeWindingsTransformer.Leg leg1 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        ThreeWindingsTransformer.Leg leg2 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        ThreeWindingsTransformer.Leg leg3 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg1.getLimits(LimitType.CURRENT)).thenAnswer(x -> createLimits(120.));
        Mockito.when(leg2.getLimits(LimitType.CURRENT)).thenAnswer(x -> createLimits(250.));
        Mockito.when(leg1.getLimits(LimitType.APPARENT_POWER)).thenAnswer(x -> createLimits(654.));
        Mockito.when(leg2.getLimits(LimitType.ACTIVE_POWER)).thenAnswer(x -> createLimits(987.));
        Mockito.when(leg3.getLimits(LimitType.APPARENT_POWER)).thenAnswer(x -> createLimits(321.));
        Mockito.when(twt.getLeg(ThreeSides.ONE)).thenReturn(leg1);
        Mockito.when(twt.getLeg(ThreeSides.TWO)).thenReturn(leg2);
        Mockito.when(twt.getLeg(ThreeSides.THREE)).thenReturn(leg3);

        DefaultNetworkElementWithLimitsAdapter networkElement = new DefaultNetworkElementWithLimitsAdapter(twt);
        assertEquals("testThreeWindingsTransformer", networkElement.getId());
        assertEquals(Country.FR, networkElement.getCountry());
        assertEquals(400., networkElement.getNominalVoltage(), 0.01);
        assertEquals(400., networkElement.getNominalVoltage1(), 0.01);
        assertEquals(225., networkElement.getNominalVoltage2(), 0.01);
        assertEquals(90., networkElement.getNominalVoltage3(), 0.01);

        assertEquals(120., getPermanentLimit(networkElement, LimitType.CURRENT, ThreeSides.ONE), 0.01);
        assertEquals(250., getPermanentLimit(networkElement, LimitType.CURRENT, ThreeSides.TWO), 0.01);
        assertNull(getPermanentLimit(networkElement, LimitType.CURRENT, ThreeSides.THREE));
        assertEquals(654., getPermanentLimit(networkElement, LimitType.APPARENT_POWER, ThreeSides.ONE), 0.01);
        assertNull(getPermanentLimit(networkElement, LimitType.APPARENT_POWER, ThreeSides.TWO));
        assertEquals(321., getPermanentLimit(networkElement, LimitType.APPARENT_POWER, ThreeSides.THREE));
        assertNull(getPermanentLimit(networkElement, LimitType.ACTIVE_POWER, ThreeSides.ONE));
        assertEquals(987., getPermanentLimit(networkElement, LimitType.ACTIVE_POWER, ThreeSides.TWO), 0.01);
        assertNull(getPermanentLimit(networkElement, LimitType.ACTIVE_POWER, ThreeSides.THREE));

        assertFalse(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.TIE_LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER));
        assertTrue(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER));
        assertTrue(networkElement.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.IDENTIFIERS));
    }

    private Optional<LoadingLimits> createLimits(double permanentValue) {
        LoadingLimits limits = Mockito.mock(LoadingLimits.class);
        Mockito.when(limits.getPermanentLimit()).thenReturn(permanentValue);
        return Optional.of(limits);
    }

    private Double getPermanentLimit(DefaultNetworkElementWithLimitsAdapter networkElement, LimitType type, ThreeSides side) {
        Optional<LoadingLimits> limits = networkElement.getLimits(type, side);
        return limits.map(LoadingLimits::getPermanentLimit).orElse(null);
    }
}
