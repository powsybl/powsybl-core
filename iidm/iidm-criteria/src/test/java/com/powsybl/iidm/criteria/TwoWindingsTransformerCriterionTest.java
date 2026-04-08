/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapter;
import com.powsybl.iidm.criteria.translation.NetworkElement;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapterTest.mockTwoWindingsTransformer;
import static org.junit.jupiter.api.Assertions.*;

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
        assertCriterionTrue(criterion, twt1);
        assertCriterionTrue(criterion, twt2);
        assertCriterionTrue(criterion, twt3);

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertCriterionFalse(criterion, anotherTypeElement);
    }

    @Test
    void nominalVoltagesTest() {
        TwoWindingsTransformerCriterion criterion = new TwoWindingsTransformerCriterion(null,
                new TwoNominalVoltageCriterion(
                        VoltageInterval.between(80., 100., true, true),
                        VoltageInterval.between(40., 70., true, true)));
        assertCriterionTrue(criterion, twt1);
        assertCriterionFalse(criterion, twt2);
        assertCriterionTrue(criterion, twt3);
    }

    @Test
    void countryTest() {
        TwoWindingsTransformerCriterion criterion = new TwoWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.BE)), null);
        assertCriterionFalse(criterion, twt1);
        assertCriterionFalse(criterion, twt2);
        assertCriterionTrue(criterion, twt3);

        criterion = new TwoWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        assertCriterionTrue(criterion, twt1);
        assertCriterionTrue(criterion, twt2);
        assertCriterionTrue(criterion, twt3);
    }

    @Test
    void mixedCriteriaTest() {
        TwoWindingsTransformerCriterion criterion = new TwoWindingsTransformerCriterion(
                new SingleCountryCriterion(List.of(Country.FR)),
                new TwoNominalVoltageCriterion(
                        VoltageInterval.between(80., 100., true, true),
                        VoltageInterval.between(40., 70., true, true)));
        assertCriterionTrue(criterion, twt1);
        assertCriterionFalse(criterion, twt2);
        assertCriterionFalse(criterion, twt3);
    }

    private void assertCriterionTrue(TwoWindingsTransformerCriterion criterion, NetworkElement twt) {
        assertTrue(criterion.accept(new NetworkElementVisitor(twt)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt, ThreeSides.ONE)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt, ThreeSides.TWO)));
    }

    private void assertCriterionFalse(TwoWindingsTransformerCriterion criterion, NetworkElement twt) {
        assertFalse(criterion.accept(new NetworkElementVisitor(twt)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt, ThreeSides.ONE)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt, ThreeSides.TWO)));
    }

    protected static NetworkElement createTwoWindingsTransformer(String id, Country country, double nominalVoltage1, double nominalVoltage2) {
        TwoWindingsTransformer twt = mockTwoWindingsTransformer(id, country, nominalVoltage1, nominalVoltage2);
        return new DefaultNetworkElementAdapter(twt);
    }
}


