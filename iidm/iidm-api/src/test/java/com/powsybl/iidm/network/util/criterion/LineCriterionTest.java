/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.util.criterion.SingleNominalVoltageCriterion.VoltageInterval;
import com.powsybl.iidm.network.util.criterion.translation.NetworkElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LineCriterionTest {
    private static NetworkElement<?> line1;
    private static NetworkElement<?> line2;
    private static NetworkElement<?> line3;
    private static NetworkElement<?> line4;

    @BeforeAll
    public static void init() {
        line1 = createLine("line1", Country.FR, Country.FR, 90);
        line2 = createLine("line2", Country.FR, Country.FR, 400);
        line3 = createLine("line3", Country.FR, Country.BE, 400);
        line4 = createLine("line4", Country.BE, Country.BE, 90);
    }

    @Test
    void typeTest() {
        assertEquals(AbstractNetworkElementCriterion.NetworkElementCriterionType.LINE,
                new LineCriterion().getNetworkElementCriterionType());
    }

    @Test
    void emptyCriterionTest() {
        LineCriterion criterion = new LineCriterion();
        assertTrue(criterion.accept(new NetworkElementVisitor(line1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line4)));
    }

    @Test
    void nominalVoltageTest() {
        LineCriterion criterion = new LineCriterion()
                .setSingleNominalVoltageCriterion(new SingleNominalVoltageCriterion(
                        new VoltageInterval(40., 100., true, true)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line4)));
    }

    @Test
    void countriesTest() {
        LineCriterion criterion = new LineCriterion()
                .setTwoCountriesCriterion(new TwoCountriesCriterion(List.of(Country.FR), List.of(Country.BE)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line3)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line4)));

        criterion.setTwoCountriesCriterion(new TwoCountriesCriterion(List.of(Country.FR, Country.BE), List.of(Country.BE)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line3)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line4)));
    }

    @Test
    void mixedCriteriaTest() {
        LineCriterion criterion = new LineCriterion()
                .setSingleNominalVoltageCriterion(new SingleNominalVoltageCriterion(
                    new VoltageInterval(350., 450., true, true)))
                .setTwoCountriesCriterion(new TwoCountriesCriterion(List.of(Country.FR), List.of(Country.FR)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line3)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line4)));
    }

    private static NetworkElement<?> createLine(String id, Country country1, Country country2, double nominalVoltage) {
        NetworkElement<?> n = Mockito.mock(NetworkElement.class);
        when(n.getId()).thenReturn(id);
        when(n.getCountry1()).thenReturn(country1);
        when(n.getCountry2()).thenReturn(country2);
        when(n.getNominalVoltage()).thenReturn(nominalVoltage);
        return n;
    }
}


