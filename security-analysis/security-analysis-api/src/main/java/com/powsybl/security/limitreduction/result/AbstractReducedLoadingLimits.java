/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction.result;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.util.LoadingLimitsUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * <p>Simple abstract implementation of {@link LoadingLimits} not linked to a network element, used to provide
 * reduced limits without altering the real limits of the network element.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractReducedLoadingLimits implements LoadingLimits {
    private final double permanentLimit;
    private final double originalPermanentLimit;
    private final double permanentLimitReduction;
    private final TreeMap<Integer, TemporaryLimit> temporaryLimits = new TreeMap<>(LoadingLimitsUtil.ACCEPTABLE_DURATION_COMPARATOR);

    /**
     * Stub class to pass Sonar Quality Gates over code duplication ...
     */
    private static final class UnsupportedPropertyStubs {

        public static boolean hasProperty() {
            throw new UnsupportedOperationException();
        }

        public static String getProperty() {
            throw new UnsupportedOperationException();
        }

        public static String setProperty() {
            throw new UnsupportedOperationException();
        }

        public static boolean removeProperty() {
            throw new UnsupportedOperationException();
        }

        public static Set<String> getPropertyNames() {
            return Set.of();
        }
    }

    public record ReducedTemporaryLimit(String name, double value, int acceptableDuration, boolean fictitious,
                                        double originalValue, double limitReduction) implements TemporaryLimit {
        @Override
        public String getName() {
            return name();
        }

        @Override
        public double getValue() {
            return value();
        }

        @Override
        public int getAcceptableDuration() {
            return acceptableDuration();
        }

        @Override
        public boolean isFictitious() {
            return fictitious();
        }

        public double getOriginalValue() {
            return originalValue();
        }

        public double getLimitReduction() {
            return limitReduction();
        }

        @Override
        public boolean hasProperty() {
            return UnsupportedPropertyStubs.hasProperty();
        }

        @Override
        public boolean hasProperty(String key) {
            return UnsupportedPropertyStubs.hasProperty();
        }

        @Override
        public String getProperty(String key) {
            return UnsupportedPropertyStubs.getProperty();
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            return UnsupportedPropertyStubs.getProperty();
        }

        @Override
        public String setProperty(String key, String value) {
            return UnsupportedPropertyStubs.setProperty();
        }

        @Override
        public boolean removeProperty(String key) {
            return UnsupportedPropertyStubs.removeProperty();
        }

        @Override
        public Set<String> getPropertyNames() {
            return UnsupportedPropertyStubs.getPropertyNames();
        }
    }

    protected AbstractReducedLoadingLimits(double permanentLimit, double originalPermanentLimit,
                                           double permanentLimitReduction) {
        this.permanentLimit = permanentLimit;
        this.originalPermanentLimit = originalPermanentLimit;
        this.permanentLimitReduction = permanentLimitReduction;
    }

    public void addTemporaryLimit(String name, double value, int acceptableDuration, boolean fictitious,
                                  double originalValue, double limitReduction) {
        temporaryLimits.put(acceptableDuration, new ReducedTemporaryLimit(name, value, acceptableDuration, fictitious,
                originalValue, limitReduction));
    }

    @Override
    public double getPermanentLimit() {
        return permanentLimit;
    }

    public double getOriginalPermanentLimit() {
        return originalPermanentLimit;
    }

    public double getPermanentLimitReduction() {
        return permanentLimitReduction;
    }

    @Override
    public Collection<TemporaryLimit> getTemporaryLimits() {
        return temporaryLimits.values();
    }

    @Override
    public TemporaryLimit getTemporaryLimit(int acceptableDuration) {
        return temporaryLimits.get(acceptableDuration);
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        return Optional.ofNullable(getTemporaryLimit(acceptableDuration)).map(TemporaryLimit::getValue).orElse(Double.NaN);
    }

    @Override
    public LoadingLimits setPermanentLimit(double permanentLimit) {
        throw new UnsupportedOperationException("Unsupported operation for reduced loading limits.");
    }

    @Override
    public LoadingLimits setTemporaryLimitValue(int acceptableDuration, double temporaryLimitValue) {
        throw new UnsupportedOperationException("Unsupported operation for reduced loading limits.");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Reduced loading limits are not linked to a network element and thus cannot be removed.");
    }

    @Override
    public boolean hasProperty() {
        return UnsupportedPropertyStubs.hasProperty();
    }

    @Override
    public boolean hasProperty(String key) {
        return UnsupportedPropertyStubs.hasProperty();
    }

    @Override
    public String getProperty(String key) {
        return UnsupportedPropertyStubs.getProperty();
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return UnsupportedPropertyStubs.getProperty();
    }

    @Override
    public String setProperty(String key, String value) {
        return UnsupportedPropertyStubs.setProperty();
    }

    @Override
    public boolean removeProperty(String key) {
        return UnsupportedPropertyStubs.removeProperty();
    }

    @Override
    public Set<String> getPropertyNames() {
        return UnsupportedPropertyStubs.getPropertyNames();
    }
}
