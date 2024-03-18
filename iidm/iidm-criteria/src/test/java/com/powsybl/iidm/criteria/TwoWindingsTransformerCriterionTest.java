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
public class TwoWindingsTransformerCriterionTest {
    private static NetworkElement twt1;
    private static NetworkElement twt2;
    private static NetworkElement twt3;

    @BeforeAll
    public static void init() {
        twt1 = createTwoWindingsTransformer("twt1", Country.FR, 90, 63);
        twt2 = createTwoWindingsTransformer("twt2", Country.FR, 225, 90);
        twt3 = createTwoWindingsTransformer("twt3", Country.BE, 90, 63);
    }

    @Test
    void typeTest() {
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER,
                new TwoWindingsTransformerCriterion(null, null).getNetworkElementCriterionType());
    }

    @Test
    void emptyCriterionTest() {
        TwoWindingsTransformerCriterion criterion = new TwoWindingsTransformerCriterion(null, null);
        assertTrue(criterion.accept(new NetworkElementVisitor(twt1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt3)));

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertFalse(criterion.accept(new NetworkElementVisitor(anotherTypeElement)));
    }

    @Test
    void nominalVoltagesTest() {
        TwoWindingsTransformerCriterion criterion = new TwoWindingsTransformerCriterion(null,
                new TwoNominalVoltageCriterion(
                        new SingleNominalVoltageCriterion.VoltageInterval(80., 100., true, true),
                        new SingleNominalVoltageCriterion.VoltageInterval(40., 70., true, true)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt3)));
    }

    @Test
    void countryTest() {
        TwoWindingsTransformerCriterion criterion = new TwoWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.BE)), null);
        assertFalse(criterion.accept(new NetworkElementVisitor(twt1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt3)));

        criterion = new TwoWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        assertTrue(criterion.accept(new NetworkElementVisitor(twt1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt3)));
    }

    @Test
    void mixedCriteriaTest() {
        TwoWindingsTransformerCriterion criterion = new TwoWindingsTransformerCriterion(
                new SingleCountryCriterion(List.of(Country.FR)),
                new TwoNominalVoltageCriterion(
                        new SingleNominalVoltageCriterion.VoltageInterval(80., 100., true, true),
                        new SingleNominalVoltageCriterion.VoltageInterval(40., 70., true, true)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt3)));
    }

    protected static NetworkElement createTwoWindingsTransformer(String id, Country country, double nominalVoltage1, double nominalVoltage2) {
        NetworkElement n = Mockito.mock(NetworkElement.class);
        when(n.getId()).thenReturn(id);
        when(n.getCountry()).thenReturn(Optional.of(country));
        when(n.getCountry1()).thenReturn(Optional.of(country));
        when(n.getCountry2()).thenReturn(Optional.empty());
        when(n.getNominalVoltage1()).thenReturn(Optional.of(nominalVoltage1));
        when(n.getNominalVoltage2()).thenReturn(Optional.of(nominalVoltage2));
        when(n.getNominalVoltage3()).thenReturn(Optional.empty());
        when(n.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER)).thenReturn(true);
        when(n.isValidFor(NetworkElementCriterion.NetworkElementCriterionType.IDENTIFIABLE)).thenReturn(true);
        return n;
    }
}


