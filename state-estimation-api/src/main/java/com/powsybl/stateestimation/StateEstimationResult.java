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
import java.util.Map;

/**
 * The outcome of a state estimation.
 *
 * <p>Unlike a load flow, an estimation returns the state it computed instead of only writing it
 * into the network, because comparing an estimate against the measured snapshot requires both to
 * exist at the same time. Writing back is opt-in through
 * {@link StateEstimationParameters#isWriteResultsToNetwork()}.</p>
 *
 * <p>Extendable, so that an implementation can attach results this interface does not model,
 * such as the statistics of a bad data test, without the interface having to change.</p>
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public interface StateEstimationResult extends Extendable<StateEstimationResult> {

    enum Status {
        /** Every component in scope produced an estimate. */
        FULLY_ESTIMATED,
        /** At least one component produced an estimate and at least one did not. */
        PARTIALLY_ESTIMATED,
        /** No component produced an estimate. */
        FAILED
    }

    Status getStatus();

    default boolean isFullyEstimated() {
        return getStatus() == Status.FULLY_ESTIMATED;
    }

    default boolean isPartiallyEstimated() {
        return getStatus() == Status.PARTIALLY_ESTIMATED;
    }

    default boolean isFailed() {
        return getStatus() == Status.FAILED;
    }

    /** Implementation-defined diagnostics, keyed by name. */
    Map<String, String> getMetrics();

    String getLogs();

    List<ComponentResult> getComponentResults();

    /** The estimated state, one entry per bus that was estimated. Empty when the estimation failed. */
    List<BusResult> getBusResults();

    /**
     * One entry per measurement the estimation consumed, in no guaranteed order. Empty when the
     * implementation does not report per-measurement diagnostics.
     */
    List<MeasurementResult> getMeasurementResults();

    interface ComponentResult {

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

        int getConnectedComponentNum();

        int getSynchronousComponentNum();

        Status getStatus();

        String getStatusText();

        int getIterationCount();

        /**
         * The bus whose voltage angle was held fixed. Only angle differences are physical, so one
         * angle per synchronous component is not an unknown.
         */
        String getReferenceBusId();

        /**
         * The weighted sum of squared residuals at the solution, the quantity a least-squares
         * estimator minimizes. {@code Double.NaN} when the implementation does not minimize one.
         */
        double getObjectiveFunctionValue();

        Map<String, String> getMetrics();
    }

    /**
     * The estimated state at one bus, in the units {@link com.powsybl.iidm.network.Bus} uses:
     * kV for the magnitude, degrees for the angle.
     */
    interface BusResult {

        String getBusId();

        double getV();

        double getAngle();
    }

    /**
     * What the estimation made of one measurement. Values are in the unit the measurement was
     * given in.
     */
    interface MeasurementResult {

        String getMeasurementId();

        double getMeasuredValue();

        /** What a meter in this position would read in the estimated state. */
        double getEstimatedValue();

        /** Measured minus estimated. */
        double getResidual();

        /** The residual divided by its own standard deviation, so comparable across meters. */
        double getNormalizedResidual();

        /**
         * Whether the normalized residual exceeded
         * {@link StateEstimationParameters#getResidualFlaggingThreshold()}. Flagging reports a
         * suspicion; it never removes or reweights the measurement.
         */
        boolean isFlagged();
    }

}
