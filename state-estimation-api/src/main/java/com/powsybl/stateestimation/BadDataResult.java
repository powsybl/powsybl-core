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
import java.util.Set;

/**
 * Interface to describe the result of testing an estimate for measurements that disagree with it.
 *
 * <p>Only errors in the measurements are reported, not topology or network parameter errors.
 * Nothing is removed or reweighted: what to do about a suspect measurement is left to the caller.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public interface BadDataResult extends Extendable<BadDataResult> {

    /** The status of the test. */
    enum Status {
        /** The test ran and found nothing beyond the threshold. */
        NO_BAD_DATA,
        /** The test ran and found at least one suspect measurement. */
        BAD_DATA_DETECTED,
        /** The test could not be carried out, for example because the estimate did not converge. */
        INCONCLUSIVE
    }

    /** Whether the test ran, and what it found. */
    Status getStatus();

    /**
     * Implementation-defined text explaining the status, typically why the test was inconclusive.
     * Empty when the implementation has nothing to add.
     */
    String getStatusText();

    /** The measurements that disagree with the estimate, worst first. */
    List<SuspectMeasurement> getSuspectMeasurements();

    /**
     * The measurements that could not be tested, because no other measurement determines the same
     * quantity. These are the ones whose {@link ObservabilityResult.Quality} reports
     * {@code isRedundant()} as false.
     */
    Set<String> getUncheckableMeasurementIds();

    /** One measurement whose value disagrees with the estimate. */
    interface SuspectMeasurement {

        /** The identifier of the IIDM measurement this reading came from. */
        String getMeasurementId();

        /**
         * The residual divided by its own standard deviation. Signed: a positive value means the
         * meter read above the estimate. The test compares its absolute value against the
         * threshold, so a suspect measurement can fall either side of it.
         */
        double getNormalizedResidual();
    }
}
