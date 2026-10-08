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

    Status getStatus();

    /** The measurements that disagree with the estimate, worst first. */
    List<SuspectMeasurement> getSuspectMeasurements();

    /**
     * The measurements that could not be tested, because no other measurement determines the same
     * quantity. These are the ones whose {@link ObservabilityResult.Quality} reports
     * {@code isRedundant()} as false.
     */
    Set<String> getUncheckableMeasurementIds();

    /** Metrics are generic key/value pairs and are specific to a state estimation implementation. */
    Map<String, String> getMetrics();

    interface SuspectMeasurement {

        String getMeasurementId();

        /** The residual divided by its own standard deviation. */
        double getNormalizedResidual();
    }
}
