/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;

/**
 * Current limits are defined by:
 * <ul>
 *     <li>A permanent limit (A)</li>
 *     <li>
 * Any number of temporary limits.
 * A permanent limit (A) has an acceptable duration (s).
 * The branch can safely stay between the previous limit (could be another temporary limit or the permanent limit) and
 * this limit during the acceptable duration.
 * A NaN temporay limit value means infinite.
 *     </li>
 * </ul>
 *
 *<p>
 * The following diagram shows current areas and corresponding acceptable duration for a permanent limit and 3 temporary
 * limits with x, y and z acceptable durations.
 * <div>
 *    <object data="doc-files/currentLimits.svg" type="image/svg+xml"></object>
 * </div>
 *</p>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface CurrentLimits {

    /**
     * Temporary current limit.
     */
    interface TemporaryLimit {

        /**
         * Get the temporary limit name
         * @return the temporary limit name
         */
        String getName();

        /**
         * Get the temporary limit value in A.
         * @return the temporary limit value in A
         */
        double getValue();

        /**
         * Get the acceptable duration of the limit in second.
         * @return the acceptable duration of the limit in second
         */
        int getAcceptableDuration();

        /**
         * Check if the temporary limit is a real limit corresponding to an overloading  protection or just an operating
         * rule
         * @return false if it is a real limit, false otherwise
         */
        boolean isFictitious();
    }

    /**
     * Get the permanent limit in A.
     * @return the permanent limit in A.
     */
    double getPermanentLimit();

    /**
     * Set the permanent limit in A.
     * @param permanentLimit the permanent limit in A
     * @return itself for method chaining
     */
    CurrentLimits setPermanentLimit(double permanentLimit);

    /**
     * Get a list of temporary limits ordered by descending duration.
     * @return a list of temporary limits ordered by descending duration
     */
    Collection<TemporaryLimit> getTemporaryLimits();

    /**
     * Get a temporary limit from its acceptable duration. Return null if there is non temporay limit with this
     * acceptable duration.
     * @param acceptableDuration acceptable duration in second
     * @return the temporary limit
     */
    TemporaryLimit getTemporaryLimit(int acceptableDuration);

    /**
     * Get a temporary limit value from its acceptable duration. Return NaN if there is non temporay limit with this
     * acceptable duration.
     * @param acceptableDuration acceptable duration in second
     * @return the temporary limit value or NaN if there is no temporary limit for this acceptable duration
     */
    double getTemporaryLimitValue(int acceptableDuration);
}
