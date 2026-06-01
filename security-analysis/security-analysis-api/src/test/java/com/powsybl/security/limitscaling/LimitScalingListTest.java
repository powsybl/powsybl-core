/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling;

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
class LimitScalingListTest {

    private static LimitScaling limitScaling1;
    private static LimitScaling limitScaling2;
    private static NetworkElementCriterion networkElementCriterion1;
    private static NetworkElementCriterion networkElementCriterion2;
    private static NetworkElementCriterion networkElementCriterion3;
    private static NetworkElementCriterion networkElementCriterion4;
    private static NetworkElementCriterion networkElementCriterion5;
    private static ContingencyContext contingencyContext1;
    private static String operationalLimitsGroupId1;
    private static String operationalLimitsGroupId2;

    @BeforeAll
    static void init() {
        networkElementCriterion1 = new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1"));
        networkElementCriterion2 = new LineCriterion(null, new TwoNominalVoltageCriterion(
                VoltageInterval.between(350., 410., true, false),
                null));
        networkElementCriterion3 = new TwoWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        networkElementCriterion4 = new ThreeWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null);
        networkElementCriterion5 = new TieLineCriterion(null, new TwoNominalVoltageCriterion(
                VoltageInterval.between(350., 410., true, false),
                null));

        contingencyContext1 = ContingencyContext.specificContingency("contingency1");

        operationalLimitsGroupId1 = "default";
        operationalLimitsGroupId2 = "activated";

        limitScaling1 = LimitScaling.builder(LimitType.CURRENT, 0.9)
                .withContingencyContext(contingencyContext1)
                .withNetworkElementCriteria(networkElementCriterion1, networkElementCriterion2,
                        networkElementCriterion3, networkElementCriterion4, networkElementCriterion5)
                .withLimitDurationCriteria(new PermanentDurationCriterion(), new AllTemporaryDurationCriterion())
            .withOperationalLimitsGroupIdSelection(operationalLimitsGroupId1, operationalLimitsGroupId2)
                .build();
        limitScaling2 = new LimitScaling(LimitType.ACTIVE_POWER, 0.8, true);
    }

    @Test
    void limitReductionListTest() {
        LimitScalingList limitScalingList = new LimitScalingList(List.of(limitScaling1, limitScaling2));
        assertEquals(List.of(limitScaling1, limitScaling2), limitScalingList.getLimitReductions());
    }

    @Test
    void limitReductionGetType() {
        assertEquals(LimitType.CURRENT, limitScaling1.getLimitType());
        assertEquals(LimitType.ACTIVE_POWER, limitScaling2.getLimitType());
    }

    @Test
    void limitReductionGetValue() {
        assertEquals(0.9, limitScaling1.getValue(), 0.001);
        assertEquals(0.8, limitScaling2.getValue(), 0.001);
    }

    @Test
    void limitReductionIsMonitoringOnly() {
        assertFalse(limitScaling1.isMonitoringOnly());
        assertTrue(limitScaling2.isMonitoringOnly());
    }

    @Test
    void limitReductionGetNetworkElementCriteria() {
        assertEquals(List.of(networkElementCriterion1, networkElementCriterion2,
                networkElementCriterion3, networkElementCriterion4, networkElementCriterion5),
                limitScaling1.getNetworkElementCriteria());
        assertTrue(limitScaling2.getNetworkElementCriteria().isEmpty());
    }

    @Test
    void limitReductionGetContingencyContext() {
        assertEquals(contingencyContext1, limitScaling1.getContingencyContext());
        assertEquals(ContingencyContext.all(), limitScaling2.getContingencyContext());
    }

    @Test
    void limitReductionGetDurationCriteria() {
        assertEquals(2, limitScaling1.getDurationCriteria().size());
        assertInstanceOf(PermanentDurationCriterion.class, limitScaling1.getDurationCriteria().get(0));
        assertInstanceOf(AllTemporaryDurationCriterion.class, limitScaling1.getDurationCriteria().get(1));
        assertTrue(limitScaling2.getDurationCriteria().isEmpty());
    }

    @Test
    void operationalLimitsGroupIdSelection() {
        assertEquals(List.of(operationalLimitsGroupId1, operationalLimitsGroupId2), limitScaling1.getOperationalLimitsGroupIdsSelection());
        assertTrue(limitScaling2.getOperationalLimitsGroupIdsSelection().isEmpty());

    }

    @Test
    void unsupportedLimitType() {
        Exception e = assertThrows(PowsyblException.class, () -> new LimitScaling(LimitType.VOLTAGE, 0.9));
        assertEquals("VOLTAGE is not a supported limit type for limit reduction", e.getMessage());
    }

    @Test
    void unsupportedLimitReductionValues() {
        String expectedMessage = "Limit reduction value should be equal or greater than 0";
        Exception e = assertThrows(PowsyblException.class, () -> new LimitScaling(LimitType.CURRENT, -0.5, true));
        assertEquals(expectedMessage, e.getMessage());
    }
}
