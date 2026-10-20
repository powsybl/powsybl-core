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
    private final List<BranchResult> branchResults;
    private final List<MeasurementResult> measurementResults;

    /**
     * Records the outcome of an estimation.
     *
     * @param status the outcome being recorded
     * @param componentResults one entry per connected component
     * @param busResults the estimated state, one entry per bus
     * @param branchResults the flows that follow from the estimated state, one entry per branch
     * @param measurementResults one entry per measurement the estimation consumed
     */
    public StateEstimationResultImpl(Status status,
                                     List<ComponentResult> componentResults, List<BusResult> busResults,
                                     List<BranchResult> branchResults,
                                     List<MeasurementResult> measurementResults) {
        this.status = Objects.requireNonNull(status);
        this.componentResults = List.copyOf(Objects.requireNonNull(componentResults));
        this.busResults = List.copyOf(Objects.requireNonNull(busResults));
        this.branchResults = List.copyOf(Objects.requireNonNull(branchResults));
        this.measurementResults = List.copyOf(Objects.requireNonNull(measurementResults));
    }

    /**
     * An estimation that produced nothing.
     *
     * @return a failed estimation, holding no component, bus, branch or measurement results
     */
    public static StateEstimationResultImpl failed() {
        return new StateEstimationResultImpl(Status.FAILED, List.of(), List.of(), List.of(), List.of());
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
    public List<BranchResult> getBranchResults() {
        return branchResults;
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
        private final String voltageLevelId;
        private final double v;
        private final double angle;

        /**
         * {@code v} is in kV and {@code angle} in degrees.
         *
         * @param busId the identifier of the bus
         * @param voltageLevelId the identifier of the voltage level holding the bus
         * @param v the voltage magnitude, in kV
         * @param angle the voltage angle, in degrees
         */
        public BusResultImpl(String busId, String voltageLevelId, double v, double angle) {
            this.busId = Objects.requireNonNull(busId);
            this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
            this.v = v;
            this.angle = angle;
        }

        @Override
        public String getBusId() {
            return busId;
        }

        @Override
        public String getVoltageLevelId() {
            return voltageLevelId;
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

    /** Immutable {@link BranchResult}. */
    public static class BranchResultImpl implements BranchResult {

        private final String branchId;
        private final double p1;
        private final double q1;
        private final double i1;
        private final double p2;
        private final double q2;
        private final double i2;

        /**
         * Powers are in MW and MVar, currents in A, each positive when flowing from the bus into
         * the branch at that side. A current of {@code NaN} means the implementation does not
         * produce one.
         *
         * @param branchId the identifier of the branch
         * @param p1 the active power at side 1, in MW
         * @param q1 the reactive power at side 1, in MVar
         * @param i1 the current magnitude at side 1, in A
         * @param p2 the active power at side 2, in MW
         * @param q2 the reactive power at side 2, in MVar
         * @param i2 the current magnitude at side 2, in A
         */
        public BranchResultImpl(String branchId, double p1, double q1, double i1,
                                double p2, double q2, double i2) {
            this.branchId = Objects.requireNonNull(branchId);
            this.p1 = p1;
            this.q1 = q1;
            this.i1 = i1;
            this.p2 = p2;
            this.q2 = q2;
            this.i2 = i2;
        }

        /** The flows at the two ends, with no current magnitudes. */
        public BranchResultImpl(String branchId, double p1, double q1, double p2, double q2) {
            this(branchId, p1, q1, Double.NaN, p2, q2, Double.NaN);
        }

        @Override
        public String getBranchId() {
            return branchId;
        }

        @Override
        public double getP1() {
            return p1;
        }

        @Override
        public double getQ1() {
            return q1;
        }

        @Override
        public double getI1() {
            return i1;
        }

        @Override
        public double getP2() {
            return p2;
        }

        @Override
        public double getQ2() {
            return q2;
        }

        @Override
        public double getI2() {
            return i2;
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
