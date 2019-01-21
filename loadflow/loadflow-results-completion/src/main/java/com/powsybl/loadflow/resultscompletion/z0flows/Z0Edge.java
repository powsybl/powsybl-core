package com.powsybl.loadflow.resultscompletion.z0flows;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.powsybl.iidm.network.Line;

public class Z0Edge extends DefaultWeightedEdge {

    public Z0Edge(Line line) {
        super();
        this.line = line;
    }

    public Line getLine() {
        return line;
    }

    @Override
    protected double getWeight() {
        return line.getX();
    }

    private final Line line;
}
