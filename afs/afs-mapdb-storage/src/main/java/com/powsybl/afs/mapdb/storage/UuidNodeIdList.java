/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.google.common.collect.ImmutableList;
import com.powsybl.afs.storage.NodeId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UuidNodeIdList {

    private final List<UuidNodeId> nodeIds;

    public UuidNodeIdList() {
        this(new ArrayList<>());
    }

    public UuidNodeIdList(List<UuidNodeId> nodeIds) {
        this.nodeIds = Objects.requireNonNull(nodeIds);
    }

    public int size() {
        return nodeIds.size();
    }

    public UuidNodeId get(int i) {
        return nodeIds.get(i);
    }

    public List<NodeId> getNodeIds() {
        return nodeIds.stream().collect(Collectors.toList());
    }

    public UuidNodeIdList add(UuidNodeId nodeId) {
        return new UuidNodeIdList(ImmutableList.<UuidNodeId>builder()
                .addAll(nodeIds)
                .add(nodeId)
                .build());
    }

    public UuidNodeIdList remove(UuidNodeId nodeId) {
        List<UuidNodeId> newNodeIds = new ArrayList<>(nodeIds);
        newNodeIds.remove(nodeId);
        return new UuidNodeIdList(newNodeIds);
    }
}
