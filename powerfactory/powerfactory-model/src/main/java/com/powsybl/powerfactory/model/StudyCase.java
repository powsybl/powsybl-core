/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@JsonIgnoreProperties({"time", "index"})
@JsonPropertyOrder({"name", "dataObjects"})
public class StudyCase {

    private final String name;

    private final Instant time;

    private final DataObjectIndex index;

    public StudyCase(String name, Instant time, DataObjectIndex index) {
        this.name = Objects.requireNonNull(name);
        this.time = Objects.requireNonNull(time);
        this.index = Objects.requireNonNull(index);
    }

    public String getName() {
        return name;
    }

    public Instant getTime() {
        return time;
    }

    public DataObjectIndex getIndex() {
        return index;
    }

    public Collection<DataObject> getDataObjects() {
        return index.getDataObjects();
    }
}
