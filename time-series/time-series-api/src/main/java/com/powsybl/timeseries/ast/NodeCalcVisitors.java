/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 *
 *         This utility class implements the core iterative algorithm to perform
 *         a post-order {@link NodeCalc} tree traversal using a
 *         {@link NodeCalcVisitor}.
 */
public final class NodeCalcVisitors {

    private NodeCalcVisitors() {
    }

    /**
     * Perform the post-order tree traversal of {@code root} using {@code visitor}.
     * The argument {@code arg} is supplied to the visitors at each visit of a node.
     * <p>
     * For each node, the iterate method is first called to traverse the tree. Then,
     * after all the children have been visited, the visit method is called with the
     * result of visiting all the children.
     *
     * @param root    The NodeCalc tree
     * @param arg     an optional argument
     * @param visitor The NodeCalcVisitor
     * @return network factory with the given name
     */
    public static <R, A> R visit(NodeCalc root, A arg, NodeCalcVisitor<R, A> visitor) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(visitor);
        // We will traverse the tree and put the nodes in the visitQueue stack. We will
        // compute results for the nodes and put them in the childrenQueue stack.
        // The first time that we handle a node in the stack, we don't pop it and we
        // only push its children to the visitQueue stack.
        // The second time that we handle a node in the visitQueue stack, we pop it,
        // we pop the results of the children from the childrenQueue stack, we compute
        // the result and push it to the childrenQueue stack.
        ArrayDeque<NodeWrapper> visitQueue = new ArrayDeque<>();
        ArrayDeque<Optional<R>> childrenQueue = new ArrayDeque<>();
        visitQueue.push(new NodeWrapper(root));
        while (!visitQueue.isEmpty()) {
            NodeWrapper nodeWrapper = visitQueue.peek();
            if (nodeWrapper.afterChildren) {
                visitQueue.pop();
                visit(arg, visitor, childrenQueue, nodeWrapper);
            } else {
                nodeWrapper.afterChildren = true;
                iterate(arg, visitor, visitQueue, nodeWrapper);
            }
        }
        return childrenQueue.pop().orElse(null);
    }

    private static <A, R> void iterate(A arg, NodeCalcVisitor<R, A> visitor, ArrayDeque<NodeWrapper> visitQueue,
            NodeWrapper nodeWrapper) {
        if (nodeWrapper.node != null) {
            nodeWrapper.node.acceptIterate(visitor, arg, visitQueue);
        }
    }

    private static <R, A> void visit(A arg, NodeCalcVisitor<R, A> visitor, ArrayDeque<Optional<R>> childrenQueue,
            NodeWrapper nodeWrapper) {
        R result = nodeWrapper.node != null ? nodeWrapper.node.acceptVisit(visitor, arg, childrenQueue) : null;
        childrenQueue.push(Optional.ofNullable(result));
    }

    static class NodeWrapper {

        private NodeCalc node;
        private boolean afterChildren;

        public NodeWrapper(NodeCalc node) {
            this.node = node;
        }
    }
}
