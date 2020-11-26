/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MatpowerModel {

    @JsonProperty("caseName")
    private String caseName;
    private double baseMva;
    private String version;

    private final List<MBus> buses = new ArrayList<>();

    private final Map<Integer, MBus> busByNum = new HashMap<>();

    private final List<MGen> generators = new ArrayList<>();

    private final List<MBranch> branches = new ArrayList<>();

    @JsonCreator
    public MatpowerModel(@JsonProperty("caseName") String caseName) {
        this.caseName = Objects.requireNonNull(caseName);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCaseName() {
        return caseName;
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

    public MBus getBusByNum(int num) {
        return busByNum.get(num);
    }

    public List<MGen> getGenerators() {
        return generators;
    }

    public List<MBranch> getBranches() {
        return branches;
    }
}
