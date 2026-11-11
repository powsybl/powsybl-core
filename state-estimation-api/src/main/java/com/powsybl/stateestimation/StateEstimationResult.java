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

    /** The outcome over every component in scope, which follows from the component results. */
    Status getStatus();

    /**
     * The variant the estimation ran against. Writing a result into a different variant is allowed,
     * since a clone of the one it ran on describes the same network, and is the caller's call.
     */
    String getVariantId();

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
     * The flows through the three-winding transformers the estimation covered, one entry per
     * transformer. Empty when the estimation failed.
     */
    List<ThreeWindingsTransformerResult> getThreeWindingsTransformerResults();

    /**
     * The injections that follow from the estimated state, one entry per generator, load, shunt
     * compensator or static var compensator at an estimated bus. Empty when the estimation failed.
     */
    List<InjectionResult> getInjectionResults();

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
         * In the bus view, as {@link BusResult} is.
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
     *
     * <p>Buses are those of the bus view, since the same identifier can name a different bus in
     * each view.</p>
     */
    interface BusResult {

        /** The identifier of the bus, in the bus view. */
        String getBusId();

        /** The IIDM identifier of the voltage level holding the bus. */
        String getVoltageLevelId();

        /** The estimated voltage magnitude, in kV. */
        double getV();

        /** The estimated voltage angle, in degrees. */
        double getAngle();
    }

    /**
     * The flows at the two ends of one branch, in the units IIDM uses for a terminal. Field order
     * and naming follow {@code com.powsybl.security.results.BranchResult}.
     *
     * <p>A paired boundary line is reported through its tie line, which is itself a branch, and
     * never through the two boundary lines behind it. An unpaired boundary line is reported here
     * too, with side 1 the network side and side 2 the boundary side, since both ends carry a
     * flow even though IIDM models it as an injection.</p>
     */
    interface BranchResult {

        /** The IIDM identifier of the branch, or of an unpaired boundary line. */
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
     * The flows at the three legs of one three-winding transformer, in the units IIDM uses for a
     * terminal. Field order and naming follow
     * {@code com.powsybl.security.results.ThreeWindingsTransformerResult}.
     */
    interface ThreeWindingsTransformerResult {

        /** The IIDM identifier of the transformer. */
        String getThreeWindingsTransformerId();

        /** Active power at leg 1, in MW, positive when flowing from the bus into the leg. */
        double getP1();

        /** Reactive power at leg 1, in MVar, positive when flowing from the bus into the leg. */
        double getQ1();

        /** Current magnitude at leg 1, in A. {@code NaN} when the implementation does not produce it. */
        double getI1();

        /** Active power at leg 2, in MW, positive when flowing from the bus into the leg. */
        double getP2();

        /** Reactive power at leg 2, in MVar, positive when flowing from the bus into the leg. */
        double getQ2();

        /** Current magnitude at leg 2, in A. {@code NaN} when the implementation does not produce it. */
        double getI2();

        /** Active power at leg 3, in MW, positive when flowing from the bus into the leg. */
        double getP3();

        /** Reactive power at leg 3, in MVar, positive when flowing from the bus into the leg. */
        double getQ3();

        /** Current magnitude at leg 3, in A. {@code NaN} when the implementation does not produce it. */
        double getI3();
    }

    /**
     * The flow at the one terminal of an injection, in the units IIDM uses for a terminal. An
     * unpaired boundary line is reported as a {@link BranchResult} instead, since its boundary end
     * carries a flow of its own.
     */
    interface InjectionResult {

        /** The IIDM identifier of the generator, load, shunt compensator or static var compensator. */
        String getInjectionId();

        /** Active power, in MW, positive when flowing from the bus into the device. */
        double getP();

        /** Reactive power, in MVar, positive when flowing from the bus into the device. */
        double getQ();
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
