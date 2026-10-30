/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph.dtree;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A depth-first iterator of a connected component.
 *
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
public class DFSIterator<V, E> implements Iterator<V> {

    private DTNode<V, E> cursor;
    private DTNode<V, E> current;

    /**
     * Creates a new depth-first iterator starting at the specified root node
     * and returning node according to the pre-order.
     *
     * @param root the root of the tree to traverse. It must be a root otherwise,
     *             the iterator may visit nodes outside the subtree
     */
    DFSIterator(DTNode<V, E> root) {
        cursor = root;
    }

    @Override
    public boolean hasNext() {
        return cursor != null;
    }

    @Override
    public V next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        current = cursor;

        // Advances to the next node for the next iteration.
        // The iterator try to:
        // - descend one level whenever possible,
        // - otherwise, moves to the next sibling if any,
        // - otherwise, moves up until it finds a node with
        //   a next sibling (unvisited by construction) or
        //   the tree is fully visited.

        if (cursor.getFirstChild() != null) {
            cursor = cursor.getFirstChild();
        } else if (cursor.getNextSibling() != null) {
            cursor = cursor.getNextSibling();
        } else {
            while (cursor != null && cursor.getNextSibling() == null) {
                cursor = cursor.getParent();
            }

            if (cursor != null) {
                cursor = cursor.getNextSibling();
            }
        }

        return current.getVertex();
    }

    public DTNode<V, E> node() {
        return current;
    }
}
