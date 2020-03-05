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

    public PsseRawModel(PsseCaseIdentification caseIdentification) {
        this.caseIdentification = Objects.requireNonNull(caseIdentification);
    }

    public PsseCaseIdentification getCaseIdentification() {
        return caseIdentification;
    }

    public List<PsseBus> getBuses() {
        return buses;
    }

    public List<PsseLoad> getLoads() {
        return loads;
    }

    public List<PsseFixedShunt> getFixedShunts() {
        return fixedShunts;
    }

    public List<PsseGenerator> getGenerators() {
        return generators;
    }

    public List<PsseNonTransformerBranch> getNonTransformerBranches() {
        return nonTransformerBranches;
    }

    public List<PsseTransformer> getTransformers() {
        return transformers;
    }

    public List<PsseArea> getAreas() {
        return areas;
    }

    public List<PsseZone> getZones() {
        return zones;
    }

    public List<PsseOwner> getOwners() {
        return owners;
    }
}
