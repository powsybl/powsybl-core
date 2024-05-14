/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

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

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LimitReductionBuilderTest {

    @Test
    void defaultValuesTest() {
        LimitReduction limitReduction = LimitReduction.builder(LimitType.APPARENT_POWER, 0.5).build();
        assertEquals(LimitType.APPARENT_POWER, limitReduction.getLimitType());
        assertEquals(0.5, limitReduction.getValue(), 0.01);
        assertFalse(limitReduction.isMonitoringOnly());
        assertEquals(ContingencyContext.all(), limitReduction.getContingencyContext());
        assertTrue(limitReduction.getNetworkElementCriteria().isEmpty());
        assertTrue(limitReduction.getDurationCriteria().isEmpty());
    }

    @Test
    void allValuesTest() {
        NetworkElementCriterion nec0 = new TieLineCriterion(null, null);
        NetworkElementCriterion nec1 = new LineCriterion(null, null);
        NetworkElementCriterion nec2 = new TwoWindingsTransformerCriterion(null, null);
        LimitDurationCriterion ldc0 = new PermanentDurationCriterion();
        LimitDurationCriterion ldc1 = new EqualityTemporaryDurationCriterion(300);
        LimitReduction limitReduction = LimitReduction.builder(LimitType.CURRENT, 0.9)
                .withMonitoringOnly(true)
                .withContingencyContext(ContingencyContext.none())
                .withNetworkElementCriteria(nec0)
                .withNetworkElementCriteria(nec1, nec2) // replace previously defined NetworkElementCriteria
                .withLimitDurationCriteria(ldc0)
                .withLimitDurationCriteria(ldc1) // replace previously defined LimitDurationCriterion
                .build();
        assertEquals(LimitType.CURRENT, limitReduction.getLimitType());
        assertEquals(0.9, limitReduction.getValue(), 0.01);
        assertTrue(limitReduction.isMonitoringOnly());
        assertEquals(ContingencyContext.none(), limitReduction.getContingencyContext());
        assertEquals(2, limitReduction.getNetworkElementCriteria().size());
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.LINE,
                limitReduction.getNetworkElementCriteria().get(0).getNetworkElementCriterionType());
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER,
                limitReduction.getNetworkElementCriteria().get(1).getNetworkElementCriterionType());
        assertEquals(1, limitReduction.getDurationCriteria().size());
        assertEquals(AbstractTemporaryDurationCriterion.TemporaryDurationCriterionType.EQUALITY,
                ((AbstractTemporaryDurationCriterion) limitReduction.getDurationCriteria().get(0)).getComparisonType());
    }
}
