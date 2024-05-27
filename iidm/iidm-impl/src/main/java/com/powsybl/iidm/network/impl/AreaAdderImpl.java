/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.*;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaAdderImpl extends AbstractIdentifiableAdder<AreaAdderImpl> implements AreaAdder {

    private final Ref<NetworkImpl> networkRef;

    private AreaType areaType;

    private Double acNetInterchangeTarget;

    private Double acNetInterchangeTolerance;

    private final Set<VoltageLevel> voltageLevels;

    private final List<Area.BoundaryTerminal> boundaryTerminals;

    private final Properties properties = new Properties();

    AreaAdderImpl(Ref<NetworkImpl> networkRef) {
        this.networkRef = networkRef;
        this.boundaryTerminals = new ArrayList<>();
        this.voltageLevels = new HashSet<>();
    }

    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    public AreaAdder setAreaType(AreaType areaType) {
        this.areaType = areaType;
        return this;
    }

    public AreaAdder setAcNetInterchangeTarget(Double acNetInterchangeTarget) {
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        return this;
    }

    public AreaAdder setAcNetInterchangeTolerance(Double acNetInterchangeTolerance) {
        this.acNetInterchangeTolerance = acNetInterchangeTolerance;
        return this;
    }

    public AreaAdder addVoltageLevel(VoltageLevel voltageLevel) {
        this.voltageLevels.add(voltageLevel);
        return this;
    }

    public AreaAdder addBoundaryTerminal(Terminal terminal, boolean ac) {
        this.boundaryTerminals.add(new Area.BoundaryTerminal(terminal, ac));
        return this;
    }

    public AreaAdder addProperty(String key, String value) {
        properties.setProperty(key, value);
        return this;
    }

    public AreaAdder copy(Area otherArea) {
        this.setId(otherArea.getId())
                .setName(otherArea.getNameOrId())
                .setFictitious(otherArea.isFictitious())
                .setAreaType(otherArea.getAreaType())
                .setAcNetInterchangeTarget(otherArea.getAcNetInterchangeTarget().orElse(null))
                .setAcNetInterchangeTolerance(otherArea.getAcNetInterchangeTolerance().orElse(null));
        otherArea.getPropertyNames().forEach(key -> addProperty(key, otherArea.getProperty(key)));
        return this;
    }

    protected Ref<NetworkImpl> getNetworkRef() {
        return networkRef;
    }

    protected AreaType getAreaType() {
        return areaType;
    }

    protected Double getAcNetInterchangeTarget() {
        return acNetInterchangeTarget;
    }

    protected Double getAcNetInterchangeTolerance() {
        return acNetInterchangeTolerance;
    }

    protected Set<VoltageLevel> getVoltageLevels() {
        return voltageLevels;
    }

    protected List<Area.BoundaryTerminal> getBoundaryTerminals() {
        return boundaryTerminals;
    }

    @Override
    public Area add() {
        String id = checkAndGetUniqueId();
        AreaImpl area = new AreaImpl(getNetworkRef(), id, getName(), isFictitious(), getAreaType(), getAcNetInterchangeTarget(),
                getAcNetInterchangeTolerance());
        area.getBoundaryTerminals().addAll(getBoundaryTerminals());
        getVoltageLevels().forEach(area::addVoltageLevel);
        area.getProperties().putAll(properties);
        getNetwork().getIndex().checkAndAdd(area);
        getNetwork().getListeners().notifyCreation(area);
        return area;
    }

    @Override
    protected String getTypeDescription() {
        return "Area";
    }
}

