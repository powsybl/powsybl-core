/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaImpl extends AbstractIdentifiable<Area> implements Area {
    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;
    private final String areaType;
    private final Double acNetInterchangeTarget;
    private final Double acNetInterchangeTolerance;
    private final List<Area.BoundaryTerminal> boundaryTerminals;

    private final Set<VoltageLevel> voltagelevels = new LinkedHashSet<>();

    public AreaImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious, String areaType, Double acNetInterchangeTarget,
                       Double acNetInterchangeTolerance) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.subnetworkRef = subnetworkRef;
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
    public Network getParentNetwork() {
        return Optional.ofNullable((Network) subnetworkRef.get()).orElse(getNetwork());
    }

    @Override
    protected String getTypeDescription() {
        return "Area";
    }

    @Override
    public String getAreaType() {
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
        if (voltagelevels.contains(voltageLevel)) {
            // Nothing to do
            return;
        }

        // Check that the VoltageLevel belongs to the same network or subnetwork
        if (subnetworkRef.get() != voltageLevel.getParentNetwork() && networkRef.get() != voltageLevel.getParentNetwork()) {
            throw new PowsyblException("VoltageLevel " + voltageLevel.getId() + " cannot be added to Area " + id + ". They do not belong to the same network or subnetwork");
        }

        // Check if the voltageLevel is already in another Area of the same type
        final Optional<Area> previousArea = voltageLevel.getArea(this.getAreaType());
        if (previousArea.isPresent() && previousArea.get() != this) {
            // This instance already has a different area with the same AreaType
            throw new PowsyblException("VoltageLevel " + voltageLevel.getId() + " is already in Area of the same type=" + previousArea.get().getAreaType() + " with id=" + previousArea.get().getId());
        }
        // No conflict, add the area to this voltageLevel and vice versa
        voltagelevels.add(voltageLevel);
        voltageLevel.addArea(this);
    }

    @Override
    public void removeBoundaryTerminal(Terminal terminal) {
        boundaryTerminals.removeIf(boundaryTerminal -> boundaryTerminal.terminal().equals(terminal));
    }

    @Override
    public void removeVoltageLevel(VoltageLevel voltageLevel) {
        voltagelevels.remove(voltageLevel);
        if (Iterables.contains(voltageLevel.getAreas(), this)) {
            voltageLevel.removeArea(this);
        }
    }

    @Override
    public void addBoundaryTerminal(Terminal terminal, boolean ac) {
        Network terminalNetwork = terminal.getConnectable().getParentNetwork();
        if (subnetworkRef.get() != terminalNetwork && networkRef.get() != terminalNetwork) {
            throw new PowsyblException("Terminal of connectable " + terminal.getConnectable().getId() + " cannot be added to Area " + id + " boundaries. They do not belong to the same network or subnetwork");
        }
        boundaryTerminals.add(new Area.BoundaryTerminal(terminal, ac));
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
