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

    private final List<AreaBoundary> areaBoundaries;

    private final Set<VoltageLevel> voltagelevels;

    public AreaImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious, String areaType,
                    Double acNetInterchangeTarget) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.subnetworkRef = subnetworkRef;
        this.areaType = Objects.requireNonNull(areaType);
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        this.voltagelevels = new LinkedHashSet<>();
        this.areaBoundaries = new ArrayList<>();
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
    public Double getAcNetInterchange() {
        return -areaBoundaries.stream().filter(AreaBoundary::isAc).mapToDouble(AreaBoundary::getP).filter(p -> !Double.isNaN(p)).sum();
    }

    @Override
    public Double getDcNetInterchange() {
        return -areaBoundaries.stream().filter(areaBoundary -> !areaBoundary.isAc()).mapToDouble(AreaBoundary::getP).filter(p -> !Double.isNaN(p)).sum();
    }

    @Override
    public Double getTotalNetInterchange() {
        return getAcNetInterchange() + getDcNetInterchange();
    }

    /**
     * Adds a VoltageLevel to this Area.
     * @throws PowsyblException if the VoltageLevel is already in another Area of the same type
     * @param voltageLevel the VoltageLevel to be added to this Area
     */
    @Override
    public void addVoltageLevel(VoltageLevel voltageLevel) {
        voltageLevel.addArea(this);
        voltagelevels.add(voltageLevel);
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
        areaBoundaries.remove(areaBoundaries.stream().filter(b -> Objects.equals(b.getTerminal().orElse(null), terminal)).findFirst().orElse(null));
    }

    @Override
    public void removeAreaBoundary(DanglingLine danglingLine) {
        areaBoundaries.remove(areaBoundaries.stream().filter(b -> Objects.equals(b.getDanglingLine().orElse(null), danglingLine)).findFirst().orElse(null));
    }

    @Override
    public Iterable<AreaBoundary> getAreaBoundaries() {
        return areaBoundaries;
    }

    @Override
    public Stream<AreaBoundary> getAreaBoundaryStream() {
        return areaBoundaries.stream();
    }

    protected void addAreaBoundary(AreaBoundaryImpl areaBoundary) {
        areaBoundaries.add(areaBoundary);
    }

}
