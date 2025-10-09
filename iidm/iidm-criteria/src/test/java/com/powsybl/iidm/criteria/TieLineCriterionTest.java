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
import com.powsybl.iidm.network.TieLine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapterTest.mockTieLine;
import static org.junit.jupiter.api.Assertions.*;

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
        assertCriterionTrue(criterion, tieLine1);
        assertCriterionTrue(criterion, tieLine2);
        assertCriterionTrue(criterion, tieLine3);
        assertCriterionTrue(criterion, tieLine4);

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertCriterionFalse(criterion, anotherTypeElement);
    }

    @Test
    void nominalVoltageTest() {
        TieLineCriterion criterion = new TieLineCriterion(null, new TwoNominalVoltageCriterion(
                VoltageInterval.between(40., 100., true, true),
                null));
        assertCriterionTrue(criterion, tieLine1);
        assertCriterionFalse(criterion, tieLine2);
        assertCriterionFalse(criterion, tieLine3);
        assertCriterionTrue(criterion, tieLine4);
    }

    @Test
    void countriesTest() {
        TieLineCriterion criterion = new TieLineCriterion(new TwoCountriesCriterion(List.of(Country.FR), List.of(Country.BE)), null);
        assertCriterionFalse(criterion, tieLine1);
        assertCriterionFalse(criterion, tieLine2);
        assertCriterionTrue(criterion, tieLine3);
        assertCriterionFalse(criterion, tieLine4);

        criterion = new TieLineCriterion(new TwoCountriesCriterion(List.of(Country.FR, Country.BE), List.of(Country.BE)), null);
        assertCriterionFalse(criterion, tieLine1);
        assertCriterionFalse(criterion, tieLine2);
        assertCriterionTrue(criterion, tieLine3);
        assertCriterionTrue(criterion, tieLine4);
    }

    @Test
    void mixedCriteriaTest() {
        TieLineCriterion criterion = new TieLineCriterion(new TwoCountriesCriterion(List.of(Country.FR), List.of(Country.FR)),
                new TwoNominalVoltageCriterion(
                        VoltageInterval.between(350., 450., true, true),
                        null));
        assertCriterionFalse(criterion, tieLine1);
        assertCriterionTrue(criterion, tieLine2);
        assertCriterionFalse(criterion, tieLine3);
        assertCriterionFalse(criterion, tieLine4);
    }

    private void assertCriterionTrue(TieLineCriterion criterion, NetworkElement tieLine) {
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine, ThreeSides.ONE)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine, ThreeSides.TWO)));
    }

    private void assertCriterionFalse(TieLineCriterion criterion, NetworkElement tieLine) {
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine, ThreeSides.ONE)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine, ThreeSides.TWO)));
    }

    protected static NetworkElement createTieLine(String id, Country country1, Country country2, double nominalVoltage) {
        TieLine tieLine = mockTieLine(id, country1, country2, nominalVoltage, nominalVoltage);
        return new DefaultNetworkElementAdapter(tieLine);
    }
}


