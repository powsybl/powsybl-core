/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.criteria.translation.NetworkElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LineCriterionTest {
    private static NetworkElement line1;
    private static NetworkElement line2;
    private static NetworkElement line3;
    private static NetworkElement line4;

    @BeforeAll
    public static void init() {
        line1 = createLine("line1", Country.FR, Country.FR, 90);
        line2 = createLine("line2", Country.FR, Country.FR, 400);
        line3 = createLine("line3", Country.FR, Country.BE, 400);
        line4 = createLine("line4", Country.BE, Country.BE, 90);
    }

    @Test
    void typeTest() {
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.LINE,
                new LineCriterion(null, null).getNetworkElementCriterionType());
    }

    @Test
    void emptyCriterionTest() {
        LineCriterion criterion = new LineCriterion(null, null);
        assertTrue(criterion.accept(new NetworkElementVisitor(line1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line4)));

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertFalse(criterion.accept(new NetworkElementVisitor(anotherTypeElement)));
    }

    @Test
    void nominalVoltageTest() {
        LineCriterion criterion = new LineCriterion(null, new TwoNominalVoltageCriterion(
                new SingleNominalVoltageCriterion.VoltageInterval(40., 100., true, true), null));
        assertTrue(criterion.accept(new NetworkElementVisitor(line1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line4)));
    }

    @Test
    void countriesTest() {
        LineCriterion criterion = new LineCriterion(new TwoCountriesCriterion(List.of(Country.FR), List.of(Country.BE)), null);
        assertFalse(criterion.accept(new NetworkElementVisitor(line1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line3)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line4)));

        criterion = new LineCriterion(new TwoCountriesCriterion(List.of(Country.FR, Country.BE), List.of(Country.BE)), null);
        assertFalse(criterion.accept(new NetworkElementVisitor(line1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line4)));
    }

    @Test
    void mixedCriteriaTest() {
        LineCriterion criterion = new LineCriterion(new TwoCountriesCriterion(List.of(Country.FR), List.of(Country.FR)),
                new TwoNominalVoltageCriterion(new SingleNominalVoltageCriterion.VoltageInterval(350., 450., true, true), null));
        assertFalse(criterion.accept(new NetworkElementVisitor(line1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line3)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line4)));
    }

    protected static NetworkElement createLine(String id, Country country1, Country country2, double nominalVoltage) {
        NetworkElement n = Mockito.mock(NetworkElement.class);
        when(n.getId()).thenReturn(id);
        when(n.getCountry()).thenReturn(Optional.of(country1));
        when(n.getCountry1()).thenReturn(Optional.of(country1));
        when(n.getCountry2()).thenReturn(Optional.of(country2));
        when(n.getNominalVoltage()).thenReturn(Optional.of(nominalVoltage));
        when(n.getNominalVoltage1()).thenReturn(Optional.of(nominalVoltage));
        when(n.getNominalVoltage2()).thenReturn(Optional.empty());
        when(n.getNominalVoltage3()).thenReturn(Optional.empty());
        when(n.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.LINE)).thenReturn(true);
        when(n.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.IDENTIFIABLE)).thenReturn(true);
        return n;
    }
}


