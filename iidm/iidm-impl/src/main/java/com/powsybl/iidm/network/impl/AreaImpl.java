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
import gnu.trove.list.array.TDoubleArrayList;

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
    private final List<AreaBoundary> areaBoundaries;
    private final Set<VoltageLevel> voltageLevels;

    protected boolean removed = false;

    // attributes depending on the variant

    private final TDoubleArrayList acInterchangeTarget;

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
            // When a VoltageLevel is removed, its areas' voltageLevels attributes are directly updated. This is not managed via this listener.
        }
    }

    /**
     * Must be called whenever the Area is moved to a different root Network when merging and detaching.
     * @param fromNetwork previous root network
     * @param toNetwork new root network
     */
    void moveListener(NetworkImpl fromNetwork, NetworkImpl toNetwork) {
        fromNetwork.removeListener(this.areaListener);
        toNetwork.addListener(this.areaListener);
    }

    private final NetworkListener areaListener;

    AreaImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious, String areaType,
                    double acInterchangeTarget) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.subnetworkRef = subnetworkRef;
        this.areaType = Objects.requireNonNull(areaType);
        this.voltageLevels = new LinkedHashSet<>();
        this.areaBoundaries = new ArrayList<>();

        int variantArraySize = networkRef.get().getVariantManager().getVariantArraySize();
        this.acInterchangeTarget = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.acInterchangeTarget.add(acInterchangeTarget);
        }
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
        return voltageLevels;
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        throwIfRemoved("voltage levels");
        return voltageLevels.stream();
    }

    @Override
    public OptionalDouble getAcInterchangeTarget() {
        throwIfRemoved("AC interchange target");
        double target = acInterchangeTarget.get(getNetwork().getVariantIndex());
        if (Double.isNaN(target)) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(target);
    }

    @Override
    public Area setAcInterchangeTarget(double acInterchangeTarget) {
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        double oldValue = this.acInterchangeTarget.set(variantIndex, acInterchangeTarget);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        notifyUpdate("acInterchangeTarget", variantId, oldValue, acInterchangeTarget);
        return this;
    }

    @Override
    public double getAcInterchange() {
        throwIfRemoved("AC interchange");
        return getInterchange(AreaBoundary::isAc);
    }

    @Override
    public double getDcInterchange() {
        throwIfRemoved("DC interchange");
        return getInterchange(areaBoundary -> !areaBoundary.isAc());
    }

    @Override
    public double getTotalInterchange() {
        throwIfRemoved("total interchange");
        return getInterchange(areaBoundary -> true);
    }

    double getInterchange(Predicate<AreaBoundary> predicate) {
        return areaBoundaries.stream().filter(predicate).mapToDouble(AreaBoundary::getP).filter(p -> !Double.isNaN(p)).sum();
    }

    /**
     * Adds a VoltageLevel to this Area.
     * @throws PowsyblException if the VoltageLevel is already in another Area of the same type
     * @param voltageLevel the VoltageLevel to be added to this Area
     */
    @Override
    public Area addVoltageLevel(VoltageLevel voltageLevel) {
        Objects.requireNonNull(voltageLevel);
        if (voltageLevels.add(voltageLevel)) {
            voltageLevel.addArea(this);
        }
        return this;
    }

    @Override
    public Area removeVoltageLevel(VoltageLevel voltageLevel) {
        Objects.requireNonNull(voltageLevel);
        voltageLevels.remove(voltageLevel);
        if (Iterables.contains(voltageLevel.getAreas(), this)) {
            voltageLevel.removeArea(this);
        }
        return this;
    }

    @Override
    public AreaBoundaryAdderImpl newAreaBoundary() {
        return new AreaBoundaryAdderImpl(this);
    }

    @Override
    public Area removeAreaBoundary(Terminal terminal) {
        Objects.requireNonNull(terminal);
        areaBoundaries.removeIf(b -> Objects.equals(b.getTerminal().orElse(null), terminal));
        return this;
    }

    @Override
    public Area removeAreaBoundary(Boundary boundary) {
        Objects.requireNonNull(boundary);
        areaBoundaries.removeIf(b -> Objects.equals(b.getBoundary().orElse(null), boundary));
        return this;
    }

    @Override
    public AreaBoundary getAreaBoundary(Boundary boundary) {
        Objects.requireNonNull(boundary);
        return areaBoundaries.stream()
                .filter(ab -> Objects.equals(ab.getBoundary().orElse(null), boundary))
                .findFirst().orElse(null);
    }

    @Override
    public AreaBoundary getAreaBoundary(Terminal terminal) {
        Objects.requireNonNull(terminal);
        return areaBoundaries.stream()
                .filter(ab -> Objects.equals(ab.getTerminal().orElse(null), terminal))
                .findFirst().orElse(null);
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
        for (VoltageLevel voltageLevel : new HashSet<>(voltageLevels)) {
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

    protected void notifyUpdate(String attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, variantId, oldValue, newValue);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        acInterchangeTarget.ensureCapacity(acInterchangeTarget.size() + number);
        for (int i = 0; i < number; i++) {
            acInterchangeTarget.add(acInterchangeTarget.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        acInterchangeTarget.remove(acInterchangeTarget.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            acInterchangeTarget.set(index, acInterchangeTarget.get(sourceIndex));
        }
    }

}
