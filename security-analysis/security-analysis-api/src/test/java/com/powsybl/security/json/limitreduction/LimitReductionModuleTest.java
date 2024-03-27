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
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.security.limitreduction.LimitReductionList;
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
        ContingencyContext contingencyContext1 = ContingencyContext.specificContingency("contingency1");
        List<NetworkElementCriterion> networkElementCriteria1 =
                List.of(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1")),
                        new LineCriterion(new TwoCountriesCriterion(List.of(Country.FR)), new TwoNominalVoltageCriterion(
                                VoltageInterval.between(350., 410., true, false),
                                null)),
                        new TwoWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null),
                        new ThreeWindingsTransformerCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)), null),
                        new TieLineCriterion(null, new TwoNominalVoltageCriterion(
                                VoltageInterval.between(350., 410., true, false),
                                null)),
                        new DanglingLineCriterion(null, new SingleNominalVoltageCriterion(
                                VoltageInterval.between(80., 100., true, false))));
        List<LimitDurationCriterion> durationCriteria1 = List.of(new PermanentDurationCriterion(), new AllTemporaryDurationCriterion());
        LimitReduction limitReduction1 = LimitReduction.builder(LimitType.CURRENT, 0.9)
                .withContingencyContext(contingencyContext1)
                .withNetworkElementCriteria(networkElementCriteria1)
                .withLimitDurationCriteria(durationCriteria1)
                .build();

        LimitReduction limitReduction2 = LimitReduction.builder(LimitType.APPARENT_POWER, 0.5)
                .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_2")))
                .withLimitDurationCriteria(IntervalTemporaryDurationCriterion.builder()
                        .setLowBound(10 * 60, true)
                        .setHighBound(20 * 60, true)
                        .build())
                .build();
        LimitReduction limitReduction3 = new LimitReduction(LimitType.ACTIVE_POWER, 0.8, true);

        LimitReduction limitReduction4 = LimitReduction.builder(LimitType.CURRENT, 0.9)
                .withNetworkElementCriteria(new IdentifiableCriterion(
                        new AtLeastOneCountryCriterion(List.of(Country.FR)),
                        new AtLeastOneNominalVoltageCriterion(
                                VoltageInterval.between(380., 410., true, true)
                        )))
                .withLimitDurationCriteria(IntervalTemporaryDurationCriterion.between(300, 600, true, false))
                .build();

        LimitReductionList limitReductionList = new LimitReductionList(List.of(limitReduction1, limitReduction2, limitReduction3, limitReduction4));

        roundTripTest(limitReductionList, LimitReductionListSerDeUtil::write,
                LimitReductionListSerDeUtil::read,
                "/LimitReductions.json");
    }
}
