/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Collection;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Thibaut Vermeulen {@literal <thibaut.vermeulen at rte-france.com>}
 */
public interface LoadingLimits extends OperationalLimits {

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
         * Get the temporary limit value.
         * @return the temporary limit value
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

        void setValue(double temporaryLimitValue);
    }

    /**
     * Get the permanent limit.
     * @return the permanent limit.
     */
    double getPermanentLimit();

    /**
     * Set the permanent limit
     * @param permanentLimit the permanent limit
     * @return itself for method chaining
     */
    LoadingLimits setPermanentLimit(double permanentLimit);

    /**
     * Get a list of temporary limits ordered by descending duration.
     * @return a list of temporary limits ordered by descending duration
     */
    Collection<TemporaryLimit> getTemporaryLimits();

    /**
     * Get a temporary limit from its acceptable duration. Return null if there is non temporary limit with this
     * acceptable duration.
     * @param acceptableDuration acceptable duration in second
     * @return the temporary limit
     */
    TemporaryLimit getTemporaryLimit(int acceptableDuration);

    /**
     * Get a temporary limit value from its acceptable duration. Return NaN if there is non temporary limit with this
     * acceptable duration.
     * @param acceptableDuration acceptable duration in second
     * @return the temporary limit value or NaN if there is no temporary limit for this acceptable duration
     */
    double getTemporaryLimitValue(int acceptableDuration);

    /**
     * Set the temporary limit value.
     * <p>Throws an exception when no temporary limit of the given acceptable duration is found,
     * and changes the value but logs a warning when the new value is not valid.</p>
     * @param acceptableDuration the acceptable duration
     * @param temporaryLimitValue the temporary limit value
     * @return itself for method chaining
     */
    LoadingLimits setTemporaryLimitValue(int acceptableDuration, double temporaryLimitValue);
}
