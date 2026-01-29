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
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.ThreeSides;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapterTest.mockBoundaryLine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class BoundaryLineCriterionTest {
    private static NetworkElement boundaryLine1;
    private static NetworkElement boundaryLine2;
    private static NetworkElement boundaryLine3;
    private static NetworkElement boundaryLine4;

    @BeforeAll
    static void init() {
        boundaryLine1 = createBoundaryLine("boundaryLine1", Country.FR, 90);
        boundaryLine2 = createBoundaryLine("boundaryLine2", Country.FR, 400);
        boundaryLine3 = createBoundaryLine("boundaryLine3", Country.BE, 400);
        boundaryLine4 = createBoundaryLine("boundaryLine4", Country.BE, 90);
    }

    @Test
    void typeTest() {
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.DANGLING_LINE,
                new BoundaryLineCriterion(null, null).getNetworkElementCriterionType());
    }

    @Test
    void emptyCriterionTest() {
        BoundaryLineCriterion criterion = new BoundaryLineCriterion(null, null);
        assertCriterionTrue(criterion, boundaryLine1);
        assertCriterionTrue(criterion, boundaryLine2);
        assertCriterionTrue(criterion, boundaryLine3);
        assertCriterionTrue(criterion, boundaryLine4);

        NetworkElement anotherTypeElement = Mockito.mock(NetworkElement.class);
        assertFalse(criterion.accept(new NetworkElementVisitor(anotherTypeElement)));
    }

    @Test
    void nominalVoltageTest() {
        BoundaryLineCriterion criterion = new BoundaryLineCriterion(null, new SingleNominalVoltageCriterion(
                VoltageInterval.between(40., 100., true, true)));
        assertCriterionTrue(criterion, boundaryLine1);
        assertCriterionFalse(criterion, boundaryLine2);
        assertCriterionFalse(criterion, boundaryLine3);
        assertCriterionTrue(criterion, boundaryLine4);
    }

    @Test
    void countryTest() {
        BoundaryLineCriterion criterion = new BoundaryLineCriterion(new SingleCountryCriterion(List.of(Country.BE)), null);
        assertCriterionFalse(criterion, boundaryLine1);
        assertCriterionFalse(criterion, boundaryLine2);
        assertCriterionTrue(criterion, boundaryLine3);
        assertCriterionTrue(criterion, boundaryLine4);

        criterion = new BoundaryLineCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        assertCriterionTrue(criterion, boundaryLine1);
        assertCriterionTrue(criterion, boundaryLine2);
        assertCriterionTrue(criterion, boundaryLine3);
        assertCriterionTrue(criterion, boundaryLine4);
    }

    @Test
    void mixedCriteriaTest() {
        BoundaryLineCriterion criterion = new BoundaryLineCriterion(new SingleCountryCriterion(List.of(Country.FR)),
                new SingleNominalVoltageCriterion(
                        VoltageInterval.between(350., 450., true, true)));
        assertCriterionFalse(criterion, boundaryLine1);
        assertCriterionTrue(criterion, boundaryLine2);
        assertCriterionFalse(criterion, boundaryLine3);
        assertCriterionFalse(criterion, boundaryLine4);
    }

    private void assertCriterionTrue(BoundaryLineCriterion criterion, NetworkElement boundaryLine) {
        assertTrue(criterion.accept(new NetworkElementVisitor(boundaryLine)));
        assertTrue(criterion.accept(new NetworkElementVisitor(boundaryLine, ThreeSides.ONE)));
    }

    private void assertCriterionFalse(BoundaryLineCriterion criterion, NetworkElement boundaryLine) {
        assertFalse(criterion.accept(new NetworkElementVisitor(boundaryLine)));
        assertFalse(criterion.accept(new NetworkElementVisitor(boundaryLine, ThreeSides.ONE)));
    }

    protected static NetworkElement createBoundaryLine(String id, Country country, double nominalVoltage) {
        BoundaryLine boundaryLine = mockBoundaryLine(id, country, nominalVoltage);
        return new DefaultNetworkElementAdapter(boundaryLine);
    }
}


