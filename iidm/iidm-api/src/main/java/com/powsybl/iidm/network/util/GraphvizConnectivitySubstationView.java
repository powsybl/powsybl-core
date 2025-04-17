/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;
import org.anarres.graphviz.builder.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Objects;

/* Note that this is very similar to GraphvizConnectivity,
 this could probably be factorized with an abstract class
 or an interface
 */
/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class GraphvizConnectivitySubstationView {
    private final Network network;

    public GraphvizConnectivitySubstationView(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    public void write(Writer writer) {
        Objects.requireNonNull(writer);
        GraphVizGraph graph = new GraphVizGraph();
        GraphVizScope scope = new GraphVizScope.Impl();

        for (Branch<?> branch : network.getBranches()) {
            VoltageLevel v1 = branch.getTerminal1().getVoltageLevel();
            VoltageLevel v2 = branch.getTerminal2().getVoltageLevel();
            if (v1 != null && v2 != null) {
                graph.edge(scope, v1.getId(), v2.getId());
            }
        }
        try {
            graph.writeTo(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
