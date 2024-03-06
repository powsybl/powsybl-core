/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitsreduction;

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
class LimitReductionDefinitionListTest {

    private static LimitReductionDefinition definition1;
    private static LimitReductionDefinition definition2;
    private static NetworkElementCriterion networkElementCriterion1;
    private static NetworkElementCriterion networkElementCriterion2;
    private static NetworkElementCriterion networkElementCriterion3;
    private static NetworkElementCriterion networkElementCriterion4;
    private static ContingencyContext contingencyContext1;

    @BeforeAll
    static void init() {
        networkElementCriterion1 = new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1"));
        networkElementCriterion2 = new LineCriterion().setSingleNominalVoltageCriterion(new SingleNominalVoltageCriterion(
                new SingleNominalVoltageCriterion.VoltageInterval(350., 410., true, false)));
        networkElementCriterion3 = new TwoWindingsTransformerCriterion().setSingleCountryCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)));
        networkElementCriterion4 = new ThreeWindingsTransformerCriterion().setSingleCountryCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)));

        contingencyContext1 = ContingencyContext.specificContingency("contingency1");
        definition1 = new LimitReductionDefinition(LimitType.CURRENT)
                .setLimitReduction(0.9f)
                .setNetworkElementCriteria(networkElementCriterion1, networkElementCriterion2,
                        networkElementCriterion3, networkElementCriterion4)
                .setContingencyContexts(contingencyContext1, ContingencyContext.none())
                .setDurationCriteria(new PermanentDurationCriterion(), new AllTemporaryDurationCriterion());
        definition2 = new LimitReductionDefinition(LimitType.ACTIVE_POWER)
                .setLimitReduction(0.8f);
    }

    @Test
    void limitReductionDefinitionListTest() {
        LimitReductionDefinitionList definitionList = new LimitReductionDefinitionList()
                .setLimitReductionDefinitions(List.of(definition1, definition2));
        assertEquals(List.of(definition1, definition2), definitionList.getLimitReductionDefinitions());
    }

    @Test
    void limitReductionDefinitionGetType() {
        assertEquals(LimitType.CURRENT, definition1.getLimitType());
        assertEquals(LimitType.ACTIVE_POWER, definition2.getLimitType());
    }

    @Test
    void limitReductionDefinitionGetValue() {
        assertEquals(0.9f, definition1.getLimitReduction());
        assertEquals(0.8f, definition2.getLimitReduction());
    }

    @Test
    void limitReductionDefinitionGetNetworkElementCriteria() {
        assertEquals(List.of(networkElementCriterion1, networkElementCriterion2,
                networkElementCriterion3, networkElementCriterion4),
                definition1.getNetworkElementCriteria());
        assertTrue(definition2.getNetworkElementCriteria().isEmpty());
    }

    @Test
    void limitReductionDefinitionGetContingencyContexts() {
        assertEquals(List.of(contingencyContext1, ContingencyContext.none()), definition1.getContingencyContexts());
        assertTrue(definition2.getContingencyContexts().isEmpty());
    }

    @Test
    void limitReductionDefinitionGetDurationCriteria() {
        assertEquals(2, definition1.getDurationCriteria().size());
        assertInstanceOf(PermanentDurationCriterion.class, definition1.getDurationCriteria().get(0));
        assertInstanceOf(AllTemporaryDurationCriterion.class, definition1.getDurationCriteria().get(1));
        assertTrue(definition2.getDurationCriteria().isEmpty());
    }

    @Test
    void unsupportedLimitType() {
        Exception e = assertThrows(PowsyblException.class, () -> new LimitReductionDefinition(LimitType.VOLTAGE));
        assertEquals("VOLTAGE is not a supported limit type for limit reduction", e.getMessage());
    }

    @Test
    void unsupportedLimitReductionValues() {
        String expectedMessage = "Limit reduction value should be in [0;1]";
        LimitReductionDefinition definition = new LimitReductionDefinition(LimitType.CURRENT);

        Exception e = assertThrows(PowsyblException.class, () -> definition.setLimitReduction(-0.5f));
        assertEquals(expectedMessage, e.getMessage());

        e = assertThrows(PowsyblException.class, () -> definition.setLimitReduction(1.3f));
        assertEquals(expectedMessage, e.getMessage());
    }
}
