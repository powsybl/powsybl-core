/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaImpl extends AbstractIdentifiable<Area> implements Area {
    private final Ref<NetworkImpl> networkRef;
    private final AreaType areaType;
    private final Double acNetInterchangeTarget;
    private final Double acNetInterchangeTolerance;
    private final List<Area.BoundaryTerminal> boundaryTerminals;

    private final Set<VoltageLevel> voltagelevels = new LinkedHashSet<>();

    public AreaImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious, AreaType areaType, Double acNetInterchangeTarget,
                       Double acNetInterchangeTolerance) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.areaType = Objects.requireNonNull(areaType);
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        this.acNetInterchangeTolerance = acNetInterchangeTolerance;
        this.boundaryTerminals = new ArrayList<>();
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
    public Iterable<VoltageLevel> getVoltageLevels() {
        return voltagelevels;
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return voltagelevels.stream();
    }

    @Override
    public Optional<Double> getAcNetInterchangeTarget() {
        return Optional.ofNullable(acNetInterchangeTarget);
    }

    @Override
    public Optional<Double> getAcNetInterchangeTolerance() {
        return Optional.ofNullable(acNetInterchangeTolerance);
    }

    /**
     * Adds a VoltageLevel to this Area.
     * @throws PowsyblException if the VoltageLevel is already in another Area of the same type
     * @param voltageLevel the VoltageLevel to be added to this Area
     */
    @Override
    public void addVoltageLevel(VoltageLevel voltageLevel) {
        voltagelevels.add(voltageLevel);
        voltageLevel.addArea(this);
    }

    @Override
    public void remove(VoltageLevel voltageLevel) {
        voltagelevels.remove(voltageLevel);
    }

    @Override
    public List<BoundaryTerminal> getBoundaryTerminals() {
        return boundaryTerminals;
    }

    @Override
    public Stream<BoundaryTerminal> getBoundaryTerminalStream() {
        return boundaryTerminals.stream();
    }
}
