/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import static com.powsybl.timeseries.ast.NodeCalcDuplicateDetector.detectDuplicates;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class NodeCalcCacheCreator extends NodeCalcModifier<Map<NodeCalc, NodeCalc>> {

    /**
     * Find the duplicated nodes in the tree and cache them depending on their type
     * @param nodeCalc Head of the node tree
     * @return null
     */
    public static NodeCalc cacheDuplicated(NodeCalc nodeCalc) {
        return new NodeCalcCacheCreator().createCachedNodes(nodeCalc);
    }

    private NodeCalc createCachedNodes(NodeCalc nodeCalc) {
        // Get the list of parents for each NodeCalc
        Map<NodeCalc, Set<NodeCalc>> parents = detectDuplicates(nodeCalc);

        // Creates the list of CachedNodeCalc
        Map<NodeCalc, NodeCalc> cachedNodes = new IdentityHashMap<>();
        parents.forEach((NodeCalc child, Set<NodeCalc> childParents) -> {
            // If a NodeCalc has 2 or more parents, a CachedNodeCalc is used.
            if (childParents.size() > 1) {
                cachedNodes.put(
                    child,
                    // If the NodeCalc already has a CachedNodeCalc as parent, it is used.
                    // If not, a new CachedNodeCalc is created
                    childParents.stream()
                        .filter(CachedNodeCalc.class::isInstance)
                        .findFirst()
                        .orElseGet(() -> new CachedNodeCalc(child)));
            }
        });

        // Cache the chosen NodeCalc
        return nodeCalc.accept(this, cachedNodes, 0);
    }

    @Override
    public NodeCalc visit(BinaryOperation nodeCalc, Map<NodeCalc, NodeCalc> parents, NodeCalc left, NodeCalc right) {
        return visitBinaryNodeCalc(nodeCalc, parents, left, right);
    }

    @Override
    public NodeCalc visit(BinaryMaxCalc nodeCalc, Map<NodeCalc, NodeCalc> parents, NodeCalc left, NodeCalc right) {
        return visitBinaryNodeCalc(nodeCalc, parents, left, right);
    }

    @Override
    public NodeCalc visit(BinaryMinCalc nodeCalc, Map<NodeCalc, NodeCalc> parents, NodeCalc left, NodeCalc right) {
        return visitBinaryNodeCalc(nodeCalc, parents, left, right);
    }

    @Override
    public NodeCalc visit(UnaryOperation nodeCalc, Map<NodeCalc, NodeCalc> parents, NodeCalc child) {
        return visitSingleChildNodeCalc(nodeCalc, parents, child);
    }

    @Override
    public NodeCalc visit(MinNodeCalc nodeCalc, Map<NodeCalc, NodeCalc> parents, NodeCalc child) {
        return visitSingleChildNodeCalc(nodeCalc, parents, child);
    }

    @Override
    public NodeCalc visit(MaxNodeCalc nodeCalc, Map<NodeCalc, NodeCalc> parents, NodeCalc child) {
        return visitSingleChildNodeCalc(nodeCalc, parents, child);
    }

    @Override
    public NodeCalc visit(TimeNodeCalc nodeCalc, Map<NodeCalc, NodeCalc> parents, NodeCalc child) {
        return visitSingleChildNodeCalc(nodeCalc, parents, child);
    }

    @Override
    public NodeCalc visit(CachedNodeCalc nodeCalc, Map<NodeCalc, NodeCalc> parents, NodeCalc child) {
        if (child != null && !(child instanceof CachedNodeCalc)) {
            nodeCalc.setChild(child);
        }
        return null;
    }

    private NodeCalc visitBinaryNodeCalc(AbstractBinaryNodeCalc nodeCalc, Map<NodeCalc, NodeCalc> parents, NodeCalc left, NodeCalc right) {
        if (left != null) {
            nodeCalc.setLeft(left);
        }
        if (right != null) {
            nodeCalc.setRight(right);
        }
        return parents.getOrDefault(nodeCalc, null);
    }

    private NodeCalc visitSingleChildNodeCalc(AbstractSingleChildNodeCalc nodeCalc, Map<NodeCalc, NodeCalc> parents, NodeCalc child) {
        if (child != null) {
            nodeCalc.setChild(child);
        }
        return parents.getOrDefault(nodeCalc, null);
    }
}
