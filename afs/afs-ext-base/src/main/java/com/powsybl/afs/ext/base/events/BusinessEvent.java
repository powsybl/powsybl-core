/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.afs.storage.events.NodeEvent;
/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */

public class BusinessEvent extends NodeEvent {

    @JsonProperty("parentId")
    protected final String parentId;

    @JsonProperty("path")
    protected final String path;

    @JsonCreator
    public BusinessEvent(@JsonProperty("id") String id, @JsonProperty("parentId") String parentId,
                         @JsonProperty("path") String path, String nodeEventType) {
        super(id, nodeEventType);
        this.parentId = parentId;
        this.path = path;
    }

    public String getParentId() {
        return parentId;
    }

    public String getPath() {
        return path;
    }
}
