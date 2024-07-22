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
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.ThreeSides;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapterTest.mockLine;
import static org.junit.jupiter.api.Assertions.*;

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
        assertCriterionTrue(criterion, line1);
        assertCriterionTrue(criterion, line2);
        assertCriterionTrue(criterion, line3);
        assertCriterionTrue(criterion, line4);

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertCriterionFalse(criterion, anotherTypeElement);
    }

    @Test
    void nominalVoltageTest() {
        LineCriterion criterion = new LineCriterion(null, new TwoNominalVoltageCriterion(
                VoltageInterval.between(40., 100., true, true),
                null));
        assertCriterionTrue(criterion, line1);
        assertCriterionFalse(criterion, line2);
        assertCriterionFalse(criterion, line3);
        assertCriterionTrue(criterion, line4);
    }

    @Test
    void countriesTest() {
        LineCriterion criterion = new LineCriterion(new TwoCountriesCriterion(List.of(Country.FR), List.of(Country.BE)), null);
        assertCriterionFalse(criterion, line1);
        assertCriterionFalse(criterion, line2);
        assertCriterionTrue(criterion, line3);
        assertCriterionFalse(criterion, line4);

        criterion = new LineCriterion(new TwoCountriesCriterion(List.of(Country.FR, Country.BE), List.of(Country.BE)), null);
        assertCriterionFalse(criterion, line1);
        assertCriterionFalse(criterion, line2);
        assertCriterionTrue(criterion, line3);
        assertCriterionTrue(criterion, line4);
    }

    @Test
    void mixedCriteriaTest() {
        LineCriterion criterion = new LineCriterion(new TwoCountriesCriterion(List.of(Country.FR), List.of(Country.FR)),
                new TwoNominalVoltageCriterion(
                        VoltageInterval.between(350., 450., true, true),
                        null));
        assertCriterionFalse(criterion, line1);
        assertCriterionTrue(criterion, line2);
        assertCriterionFalse(criterion, line3);
        assertCriterionFalse(criterion, line4);
    }

    private void assertCriterionTrue(LineCriterion criterion, NetworkElement line) {
        assertTrue(criterion.accept(new NetworkElementVisitor(line)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line, ThreeSides.ONE)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line, ThreeSides.TWO)));
    }

    private void assertCriterionFalse(LineCriterion criterion, NetworkElement line) {
        assertFalse(criterion.accept(new NetworkElementVisitor(line)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line, ThreeSides.ONE)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line, ThreeSides.TWO)));
    }

    protected static NetworkElement createLine(String id, Country country1, Country country2, double nominalVoltage) {
        Line l = mockLine(id, country1, country2, nominalVoltage, nominalVoltage);
        return new DefaultNetworkElementAdapter(l);
    }
}


