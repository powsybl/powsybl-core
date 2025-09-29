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
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.ThreeSides;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapterTest.mockDanglingLine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class BoundaryLineCriterionTest {
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
                new BoundaryLineCriterion(null, null).getNetworkElementCriterionType());
    }

    @Test
    void emptyCriterionTest() {
        BoundaryLineCriterion criterion = new BoundaryLineCriterion(null, null);
        assertCriterionTrue(criterion, danglingLine1);
        assertCriterionTrue(criterion, danglingLine2);
        assertCriterionTrue(criterion, danglingLine3);
        assertCriterionTrue(criterion, danglingLine4);

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertFalse(criterion.accept(new NetworkElementVisitor(anotherTypeElement)));
    }

    @Test
    void nominalVoltageTest() {
        BoundaryLineCriterion criterion = new BoundaryLineCriterion(null, new SingleNominalVoltageCriterion(
                VoltageInterval.between(40., 100., true, true)));
        assertCriterionTrue(criterion, danglingLine1);
        assertCriterionFalse(criterion, danglingLine2);
        assertCriterionFalse(criterion, danglingLine3);
        assertCriterionTrue(criterion, danglingLine4);
    }

    @Test
    void countryTest() {
        BoundaryLineCriterion criterion = new BoundaryLineCriterion(new SingleCountryCriterion(List.of(Country.BE)), null);
        assertCriterionFalse(criterion, danglingLine1);
        assertCriterionFalse(criterion, danglingLine2);
        assertCriterionTrue(criterion, danglingLine3);
        assertCriterionTrue(criterion, danglingLine4);

        criterion = new BoundaryLineCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        assertCriterionTrue(criterion, danglingLine1);
        assertCriterionTrue(criterion, danglingLine2);
        assertCriterionTrue(criterion, danglingLine3);
        assertCriterionTrue(criterion, danglingLine4);
    }

    @Test
    void mixedCriteriaTest() {
        BoundaryLineCriterion criterion = new BoundaryLineCriterion(new SingleCountryCriterion(List.of(Country.FR)),
                new SingleNominalVoltageCriterion(
                        VoltageInterval.between(350., 450., true, true)));
        assertCriterionFalse(criterion, danglingLine1);
        assertCriterionTrue(criterion, danglingLine2);
        assertCriterionFalse(criterion, danglingLine3);
        assertCriterionFalse(criterion, danglingLine4);
    }

    private void assertCriterionTrue(BoundaryLineCriterion criterion, NetworkElement danglingLine) {
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine, ThreeSides.ONE)));
    }

    private void assertCriterionFalse(BoundaryLineCriterion criterion, NetworkElement danglingLine) {
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine, ThreeSides.ONE)));
    }

    protected static NetworkElement createDanglingLine(String id, Country country, double nominalVoltage) {
        BoundaryLine boundaryLine = mockDanglingLine(id, country, nominalVoltage);
        return new DefaultNetworkElementAdapter(boundaryLine);
    }
}


