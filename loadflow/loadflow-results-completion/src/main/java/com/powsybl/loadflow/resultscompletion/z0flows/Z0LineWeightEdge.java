package com.powsybl.loadflow.resultscompletion.z0flows;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.powsybl.iidm.network.Line;

public class Z0LineWeightEdge extends DefaultWeightedEdge {

    private Line line;

    public Z0LineWeightEdge(Line line) {
        super();
        this.line = line;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }
    
    @Override
    protected double getWeight()
    {
        return line.getX();
    }
}
