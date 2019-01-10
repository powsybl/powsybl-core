/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows.kruskal;

public class Edge implements Comparable<Edge> {

    private final String lineId;
    private final int v;
    private final int w;
    private final double weight;

    /**
     * Initializes an edge between vertices {@code v} and {@code w} of the given
     * {@code weight}.
     *
     * @param v      one vertex
     * @param w      the other vertex
     * @param weight the weight of this edge
     * @throws IllegalArgumentException if either {@code v} or {@code w} is a
     *                                  negative integer
     * @throws IllegalArgumentException if {@code weight} is {@code NaN}
     */
    public Edge(String lineId, int v, int w, double weight) {
        if (v < 0) {
            throw new IllegalArgumentException("vertex index must be a nonnegative integer");
        }
        if (w < 0) {
            throw new IllegalArgumentException("vertex index must be a nonnegative integer");
        }
        if (Double.isNaN(weight)) {
            throw new IllegalArgumentException("Weight is NaN");
        }
        this.lineId = lineId;
        this.v = v;
        this.w = w;
        this.weight = weight;
    }

    public String getLineId() {
        return lineId;
    }

    /**
     * Returns the weight of this edge.
     *
     * @return the weight of this edge
     */
    public double weight() {
        return weight;
    }

    /**
     * Returns either endpoint of this edge.
     *
     * @return either endpoint of this edge
     */
    public int either() {
        return v;
    }

    /**
     * Returns the endpoint of this edge that is different from the given vertex.
     *
     * @param vertex one endpoint of this edge
     * @return the other endpoint of this edge
     * @throws IllegalArgumentException if the vertex is not one of the endpoints of
     *                                  this edge
     */
    public int other(int vertex) {
        if (vertex == v) {
            return w;
        } else if (vertex == w) {
            return v;
        } else {
            throw new IllegalArgumentException("Illegal endpoint");
        }
    }

    /**
     * Compares two edges by weight. Note that {@code compareTo()} is not consistent
     * with {@code equals()}, which uses the reference equality implementation
     * inherited from {@code Object}.
     *
     * @param that the other edge
     * @return a negative integer, zero, or positive integer depending on whether
     *         the weight of this is less than, equal to, or greater than the
     *         argument edge
     */
    @Override
    public int compareTo(Edge that) {
        return Double.compare(this.weight, that.weight);
    }

    /**
     * Returns a string representation of this edge.
     *
     * @return a string representation of this edge
     */
    public String toString() {
        return String.format("%d-%d %.12f", v, w, weight);
    }

}
