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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DanglingLineCriterionTest {
    private static NetworkElement danglingLine1;
    private static NetworkElement danglingLine2;
    private static NetworkElement danglingLine3;
    private static NetworkElement danglingLine4;

    @BeforeAll
    public static void init() {
        danglingLine1 = createDanglingLine("danglingLine1", Country.FR, 90);
        danglingLine2 = createDanglingLine("danglingLine2", Country.FR, 400);
        danglingLine3 = createDanglingLine("danglingLine3", Country.BE, 400);
        danglingLine4 = createDanglingLine("danglingLine4", Country.BE, 90);
    }

    @Test
    void typeTest() {
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.DANGLING_LINE,
                new DanglingLineCriterion(null, null).getNetworkElementCriterionType());
    }

    @Test
    void emptyCriterionTest() {
        DanglingLineCriterion criterion = new DanglingLineCriterion(null, null);
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine4)));

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertFalse(criterion.accept(new NetworkElementVisitor(anotherTypeElement)));
    }

    @Test
    void nominalVoltageTest() {
        DanglingLineCriterion criterion = new DanglingLineCriterion(null, new SingleNominalVoltageCriterion(
                new SingleNominalVoltageCriterion.VoltageInterval(40., 100., true, true)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine4)));
    }

    @Test
    void countryTest() {
        DanglingLineCriterion criterion = new DanglingLineCriterion(new SingleCountryCriterion(List.of(Country.BE)), null);
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine4)));

        criterion = new DanglingLineCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine4)));
    }

    @Test
    void mixedCriteriaTest() {
        DanglingLineCriterion criterion = new DanglingLineCriterion(new SingleCountryCriterion(List.of(Country.FR)),
                new SingleNominalVoltageCriterion(
                        new SingleNominalVoltageCriterion.VoltageInterval(350., 450., true, true)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine3)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine4)));
    }

    protected static NetworkElement createDanglingLine(String id, Country country, double nominalVoltage) {
        NetworkElement n = Mockito.mock(NetworkElement.class);
        when(n.getId()).thenReturn(id);
        when(n.getCountry()).thenReturn(Optional.of(country));
        when(n.getCountry1()).thenReturn(Optional.of(country));
        when(n.getCountry2()).thenReturn(Optional.empty());
        when(n.getNominalVoltage()).thenReturn(Optional.of(nominalVoltage));
        when(n.getNominalVoltage1()).thenReturn(Optional.of(nominalVoltage));
        when(n.getNominalVoltage2()).thenReturn(Optional.empty());
        when(n.getNominalVoltage3()).thenReturn(Optional.empty());
        when(n.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.DANGLING_LINE)).thenReturn(true);
        when(n.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.IDENTIFIABLE)).thenReturn(true);
        return n;
    }
}


