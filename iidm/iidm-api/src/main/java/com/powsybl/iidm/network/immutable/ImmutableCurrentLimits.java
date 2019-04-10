/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.CurrentLimits;

import java.util.*;

/**
 * An immutable {@link CurrentLimits}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableCurrentLimits implements CurrentLimits {

    private CurrentLimits currentLimits;

    public ImmutableCurrentLimits(CurrentLimits currentLimits) {
        this.currentLimits = Objects.requireNonNull(currentLimits);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPermanentLimit() {
        return currentLimits.getPermanentLimit();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public CurrentLimits setPermanentLimit(double permanentLimit) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * @return an unmodifiable collection of {@link com.powsybl.iidm.network.CurrentLimits.TemporaryLimit}
     */
    @Override
    public Collection<TemporaryLimit> getTemporaryLimits() {
        return Collections.unmodifiableCollection(currentLimits.getTemporaryLimits());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryLimit getTemporaryLimit(int acceptableDuration) {
        return currentLimits.getTemporaryLimit(acceptableDuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        return currentLimits.getTemporaryLimitValue(acceptableDuration);
    }
}
