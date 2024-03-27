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

import java.util.List;

import static com.powsybl.iidm.criteria.DanglingLineCriterionTest.createDanglingLine;
import static com.powsybl.iidm.criteria.LineCriterionTest.createLine;
import static com.powsybl.iidm.criteria.ThreeWindingsTransformerCriterionTest.createThreeWindingsTransformer;
import static com.powsybl.iidm.criteria.TieLineCriterionTest.createTieLine;
import static com.powsybl.iidm.criteria.TwoWindingsTransformerCriterionTest.createTwoWindingsTransformer;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class IdentifiableCriterionTest {
    private static NetworkElement danglingLine1;
    private static NetworkElement danglingLine2;
    private static NetworkElement line1;
    private static NetworkElement line2;
    private static NetworkElement tieLine1;
    private static NetworkElement tieLine2;
    private static NetworkElement twoWt1;
    private static NetworkElement twoWt2;
    private static NetworkElement threeWt1;
    private static NetworkElement threeWt2;

    @BeforeAll
    public static void init() {
        danglingLine1 = createDanglingLine("danglingLine4", Country.BE, 90);
        danglingLine2 = createDanglingLine("danglingLine2", Country.FR, 400);
        line1 = createLine("line1", Country.BE, Country.BE, 90);
        line2 = createLine("line2", Country.FR, Country.FR, 400);
        tieLine1 = createTieLine("tieLine1", Country.BE, Country.DE, 90);
        tieLine2 = createTieLine("tieLine2", Country.DE, Country.FR, 400);
        twoWt1 = createTwoWindingsTransformer("2wt1", Country.BE, 90, 63);
        twoWt2 = createTwoWindingsTransformer("2wt2", Country.FR, 220, 400);
        threeWt1 = createThreeWindingsTransformer("3wt1", Country.BE, 400, 90, 63);
        threeWt2 = createThreeWindingsTransformer("3wt2", Country.FR, 400, 220, 125);
    }

    @Test
    void typeTest() {
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.IDENTIFIABLE,
                new IdentifiableCriterion(new AtLeastOneCountryCriterion(List.of(Country.BE))).getNetworkElementCriterionType());
    }

    @Test
    void creationErrorsTest() {
        AtLeastOneCountryCriterion countryCriterion = new AtLeastOneCountryCriterion(List.of(Country.DE));
        AtLeastOneNominalVoltageCriterion nominalVoltageCriterion = new AtLeastOneNominalVoltageCriterion(
                VoltageInterval.between(40., 100., true, true));
        assertThrows(NullPointerException.class, () -> new IdentifiableCriterion("test1", null, null));
        assertThrows(NullPointerException.class, () -> new IdentifiableCriterion("test2", countryCriterion, null));
        assertThrows(NullPointerException.class, () -> new IdentifiableCriterion("test3", null, nominalVoltageCriterion));
        assertThrows(NullPointerException.class, () -> new IdentifiableCriterion((AtLeastOneCountryCriterion) null, null));
        assertThrows(NullPointerException.class, () -> new IdentifiableCriterion(countryCriterion, null));
        assertThrows(NullPointerException.class, () -> new IdentifiableCriterion((AtLeastOneCountryCriterion) null, nominalVoltageCriterion));
        assertThrows(NullPointerException.class, () -> new IdentifiableCriterion("test7", (AtLeastOneCountryCriterion) null));
        assertThrows(NullPointerException.class, () -> new IdentifiableCriterion(null, (AtLeastOneCountryCriterion) null));
        assertThrows(NullPointerException.class, () -> new IdentifiableCriterion("test8", (AtLeastOneNominalVoltageCriterion) null));
        assertThrows(NullPointerException.class, () -> new IdentifiableCriterion((String) null, (AtLeastOneNominalVoltageCriterion) null));

    }

    @Test
    void nominalVoltageTest() {
        IdentifiableCriterion criterion = new IdentifiableCriterion(new AtLeastOneNominalVoltageCriterion(
                VoltageInterval.between(40., 100., true, true)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twoWt1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twoWt2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(threeWt1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(threeWt2)));
    }

    @Test
    void countriesTest() {
        IdentifiableCriterion criterion = new IdentifiableCriterion(new AtLeastOneCountryCriterion(List.of(Country.FR)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twoWt1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twoWt2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(threeWt1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(threeWt2)));

        criterion = new IdentifiableCriterion(new AtLeastOneCountryCriterion(List.of(Country.FR, Country.BE)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(danglingLine2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(line2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(tieLine2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twoWt1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twoWt2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(threeWt1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(threeWt2)));
    }

    @Test
    void mixedCriteriaTest() {
        IdentifiableCriterion criterion = new IdentifiableCriterion(new AtLeastOneCountryCriterion(List.of(Country.FR)),
                new AtLeastOneNominalVoltageCriterion(VoltageInterval.between(190., 230., true, true)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(danglingLine2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(line2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(tieLine2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(twoWt1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(twoWt2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(threeWt1)));
        assertTrue(criterion.accept(new NetworkElementVisitor(threeWt2)));
    }

}
