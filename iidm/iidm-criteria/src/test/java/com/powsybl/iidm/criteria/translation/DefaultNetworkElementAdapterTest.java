/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.translation;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.criteria.NetworkElementCriterion.NetworkElementCriterionType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultNetworkElementAdapterTest {

    @Test
    void testLine() {
        Line line = mockLine("testLine", Country.FR, Country.DE, 225., 226.);
        DefaultNetworkElementAdapter networkElement = new DefaultNetworkElementAdapter(line);
        assertEquals("testLine", networkElement.getId());
        assertEquals(Country.FR, networkElement.getCountry().orElseThrow());
        assertEquals(Country.FR, networkElement.getCountry1().orElseThrow());
        assertEquals(Country.DE, networkElement.getCountry2().orElseThrow());
        assertEquals(225., networkElement.getNominalVoltage().orElseThrow(), 0.01);
        assertEquals(225., networkElement.getNominalVoltage1().orElseThrow(), 0.01);
        assertEquals(226., networkElement.getNominalVoltage2().orElseThrow(), 0.01);
        assertTrue(networkElement.getNominalVoltage3().isEmpty());

        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.TIE_LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.DANGLING_LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.IDENTIFIABLE));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.IDENTIFIER));
        assertEquals(line, networkElement.getIdentifiable());
    }

    @Test
    void testTieLine() {
        TieLine tieLine = mockTieLine("testTieLine", Country.FR, Country.DE, 225., 226.);

        DefaultNetworkElementAdapter networkElement = new DefaultNetworkElementAdapter(tieLine);
        assertEquals("testTieLine", networkElement.getId());
        assertEquals(Country.FR, networkElement.getCountry().orElseThrow());
        assertEquals(Country.FR, networkElement.getCountry1().orElseThrow());
        assertEquals(Country.DE, networkElement.getCountry2().orElseThrow());
        assertEquals(225., networkElement.getNominalVoltage().orElseThrow(), 0.01);
        assertEquals(225., networkElement.getNominalVoltage1().orElseThrow(), 0.01);
        assertEquals(226., networkElement.getNominalVoltage2().orElseThrow(), 0.01);
        assertTrue(networkElement.getNominalVoltage3().isEmpty());

        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.LINE));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.TIE_LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.DANGLING_LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.IDENTIFIABLE));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.IDENTIFIER));
        assertEquals(tieLine, networkElement.getIdentifiable());
    }

    @Test
    void testDanglingLine() {
        DanglingLine danglingLine = mockDanglingLine("testDanglingLine", Country.FR, 225.);
        DefaultNetworkElementAdapter networkElement = new DefaultNetworkElementAdapter(danglingLine);
        assertEquals("testDanglingLine", networkElement.getId());
        assertEquals(Country.FR, networkElement.getCountry().orElseThrow());
        assertEquals(Country.FR, networkElement.getCountry1().orElseThrow());
        assertTrue(networkElement.getCountry2().isEmpty());
        assertEquals(225., networkElement.getNominalVoltage().orElseThrow(), 0.01);
        assertEquals(225., networkElement.getNominalVoltage1().orElseThrow(), 0.01);
        assertTrue(networkElement.getNominalVoltage2().isEmpty());
        assertTrue(networkElement.getNominalVoltage3().isEmpty());

        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.TIE_LINE));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.DANGLING_LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.IDENTIFIABLE));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.IDENTIFIER));
        assertEquals(danglingLine, networkElement.getIdentifiable());
    }

    @Test
    void testTwoWindingsTransformer() {
        TwoWindingsTransformer twt = mockTwoWindingsTransformer("testTwoWindingsTransformer", Country.FR, 225., 90.);

        DefaultNetworkElementAdapter networkElement = new DefaultNetworkElementAdapter(twt);
        assertEquals("testTwoWindingsTransformer", networkElement.getId());
        assertEquals(Country.FR, networkElement.getCountry().orElseThrow());
        assertEquals(225., networkElement.getNominalVoltage().orElseThrow(), 0.01);
        assertEquals(225., networkElement.getNominalVoltage1().orElseThrow(), 0.01);
        assertEquals(90., networkElement.getNominalVoltage2().orElseThrow(), 0.01);
        assertTrue(networkElement.getNominalVoltage3().isEmpty());

        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.TIE_LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.DANGLING_LINE));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.IDENTIFIABLE));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.IDENTIFIER));
        assertEquals(twt, networkElement.getIdentifiable());
    }

    @Test
    void testThreeWindingsTransformer() {
        ThreeWindingsTransformer twt = mockThreeWindingsTransformer("testThreeWindingsTransformer", Country.FR, 400., 225., 90.);
        DefaultNetworkElementAdapter networkElement = new DefaultNetworkElementAdapter(twt);
        assertEquals("testThreeWindingsTransformer", networkElement.getId());
        assertEquals(Country.FR, networkElement.getCountry().orElseThrow());
        assertEquals(400., networkElement.getNominalVoltage().orElseThrow(), 0.01);
        assertEquals(400., networkElement.getNominalVoltage1().orElseThrow(), 0.01);
        assertEquals(225., networkElement.getNominalVoltage2().orElseThrow(), 0.01);
        assertEquals(90., networkElement.getNominalVoltage3().orElseThrow(), 0.01);

        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.TIE_LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.DANGLING_LINE));
        assertFalse(networkElement.isValidFor(NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.IDENTIFIABLE));
        assertTrue(networkElement.isValidFor(NetworkElementCriterionType.IDENTIFIER));
        assertEquals(twt, networkElement.getIdentifiable());
    }

    public static Line mockLine(String id, Country country1, Country country2,
                                double nominalVoltage1, double nominalVoltage2) {
        Substation substation1 = Mockito.mock(Substation.class);
        Mockito.when(substation1.getNullableCountry()).thenReturn(country1);
        Substation substation2 = Mockito.mock(Substation.class);
        Mockito.when(substation2.getNullableCountry()).thenReturn(country2);

        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(nominalVoltage1);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(nominalVoltage2);

        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);
        Mockito.when(line.getId()).thenReturn(id);
        Mockito.when(line.getTerminal(TwoSides.ONE)).thenReturn(terminal1);
        Mockito.when(line.getTerminal(TwoSides.TWO)).thenReturn(terminal2);
        return line;
    }

    public static TieLine mockTieLine(String id, Country country1, Country country2,
                                       double nominalVoltage1, double nominalVoltage2) {
        Substation substation1 = Mockito.mock(Substation.class);
        Mockito.when(substation1.getNullableCountry()).thenReturn(country1);
        Substation substation2 = Mockito.mock(Substation.class);
        Mockito.when(substation2.getNullableCountry()).thenReturn(country2);

        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(nominalVoltage1);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(nominalVoltage2);

        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        TieLine tieLine = Mockito.mock(TieLine.class);
        Mockito.when(tieLine.getType()).thenReturn(IdentifiableType.TIE_LINE);
        Mockito.when(tieLine.getId()).thenReturn(id);
        Mockito.when(tieLine.getTerminal(TwoSides.ONE)).thenReturn(terminal1);
        Mockito.when(tieLine.getTerminal(TwoSides.TWO)).thenReturn(terminal2);
        return tieLine;
    }

    public static DanglingLine mockDanglingLine(String id, Country country, double nominalVoltage) {
        Substation substation1 = Mockito.mock(Substation.class);
        Mockito.when(substation1.getNullableCountry()).thenReturn(country);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation1));
        Mockito.when(voltageLevel.getNominalV()).thenReturn(nominalVoltage);

        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);

        DanglingLine danglingLine = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine.getType()).thenReturn(IdentifiableType.DANGLING_LINE);
        Mockito.when(danglingLine.getId()).thenReturn(id);
        Mockito.when(danglingLine.getTerminal()).thenReturn(terminal);
        return danglingLine;
    }

    public static TwoWindingsTransformer mockTwoWindingsTransformer(String id, Country country,
                                                                    double nominalVoltage1, double nominalVoltage2) {
        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getNullableCountry()).thenReturn(country);

        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(nominalVoltage1);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(nominalVoltage2);

        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        TwoWindingsTransformer twt = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twt.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(twt.getId()).thenReturn(id);
        Mockito.when(twt.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(twt.getTerminal(TwoSides.ONE)).thenReturn(terminal1);
        Mockito.when(twt.getTerminal(TwoSides.TWO)).thenReturn(terminal2);
        return twt;
    }

    public static ThreeWindingsTransformer mockThreeWindingsTransformer(String id, Country country,
                                                                        double nominalVoltage1, double nominalVoltage2,
                                                                        double nominalVoltage3) {
        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getNullableCountry()).thenReturn(country);

        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(nominalVoltage1);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(nominalVoltage2);
        VoltageLevel voltageLevel3 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel3.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(voltageLevel3.getNominalV()).thenReturn(nominalVoltage3);

        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Terminal terminal3 = Mockito.mock(Terminal.class);
        Mockito.when(terminal3.getVoltageLevel()).thenReturn(voltageLevel3);

        ThreeWindingsTransformer twt = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(twt.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        Mockito.when(twt.getId()).thenReturn(id);
        Mockito.when(twt.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(twt.getTerminal(ThreeSides.ONE)).thenReturn(terminal1);
        Mockito.when(twt.getTerminal(ThreeSides.TWO)).thenReturn(terminal2);
        Mockito.when(twt.getTerminal(ThreeSides.THREE)).thenReturn(terminal3);
        return twt;
    }
}
