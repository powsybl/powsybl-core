/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows.kruskal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.loadflow.resultscompletion.z0flows.EdgeWeightedGraph;

public class KruskalMST {
    private static final double FLOATING_POINT_EPSILON = 1E-12;

    private double weight; // weight of MST
    private Queue<Edge> mst = new Queue<Edge>(); // edges in MST

    /**
     * Compute a minimum spanning tree (or forest) of an edge-weighted graph.
     *
     * @param eG the edge-weighted graph
     */
    public KruskalMST(EdgeWeightedGraph eG) {
        // more efficient to build heap by passing array of edges
        MinPQ<Edge> pq = new MinPQ<Edge>();
        for (Edge e : eG.edges()) {
            pq.insert(e);
        }

        // run greedy algorithm
        UF uf = new UF(eG.nV());
        while (!pq.isEmpty() && mst.size() < eG.nV() - 1) {
            Edge e = pq.delMin();
            int v = e.either();
            int w = e.other(v);
            if (!uf.connected(v, w)) { // v-w does not create a cycle
                uf.union(v, w); // merge v and w components
                mst.enqueue(e); // add edge e to mst
                weight += e.weight();
            }
        }

        // check optimality conditions
        assert check(eG);
    }

    /**
     * Returns the edges in a minimum spanning tree (or forest).
     *
     * @return the edges in a minimum spanning tree (or forest) as an iterable of
     *         edges
     */
    public Iterable<Edge> edges() {
        return mst;
    }

    /**
     * Returns the sum of the edge weights in a minimum spanning tree (or forest).
     *
     * @return the sum of the edge weights in a minimum spanning tree (or forest)
     */
    public double weight() {
        return weight;
    }

    // check optimality conditions (takes time proportional to E V lg* V)
    private boolean check(EdgeWeightedGraph eG) {

        // check total weight
        double total = 0.0;
        for (Edge e : edges()) {
            total += e.weight();
        }
        if (Math.abs(total - weight()) > FLOATING_POINT_EPSILON) {
            LOG.error("Weight of edges does not equal weight(): %f vs. %f\n", total, weight());
            return false;
        }

        // check that it is acyclic
        UF uf = new UF(eG.nV());
        for (Edge e : edges()) {
            int v = e.either();
            int w = e.other(v);
            if (uf.connected(v, w)) {
                LOG.error("Not a forest");
                return false;
            }
            uf.union(v, w);
        }

        // check that it is a spanning forest
        for (Edge e : eG.edges()) {
            int v = e.either();
            int w = e.other(v);
            if (!uf.connected(v, w)) {
                LOG.error("Not a spanning forest");
                return false;
            }
        }

        // check that it is a minimal spanning forest (cut optimality conditions)
        for (Edge e : edges()) {

            // all edges in MST except e
            uf = new UF(eG.nV());
            for (Edge f : mst) {
                int x = f.either();
                int y = f.other(x);
                if (f != e) {
                    uf.union(x, y);
                }
            }

            // check that e is min weight edge in crossing cut
            for (Edge f : eG.edges()) {
                int x = f.either();
                int y = f.other(x);
                if (!uf.connected(x, y)) {
                    if (f.weight() < e.weight()) {
                        LOG.error("Edge " + f + " violates cut optimality conditions");
                        return false;
                    }
                }
            }

        }

        return true;
    }

    private static final Logger LOG = LoggerFactory.getLogger(KruskalMST.class);
}
