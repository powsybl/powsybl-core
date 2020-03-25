/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MatpowerModel {

    private final String caseName;
    private Double baseMva;

    private List<MBus> buses = new ArrayList<>();

    private List<MGen> generators = new ArrayList<>();

    private List<MBranch> branches = new ArrayList<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private String version;

    public MatpowerModel(String caseName) {
        this.caseName = Objects.requireNonNull(caseName);
    }

    public String getCaseName() {
        return caseName;
    }

    public Double getBaseMva() {
        return baseMva;
    }

    public void setBaseMva(Double baseMva) {
        this.baseMva = baseMva;
    }

    public List<MBus> getBuses() {
        return buses;
    }

    public List<MGen> getGenerators() {
        return generators;
    }

    public List<MBranch> getBranches() {
        return branches;
    }
}
