/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import com.powsybl.commons.config.PlatformConfig;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * This utility class implements the core iterative algorithm to perform
 * a post-order {@link NodeCalc} tree traversal using a
 * {@link NodeCalcVisitor}.
 *
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 *
 */
public final class NodeCalcVisitors {

    // The max number of recursive calls. On normal computers using the defaults, a
    // threshold of more than a few thousands may throw StackOverflowException.
    // A recursive traversal is used below the threshold instead of an iterative
    // traversal and is up to 5x faster.
    private static final int DEFAULT_RECURSION_THRESHOLD = 1000;
    public static final int RECURSION_THRESHOLD = PlatformConfig.defaultConfig()
            .getOptionalModuleConfig("timeseries")
            .map(moduleConfig -> moduleConfig.getIntProperty("recursion-threshold", DEFAULT_RECURSION_THRESHOLD))
            .orElse(DEFAULT_RECURSION_THRESHOLD);

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
    @SuppressWarnings("unchecked")
    public static <R, A> R visit(NodeCalc root, A arg, NodeCalcVisitor<R, A> visitor) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(visitor);

        // First traverse the nodes in right-left pre-order using the preOrderStack
        // stack and push nodes as we go to the postOrderStack stack. Later, popping
        // from the postOrderStack stack will give a left-right post-order traversal of
        // the tree, like the "normal" recursive traversal.
        // The stacks have a generic type of <Object> to allow to insert a null Object
        // because ArrayDeque doesn't allow nulls.
        Deque<Object> preOrderStack = new ArrayDeque<>();
        Deque<Object> postOrderStack = new ArrayDeque<>();
        preOrderStack.push(root);
        while (!preOrderStack.isEmpty()) {
            Object node = preOrderStack.pop();
            postOrderStack.push(node);
            if (node != NULL) {
                ((NodeCalc) node).acceptIterate(visitor, arg, preOrderStack);
            }
        }
        // Now do the left-right post-order traversal.
        // reuse prepareQueue for performance, it's empty but has the correct capacity
        // already allocated.
        // The stack have a generic type of <Object> to allow to insert a null Object
        // because ArrayDeque doesn't allow nulls.
        while (!postOrderStack.isEmpty()) {
            Object nodeWrapper = postOrderStack.pop();
            R result = nodeWrapper != NULL ? ((NodeCalc) nodeWrapper).acceptHandle(visitor, arg, preOrderStack) : null;
            preOrderStack.push(result == null ? NULL : result);
        }
        Object result = preOrderStack.pop();
        return result == NULL ? null : (R) result;
    }

}
