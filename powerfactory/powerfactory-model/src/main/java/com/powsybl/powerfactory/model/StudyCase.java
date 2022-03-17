/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

@JsonIgnoreProperties({"time", "elmNets"})
@JsonPropertyOrder({"name", "dataObjects"})

public class StudyCase {

    private final String name;

    private final Instant time;

    private final List<DataObject> elmNets;

    public StudyCase(String name, Instant time, List<DataObject> elmNets) {
        this.name = Objects.requireNonNull(name);
        this.time = Objects.requireNonNull(time);
        if (elmNets.isEmpty()) {
            throw new IllegalArgumentException("Empty ElmNet list");
        }
        this.elmNets = Objects.requireNonNull(elmNets);
    }

    public String getName() {
        return name;
    }

    public Instant getTime() {
        return time;
    }

    public List<DataObject> getElmNets() {
        return elmNets;
    }

    public List<DataObject> getDataObjects() {
        Set<DataObject> dataObjectsSet = new HashSet<>();

        for (DataObject elmNet : elmNets) {
            elmNet.traverseAndReference(obj -> dataObjectsSet.add(obj));
        }
        List<DataObject> dataObjects = new ArrayList<>();
        dataObjects.addAll(dataObjectsSet);

        Collections.sort(dataObjects, (do1, do2) -> ((Long) do1.getId()).compareTo(do2.getId()));

        return dataObjects;
    }
}
