/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitsreduction;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.util.criterion.*;
import com.powsybl.security.limitsreduction.LimitReductionDefinitionList;
import com.powsybl.security.limitsreduction.LimitReductionDefinitionList.LimitReductionDefinition;
import com.powsybl.security.limitsreduction.criterion.duration.AllTemporaryDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.duration.IntervalTemporaryDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.duration.PermanentDurationCriterion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LimitReductionModuleTest extends AbstractSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        LimitReductionDefinition definition1 = new LimitReductionDefinition(LimitType.CURRENT)
                .setLimitReduction(0.9)
                .setNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1")),
                        new LineCriterion().setSingleNominalVoltageCriterion(new SingleNominalVoltageCriterion(
                                new SingleNominalVoltageCriterion.VoltageInterval(350., 410., true, false))),
                        new TwoWindingsTransformerCriterion().setSingleCountryCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE))),
                        new ThreeWindingsTransformerCriterion().setSingleCountryCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE))))
                .setContingencyContexts(ContingencyContext.specificContingency("contingency1"), ContingencyContext.none())
                .setDurationCriteria(new PermanentDurationCriterion(), new AllTemporaryDurationCriterion());
        LimitReductionDefinition definition2 = new LimitReductionDefinition(LimitType.APPARENT_POWER)
                .setLimitReduction(0.5)
                .setNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_2")))
                .setDurationCriteria(IntervalTemporaryDurationCriterion.builder()
                        .setLowBound(10 * 60, true)
                        .setHighBound(20 * 60, true)
                        .build());
        LimitReductionDefinition definition3 = new LimitReductionDefinition(LimitType.ACTIVE_POWER)
                .setLimitReduction(0.8);
        LimitReductionDefinitionList definitionList = new LimitReductionDefinitionList()
                .setLimitReductionDefinitions(List.of(definition1, definition2, definition3));

        roundTripTest(definitionList, LimitReductionDefinitionListSerDeUtil::write,
                LimitReductionDefinitionListSerDeUtil::read,
                "/LimitReductions.json");
    }
}
