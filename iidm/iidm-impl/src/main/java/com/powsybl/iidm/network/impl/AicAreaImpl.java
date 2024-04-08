/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AicArea;
import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.impl.util.Ref;

public class AicAreaImpl extends AreaImpl implements AicArea {
    private final double acNetInterchangeTarget;
    private final double acNetInterchangeTolerance;

    public AicAreaImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious, AreaType areaType, double acNetInterchangeTarget,
                    double acNetInterchangeTolerance) {
        super(ref, id, name, fictitious, areaType);
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        this.acNetInterchangeTolerance = acNetInterchangeTolerance;
    }

    @Override
    public double getAcNetInterchangeTarget() {
        return acNetInterchangeTarget;
    }

    @Override
    public double getAcNetInterchangeTolerance() {
        return acNetInterchangeTolerance;
    }
}

