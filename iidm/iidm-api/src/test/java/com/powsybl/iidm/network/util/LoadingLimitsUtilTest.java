/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LoadingLimitsUtilTest {

    @Test
    void computeAs100PercentOfFirstTemporaryLimitTest() {
        LoadingLimits.TemporaryLimit tl1 = createTempLimit(300, 1000.);
        LoadingLimits.TemporaryLimit tl2 = createTempLimit(60, 1200.);
        LoadingLimits limits = createLimits(tl1, tl2);

        LoadingLimitsUtil.fixMissingPermanentLimit(limits, 100.);
        assertEquals(1000., limits.getPermanentLimit(), 0.001);
        assertEquals(2, limits.getTemporaryLimits().size());
        List<LoadingLimits.TemporaryLimit> temps = limits.getTemporaryLimits().stream().toList();
        assertEquals(1000., temps.get(0).getValue());
        assertEquals(1200., temps.get(1).getValue());
    }

    @Test
    void computeAsCustomPercentageOfFirstTemporaryLimitTest() {
        LoadingLimits.TemporaryLimit tl1 = createTempLimit(300, 1000.);
        LoadingLimits.TemporaryLimit tl2 = createTempLimit(60, 1200.);
        LoadingLimits limits = createLimits(tl1, tl2);

        CustomReporter reporter = new CustomReporter();
        LoadingLimitsUtil.fixMissingPermanentLimit(limits, 90., "parentId", reporter);

        // The percentage is applied
        assertEquals(900., limits.getPermanentLimit(), 0.001);
        assertEquals(2, limits.getTemporaryLimits().size());

        List<LoadingLimits.TemporaryLimit> temps = limits.getTemporaryLimits().stream().toList();
        assertEquals(1000., temps.get(0).getValue());
        assertEquals(1200., temps.get(1).getValue());

        ReportedData reportedData = reporter.getReportedData();
        assertEquals("Operational Limit Set of parentId", reportedData.what());
        assertEquals("An operational limit set without permanent limit is considered with permanent limit " +
                "equal to lowest TATL value weighted by a coefficient of 0.9.", reportedData.reason());
        assertTrue(Double.isNaN(reportedData.wrongValue()));
        assertEquals(900., reportedData.fixedValue(), 0.001);
    }

    @Test
    void computeWithTemporaryLimitWithInfiniteAcceptableDurationTest() {
        LoadingLimits.TemporaryLimit tl1 = createTempLimit(MAX_VALUE, 1000.);
        LoadingLimits.TemporaryLimit tl2 = createTempLimit(60, 1200.);
        LoadingLimits limits = createLimits(tl1, tl2);

        CustomReporter reporter = new CustomReporter();
        LoadingLimitsUtil.fixMissingPermanentLimit(limits, 75., "id", reporter);
        // The permanent limit is set with the value of temporary limit of infinite acceptable duration.
        // The percentage is not applied and the said temporary limit is removed from the temporary limits list.
        assertEquals(1000., limits.getPermanentLimit(), 0.001);
        assertEquals(1, limits.getTemporaryLimits().size());
        List<LoadingLimits.TemporaryLimit> temps = limits.getTemporaryLimits().stream().toList();
        assertEquals(1200., temps.get(0).getValue());

        ReportedData reportedData = reporter.getReportedData();
        assertEquals("Operational Limit Set of id", reportedData.what());
        assertEquals("An operational limit set without permanent limit is considered with permanent limit " +
                "equal to lowest TATL value with infinite acceptable duration", reportedData.reason());
        assertTrue(Double.isNaN(reportedData.wrongValue()));
        assertEquals(1000., reportedData.fixedValue(), 0.001);
    }

    private LoadingLimits createLimits(LoadingLimits.TemporaryLimit... tempLimits) {
        return new LoadingLimits() {
            private double permanentLimit = Double.NaN;
            private final Collection<TemporaryLimit> temporaryLimitCollection = new ArrayList<>(List.of(tempLimits));

            @Override
            public double getPermanentLimit() {
                return permanentLimit;
            }

            @Override
            public LoadingLimits setPermanentLimit(double permanentLimit) {
                this.permanentLimit = permanentLimit;
                return this;
            }

            @Override
            public Collection<TemporaryLimit> getTemporaryLimits() {
                return temporaryLimitCollection;
            }

            @Override
            public TemporaryLimit getTemporaryLimit(int acceptableDuration) {
                return null;
            }

            @Override
            public double getTemporaryLimitValue(int acceptableDuration) {
                return 0;
            }

            @Override
            public LimitType getLimitType() {
                return null;
            }
        };
    }

    private LoadingLimits.TemporaryLimit createTempLimit(int acceptableDuration, double value) {

        return new LoadingLimits.TemporaryLimit() {
            @Override
            public int getAcceptableDuration() {
                return acceptableDuration;
            }

            @Override
            public double getValue() {
                return value;
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public boolean isFictitious() {
                return false;
            }
        };
    }

    private record ReportedData(String what, String reason, double wrongValue, double fixedValue) {
    }

    private class CustomReporter implements LoadingLimitsUtil.PermanentLimitFixReporter {
        private ReportedData reportedData;

        public void log(String what, String reason, double wrongValue, double fixedValue) {
            reportedData = new ReportedData(what, reason, wrongValue, fixedValue);
        }

        public ReportedData getReportedData() {
            return reportedData;
        }
    }

}
