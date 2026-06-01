/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitscaling;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.criteria.*;
import com.powsybl.iidm.criteria.duration.AllTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.IntervalTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;
import com.powsybl.iidm.criteria.duration.PermanentDurationCriterion;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.security.limitscaling.LimitScaling;
import com.powsybl.security.limitscaling.LimitScalingList;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;
import static org.assertj.core.groups.Tuple.tuple;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LimitScalingModuleTest extends AbstractSerDeTest {

    @Test
    void limitReductionReadV10() {
        LimitScalingList limitScalingList = LimitScalingListSerDeUtil.read(getClass().getResourceAsStream("/LimitReductionsV1.0.json"));
        LimitScalingList expectedReductions = new LimitScalingList(
            List.of(
                getLimitReduction1(),
                getLimitReduction2(),
                getLimitReduction3(),
                getLimitReduction4()
            )
        );
        compareLimitReductionList(expectedReductions, limitScalingList);
    }

    @Test
    void limitReductionReadV11() {
        LimitScalingList limitScalingList = LimitScalingListSerDeUtil.read(getClass().getResourceAsStream("/LimitReductionsV1.1.json"));
        LimitScalingList expectedReductions = new LimitScalingList(
            List.of(
                getLimitReduction1(),
                getLimitReduction2(),
                getLimitReduction3(),
                getLimitReduction4(),
                getLimitReduction5()
            )
        );
        compareLimitReductionList(expectedReductions, limitScalingList);
    }

    private void compareLimitReductionList(LimitScalingList expected, LimitScalingList actual) {
        Assertions.assertThat(actual.getLimitReductions())
            .hasSize(expected.getLimitReductions().size())
            .usingComparatorForType((Double a, Double b) -> {
                double tolerance = 1e-2;
                return Math.abs(a - b) < tolerance ? 0 : Double.compare(a, b);
            }, Double.class)
            .extracting(
                LimitScaling::getLimitType,
                LimitScaling::getValue,
                l -> l.getContingencyContext().getContingencyId(),
                l -> l.getNetworkElementCriteria().stream().map(NetworkElementCriterion::getNetworkElementCriterionType).toList(),
                l -> l.getDurationCriteria().stream().map(LimitDurationCriterion::getType).toList(),
                LimitScaling::getOperationalLimitsGroupIdsSelection
            ).containsExactlyInAnyOrderElementsOf(expected.getLimitReductions().stream()
                .map(r -> tuple(
                        r.getLimitType(),
                        r.getValue(),
                        r.getContingencyContext().getContingencyId(),
                        r.getNetworkElementCriteria().stream().map(NetworkElementCriterion::getNetworkElementCriterionType).toList(),
                        r.getDurationCriteria().stream().map(LimitDurationCriterion::getType).toList(),
                        r.getOperationalLimitsGroupIdsSelection()
                    )
                ).toList()
            );
    }

    @Test
    void roundTripTest() throws IOException {
        LimitScalingList limitScalingList = new LimitScalingList(
            List.of(
                getLimitReduction1(),
                getLimitReduction2(),
                getLimitReduction3(),
                getLimitReduction4(),
                getLimitReduction5(),
                getLimitReduction6()
            ));

        roundTripTest(limitScalingList, LimitScalingListSerDeUtil::write,
                LimitScalingListSerDeUtil::read,
            "/LimitReductionsV1.2.json");
    }

    @Test
    void compatibilityWithOldCriterion() throws IOException {
        LimitScalingList reductionList = LimitScalingListSerDeUtil.read(getClass().getResourceAsStream("/LimitReductionsV1.0.json"));
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            LimitScalingListSerDeUtil.write(reductionList, bos);
            ComparisonUtils.assertTxtEquals(getClass().getResourceAsStream("/LimitReductions_no_limits_groupV1.2.json"), new ByteArrayInputStream(bos.toByteArray()));
        } catch (Exception e) {
            // Should not happen
            fail();
        }
    }

    private LimitScaling getLimitReduction1() {
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
                new BoundaryLineCriterion(null, new SingleNominalVoltageCriterion(
                    VoltageInterval.between(80., 100., true, false))));
        List<LimitDurationCriterion> durationCriteria1 = List.of(new PermanentDurationCriterion(), new AllTemporaryDurationCriterion());
        return LimitScaling.builder(LimitType.CURRENT, 0.9)
            .withContingencyContext(contingencyContext1)
            .withNetworkElementCriteria(networkElementCriteria1)
            .withLimitDurationCriteria(durationCriteria1)
            .build();
    }

    private LimitScaling getLimitReduction2() {
        return LimitScaling.builder(LimitType.APPARENT_POWER, 0.5)
            .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_2")))
            .withLimitDurationCriteria(IntervalTemporaryDurationCriterion.builder()
                .setLowBound(10 * 60, true)
                .setHighBound(20 * 60, true)
                .build())
            .build();
    }

    private LimitScaling getLimitReduction3() {
        return new LimitScaling(LimitType.ACTIVE_POWER, 0.8, true);
    }

    private LimitScaling getLimitReduction4() {
        return LimitScaling.builder(LimitType.CURRENT, 0.9)
            .withNetworkElementCriteria(new IdentifiableCriterion(
                new AtLeastOneCountryCriterion(List.of(Country.FR)),
                new AtLeastOneNominalVoltageCriterion(
                    VoltageInterval.between(380., 410., true, true)
                )))
            .withLimitDurationCriteria(IntervalTemporaryDurationCriterion.between(300, 600, true, false))
            .build();
    }

    private LimitScaling getLimitReduction5() {
        return LimitScaling.builder(LimitType.ACTIVE_POWER, 0.88)
            .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1", "NHV1_NHV2_2")))
            .withOperationalLimitsGroupIdSelection("DEFAULT", "activated_1_3", "activated_2_1")
            .build();
    }

    private LimitScaling getLimitReduction6() {
        return LimitScaling.builder(LimitType.CURRENT, 1.13)
            .withLimitDurationCriteria(IntervalTemporaryDurationCriterion.builder().setLowBound(60, true).build())
            .build();
    }
}
