/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AreaImpl extends AbstractIdentifiable<Area> implements Area {
    private final Ref<NetworkImpl> networkRef;
    private final AreaType areaType;

    private final Set<VoltageLevel> voltagelevels = new HashSet<>();

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

    @Override
    public Set<VoltageLevel> getVoltageLevels() {
        return voltagelevels;
    }

    /**
     * Adds a VoltageLevel to this Area. If the VoltageLevel was previously in another Area of the same type,
     * it is removed from that Area. The VoltageLevel's reference to an Area of this type is updated to this Area.
     *
     * @param voltageLevel the VoltageLevel to be added to this Area
     */
    @Override
    public void addVoltageLevel(VoltageLevel voltageLevel) {
        Optional<Area> previousArea = voltageLevel.getArea(this.getAreaType());
        if (voltageLevel.getNetwork() != this.getNetwork()) {
            throw new PowsyblException("VoltageLevel must belong to the same network as the Area");
        }
        previousArea.ifPresent(area -> area.getVoltageLevels().remove(voltageLevel));
        voltageLevel.getAreasByType().put(this.getAreaType(), this);
        voltagelevels.add(voltageLevel);
    }
}
