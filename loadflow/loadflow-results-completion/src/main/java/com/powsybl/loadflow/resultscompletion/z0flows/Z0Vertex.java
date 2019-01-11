package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.List;

import com.powsybl.iidm.network.Bus;

public class Z0Vertex {

    private Bus       bus;
    private Bus       parent;
    private String    lineIdParent;
    private int       level;
    private List<Bus> children;

    public Z0Vertex(Bus bus) {
        super();
        this.bus = bus;
    }
}
