/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.extensions.Extendable;

import java.util.List;

/**
 * The outcome of a state estimation.
 *
 * <p>Unlike a load flow, an estimation returns the state it computed instead of only writing it
 * into the network. With {@link StateEstimationParameters.UnobservablePolicy#ESTIMATE_OBSERVABLE_ISLANDS},
 * one component can come back partly estimated, and which of its buses carry a value is exactly
 * what the set of {@link BusResult} entries says. Writing back is opt-in through
 * {@link StateEstimationParameters#isWriteResultsToNetwork()}.</p>
 *
 * <p>Extendable, so that an implementation can attach results this interface does not model,
 * such as the statistics of a bad data test, without the interface having to change.</p>
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public interface StateEstimationResult extends Extendable<StateEstimationResult> {

    /** How much of the network in scope produced an estimate. */
    enum Status {
        /** Every component in scope produced an estimate. */
        FULLY_ESTIMATED,
        /** At least one component produced an estimate and at least one did not. */
        PARTIALLY_ESTIMATED,
        /** No component produced an estimate. */
        FAILED
    }

    /** The outcome over every component in scope. */
    Status getStatus();

    /** One entry per synchronous component the estimation covered. */
    List<ComponentResult> getComponentResults();

    /** The estimated state, one entry per bus that was estimated. Empty when the estimation failed. */
    List<BusResult> getBusResults();

    /**
     * The flows that follow from the estimated state, one entry per branch both of whose ends were
     * estimated. Empty when the estimation failed.
     *
     * <p>A flow is derived, not estimated: it follows from the voltages at the two ends and the
     * branch admittance. It is carried here so that a caller holding a result can write it back or
     * serialize it without building the network model a second time.</p>
     */
    List<BranchResult> getBranchResults();

    /**
     * One entry per measurement the estimation consumed, in no guaranteed order. Empty when the
     * implementation does not report per-measurement diagnostics.
     */
    List<MeasurementResult> getMeasurementResults();

    /** The outcome on one synchronous component. Components are estimated independently of each other. */
    interface ComponentResult {

        /** Whether this component was estimated, and what stopped it when it was not. */
        enum Status {
            /** The component was observable and the estimation converged on it. */
            ESTIMATED,
            /** The measurement set does not determine the state of this component. */
            UNOBSERVABLE,
            /** Observable, but the iteration reached its cap without converging. */
            DID_NOT_CONVERGE,
            /** The estimation could not be carried out, for instance because the problem was numerically unsolvable. */
            FAILED
        }

        /** Whether this component was estimated. */
        Status getStatus();

        /**
         * Implementation-defined text explaining the status, typically the reason an estimation did
         * not succeed. Empty when the implementation has nothing to add.
         */
        String getStatusText();

        /** How many iterations the estimation took on this component. */
        int getIterationCount();

        /**
         * The bus whose voltage angle was held fixed, which also identifies the component. Only
         * angle differences are physical, so one angle per synchronous component is not an unknown.
         */
        String getReferenceBusId();

        /**
         * The weighted sum of squared residuals at the solution, the quantity a least-squares
         * estimator minimizes. {@code Double.NaN} when the implementation does not minimize one.
         */
        double getObjectiveFunctionValue();

    }

    /**
     * The estimated state at one bus, in the units {@link com.powsybl.iidm.network.Bus} uses:
     * kV for the magnitude, degrees for the angle.
     */
    interface BusResult {

        /** The IIDM identifier of the bus. */
        String getBusId();

        /**
         * The IIDM identifier of the voltage level holding the bus. A bus identifier alone does not
         * say which topology view it belongs to, so both are needed to find the bus again.
         */
        String getVoltageLevelId();

        /** The estimated voltage magnitude, in kV. */
        double getV();

        /** The estimated voltage angle, in degrees. */
        double getAngle();
    }

    /**
     * The flows at the two ends of one branch, in the units IIDM uses for a terminal. Field order
     * and naming follow {@code com.powsybl.security.results.BranchResult}.
     */
    interface BranchResult {

        /** The IIDM identifier of the branch. */
        String getBranchId();

        /** Active power at side 1, in MW, positive when flowing from the bus into the branch. */
        double getP1();

        /** Reactive power at side 1, in MVar, positive when flowing from the bus into the branch. */
        double getQ1();

        /** Current magnitude at side 1, in A. {@code NaN} when the implementation does not produce it. */
        double getI1();

        /** Active power at side 2, in MW, positive when flowing from the bus into the branch. */
        double getP2();

        /** Reactive power at side 2, in MVar, positive when flowing from the bus into the branch. */
        double getQ2();

        /** Current magnitude at side 2, in A. {@code NaN} when the implementation does not produce it. */
        double getI2();
    }

    /**
     * What the estimation made of one measurement. Values are in the unit the measurement was
     * given in.
     */
    interface MeasurementResult {

        /** The identifier of the IIDM measurement this reading came from. */
        String getMeasurementId();

        /** The value the meter reported. */
        double getMeasuredValue();

        /** What a meter in this position would read in the estimated state. */
        double getEstimatedValue();

        /** Measured minus estimated. */
        double getResidual();

        /**
         * The residual divided by its own standard deviation, so comparable across meters. Signed:
         * a positive value means the meter read above the estimate. Flagging compares its absolute
         * value against the threshold.
         */
        double getNormalizedResidual();

        /**
         * Whether the normalized residual exceeded
         * {@link StateEstimationParameters#getResidualFlaggingThreshold()}. Flagging reports a
         * suspicion; it never removes or reweights the measurement.
         */
        boolean isFlagged();
    }

}
