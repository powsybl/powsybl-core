/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
public class MatpowerModel {

    @JsonProperty("caseName")
    private String caseName;
    private double baseMva;
    private MatpowerFormatVersion version;

    private final List<MBus> buses = new ArrayList<>();

    private final Map<Integer, MBus> busByNum = new HashMap<>();

    private final List<MGen> generators = new ArrayList<>();

    private final Map<Integer, List<MGen>> generatorsByBusNum = new HashMap<>();

    private final List<MBranch> branches = new ArrayList<>();

    private final List<MDcLine> dcLines = new ArrayList<>();

    private final List<MSwitch> switches = new ArrayList<>();

    @JsonCreator
    public MatpowerModel(@JsonProperty("caseName") String caseName) {
        this.caseName = Objects.requireNonNull(caseName);
    }

    public MatpowerFormatVersion getVersion() {
        return version;
    }

    public void setVersion(MatpowerFormatVersion version) {
        this.version = Objects.requireNonNull(version);
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = Objects.requireNonNull(caseName);
    }

    public double getBaseMva() {
        return baseMva;
    }

    public void setBaseMva(double baseMva) {
        this.baseMva = baseMva;
    }

    public void addBus(MBus bus) {
        Objects.requireNonNull(bus);
        buses.add(bus);
        busByNum.put(bus.getNumber(), bus);
    }

    public List<MBus> getBuses() {
        return buses;
    }

    public void setBuses(List<MBus> buses) {
        Objects.requireNonNull(buses);
        this.buses.clear();
        for (MBus bus : buses) {
            addBus(bus);
        }
    }

    public MBus getBusByNum(int num) {
        return busByNum.get(num);
    }

    public List<MGen> getGenerators() {
        return generators;
    }

    public void setGenerators(List<MGen> generators) {
        Objects.requireNonNull(generators);
        this.generators.clear();
        for (MGen generator : generators) {
            addGenerator(generator);
        }
    }

    public void addGenerator(MGen generator) {
        Objects.requireNonNull(generator);
        generators.add(generator);
        generatorsByBusNum.computeIfAbsent(generator.getNumber(), k -> new ArrayList<>())
                .add(generator);
    }

    public List<MGen> getGeneratorsByBusNum(int busNum) {
        return generatorsByBusNum.getOrDefault(busNum, Collections.emptyList());
    }

    public List<MBranch> getBranches() {
        return branches;
    }

    public void setBranches(List<MBranch> branches) {
        Objects.requireNonNull(branches);
        this.branches.clear();
        for (MBranch branch : branches) {
            addBranch(branch);
        }
    }

    public void addBranch(MBranch branch) {
        Objects.requireNonNull(branch);
        branches.add(branch);
    }

    public List<MDcLine> getDcLines() {
        return dcLines;
    }

    public void addDcLine(MDcLine dcLine) {
        Objects.requireNonNull(dcLine);
        dcLines.add(dcLine);
    }

    public List<MSwitch> getSwitches() {
        return switches;
    }

    public void addSwitch(MSwitch mSwitch) {
        Objects.requireNonNull(mSwitch);
        switches.add(mSwitch);
    }
}
