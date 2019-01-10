/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import com.powsybl.loadflow.resultscompletion.z0flows.kruskal.Bag;
import com.powsybl.loadflow.resultscompletion.z0flows.kruskal.Edge;

public class EdgeWeightedGraph {
    private static final String NEWLINE = System.getProperty("line.separator");

    private final int nV;
    private int nE;
    private Bag<Edge>[] adj;

    /**
     * Initializes an empty edge-weighted graph with {@code V} vertices and 0 edges.
     *
     * @param nV the number of vertices
     * @throws IllegalArgumentException if {@code V < 0}
     */
    public EdgeWeightedGraph(int nV) {
        if (nV < 0) {
            throw new IllegalArgumentException("Number of vertices must be nonnegative");
        }
        this.nV = nV;
        this.nE = 0;
        adj = (Bag<Edge>[]) new Bag[nV];
        for (int v = 0; v < nV; v++) {
            adj[v] = new Bag<Edge>();
        }
    }

    /**
     * Returns the number of vertices in this edge-weighted graph.
     *
     * @return the number of vertices in this edge-weighted graph
     */
    public int nV() {
        return nV;
    }

    /**
     * Returns the number of edges in this edge-weighted graph.
     *
     * @return the number of edges in this edge-weighted graph
     */
    public int nE() {
        return nE;
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        if (v < 0 || v >= nV) {
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (nV - 1));
        }
    }

    /**
     * Adds the undirected edge {@code e} to this edge-weighted graph.
     *
     * @param e the edge
     * @throws IllegalArgumentException unless both endpoints are between {@code 0}
     *                                  and {@code V-1}
     */
    public void addEdge(Edge e) {
        int v = e.either();
        int w = e.other(v);
        validateVertex(v);
        validateVertex(w);
        adj[v].add(e);
        adj[w].add(e);
        nE++;
    }

    /**
     * Returns the edges incident on vertex {@code v}.
     *
     * @param v the vertex
     * @return the edges incident on vertex {@code v} as an Iterable
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public Iterable<Edge> adj(int v) {
        validateVertex(v);
        return adj[v];
    }

    /**
     * Returns the degree of vertex {@code v}.
     *
     * @param v the vertex
     * @return the degree of vertex {@code v}
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public int degree(int v) {
        validateVertex(v);
        return adj[v].size();
    }

    /**
     * Returns all edges in this edge-weighted graph. To iterate over the edges in
     * this edge-weighted graph, use foreach notation:
     * {@code for (Edge e : G.edges())}.
     *
     * @return all edges in this edge-weighted graph, as an iterable
     */
    public Iterable<Edge> edges() {
        Bag<Edge> list = new Bag<Edge>();
        for (int v = 0; v < nV; v++) {
            int selfLoops = 0;
            for (Edge e : adj(v)) {
                if (e.other(v) > v) {
                    list.add(e);
                } else if (e.other(v) == v) {
                    if (selfLoops % 2 == 0) {
                        list.add(e);
                    }
                    selfLoops++;
                }
            }
        }
        return list;
    }

    /**
     * Returns a string representation of the edge-weighted graph. This method takes
     * time proportional to <em>E</em> + <em>V</em>.
     *
     * @return the number of vertices <em>V</em>, followed by the number of edges
     *         <em>E</em>, followed by the <em>V</em> adjacency lists of edges
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(nV + " " + nE + NEWLINE);
        for (int v = 0; v < nV; v++) {
            s.append(v + ": ");
            for (Edge e : adj[v]) {
                s.append(e + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }
}
