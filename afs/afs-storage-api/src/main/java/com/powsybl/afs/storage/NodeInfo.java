/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfo {

    private final NodeId id;

    private final String name;

    private final String pseudoClass;

    public NodeInfo(NodeId id, String name, String pseudoClass) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.pseudoClass = Objects.requireNonNull(pseudoClass);
    }

    public NodeId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPseudoClass() {
        return pseudoClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, pseudoClass);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeInfo) {
            NodeInfo other = (NodeInfo) obj;
            return id.equals(other.id) && name.equals(other.name) && pseudoClass.equals(other.pseudoClass);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeInfo(id=" + id + ", name=" + name + ", pseudoClass=" + pseudoClass + ")";
    }
}
