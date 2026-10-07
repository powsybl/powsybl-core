/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.criteria.LineCriterion;
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.iidm.criteria.TieLineCriterion;
import com.powsybl.iidm.criteria.TwoWindingsTransformerCriterion;
import com.powsybl.iidm.criteria.duration.AbstractTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.EqualityTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;
import com.powsybl.iidm.criteria.duration.PermanentDurationCriterion;
import com.powsybl.iidm.network.LimitType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LimitScalingBuilderTest {

    @Test
    void defaultValuesTest() {
        LimitScaling limitScaling = LimitScaling.builder(LimitType.APPARENT_POWER, 0.5).build();
        assertEquals(LimitType.APPARENT_POWER, limitScaling.getLimitType());
        assertEquals(0.5, limitScaling.getValue(), 0.01);
        assertFalse(limitScaling.isMonitoringOnly());
        assertEquals(ContingencyContext.all(), limitScaling.getContingencyContext());
        assertTrue(limitScaling.getNetworkElementCriteria().isEmpty());
        assertTrue(limitScaling.getDurationCriteria().isEmpty());
        assertTrue(limitScaling.getOperationalLimitsGroupIdsSelection().isEmpty());
    }

    @Test
    void allValuesTest() {
        NetworkElementCriterion nec0 = new TieLineCriterion(null, null);
        NetworkElementCriterion nec1 = new LineCriterion(null, null);
        NetworkElementCriterion nec2 = new TwoWindingsTransformerCriterion(null, null);
        LimitDurationCriterion ldc0 = new PermanentDurationCriterion();
        LimitDurationCriterion ldc1 = new EqualityTemporaryDurationCriterion(300);
        LimitScaling limitScaling = LimitScaling.builder(LimitType.CURRENT, 0.9)
                .withMonitoringOnly(true)
                .withContingencyContext(ContingencyContext.none())
                .withNetworkElementCriteria(nec0)
                .withNetworkElementCriteria(nec1, nec2) // replace previously defined NetworkElementCriteria
                .withLimitDurationCriteria(ldc0)
                .withLimitDurationCriteria(ldc1) // replace previously defined LimitDurationCriterion
                .withOperationalLimitsGroupIdSelection("incorrect", "also incorrect")
                .withOperationalLimitsGroupIdSelection("correct", "good", "also good")
                .build();
        assertEquals(LimitType.CURRENT, limitScaling.getLimitType());
        assertEquals(0.9, limitScaling.getValue(), 0.01);
        assertTrue(limitScaling.isMonitoringOnly());
        assertEquals(ContingencyContext.none(), limitScaling.getContingencyContext());
        assertEquals(2, limitScaling.getNetworkElementCriteria().size());
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.LINE,
                limitScaling.getNetworkElementCriteria().get(0).getNetworkElementCriterionType());
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER,
                limitScaling.getNetworkElementCriteria().get(1).getNetworkElementCriterionType());
        assertEquals(1, limitScaling.getDurationCriteria().size());
        assertEquals(AbstractTemporaryDurationCriterion.TemporaryDurationCriterionType.EQUALITY,
                ((AbstractTemporaryDurationCriterion) limitScaling.getDurationCriteria().get(0)).getComparisonType());
        assertEquals(limitScaling.getOperationalLimitsGroupIdsSelection(), List.of("correct", "good", "also good"));
    }
}
