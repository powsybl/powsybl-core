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
import java.util.Objects;

/**
 * Immutable {@link StateEstimationResult}. Every collection passed in is copied.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class StateEstimationResultImpl extends AbstractExtendable<StateEstimationResult>
        implements StateEstimationResult {

    private final Status status;
    private final List<ComponentResult> componentResults;
    private final List<BusResult> busResults;
    private final List<MeasurementResult> measurementResults;

    /**
     * Records the outcome of an estimation.
     *
     * @param status the outcome being recorded
     * @param componentResults one entry per connected component
     * @param busResults the estimated state, one entry per bus
     * @param measurementResults one entry per measurement the estimation consumed
     */
    public StateEstimationResultImpl(Status status,
                                     List<ComponentResult> componentResults, List<BusResult> busResults,
                                     List<MeasurementResult> measurementResults) {
        this.status = Objects.requireNonNull(status);
        this.componentResults = List.copyOf(Objects.requireNonNull(componentResults));
        this.busResults = List.copyOf(Objects.requireNonNull(busResults));
        this.measurementResults = List.copyOf(Objects.requireNonNull(measurementResults));
    }

    /**
     * An estimation that produced nothing.
     *
     * @return a failed estimation, holding no component, bus or measurement results
     */
    public static StateEstimationResultImpl failed() {
        return new StateEstimationResultImpl(Status.FAILED, List.of(), List.of(), List.of());
    }

    @Override
    public Status getStatus() {
        return status;
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

        private final Status status;
        private final String statusText;
        private final int iterationCount;
        private final String referenceBusId;
        private final double objectiveFunctionValue;

        /**
         * @param status the outcome being recorded
         * @param statusText implementation-defined text explaining the status
         * @param iterationCount how many iterations the estimation took on this component
         * @param referenceBusId the identifier of the bus whose voltage angle was held fixed
         * @param objectiveFunctionValue the weighted sum of squared residuals at the solution
             */
        public ComponentResultImpl(Status status, String statusText, int iterationCount,
                                   String referenceBusId, double objectiveFunctionValue) {
            this.status = Objects.requireNonNull(status);
            this.statusText = statusText;
            this.iterationCount = iterationCount;
            this.referenceBusId = referenceBusId;
            this.objectiveFunctionValue = objectiveFunctionValue;
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
