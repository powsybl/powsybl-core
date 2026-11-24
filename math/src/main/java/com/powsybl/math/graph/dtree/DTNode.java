/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph.dtree;

import com.powsybl.math.graph.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTNode (Dynamic Tree Node) is a node in a spanning tree.
 * Each DTNode maintains the following information:
 * <ul>
 *     <li>the vertex in the graph,</li>
 *     <li>the size of the subtree,</li>
 *     <li>its parent in the tree and the edge linking them,</li>
 *     <li>its children in the tree and the edges linking them,</li>
 *     <li>all non tree edges having at least one endpoint that is the DTNode</li>
 *     <li>if the node is a root, its index in the list of roots ({@link DTGraph#getRoots()})</li>
 * </ul>
 *
 * <p>
 * However, children are stored in a particular way, allowing
 * fast iteration over a tree and insertion and removal of a child,
 * but slow access to an arbitrary element. Instead of storing them
 * in a list or a map, each DTNode has a pointer to its previous sibling,
 * its next sibling and its first child. In other words, the children of
 * a DTNode are stored in a doubly-linked list. A DTNode stores the first
 * element in this list and is also used as an element in its parent doubly
 * linked list of children.
 * </p>
 *
 * <p>
 * Example:
 * <pre>
 * +----- first child ------ 1
 * |                         ^
 * |                         |
 * |                      parent
 * |                         |
 * | +-----------------------+-----------------------+
 * v/                        |                        \
 * 2 <-- previous sibling -- 3 <-- previous sibling -- 4
 *  \_____ next sibling _____^\_____ next sibling _____^
 * </pre>
 * X --> Y indicates that X contains a pointer to Y.
 * </p>
 *
 * <p>
 * This complex structure allows fast insertion and removal as we only need
 * to update the sibling list and eventually the first child pointer. But the
 * biggest advantage is that it allows fast iteration of a tree with 0 memory
 * allocations by only following pointers. See {@link DFSIterator}
 * </p>
 *
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
public class DTNode<V, E> {

    private final DTGraph<V, E> graph;

    private final V vertex;

    // the size of this subtree
    private int size;

    private DTNode<V, E> parent = null;
    private Edge<V, E> parentEdge = null;

    // the children of this node. They are stored in a doubly linked list
    // firstChild is the head of the linked list. previousSibling and nextSibling
    // are used to navigate the list.
    private DTNode<V, E> firstChild = null;
    private DTNode<V, E> previousSibling = null;
    private DTNode<V, E> nextSibling = null;

    private final Set<Edge<V, E>> nonTreeEdges = new HashSet<>();

    // index in the list of roots, valid only if this node is a root
    private int rootIndex;

    private ComponentView<V, E> componentView = null;

    DTNode(DTGraph<V, E> graph, V vertex) {
        this.graph = graph;
        this.vertex = vertex;
        this.size = 1;
    }

    /**
     * Make this DTNode the root of the tree in which it is. This is a two steps process
     * with an intermediate optional operation:
     * <ol>
     *     <li>Swap parent-child relationship for each DTNode from this dTNode to the original root</li>
     *     <li>Optionally, update the list of roots. For most cases, it must be {@code true}</li>
     *     <li>Update the subtree size attribute from the original root to the new root (this DTNode)</li>
     * </ol>
     *
     * @param updateRoots {@code true} to update the list of roots
     */
    public void makeRoot(boolean updateRoots) {
        if (parent == null) {
            return;
        }

        DTNode<V, E> child = this;
        DTNode<V, E> oldParent = child.parent;
        Edge<V, E> oldParentEdge = child.parentEdge;
        oldParent.removeChildUnchecked(child); // remove before making parentEdge null

        this.parent = null;
        this.parentEdge = null;

        // swap parent/child relation
        while (oldParent != null) {
            DTNode<V, E> greatParent = oldParent.parent;
            Edge<V, E> greatParentEdge = oldParent.parentEdge;

            // At this point:
            // - 'oldParent' is in the linked list of children of 'greatParent', and must be
            //   removed from it because adding 'oldParent' as a child of 'child' will break
            //   this linked list.
            // - 'child' is NOT in the linked list of children of 'oldParent'.
            //   It was removed by the last iteration or before entering in the loop (for the first iteration)
            // - the parent of 'oldParent' aka 'greatParent' should be changed to child
            if (greatParent != null) {
                greatParent.removeChildUnchecked(oldParent);
            }

            child.addChildUnchecked(oldParent);
            oldParent.parent = child;
            oldParent.parentEdge = oldParentEdge;

            // At this point:
            // - 'oldParent' isn't anymore is the linked list of child of 'greatParent'
            // - 'oldParent' is a child of 'child'

            // process to the next parent/child
            child = oldParent;
            oldParent = greatParent;
            oldParentEdge = greatParentEdge;
        }

        // child is the old root
        DTNode<V, E> oldRoot = child;

        // update the list of roots
        if (updateRoots) {
            graph.replaceRoot(oldRoot, this);
        }

        // update size attributes, going from oldRoot to this DTNode
        while (oldRoot.parent != null) {
            oldRoot.size -= oldRoot.parent.size;
            oldRoot.parent.size += oldRoot.size;
            oldRoot = oldRoot.parent;
        }
    }

    /**
     * Make this node a child of {@code parent} whose tree is rooted at {@code parentRoot}.
     *
     * @param parentRoot root of parent
     * @param parent     node that will become the parent of {@code this}.
     * @param edge       the edge linking {@code this} and {@code parent}
     * @return root of the merged tree. In some cases {@code parentRoot} won't be the final root
     * because of the restoration of the centroid property
     */
    public DTNode<V, E> link(DTNode<V, E> parentRoot, DTNode<V, E> parent, Edge<V, E> edge) {
        // first: update parent/child relations
        parent.addChildUnchecked(this);
        this.parent = parent;
        this.parentEdge = edge;

        // next: update size attributes in the parent tree
        DTNode<V, E> newCentroid = null;
        DTNode<V, E> cur = parent;

        while (cur != null) {
            cur.size += this.size;

            if (newCentroid == null && cur != parentRoot && cur.size > (parentRoot.size + this.size) / 2) {
                // the new root is the first node in the path from parent to parentRoot
                // such that it contains more than half of the nodes in the merged
                // tree. This reduces the sum of distances.
                newCentroid = cur;
            }

            cur = cur.parent;
        }

        // eventually, change the root to a better one
        if (newCentroid != null) {
            newCentroid.makeRoot(true);
            return newCentroid;
        } else {
            return parentRoot;
        }
    }

    /**
     * Unlink this node from its parent, creating two trees, one
     * whose root is {@code this} and one whose root is returned and
     * was previously the root of the linked tree.
     *
     * @return the root of the other tree
     */
    public DTNode<V, E> unlink() {
        Objects.requireNonNull(parent);

        // first step: update size attribute in the parent tree
        DTNode<V, E> newTree = this;
        while (newTree.parent != null) {
            newTree = newTree.parent;
            newTree.size -= size;
        }

        // second step: update parent/child relations
        parent.removeChildUnchecked(this);
        parent = null;
        parentEdge = null;
        return newTree;
    }

    /**
     * Add child in the doubly linked list of children.
     * The child mustn't be in a linked list. No verification
     * is performed.
     *
     * @param child the child node to add
     */
    private void addChildUnchecked(DTNode<V, E> child) {
        DTNode<V, E> oldFirstChild = this.firstChild;

        firstChild = child;
        child.nextSibling = oldFirstChild;

        if (oldFirstChild != null) {
            oldFirstChild.previousSibling = child;
        }
    }

    /**
     * Remove child from the doubly linked list of children.
     * The child must be in the linked list. No verification
     * is performed.
     *
     * @param child the child node to remove
     */
    private void removeChildUnchecked(DTNode<V, E> child) {
        DTNode<V, E> prev = child.previousSibling;
        DTNode<V, E> next = child.nextSibling;

        child.previousSibling = null;
        child.nextSibling = null;

        if (prev != null) {
            prev.nextSibling = next;
        } else {
            // if there is no 'prev' node, that means
            // that 'child' was the first child, and
            // we need to update it
            firstChild = next;
        }
        if (next != null) {
            next.previousSibling = prev;
        }
    }

    /**
     * Remove the tree edge between this node and its parent
     * and replace it by a non tree edge.
     *
     * @throws NullPointerException if this node is the root (no parent)
     */
    public void replaceParentLinkByNonTreeEdge() {
        parent.nonTreeEdges.add(parentEdge);
        nonTreeEdges.add(parentEdge);
        unlink();
    }

    public DTNode<V, E> findRoot() {
        DTNode<V, E> node = this;

        while (node.parent != null) {
            node = node.parent;
        }

        return node;
    }

    public DTNodeWithDepth<V, E> findRootWithDepth() {
        DTNode<V, E> node = this;
        int depth = 0;

        while (node.parent != null) {
            node = node.parent;
            depth++;
        }

        return new DTNodeWithDepth<>(node, depth);
    }

    public void addNonTreeEdge(Edge<V, E> edge) {
        nonTreeEdges.add(edge);
    }

    public void removeNonTreeEdge(Edge<V, E> edge) {
        nonTreeEdges.remove(edge);
    }

    public Component<V> componentView() {
        if (componentView == null) {
            componentView = new ComponentView<>(this);
        }

        return componentView;
    }

    public Set<E> getNeighborEdges() {
        Set<E> neighbor = new HashSet<>();

        if (parentEdge != null) {
            neighbor.add(parentEdge.edgeData());
        }

        for (Edge<V, E> nte : nonTreeEdges) {
            neighbor.add(nte.edgeData());
        }

        DTNode<V, E> child = firstChild;
        while (child != null) {
            neighbor.add(child.parentEdge.edgeData());
            child = child.nextSibling;
        }

        return neighbor;
    }

    public DTGraph<V, E> getGraph() {
        return graph;
    }

    public V getVertex() {
        return vertex;
    }

    public int size() {
        return size;
    }

    public DTNode<V, E> getParent() {
        return parent;
    }

    public Edge<V, E> getParentEdge() {
        return parentEdge;
    }

    public DTNode<V, E> getFirstChild() {
        return firstChild;
    }

    public DTNode<V, E> getPreviousSibling() {
        return previousSibling;
    }

    public DTNode<V, E> getNextSibling() {
        return nextSibling;
    }

    public Set<Edge<V, E>> getNonTreeEdges() {
        return nonTreeEdges;
    }

    void setIndex(int index) {
        this.rootIndex = index;
    }

    public int getIndex() {
        return rootIndex;
    }
}
