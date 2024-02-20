/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class NodeCalcDuplicateDetector extends NodeCalcModifier<Map<NodeCalc, Set<NodeCalc>>> {

    /**
     * Create a set of parents for each node in the tree that could be cached.
     * @param nodeCalc Head of the node tree
     * @return Map of the parents for each node in the tree
     */
    public static Map<NodeCalc, Set<NodeCalc>> detectDuplicates(NodeCalc nodeCalc) {
        return new NodeCalcDuplicateDetector().detect(nodeCalc);
    }

    private Map<NodeCalc, Set<NodeCalc>> detect(NodeCalc nodeCalc) {
        Map<NodeCalc, Set<NodeCalc>> parents = new IdentityHashMap<>(100);
        nodeCalc.accept(this, parents, 0);
        return parents;
    }

    @Override
    public NodeCalc visit(BinaryOperation nodeCalc, Map<NodeCalc, Set<NodeCalc>> parents, NodeCalc left, NodeCalc right) {
        return visitBinaryNodeCalc(nodeCalc, parents, left, right);
    }

    @Override
    public NodeCalc visit(BinaryMinCalc nodeCalc, Map<NodeCalc, Set<NodeCalc>> parents, NodeCalc left, NodeCalc right) {
        return visitBinaryNodeCalc(nodeCalc, parents, left, right);
    }

    @Override
    public NodeCalc visit(BinaryMaxCalc nodeCalc, Map<NodeCalc, Set<NodeCalc>> parents, NodeCalc left, NodeCalc right) {
        return visitBinaryNodeCalc(nodeCalc, parents, left, right);
    }

    @Override
    public NodeCalc visit(UnaryOperation nodeCalc, Map<NodeCalc, Set<NodeCalc>> parents, NodeCalc child) {
        return visitSingleChildNodeCalc(nodeCalc, parents, child);
    }

    @Override
    public NodeCalc visit(MinNodeCalc nodeCalc, Map<NodeCalc, Set<NodeCalc>> parents, NodeCalc child) {
        return visitSingleChildNodeCalc(nodeCalc, parents, child);
    }

    @Override
    public NodeCalc visit(MaxNodeCalc nodeCalc, Map<NodeCalc, Set<NodeCalc>> parents, NodeCalc child) {
        return visitSingleChildNodeCalc(nodeCalc, parents, child);
    }

    @Override
    public NodeCalc visit(TimeNodeCalc nodeCalc, Map<NodeCalc, Set<NodeCalc>> parents, NodeCalc child) {
        return visitSingleChildNodeCalc(nodeCalc, parents, child);
    }

    @Override
    public NodeCalc visit(CachedNodeCalc nodeCalc, Map<NodeCalc, Set<NodeCalc>> parents, NodeCalc child) {
        visitSingleChildNodeCalc(nodeCalc, parents, child);
        return null;
    }

    private void visitNodeCalc(NodeCalc parent, NodeCalc child, Map<NodeCalc, Set<NodeCalc>> parents) {
        if (parents.containsKey(child)) {
            // If the child is already in the map, the parent is added to its set
            parents.get(child).add(parent);
        } else {
            // If the child is not already in the map, is is added and a set based on an IdentityHashMap (in order to
            // use the identity comparison) is created for this child, with the parent in it.
            Set<NodeCalc> nodeCalcParents = Collections.newSetFromMap(new IdentityHashMap<>());
            nodeCalcParents.add(parent);
            parents.put(child, nodeCalcParents);
        }
    }

    private NodeCalc visitBinaryNodeCalc(AbstractBinaryNodeCalc parent, Map<NodeCalc, Set<NodeCalc>> parents, NodeCalc left, NodeCalc right) {
        if (left != null) {
            visitNodeCalc(parent, left, parents);
        }
        if (right != null) {
            visitNodeCalc(parent, right, parents);
        }
        return parent;
    }

    private NodeCalc visitSingleChildNodeCalc(AbstractSingleChildNodeCalc parent, Map<NodeCalc, Set<NodeCalc>> parents, NodeCalc child) {
        if (child != null) {
            visitNodeCalc(parent, child, parents);
        }
        return parent;
    }

}
