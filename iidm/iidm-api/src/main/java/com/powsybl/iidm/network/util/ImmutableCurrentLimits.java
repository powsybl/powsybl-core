/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.CurrentLimits;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableCurrentLimits implements CurrentLimits {

    CurrentLimits currentLimits;

    public ImmutableCurrentLimits(CurrentLimits currentLimits) {
        this.currentLimits = Objects.requireNonNull(currentLimits);
    }

    public static ImmutableCurrentLimits ofNullable(CurrentLimits currentLimits) {
        return currentLimits == null ? null : new ImmutableCurrentLimits(currentLimits);
    }

    @Override
    public double getPermanentLimit() {
        return currentLimits.getPermanentLimit();
    }

    @Override
    public CurrentLimits setPermanentLimit(double permanentLimit) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Collection<TemporaryLimit> getTemporaryLimits() {
        return Collections.unmodifiableCollection(currentLimits.getTemporaryLimits());
    }

    @Override
    public TemporaryLimit getTemporaryLimit(int acceptableDuration) {
        return currentLimits.getTemporaryLimit(acceptableDuration);
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        return currentLimits.getTemporaryLimitValue(acceptableDuration);
    }
}
