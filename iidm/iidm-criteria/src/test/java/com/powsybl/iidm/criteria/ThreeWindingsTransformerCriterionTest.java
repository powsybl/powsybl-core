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
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapterTest.mockThreeWindingsTransformer;
import static org.junit.jupiter.api.Assertions.*;

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
        assertCriterionTrue(criterion, twt1);
        assertCriterionTrue(criterion, twt2);
        assertCriterionTrue(criterion, twt3);

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertCriterionFalse(criterion, anotherTypeElement);
    }

    @Test
    void nominalVoltagesTest() {
        ThreeWindingsTransformerCriterion criterion = new ThreeWindingsTransformerCriterion(null,
                new ThreeNominalVoltageCriterion(
                        VoltageInterval.between(80., 100., true, true),
                        VoltageInterval.between(350., 550., true, true),
                        VoltageInterval.between(40., 70., true, true)));
        assertCriterionTrue(criterion, twt1);
        assertCriterionFalse(criterion, twt2);
        assertCriterionTrue(criterion, twt3);
    }

    @Test
    void countryTest() {
        ThreeWindingsTransformerCriterion criterion = new ThreeWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.BE)), null);
        assertCriterionFalse(criterion, twt1);
        assertCriterionFalse(criterion, twt2);
        assertCriterionTrue(criterion, twt3);

        criterion = new ThreeWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        assertCriterionTrue(criterion, twt1);
        assertCriterionTrue(criterion, twt2);
        assertCriterionTrue(criterion, twt3);
    }

    @Test
    void mixedCriteriaTest() {
        ThreeWindingsTransformerCriterion criterion = new ThreeWindingsTransformerCriterion(
                new SingleCountryCriterion(List.of(Country.FR)),
                new ThreeNominalVoltageCriterion(
                        VoltageInterval.between(80., 100., true, true),
                        VoltageInterval.between(350., 550., true, true),
                        VoltageInterval.between(40., 70., true, true)));
        assertCriterionTrue(criterion, twt1);
        assertCriterionFalse(criterion, twt2);
        assertCriterionFalse(criterion, twt3);
    }

    private void assertCriterionTrue(ThreeWindingsTransformerCriterion criterion, NetworkElement twt) {
        assertTrue(criterion.accept(new NetworkElementVisitor(twt)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt, ThreeSides.ONE)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt, ThreeSides.TWO)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twt, ThreeSides.THREE)));
    }

    private void assertCriterionFalse(ThreeWindingsTransformerCriterion criterion, NetworkElement twt) {
        assertFalse(criterion.accept(new NetworkElementVisitor(twt)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt, ThreeSides.ONE)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt, ThreeSides.TWO)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twt, ThreeSides.THREE)));
    }

    protected static NetworkElement createThreeWindingsTransformer(String id, Country country, double nominalVoltage1,
                                                                    double nominalVoltage2, double nominalVoltage3) {
        ThreeWindingsTransformer twt = mockThreeWindingsTransformer(id, country, nominalVoltage1, nominalVoltage2, nominalVoltage3);
        return new DefaultNetworkElementAdapter(twt);
    }
}


