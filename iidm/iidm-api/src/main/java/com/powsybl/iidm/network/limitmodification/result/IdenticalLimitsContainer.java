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
public class IdenticalLimitsContainer<L> implements LimitsContainer<L> {
    private final L originalLimits;
    private final String operationalLimitsGroupId;

    public IdenticalLimitsContainer(L originalLimits, String operationalLimitsGroupId) {
        this.originalLimits = originalLimits;
        this.operationalLimitsGroupId = operationalLimitsGroupId;
    }

    @Override
    public String getOperationalLimitsGroupId() {
        return operationalLimitsGroupId;
    }

    @Override
    public L getLimits() {
        return getOriginalLimits();
    }

    @Override
    public L getOriginalLimits() {
        return originalLimits;
    }

    @Override
    public boolean isDistinct() {
        return false;
    }
}
