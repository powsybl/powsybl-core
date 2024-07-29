/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.util.LoadingLimitsUtil;

import java.util.Collection;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface LoadingLimitsAdder<L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> extends OperationalLimitsAdder<L, A> {

    interface TemporaryLimitAdder<A> {

        TemporaryLimitAdder<A> setName(String name);

        TemporaryLimitAdder<A> setValue(double value);

        TemporaryLimitAdder<A> setAcceptableDuration(int duration);

        TemporaryLimitAdder<A> setFictitious(boolean fictitious);

        TemporaryLimitAdder<A> ensureNameUnicity();

        A endTemporaryLimit();
    }

    A setPermanentLimit(double limit);

    A setTemporaryLimitValue (int acceptableDuration, double limit);

    TemporaryLimitAdder<A> beginTemporaryLimit();

    /**
     * <p>Get the permanent limit to add.</p>
     * <p>This method is useful to fix permanent or temporary limits before calling {@link #add()}.</p>
     * @return the permanent limit
     */
    double getPermanentLimit();

    /**
     * <p>Get the temporary limit value corresponding to the given acceptable duration.</p>
     * <p>This method is useful to fix permanent or temporary limits before calling {@link #add()}.</p>
     * @param acceptableDuration the acceptable duration
     * @return the corresponding temporary limit value, or <code>Double.NaN</code> if none is defined.
     */
    double getTemporaryLimitValue(int acceptableDuration);

    /**
     * <p>Get the temporary limit value corresponding to the given name.</p>
     * <p>This method is useful to fix permanent or temporary limits before calling {@link #add()}.</p>
     * @param name the temporary limit name
     * @return the corresponding temporary limit value, or <code>Double.NaN</code> if none is defined.
     */
    double getTemporaryLimitValue(String name);

    /**
     * <p>Get the temporary limit value corresponding to the given name.</p>
     * <p>This method is useful to fix permanent or temporary limits before calling {@link #add()}.</p>
     * @param name the temporary limit name
     * @return the corresponding temporary limit value, or <code>Integer.MAX_VALUE</code> if none is defined.
     */
    int getTemporaryLimitAcceptableDuration(String name);

    /**
     * <p>Get the lowest value of the temporary limits to create.</p>
     * <p>This method is useful to fix permanent or temporary limits before calling {@link #add()}.</p>
     * @return the lowest temporary limit value, or <code>Double.NaN</code> if no temporary limits are defined.
     */
    double getLowestTemporaryLimitValue();

    /**
     * <p>Indicate if temporary limits to create are defined in the adder.</p>
     * <p>This method is useful to fix permanent or temporary limits before calling {@link #add()}.</p>
     * @return <code>true</code> if temporary limits to create are defined, <code>false</code> otherwise.
     */
    boolean hasTemporaryLimits();

    /**
     * <p>Get the names of the temporary limits to create.</p>
     * <p>This method is useful to fix permanent or temporary limits before calling {@link #add()}.</p>
     * @return a collection containing the names of the defined temporary limits.
     */
    Collection<String> getTemporaryLimitNames();

    /**
     * <p>Remove from the temporary limits to create the one(s) corresponding to the given name.</p>
     * <p>This method doesn't throw any <code>Exception</code> if no corresponding temporary limits are found.</p>
     * <p>This method is useful to fix permanent or temporary limits before calling {@link #add()}.</p>
     * @param name a temporary limit name
     */
    void removeTemporaryLimit(String name);

    /**
     * <p>Get the id of the network element on which the LoadingLimits should be added.</p>
     * @return the id of the owner network element
     */
    String getOwnerId();

    /**
     * <p>Fix the limits to create if needed, especially the permanent limit when it is not defined (equals <code>Double.NaN</code>).</p>
     * @return the adder
     * @see LoadingLimitsAdder#fixLimits(double, LoadingLimitsUtil.LimitFixLogger)
     */
    default A fixLimits() {
        return fixLimits(100., LoadingLimitsUtil.LimitFixLogger.NO_OP);
    }

    /**
     * <p>Fix the limits to create if needed, especially the permanent limit when it is not defined (equals <code>Double.NaN</code>).</p>
     * @param missingPermanentLimitPercentage the percentage to use to compute the permanentLimit if it is not defined.
     * @return the adder
     * @see LoadingLimitsAdder#fixLimits(double, LoadingLimitsUtil.LimitFixLogger)
     */
    default A fixLimits(double missingPermanentLimitPercentage) {
        return fixLimits(missingPermanentLimitPercentage, LoadingLimitsUtil.LimitFixLogger.NO_OP);
    }

    /**
     * <p>Fix the limits to create if needed, especially the permanent limit when it is not defined (equals <code>Double.NaN</code>).</p>
     * @param missingPermanentLimitPercentage the percentage to use to compute the permanentLimit if it is not defined.
     * @param limitFixLogger a logger allowing to report the changes applied when fixing the permanent limit.
     * @return the adder
     * @see LoadingLimitsUtil#fixMissingPermanentLimit(LoadingLimitsAdder, double, String, LoadingLimitsUtil.LimitFixLogger)
     */
    default A fixLimits(double missingPermanentLimitPercentage, LoadingLimitsUtil.LimitFixLogger limitFixLogger) {
        LoadingLimitsUtil.fixMissingPermanentLimit(this, missingPermanentLimitPercentage, getOwnerId(), limitFixLogger);
        return (A) this;
    }
}
