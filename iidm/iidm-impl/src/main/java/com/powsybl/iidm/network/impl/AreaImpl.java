/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.Objects;

public class AreaImpl extends AbstractIdentifiable<Area> implements Area {
    private final Ref<NetworkImpl> networkRef;
    private final AreaType areaType;

    public AreaImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious, AreaType areaType) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.areaType = Objects.requireNonNull(areaType);
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "Area";
    }

    @Override
    public AreaType getAreaType() {
        return areaType;
    }
}
