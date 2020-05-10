/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseRawModel {

    private final PsseCaseIdentification caseIdentification;

    private final Map<Integer, PsseBus> buses = new LinkedHashMap<>();

    private final List<PsseLoad> loads = new ArrayList<>();

    private final List<PsseFixedShunt> fixedShunts = new ArrayList<>();

    private final List<PsseGenerator> generators = new ArrayList<>();

    private final List<PsseNonTransformerBranch> nonTransformerBranches = new ArrayList<>();

    private final List<PsseTransformer> transformers = new ArrayList<>();

    private final List<PsseArea> areas = new ArrayList<>();

    private final List<PsseZone> zones = new ArrayList<>();

    private final List<PsseOwner> owners = new ArrayList<>();

    public PsseRawModel(PsseCaseIdentification caseIdentification) {
        this.caseIdentification = Objects.requireNonNull(caseIdentification);
    }

    public PsseCaseIdentification getCaseIdentification() {
        return caseIdentification;
    }

    public void addBuses(Collection<PsseBus> buses) {
        for (PsseBus bus : buses) {
            this.buses.put(bus.getI(), bus);
        }
    }

    public Collection<PsseBus> getBuses() {
        return buses.values();
    }

    public PsseBus getBus(int i) {
        return buses.get(i);
    }

    public void addLoads(Collection<PsseLoad> loads) {
        this.loads.addAll(loads);
    }

    public Collection<PsseLoad> getLoads() {
        return loads;
    }

    public void addFixedShunts(Collection<PsseFixedShunt> fixedShunts) {
        this.fixedShunts.addAll(fixedShunts);
    }

    public Collection<PsseFixedShunt> getFixedShunts() {
        return fixedShunts;
    }

    public void addGenerators(Collection<PsseGenerator> generators) {
        this.generators.addAll(generators);
    }

    public Collection<PsseGenerator> getGenerators() {
        return generators;
    }

    public void addNonTransformerBranches(Collection<PsseNonTransformerBranch> nonTransformerBranches) {
        this.nonTransformerBranches.addAll(nonTransformerBranches);
    }

    public Collection<PsseNonTransformerBranch> getNonTransformerBranches() {
        return nonTransformerBranches;
    }

    public void addTransformers(Collection<PsseTransformer> transformers) {
        this.transformers.addAll(transformers);
    }

    public Collection<PsseTransformer> getTransformers() {
        return transformers;
    }

    public void addAreas(Collection<PsseArea> areas) {
        this.areas.addAll(areas);
    }

    public Collection<PsseArea> getAreas() {
        return areas;
    }

    public void addZones(Collection<PsseZone> zones) {
        this.zones.addAll(zones);
    }

    public Collection<PsseZone> getZones() {
        return zones;
    }

    public void addOwners(Collection<PsseOwner> owners) {
        this.owners.addAll(owners);
    }

    public Collection<PsseOwner> getOwners() {
        return owners;
    }

    public void postProcess() {
        for (PsseLoad load : loads) {
            load.postProcess(this);
        }
        for (PsseGenerator generator : generators) {
            generator.postProcess(this);
        }
        for (PsseNonTransformerBranch nonTransformerBranch : nonTransformerBranches) {
            nonTransformerBranch.postProcess(this);
        }
    }
}
