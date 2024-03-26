/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import com.powsybl.iidm.criteria.translation.NetworkElement;
import com.powsybl.iidm.network.Country;
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
public class TieLineCriterionTest {
    private static NetworkElement tieLine1;
    private static NetworkElement tieLine2;
    private static NetworkElement tieLine3;
    private static NetworkElement tieLine4;

    @BeforeAll
    public static void init() {
        tieLine1 = createTieLine("tieLine1", Country.FR, Country.FR, 90);
        tieLine2 = createTieLine("tieLine2", Country.FR, Country.FR, 400);
        tieLine3 = createTieLine("tieLine3", Country.FR, Country.BE, 400);
        tieLine4 = createTieLine("tieLine4", Country.BE, Country.BE, 90);
    }

    @Test
    void typeTest() {
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.TIE_LINE,
                new TieLineCriterion(null, null).getNetworkElementCriterionType());
    }

    @Test
    void emptyCriterionTest() {
        TieLineCriterion criterion = new TieLineCriterion(null, null);
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine4)));

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertFalse(criterion.accept(new NetworkElementVisitor(anotherTypeElement)));
    }

    @Test
    void nominalVoltageTest() {
        TieLineCriterion criterion = new TieLineCriterion(null, new TwoNominalVoltageCriterion(
                        new SingleNominalVoltageCriterion.VoltageInterval(40., 100., true, true), null));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine4)));
    }

    @Test
    void countriesTest() {
        TieLineCriterion criterion = new TieLineCriterion(new TwoCountriesCriterion(List.of(Country.FR), List.of(Country.BE)), null);
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine3)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine4)));

        criterion = new TieLineCriterion(new TwoCountriesCriterion(List.of(Country.FR, Country.BE), List.of(Country.BE)), null);
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine4)));
    }

    @Test
    void mixedCriteriaTest() {
        TieLineCriterion criterion = new TieLineCriterion(new TwoCountriesCriterion(List.of(Country.FR), List.of(Country.FR)),
                new TwoNominalVoltageCriterion(
                    new SingleNominalVoltageCriterion.VoltageInterval(350., 450., true, true), null));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine3)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine4)));
    }

    protected static NetworkElement createTieLine(String id, Country country1, Country country2, double nominalVoltage) {
        NetworkElement n = Mockito.mock(NetworkElement.class);
        when(n.getId()).thenReturn(id);
        when(n.getCountry()).thenReturn(Optional.of(country1));
        when(n.getCountry1()).thenReturn(Optional.of(country1));
        when(n.getCountry2()).thenReturn(Optional.of(country2));
        when(n.getNominalVoltage()).thenReturn(Optional.of(nominalVoltage));
        when(n.getNominalVoltage1()).thenReturn(Optional.of(nominalVoltage));
        when(n.getNominalVoltage2()).thenReturn(Optional.empty());
        when(n.getNominalVoltage3()).thenReturn(Optional.empty());
        when(n.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.TIE_LINE)).thenReturn(true);
        when(n.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.IDENTIFIABLE)).thenReturn(true);
        return n;
    }
}


