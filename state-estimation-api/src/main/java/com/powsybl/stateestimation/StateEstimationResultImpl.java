/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable {@link StateEstimationResult}. Every collection passed in is copied.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class StateEstimationResultImpl extends AbstractExtendable<StateEstimationResult>
        implements StateEstimationResult {

    private final Status status;
    private final Map<String, String> metrics;
    private final String logs;
    private final List<ComponentResult> componentResults;
    private final List<BusResult> busResults;
    private final List<MeasurementResult> measurementResults;

    /**
     * {@code logs} may be null; nothing else may.
     *
     * @param status the outcome being recorded
     * @param metrics implementation-defined diagnostics, keyed by name
     * @param logs text to return alongside the result, or null
     * @param componentResults one entry per connected component
     * @param busResults the estimated state, one entry per bus
     * @param measurementResults one entry per measurement the estimation consumed
     */
    public StateEstimationResultImpl(Status status, Map<String, String> metrics, String logs,
                                     List<ComponentResult> componentResults, List<BusResult> busResults,
                                     List<MeasurementResult> measurementResults) {
        this.status = Objects.requireNonNull(status);
        this.metrics = Map.copyOf(Objects.requireNonNull(metrics));
        this.logs = logs;
        this.componentResults = List.copyOf(Objects.requireNonNull(componentResults));
        this.busResults = List.copyOf(Objects.requireNonNull(busResults));
        this.measurementResults = List.copyOf(Objects.requireNonNull(measurementResults));
    }

    /**
     * A failed estimation, carrying only the logs.
     *
     * @param logs text to return alongside the result, or null
     * @return a failed estimation, carrying only the logs
     */
    public static StateEstimationResultImpl failed(String logs) {
        return new StateEstimationResultImpl(Status.FAILED, Map.of(), logs, List.of(), List.of(), List.of());
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Map<String, String> getMetrics() {
        return metrics;
    }

    @Override
    public String getLogs() {
        return logs;
    }

    @Override
    public List<ComponentResult> getComponentResults() {
        return componentResults;
    }

    @Override
    public List<BusResult> getBusResults() {
        return busResults;
    }

    @Override
    public List<MeasurementResult> getMeasurementResults() {
        return measurementResults;
    }

    /** Immutable {@link ComponentResult}. */
    public static class ComponentResultImpl implements ComponentResult {

        private final int connectedComponentNum;
        private final int synchronousComponentNum;
        private final Status status;
        private final String statusText;
        private final int iterationCount;
        private final String referenceBusId;
        private final double objectiveFunctionValue;
        private final Map<String, String> metrics;

        /**
         * A null {@code metrics} is read as an empty map.
         *
         * @param connectedComponentNum the connected component number
         * @param synchronousComponentNum the synchronous component number
         * @param status the outcome being recorded
         * @param statusText implementation-defined text explaining the status
         * @param iterationCount how many iterations the estimation took on this component
         * @param referenceBusId the identifier of the bus whose voltage angle was held fixed
         * @param objectiveFunctionValue the weighted sum of squared residuals at the solution
         * @param metrics implementation-defined diagnostics, keyed by name
         */
        public ComponentResultImpl(int connectedComponentNum, int synchronousComponentNum, Status status, String statusText,
                                   int iterationCount, String referenceBusId, double objectiveFunctionValue,
                                   Map<String, String> metrics) {
            this.connectedComponentNum = connectedComponentNum;
            this.synchronousComponentNum = synchronousComponentNum;
            this.status = Objects.requireNonNull(status);
            this.statusText = statusText;
            this.iterationCount = iterationCount;
            this.referenceBusId = referenceBusId;
            this.objectiveFunctionValue = objectiveFunctionValue;
            this.metrics = Map.copyOf(Objects.requireNonNullElse(metrics, Collections.emptyMap()));
        }

        @Override
        public int getConnectedComponentNum() {
            return connectedComponentNum;
        }

        @Override
        public int getSynchronousComponentNum() {
            return synchronousComponentNum;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public String getStatusText() {
            return statusText;
        }

        @Override
        public int getIterationCount() {
            return iterationCount;
        }

        @Override
        public String getReferenceBusId() {
            return referenceBusId;
        }

        @Override
        public double getObjectiveFunctionValue() {
            return objectiveFunctionValue;
        }

        @Override
        public Map<String, String> getMetrics() {
            return metrics;
        }
    }

    /** Immutable {@link BusResult}. */
    public static class BusResultImpl implements BusResult {

        private final String busId;
        private final double v;
        private final double angle;

        /**
         * {@code v} is in kV and {@code angle} in degrees.
         *
         * @param busId the identifier of the bus
         * @param v the voltage magnitude, in kV
         * @param angle the voltage angle, in degrees
         */
        public BusResultImpl(String busId, double v, double angle) {
            this.busId = Objects.requireNonNull(busId);
            this.v = v;
            this.angle = angle;
        }

        @Override
        public String getBusId() {
            return busId;
        }

        @Override
        public double getV() {
            return v;
        }

        @Override
        public double getAngle() {
            return angle;
        }
    }

    /**
     * Immutable {@link MeasurementResult}. The residual is computed from the measured and estimated
     * values.
     */
    public static class MeasurementResultImpl implements MeasurementResult {

        private final String measurementId;
        private final double measuredValue;
        private final double estimatedValue;
        private final double normalizedResidual;
        private final boolean flagged;

        /**
         * Values are in the unit the measurement was given in.
         *
         * @param measurementId the identifier of the IIDM measurement
         * @param measuredValue the value the meter reported
         * @param estimatedValue what a meter in this position would read in the estimated state
         * @param normalizedResidual the residual divided by its own standard deviation
         * @param flagged whether the normalized residual exceeded the flagging threshold
         */
        public MeasurementResultImpl(String measurementId, double measuredValue, double estimatedValue,
                                     double normalizedResidual, boolean flagged) {
            this.measurementId = Objects.requireNonNull(measurementId);
            this.measuredValue = measuredValue;
            this.estimatedValue = estimatedValue;
            this.normalizedResidual = normalizedResidual;
            this.flagged = flagged;
        }

        @Override
        public String getMeasurementId() {
            return measurementId;
        }

        @Override
        public double getMeasuredValue() {
            return measuredValue;
        }

        @Override
        public double getEstimatedValue() {
            return estimatedValue;
        }

        @Override
        public double getResidual() {
            return measuredValue - estimatedValue;
        }

        @Override
        public double getNormalizedResidual() {
            return normalizedResidual;
        }

        @Override
        public boolean isFlagged() {
            return flagged;
        }
    }

}
