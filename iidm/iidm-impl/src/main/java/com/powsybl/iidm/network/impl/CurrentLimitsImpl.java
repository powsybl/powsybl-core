/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.CurrentLimits;

import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class CurrentLimitsImpl extends AbstractLoadingLimits<CurrentLimitsImpl> implements CurrentLimits {

    CurrentLimitsImpl(OperationalLimitsGroupImpl group, double permanentLimit, String permanentLimitName, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        super(group, permanentLimit, permanentLimitName, temporaryLimits);
    }

    /**
     * Create a {@link CurrentLimits} with a permanent limit and {@link com.powsybl.iidm.network.DetectionKind#HIGH}.
     */
    CurrentLimitsImpl(OperationalLimitsGroupImpl group, double permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        super(group, permanentLimit, temporaryLimits);
    }

    /**
     * Create a {@link CurrentLimits} with no permanent limit and {@link com.powsybl.iidm.network.DetectionKind#LOW}.
     */
    CurrentLimitsImpl(OperationalLimitsGroupImpl group, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        super(group, temporaryLimits);
    }

    @Override
    public void remove() {
        group.removeCurrentLimits();
    }

}
