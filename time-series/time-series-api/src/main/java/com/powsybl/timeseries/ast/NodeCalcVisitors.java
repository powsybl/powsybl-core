/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import java.util.ArrayDeque;
import java.util.Objects;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 *
 *         This utility class implements the core iterative algorithm to perform
 *         a post-order {@link NodeCalc} tree traversal using a
 *         {@link NodeCalcVisitor}.
 */
public final class NodeCalcVisitors {

    public static final Object NULL = new Object();

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
        ArrayDeque<Object> prepareQueue = new ArrayDeque<>();
        ArrayDeque<Object> visitQueue = new ArrayDeque<>();
        prepareQueue.push(root);
        while (!prepareQueue.isEmpty()) {
            Object nodeWrapper = prepareQueue.pop();
            visitQueue.push(nodeWrapper);
            if (nodeWrapper != NULL) {
                ((NodeCalc) nodeWrapper).acceptIterate(visitor, arg, prepareQueue);
            }
        }
        // reuse prepareQueue for performance, it's empty but as the correct capacity
        ArrayDeque<Object> childrenQueue = prepareQueue;
        while (!visitQueue.isEmpty()) {
            Object nodeWrapper = visitQueue.pop();
            R result = nodeWrapper != NULL ? ((NodeCalc) nodeWrapper).acceptVisit(visitor, arg, childrenQueue) : null;
            childrenQueue.push(result == null ? NULL : result);
        }
        Object result = childrenQueue.pop();
        return result == NULL ? null : (R) result;
    }

}
