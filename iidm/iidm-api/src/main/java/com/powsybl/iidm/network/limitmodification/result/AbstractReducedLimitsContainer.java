/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.limitmodification.result;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractReducedLimitsContainer<H extends L, L> implements LimitsContainer<L> {
    private final H limits;
    private final L originalLimits;

    protected AbstractReducedLimitsContainer(H limits, L originalLimits) {
        this.limits = limits;
        this.originalLimits = originalLimits;
    }

    @Override
    public H getLimits() {
        return limits;
    }

    @Override
    public L getOriginalLimits() {
        return originalLimits;
    }

    @Override
    public boolean isUnchanged() {
        return false;
    }

    public abstract double getOriginalPermanentLimit();

    public abstract Double getOriginalTemporaryLimit(int acceptableDuration);

    public abstract double getPermanentLimitReduction();

    public abstract Double getTemporaryLimitReduction(int acceptableDuration);
}
