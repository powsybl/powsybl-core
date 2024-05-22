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
    private AreaType areaType;
    private final Optional<Double> acNetInterchangeTarget;
    private final Optional<Double> acNetInterchangeTolerance;
    private final Map<BoundaryPointType, List<Terminal>> boundaryPointsByType;

    private final Set<VoltageLevel> voltagelevels = new LinkedHashSet<>();

    public AreaImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious, AreaType areaType, Double acNetInterchangeTarget,
                       Double acNetInterchangeTolerance) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.areaType = Objects.requireNonNull(areaType);
        this.acNetInterchangeTarget = Optional.ofNullable(acNetInterchangeTarget);
        this.acNetInterchangeTolerance = Optional.ofNullable(acNetInterchangeTolerance);
        this.boundaryPointsByType = new EnumMap<>(BoundaryPointType.class);
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
    public void setAreaType(AreaType areaType) {
        this.areaType = Objects.requireNonNull(areaType);
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
        return acNetInterchangeTarget;
    }

    @Override
    public Optional<Double> getAcNetInterchangeTolerance() {
        return acNetInterchangeTolerance;
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
    public boolean isMergeable(Area other) {
        if (other == null || !other.getId().equals(getId())) {
            return false;
        }
        String name = getNameOrId();
        String nameOther = other.getNameOrId();
        if (!name.equals(nameOther)) {
            throw new PowsyblException("Cannot merge areas with same id" + getId() + " but different names: " + name + " and " + nameOther);
        }

        AreaType areaType = getAreaType();
        AreaType areaTypeOther = other.getAreaType();
        if (!areaType.equals(areaTypeOther)) {
            throw new PowsyblException("Cannot merge areas with same id" + getId() + " but different area types: " + areaType.getId() + " and " + areaTypeOther.getId());
        }

        Optional<Double> acNetInterchangeTarget = getAcNetInterchangeTarget();
        Optional<Double> acNetInterchangeTargetOther = other.getAcNetInterchangeTarget();
        if (!acNetInterchangeTarget.equals(acNetInterchangeTargetOther)) {
            throw new PowsyblException("Cannot merge areas with same id" + getId() + " but different AC Net Interchange Targets: " + acNetInterchangeTarget + " and " + acNetInterchangeTargetOther);
        }

        Optional<Double> acNetInterchangeTolerance = getAcNetInterchangeTolerance();
        Optional<Double> acNetInterchangeToleranceOther = other.getAcNetInterchangeTolerance();
        if (!acNetInterchangeTolerance.equals(acNetInterchangeToleranceOther)) {
            throw new PowsyblException("Cannot merge areas with same id" + getId() + " but different AC Net Interchange Tolerances: " + acNetInterchangeTolerance + " and " + acNetInterchangeToleranceOther);
        }

        return true;
    }

    @Override
    public void remove(VoltageLevel voltageLevel) {
        voltagelevels.remove(voltageLevel);
    }

    @Override
    public Map<BoundaryPointType, List<Terminal>> getBoundaryPointsByType() {
        return boundaryPointsByType;
    }

    @Override
    public Iterable<Terminal> getBoundaryPoints() {
        return getBoundaryPointStream().toList();
    }

    @Override
    public Stream<Terminal> getBoundaryPointStream() {
        return boundaryPointsByType.values().stream().flatMap(Collection::stream);
    }

    @Override
    public Iterable<Terminal> getBoundaryPoints(final BoundaryPointType boundaryPointType) {
        return boundaryPointsByType.get(boundaryPointType);
    }

    @Override
    public Stream<Terminal> getBoundaryPointStream(final BoundaryPointType boundaryPointType) {
        return boundaryPointsByType.get(boundaryPointType).stream();
    }

}
