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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class ThreeWindingsTransformerCriterionTest {
    private static NetworkElement twt1;
    private static NetworkElement twt2;
    private static NetworkElement twt3;

    @BeforeAll
    public static void init() {
        twt1 = createThreeWindingsTransformer("twt1", Country.FR, 400, 90, 63);
        twt2 = createThreeWindingsTransformer("twt2", Country.FR, 225, 90, 63);
        twt3 = createThreeWindingsTransformer("twt3", Country.BE, 400, 90, 63);
    }

    @Test
    void typeTest() {
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER,
                new ThreeWindingsTransformerCriterion(null, null).getNetworkElementCriterionType());
    }

    @Test
    void emptyCriterionTest() {
        ThreeWindingsTransformerCriterion criterion = new ThreeWindingsTransformerCriterion(null, null);
        assertTrue(criterion.accept(new NetworkElementVisitor(twt1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt3)));

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertFalse(criterion.accept(new NetworkElementVisitor(anotherTypeElement)));
    }

    @Test
    void nominalVoltagesTest() {
        ThreeWindingsTransformerCriterion criterion = new ThreeWindingsTransformerCriterion(null,
                new ThreeNominalVoltageCriterion(
                        new SingleNominalVoltageCriterion.VoltageInterval(80., 100., true, true),
                        new SingleNominalVoltageCriterion.VoltageInterval(350., 550., true, true),
                        new SingleNominalVoltageCriterion.VoltageInterval(40., 70., true, true)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt3)));
    }

    @Test
    void countryTest() {
        ThreeWindingsTransformerCriterion criterion = new ThreeWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.BE)), null);
        assertFalse(criterion.accept(new NetworkElementVisitor(twt1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt3)));

        criterion = new ThreeWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        assertTrue(criterion.accept(new NetworkElementVisitor(twt1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt3)));
    }

    @Test
    void mixedCriteriaTest() {
        ThreeWindingsTransformerCriterion criterion = new ThreeWindingsTransformerCriterion(
                new SingleCountryCriterion(List.of(Country.FR)),
                new ThreeNominalVoltageCriterion(
                        new SingleNominalVoltageCriterion.VoltageInterval(80., 100., true, true),
                        new SingleNominalVoltageCriterion.VoltageInterval(350., 550., true, true),
                        new SingleNominalVoltageCriterion.VoltageInterval(40., 70., true, true)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt3)));
    }

    protected static NetworkElement createThreeWindingsTransformer(String id, Country country, double nominalVoltage1,
                                                                    double nominalVoltage2, double nominalVoltage3) {
        NetworkElement n = Mockito.mock(NetworkElement.class);
        when(n.getId()).thenReturn(id);
        when(n.getCountry()).thenReturn(country);
        when(n.getCountry1()).thenReturn(country);
        when(n.getCountry2()).thenReturn(null);
        when(n.getNominalVoltage()).thenReturn(nominalVoltage1);
        when(n.getNominalVoltage1()).thenReturn(nominalVoltage1);
        when(n.getNominalVoltage2()).thenReturn(nominalVoltage2);
        when(n.getNominalVoltage3()).thenReturn(nominalVoltage3);
        when(n.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER)).thenReturn(true);
        when(n.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.EVERY_EQUIPMENT)).thenReturn(true);
        return n;
    }
}


