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
import java.util.function.Predicate;
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
    private final List<AreaBoundary> areaBoundaries;
    private final Set<VoltageLevel> voltagelevels;

    protected boolean removed = false;

    private final class AreaListener extends DefaultNetworkListener {
        @Override
        public void beforeRemoval(Identifiable<?> identifiable) {
            if (identifiable instanceof DanglingLine danglingLine) {
                // if dangling line removed from network, remove its boundary from this extension
                AreaImpl.this.removeAreaBoundary(danglingLine.getBoundary());
            } else if (identifiable instanceof Connectable<?> connectable) {
                // if connectable removed from network, remove its terminals from this extension
                connectable.getTerminals().forEach(AreaImpl.this::removeAreaBoundary);
            }
            // If the identifiable removed is a VoltageLevel: the area voltageLevels attribute will be updated by VoltageLevel::remove()
        }
    }

    private final NetworkListener areaListener;

    public AreaImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious, String areaType,
                    Double acNetInterchangeTarget) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.subnetworkRef = subnetworkRef;
        this.areaType = Objects.requireNonNull(areaType);
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        this.voltagelevels = new LinkedHashSet<>();
        this.areaBoundaries = new ArrayList<>();
        this.areaListener = new AreaListener();
        getNetwork().addListener(this.areaListener);
    }

    @Override
    public NetworkImpl getNetwork() {
        throwIfRemoved("network");
        return networkRef.get();
    }

    @Override
    public Network getParentNetwork() {
        throwIfRemoved("network");
        return Optional.ofNullable((Network) subnetworkRef.get()).orElse(getNetwork());
    }

    @Override
    protected String getTypeDescription() {
        return "Area";
    }

    @Override
    public String getAreaType() {
        throwIfRemoved("area type");
        return areaType;
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        throwIfRemoved("voltage levels");
        return voltagelevels;
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        throwIfRemoved("voltage levels");
        return voltagelevels.stream();
    }

    @Override
    public Optional<Double> getAcNetInterchangeTarget() {
        throwIfRemoved("AC net interchange target");
        return Optional.ofNullable(acNetInterchangeTarget);
    }

    @Override
    public Double getAcNetInterchange() {
        throwIfRemoved("AC net interchange");
        return getInterchange(AreaBoundary::isAc);
    }

    @Override
    public Double getDcNetInterchange() {
        throwIfRemoved("DC net interchange");
        return getInterchange(areaBoundary -> !areaBoundary.isAc());
    }

    @Override
    public Double getTotalNetInterchange() {
        throwIfRemoved("total net interchange");
        return getInterchange(areaBoundary -> true);
    }

    Double getInterchange(Predicate<AreaBoundary> predicate) {
        return areaBoundaries.stream().filter(predicate).mapToDouble(AreaBoundary::getP).filter(p -> !Double.isNaN(p)).sum();
    }

    /**
     * Adds a VoltageLevel to this Area.
     * @throws PowsyblException if the VoltageLevel is already in another Area of the same type
     * @param voltageLevel the VoltageLevel to be added to this Area
     */
    @Override
    public void addVoltageLevel(VoltageLevel voltageLevel) {
        if (voltagelevels.add(voltageLevel)) {
            voltageLevel.addArea(this);
        }
    }

    @Override
    public void removeVoltageLevel(VoltageLevel voltageLevel) {
        voltagelevels.remove(voltageLevel);
        if (Iterables.contains(voltageLevel.getAreas(), this)) {
            voltageLevel.removeArea(this);
        }
    }

    @Override
    public AreaBoundaryAdderImpl newAreaBoundary() {
        return new AreaBoundaryAdderImpl(this);
    }

    @Override
    public void removeAreaBoundary(Terminal terminal) {
        areaBoundaries.removeIf(b -> Objects.equals(b.getTerminal().orElse(null), terminal));
    }

    @Override
    public void removeAreaBoundary(Boundary boundary) {
        areaBoundaries.removeIf(b -> Objects.equals(b.getBoundary().orElse(null), boundary));
    }

    @Override
    public Iterable<AreaBoundary> getAreaBoundaries() {
        throwIfRemoved("area boundaries");
        return areaBoundaries;
    }

    @Override
    public Stream<AreaBoundary> getAreaBoundaryStream() {
        throwIfRemoved("area boundaries");
        return areaBoundaries.stream();
    }

    protected void addAreaBoundary(AreaBoundaryImpl areaBoundary) {
        Optional<Terminal> terminal = areaBoundary.getTerminal();
        Optional<Boundary> boundary = areaBoundary.getBoundary();
        boundary.ifPresent(b -> checkBoundaryNetwork(b.getDanglingLine().getParentNetwork(), "Boundary of DanglingLine" + b.getDanglingLine().getId()));
        terminal.ifPresent(t -> checkBoundaryNetwork(t.getConnectable().getParentNetwork(), "Terminal of connectable " + t.getConnectable().getId()));
        areaBoundaries.add(areaBoundary);
    }

    void checkBoundaryNetwork(Network network, String boundaryTypeAndId) {
        if (getParentNetwork() != network) {
            throw new PowsyblException(boundaryTypeAndId + " cannot be added to Area " + getId() + " boundaries. It does not belong to the same network or subnetwork.");
        }
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();
        network.getListeners().notifyBeforeRemoval(this);
        network.getIndex().remove(this);
        for (VoltageLevel voltageLevel : new HashSet<>(voltagelevels)) {
            voltageLevel.removeArea(this);
        }
        network.getListeners().notifyAfterRemoval(id);
        network.removeListener(this.areaListener);
        removed = true;
    }

    void throwIfRemoved(String attribute) {
        if (removed) {
            throw new PowsyblException("Cannot access " + attribute + " of removed area " + id);
        }
    }

}
