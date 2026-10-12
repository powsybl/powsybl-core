/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable {@link BadDataResult}. Every collection passed in is copied.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class BadDataResultImpl extends AbstractExtendable<BadDataResult> implements BadDataResult {

    private final Status status;
    private final List<SuspectMeasurement> suspectMeasurements;
    private final Set<String> uncheckableMeasurementIds;
    private final Map<String, String> metrics;

    /**
     * No argument may be null.
     *
     * @param status the outcome being recorded
     * @param suspectMeasurements the measurements that disagree with the estimate, worst first
     * @param uncheckableMeasurementIds the identifiers of the measurements that could not be tested
     * @param metrics implementation-defined diagnostics, keyed by name
     */
    public BadDataResultImpl(Status status, List<SuspectMeasurement> suspectMeasurements,
                             Set<String> uncheckableMeasurementIds, Map<String, String> metrics) {
        this.status = Objects.requireNonNull(status);
        this.suspectMeasurements = List.copyOf(Objects.requireNonNull(suspectMeasurements));
        this.uncheckableMeasurementIds = Set.copyOf(Objects.requireNonNull(uncheckableMeasurementIds));
        this.metrics = Map.copyOf(Objects.requireNonNull(metrics));
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public List<SuspectMeasurement> getSuspectMeasurements() {
        return suspectMeasurements;
    }

    @Override
    public Set<String> getUncheckableMeasurementIds() {
        return uncheckableMeasurementIds;
    }

    @Override
    public Map<String, String> getMetrics() {
        return metrics;
    }

    /** Immutable {@link SuspectMeasurement}. */
    public static class SuspectMeasurementImpl implements SuspectMeasurement {

        private final String measurementId;
        private final double normalizedResidual;

        /**
         * {@code normalizedResidual} is the residual divided by its own standard deviation.
         *
         * @param measurementId the identifier of the IIDM measurement
         * @param normalizedResidual the residual divided by its own standard deviation
         */
        public SuspectMeasurementImpl(String measurementId, double normalizedResidual) {
            this.measurementId = Objects.requireNonNull(measurementId);
            this.normalizedResidual = normalizedResidual;
        }

        @Override
        public String getMeasurementId() {
            return measurementId;
        }

        @Override
        public double getNormalizedResidual() {
            return normalizedResidual;
        }
    }
}
