/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import java.util.Objects;

/**
 * Represents a named dependency to a node in the tree.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeDependency {

    private final String name;

    private final NodeInfo nodeInfo;

    public NodeDependency(String name, NodeInfo nodeInfo) {
        this.name = Objects.requireNonNull(name);
        this.nodeInfo = Objects.requireNonNull(nodeInfo);
    }

    public String getName() {
        return name;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, nodeInfo);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeDependency) {
            NodeDependency other = (NodeDependency) obj;
            return name.equals(other.name) && nodeInfo.equals(other.nodeInfo);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeDependency(name=" + name + ", nodeInfo=" + nodeInfo + ")";
    }
}
