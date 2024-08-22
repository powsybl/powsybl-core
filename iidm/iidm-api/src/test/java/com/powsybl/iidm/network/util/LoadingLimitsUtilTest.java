/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.LoadingLimitsAdder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LoadingLimitsUtilTest {

    @ParameterizedTest()
    @MethodSource("getDirectCallFromAdderParameters")
    void computeAs100PercentOfFirstTemporaryLimitTest(boolean directCallFromAdder) {
        TemporaryLimitToCreate tl1 = new TemporaryLimitToCreate("5'", 300, 1000.);
        TemporaryLimitToCreate tl2 = new TemporaryLimitToCreate("1'", 60, 1200.);
        LimitsAdder<?, ?> adder = new LimitsAdder<>("ownerId", Double.NaN, List.of(tl1, tl2));

        if (directCallFromAdder) {
            adder.fixLimits();
        } else {
            LoadingLimitsUtil.fixMissingPermanentLimit(adder, 100.);
        }
        assertEquals(1000., adder.getPermanentLimit(), 0.001);
        assertEquals(2, adder.getTemporaryLimitToCreateList().size());
        List<TemporaryLimitToCreate> temps = adder.getTemporaryLimitToCreateList().stream().toList();
        assertEquals(1000., temps.get(0).value());
        assertEquals(1200., temps.get(1).value());
    }

    @ParameterizedTest()
    @MethodSource("getDirectCallFromAdderParameters")
    void computeAsCustomPercentageOfFirstTemporaryLimitTest(boolean directCallFromAdder) {
        TemporaryLimitToCreate tl1 = new TemporaryLimitToCreate("5'", 300, 1000.);
        TemporaryLimitToCreate tl2 = new TemporaryLimitToCreate("1'", 60, 1200.);
        LimitsAdder<?, ?> adder = new LimitsAdder<>("ownerId", Double.NaN, List.of(tl1, tl2));

        CustomLogger customLogger = new CustomLogger();
        if (directCallFromAdder) {
            adder.fixLimits(90., customLogger);
        } else {
            LoadingLimitsUtil.fixMissingPermanentLimit(adder, 90., adder.getOwnerId(), customLogger);
        }

        // The percentage is applied
        assertEquals(900., adder.getPermanentLimit(), 0.001);
        assertEquals(2, adder.getTemporaryLimitToCreateList().size());

        List<TemporaryLimitToCreate> temps = adder.getTemporaryLimitToCreateList().stream().toList();
        assertEquals(1000., temps.get(0).value());
        assertEquals(1200., temps.get(1).value());

        LoggedData loggedData = customLogger.getLoggedData();
        assertEquals("Operational Limits of ownerId", loggedData.what());
        assertEquals("Operational limits without permanent limit is considered with permanent limit " +
                "equal to lowest temporary limit value weighted by a coefficient of 0.9.", loggedData.reason());
        assertTrue(Double.isNaN(loggedData.wrongValue()));
        assertEquals(900., loggedData.fixedValue(), 0.001);
    }

    static Stream<Arguments> getDirectCallFromAdderParameters() {
        return Stream.of(
                Arguments.of(Boolean.FALSE),
                Arguments.of(Boolean.TRUE)
        );
    }

    @Test
    void computeWithTemporaryLimitWithInfiniteAcceptableDurationTest() {
        TemporaryLimitToCreate tl1 = new TemporaryLimitToCreate("Infinite", MAX_VALUE, 1000.);
        TemporaryLimitToCreate tl2 = new TemporaryLimitToCreate("1'", 60, 1200.);
        LimitsAdder<?, ?> adder = new LimitsAdder<>("ownerId", Double.NaN, List.of(tl1, tl2));

        CustomLogger customLogger = new CustomLogger();
        LoadingLimitsUtil.fixMissingPermanentLimit(adder, 75., adder.getOwnerId(), customLogger);

        // The permanent limit is set with the value of temporary limit of infinite acceptable duration.
        // The percentage is not applied and the said temporary limit is removed from the temporary limits list.
        assertEquals(1000., adder.getPermanentLimit(), 0.001);
        assertEquals(1, adder.getTemporaryLimitToCreateList().size());
        List<TemporaryLimitToCreate> temps = adder.getTemporaryLimitToCreateList().stream().toList();
        assertEquals(1200., temps.get(0).value());

        LoggedData loggedData = customLogger.getLoggedData();
        assertEquals("Operational Limits of ownerId", loggedData.what());
        assertEquals("Operational limits without permanent limit is considered with permanent limit " +
                "equal to lowest temporary limit value with infinite acceptable duration", loggedData.reason());
        assertTrue(Double.isNaN(loggedData.wrongValue()));
        assertEquals(1000., loggedData.fixedValue(), 0.001);
    }

    private record TemporaryLimitToCreate(String name, int acceptableDuration, double value) {
    }

    private static class LimitsAdder<L extends LoadingLimits, A extends LimitsAdder<L, A>> implements LoadingLimitsAdder<L, A> {
        private final String ownerId;
        private final List<TemporaryLimitToCreate> temporaryLimitToCreateList;
        private double permanentLimit;

        public LimitsAdder(String ownerId, double permanentLimit, List<TemporaryLimitToCreate> temporaryLimitToCreateList) {
            this.ownerId = ownerId;
            this.permanentLimit = permanentLimit;
            this.temporaryLimitToCreateList = new ArrayList<>(temporaryLimitToCreateList);
        }

        public List<TemporaryLimitToCreate> getTemporaryLimitToCreateList() {
            return this.temporaryLimitToCreateList;
        }

        @Override
        public A setPermanentLimit(double limit) {
            this.permanentLimit = limit;
            return (A) this;
        }

        @Override
        public TemporaryLimitAdder<A> beginTemporaryLimit() {
            return null;
        }

        @Override
        public double getPermanentLimit() {
            return this.permanentLimit;
        }

        @Override
        public double getTemporaryLimitValue(int acceptableDuration) {
            return temporaryLimitToCreateList.stream()
                    .filter(t -> t.acceptableDuration() == acceptableDuration)
                    .map(TemporaryLimitToCreate::value)
                    .findFirst().orElse(Double.NaN);
        }

        private Optional<TemporaryLimitToCreate> getTemporaryLimitByName(String name) {
            return temporaryLimitToCreateList.stream()
                    .filter(t -> t.name().equals(name))
                    .findFirst();
        }

        @Override
        public double getTemporaryLimitValue(String name) {
            return getTemporaryLimitByName(name)
                    .map(TemporaryLimitToCreate::value)
                    .orElse(Double.NaN);
        }

        @Override
        public int getTemporaryLimitAcceptableDuration(String name) {
            return getTemporaryLimitByName(name)
                    .map(TemporaryLimitToCreate::acceptableDuration)
                    .orElse(MAX_VALUE);
        }

        @Override
        public double getLowestTemporaryLimitValue() {
            return temporaryLimitToCreateList.stream().map(TemporaryLimitToCreate::value).min(Double::compareTo).orElse(Double.NaN);
        }

        @Override
        public boolean hasTemporaryLimits() {
            return !temporaryLimitToCreateList.isEmpty();
        }

        @Override
        public Collection<String> getTemporaryLimitNames() {
            return temporaryLimitToCreateList.stream().map(TemporaryLimitToCreate::name).toList();
        }

        @Override
        public void removeTemporaryLimit(String name) {
            temporaryLimitToCreateList.removeIf(temporaryLimitToCreate -> temporaryLimitToCreate.name().equals(name));
        }

        @Override
        public String getOwnerId() {
            return this.ownerId;
        }

        @Override
        public L add() {
            return null;
        }
    }

    private record LoggedData(String what, String reason, double wrongValue, double fixedValue) {
    }

    private static class CustomLogger implements LoadingLimitsUtil.LimitFixLogger {
        private LoggedData loggedData;

        public void log(String what, String reason, double wrongValue, double fixedValue) {
            loggedData = new LoggedData(what, reason, wrongValue, fixedValue);
        }

        public LoggedData getLoggedData() {
            return loggedData;
        }
    }
}
