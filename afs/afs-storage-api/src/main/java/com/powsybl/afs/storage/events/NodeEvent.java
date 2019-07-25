/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
public class NodeEvent {

    @JsonProperty("id")
    protected final String id;

    @JsonProperty
    protected final String type;

    protected NodeEvent(String id, String type) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
