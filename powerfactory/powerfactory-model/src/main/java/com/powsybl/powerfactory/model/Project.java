/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Project {

    private final String name;

    private final Instant creationTime;

    private final DataObject rootObject;

    private final DataObjectIndex index;

    public Project(String name, Instant creationTime, DataObject rootObject, DataObjectIndex index) {
        this.name = Objects.requireNonNull(name);
        this.creationTime = Objects.requireNonNull(creationTime);
        this.rootObject = Objects.requireNonNull(rootObject);
        this.index = Objects.requireNonNull(index);
    }

    public String getName() {
        return name;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public DataObject getRootObject() {
        return rootObject;
    }

    public DataObjectIndex getIndex() {
        return index;
    }
}
