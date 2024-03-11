/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitreduction;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.criteria.*;
import com.powsybl.iidm.criteria.duration.AllTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.IntervalTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;
import com.powsybl.iidm.criteria.duration.PermanentDurationCriterion;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.security.limitreduction.LimitReductionDefinitionList;
import com.powsybl.security.limitreduction.LimitReductionDefinition;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LimitReductionModuleTest extends AbstractSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        List<ContingencyContext> contingencyContexts1 = List.of(ContingencyContext.specificContingency("contingency1"), ContingencyContext.none());
        List<NetworkElementCriterion> networkElementCriteria1 =
                List.of(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1")),
                        new LineCriterion(null, new SingleNominalVoltageCriterion(
                                new SingleNominalVoltageCriterion.VoltageInterval(350., 410., true, false))),
                        new TwoWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null),
                        new ThreeWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null));
        List<LimitDurationCriterion> durationCriteria1 = List.of(new PermanentDurationCriterion(), new AllTemporaryDurationCriterion());
        LimitReductionDefinition definition1 = new LimitReductionDefinition(LimitType.CURRENT, 0.9f,
                contingencyContexts1, networkElementCriteria1, durationCriteria1);

        LimitReductionDefinition definition2 = new LimitReductionDefinition(LimitType.APPARENT_POWER, 0.5f,
                Collections.emptyList(),
                List.of(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_2"))),
                List.of(IntervalTemporaryDurationCriterion.builder()
                        .setLowBound(10 * 60, true)
                        .setHighBound(20 * 60, true)
                        .build()));
        LimitReductionDefinition definition3 = new LimitReductionDefinition(LimitType.ACTIVE_POWER, 0.8f);
        LimitReductionDefinitionList definitionList = new LimitReductionDefinitionList(List.of(definition1, definition2, definition3));

        roundTripTest(definitionList, LimitReductionDefinitionListSerDeUtil::write,
                LimitReductionDefinitionListSerDeUtil::read,
                "/LimitReductions.json");
    }
}
