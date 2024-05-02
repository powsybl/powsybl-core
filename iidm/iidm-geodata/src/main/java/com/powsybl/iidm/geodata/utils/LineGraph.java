package com.powsybl.iidm.geodata.utils;

import org.jgrapht.graph.Pseudograph;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */
public class LineGraph<V, E> extends Pseudograph<V, E> {
    public LineGraph(Class<? extends E> edgeClass) {
        super(edgeClass);
    }

    public void addVerticesAndEdges(Iterable<V> vertices) {
        V previousVertex = null;
        for (V vertex : vertices) {
            if (!containsVertex(vertex)) {
                addVertex(vertex);
            }
            if (previousVertex != null) {
                addEdge(previousVertex, vertex);
            }
            previousVertex = vertex;
        }
    }
}

