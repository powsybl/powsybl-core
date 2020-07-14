/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseRawModel {

    private final PsseCaseIdentification caseIdentification;

    private final List<PsseBus> buses = new ArrayList<>();

    private final List<PsseLoad> loads = new ArrayList<>();

    private final List<PsseFixedShunt> fixedShunts = new ArrayList<>();

    private final List<PsseGenerator> generators = new ArrayList<>();

    private final List<PsseNonTransformerBranch> nonTransformerBranches = new ArrayList<>();

    private final List<PsseTransformer> transformers = new ArrayList<>();

    private final List<PsseArea> areas = new ArrayList<>();

    private final List<PsseZone> zones = new ArrayList<>();

    private final List<PsseOwner> owners = new ArrayList<>();

    private final List<PsseSwitchedShunt> switchedShunts = new ArrayList<>();

    public PsseRawModel(PsseCaseIdentification caseIdentification) {
        this.caseIdentification = Objects.requireNonNull(caseIdentification);
    }

    public PsseCaseIdentification getCaseIdentification() {
        return caseIdentification;
    }

    public void addBuses(List<PsseBus> buses) {
        this.buses.addAll(buses);
    }

    public List<PsseBus> getBuses() {
        return buses;
    }

    public void addLoads(List<PsseLoad> loads) {
        this.loads.addAll(loads);
    }

    public List<PsseLoad> getLoads() {
        return loads;
    }

    public void addFixedShunts(List<PsseFixedShunt> fixedShunts) {
        this.fixedShunts.addAll(fixedShunts);
    }

    public List<PsseFixedShunt> getFixedShunts() {
        return fixedShunts;
    }

    public void addGenerators(List<PsseGenerator> generators) {
        this.generators.addAll(generators);
    }

    public List<PsseGenerator> getGenerators() {
        return generators;
    }

    public void addNonTransformerBranches(List<PsseNonTransformerBranch> nonTransformerBranches) {
        this.nonTransformerBranches.addAll(nonTransformerBranches);
    }

    public List<PsseNonTransformerBranch> getNonTransformerBranches() {
        return nonTransformerBranches;
    }

    public List<PsseTransformer> getTransformers() {
        return transformers;
    }

    public void addAreas(List<PsseArea> areas) {
        this.areas.addAll(areas);
    }

    public List<PsseArea> getAreas() {
        return areas;
    }

    public void addZones(List<PsseZone> zones) {
        this.zones.addAll(zones);
    }

    public List<PsseZone> getZones() {
        return zones;
    }

    public void addOwners(List<PsseOwner> owners) {
        this.owners.addAll(owners);
    }

    public List<PsseOwner> getOwners() {
        return owners;
    }

    public void addSwitchedShunts(List<PsseSwitchedShunt> switchedShunts) {
        this.switchedShunts.addAll(switchedShunts);
    }

    public List<PsseSwitchedShunt> getSwitchedShunts() {
        return switchedShunts;
    }
}
