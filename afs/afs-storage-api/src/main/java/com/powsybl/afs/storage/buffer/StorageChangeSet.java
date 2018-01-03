/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.buffer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
public class StorageChangeSet {

    private final List<StorageChange> changes;

    public StorageChangeSet() {
        this(new ArrayList<>());
    }

    @JsonCreator
    public StorageChangeSet(@JsonProperty("changes") List<StorageChange> changes) {
        this.changes = changes;
    }

    @JsonIgnore
    public long getEstimatedSize() {
        return changes.stream().mapToLong(StorageChange::getEstimatedSize).sum();
    }

    public List<StorageChange> getChanges() {
        return changes;
    }

    @Override
    public int hashCode() {
        return changes.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StorageChangeSet) {
            StorageChangeSet other = (StorageChangeSet) obj;
            return changes.equals(other.changes);
        }
        return false;
    }
}
