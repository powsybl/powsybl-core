/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling.result;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DetectionKind;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.util.LoadingLimitsUtil;
import com.powsybl.iidm.network.util.UnsupportedPropertiesHolder;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * <p>Simple abstract implementation of {@link LoadingLimits} not linked to a network element, used to provide
 * scaled limits without altering the real limits of the network element.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractScaledLoadingLimits extends UnsupportedPropertiesHolder implements LoadingLimits {
    private String permanentLimitName = LoadingLimits.DEFAULT_PERMANENT_LIMIT_NAME;
    private final DetectionKind detectionKind;
    private final double permanentLimit;
    private final double originalPermanentLimit;
    private final double permanentLimitScaling;
    private final TreeMap<Integer, TemporaryLimit> temporaryLimits = new TreeMap<>(LoadingLimitsUtil.ACCEPTABLE_DURATION_COMPARATOR);

    public static final class ScaledTemporaryLimit extends UnsupportedPropertiesHolder implements TemporaryLimit {
        // 1. Fields
        private final String name;
        private final double value;
        private final int acceptableDuration;
        private final boolean fictitious;
        private final double originalValue;
        private final double limitScaling;

        // 2. Canonical Constructor (to initialize all fields)
        public ScaledTemporaryLimit(String name, double value, int acceptableDuration, boolean fictitious,
                                    double originalValue, double limitScaling) {
            this.name = name;
            this.value = value;
            this.acceptableDuration = acceptableDuration;
            this.fictitious = fictitious;
            this.originalValue = originalValue;
            this.limitScaling = limitScaling;
        }

        // 3. Accessor Methods (Getters)
        @Override
        public String getName() {
            return name;
        }

        @Override
        public double getValue() {
            return value;
        }

        @Override
        public int getAcceptableDuration() {
            return acceptableDuration;
        }

        @Override
        public boolean isFictitious() {
            return fictitious;
        }

        public double getOriginalValue() {
            return originalValue;
        }

        public double getLimitScaling() {
            return limitScaling;
        }
    }

    protected AbstractReducedLoadingLimits(double permanentLimit, double originalPermanentLimit,
                                           double permanentLimitScaling, String permanentLimitName) {
        this(DetectionKind.HIGH, permanentLimit, originalPermanentLimit, permanentLimitScaling);
        setPermanentLimitName(permanentLimitName);
    }

    protected AbstractScaledLoadingLimits() {
        this(DetectionKind.LOW, Double.NaN, Double.NaN, Double.NaN);
    }

    private AbstractScaledLoadingLimits(DetectionKind detectionKind, double permanentLimit, double originalPermanentLimit, double permanentLimitScaling) {
        this.detectionKind = detectionKind;
        this.permanentLimit = permanentLimit;
        this.originalPermanentLimit = originalPermanentLimit;
        this.permanentLimitScaling = permanentLimitScaling;
    }

    public void addTemporaryLimit(String name, double value, int acceptableDuration, boolean fictitious,
                                  double originalValue, double limitScaling) {
        temporaryLimits.put(acceptableDuration, new ScaledTemporaryLimit(name, value, acceptableDuration, fictitious,
                originalValue, limitScaling));
    }

    @Override
    public DetectionKind getDetectionKind() {
        return detectionKind;
    }

    @Override
    public double getPermanentLimit() {
        return getValueOrThrowDetectionKind(permanentLimit);
    }

    @Override
    public String getPermanentLimitName() {
        return permanentLimitName;
    }

    @Override
    public LoadingLimits setPermanentLimitName(String name) {
        this.permanentLimitName = Objects.requireNonNull(name);
        return this;
    }

    public double getOriginalPermanentLimit() {
        return getValueOrThrowDetectionKind(originalPermanentLimit);
    }

    public double getPermanentLimitScaling() {
        return getValueOrThrowDetectionKind(permanentLimitScaling);
    }

    private double getValueOrThrowDetectionKind(double value) {
        return switch (getDetectionKind()) {
            case HIGH -> value;
            case LOW -> throw new PowsyblException("There is no permanent limit for a detection kind LOW");
        };
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
        throw new UnsupportedOperationException("Unsupported operation for scaled loading limits.");
    }

    @Override
    public LoadingLimits setTemporaryLimitValue(int acceptableDuration, double temporaryLimitValue) {
        throw new UnsupportedOperationException("Unsupported operation for scaled loading limits.");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Scaled loading limits are not linked to a network element and thus cannot be removed.");
    }
}
