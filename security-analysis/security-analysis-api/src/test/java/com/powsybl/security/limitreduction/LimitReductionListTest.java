/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.criteria.*;
import com.powsybl.iidm.criteria.duration.AllTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.PermanentDurationCriterion;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.LimitType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LimitReductionListTest {

    private static LimitReduction limitReduction1;
    private static LimitReduction limitReduction2;
    private static NetworkElementCriterion networkElementCriterion1;
    private static NetworkElementCriterion networkElementCriterion2;
    private static NetworkElementCriterion networkElementCriterion3;
    private static NetworkElementCriterion networkElementCriterion4;
    private static NetworkElementCriterion networkElementCriterion5;
    private static ContingencyContext contingencyContext1;

    @BeforeAll
    static void init() {
        networkElementCriterion1 = new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1"));
        networkElementCriterion2 = new LineCriterion(null, new TwoNominalVoltageCriterion(
                new SingleNominalVoltageCriterion.VoltageInterval(350., 410., true, false), null));
        networkElementCriterion3 = new TwoWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        networkElementCriterion4 = new ThreeWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        networkElementCriterion5 = new TieLineCriterion(null, new TwoNominalVoltageCriterion(
                new SingleNominalVoltageCriterion.VoltageInterval(350., 410., true, false), null));

        contingencyContext1 = ContingencyContext.specificContingency("contingency1");
        limitReduction1 = LimitReduction.builder(LimitType.CURRENT, 0.9)
                .withContingencyContext(contingencyContext1)
                .withNetworkElementCriteria(networkElementCriterion1, networkElementCriterion2,
                        networkElementCriterion3, networkElementCriterion4, networkElementCriterion5)
                .withLimitDurationCriteria(new PermanentDurationCriterion(), new AllTemporaryDurationCriterion())
                .build();
        limitReduction2 = new LimitReduction(LimitType.ACTIVE_POWER, 0.8, true);
    }

    @Test
    void limitReductionListTest() {
        LimitReductionList limitReductionList = new LimitReductionList(List.of(limitReduction1, limitReduction2));
        assertEquals(List.of(limitReduction1, limitReduction2), limitReductionList.getLimitReductions());
    }

    @Test
    void limitReductionGetType() {
        assertEquals(LimitType.CURRENT, limitReduction1.getLimitType());
        assertEquals(LimitType.ACTIVE_POWER, limitReduction2.getLimitType());
    }

    @Test
    void limitReductionGetValue() {
        assertEquals(0.9, limitReduction1.getValue(), 0.001);
        assertEquals(0.8, limitReduction2.getValue(), 0.001);
    }

    @Test
    void limitReductionIsMonitoringOnly() {
        assertFalse(limitReduction1.isMonitoringOnly());
        assertTrue(limitReduction2.isMonitoringOnly());
    }

    @Test
    void limitReductionGetNetworkElementCriteria() {
        assertEquals(List.of(networkElementCriterion1, networkElementCriterion2,
                networkElementCriterion3, networkElementCriterion4, networkElementCriterion5),
                limitReduction1.getNetworkElementCriteria());
        assertTrue(limitReduction2.getNetworkElementCriteria().isEmpty());
    }

    @Test
    void limitReductionGetContingencyContext() {
        assertEquals(contingencyContext1, limitReduction1.getContingencyContext());
        assertEquals(ContingencyContext.all(), limitReduction2.getContingencyContext());
    }

    @Test
    void limitReductionGetDurationCriteria() {
        assertEquals(2, limitReduction1.getDurationCriteria().size());
        assertInstanceOf(PermanentDurationCriterion.class, limitReduction1.getDurationCriteria().get(0));
        assertInstanceOf(AllTemporaryDurationCriterion.class, limitReduction1.getDurationCriteria().get(1));
        assertTrue(limitReduction2.getDurationCriteria().isEmpty());
    }

    @Test
    void unsupportedLimitType() {
        Exception e = assertThrows(PowsyblException.class, () -> new LimitReduction(LimitType.VOLTAGE, 0.9));
        assertEquals("VOLTAGE is not a supported limit type for limit reduction", e.getMessage());
    }

    @Test
    void unsupportedLimitReductionValues() {
        String expectedMessage = "Limit reduction value should be in [0;1]";
        Exception e = assertThrows(PowsyblException.class, () -> new LimitReduction(LimitType.CURRENT, -0.5, true));
        assertEquals(expectedMessage, e.getMessage());

        e = assertThrows(PowsyblException.class, () -> new LimitReduction(LimitType.CURRENT, 1.3));
        assertEquals(expectedMessage, e.getMessage());
    }
}
