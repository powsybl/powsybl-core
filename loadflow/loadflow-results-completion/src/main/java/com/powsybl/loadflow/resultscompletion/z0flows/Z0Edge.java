/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.powsybl.iidm.network.Line;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
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
