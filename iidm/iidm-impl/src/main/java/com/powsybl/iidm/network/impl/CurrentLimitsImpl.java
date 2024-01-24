/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.CurrentLimits;

import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class CurrentLimitsImpl extends AbstractLoadingLimits<CurrentLimitsImpl> implements CurrentLimits {

    CurrentLimitsImpl(OperationalLimitsGroupImpl group, double permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        super(group, permanentLimit, temporaryLimits);
    }

    @Override
    public void remove() {
        group.removeCurrentLimits();
    }
}
