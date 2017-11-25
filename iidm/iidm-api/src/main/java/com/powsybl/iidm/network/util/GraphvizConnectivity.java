/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.util.Colors;
import com.powsybl.iidm.network.*;
import org.kohsuke.graphviz.Edge;
import org.kohsuke.graphviz.Graph;
import org.kohsuke.graphviz.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Example to generate a svg from the dot file:
 * sfdp -Goverlap=prism -Tsvg -o /tmp/a.svg  /tmp/a.dot
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GraphvizConnectivity {

    private static final String NEWLINE = "&#13;&#10;";

    private final Network network;

    public GraphvizConnectivity(Network network) {
        this.network = network;
    }

    private static String getBusId(Bus bus) {
        return bus.getId().replace('-', '_').replace("=", "_");
    }

    public void write(Path file) throws IOException {
        try (OutputStream os = Files.newOutputStream(file)) {
            write(os);
        }
    }

    public void write(OutputStream os) {
        Graph graph = new Graph().id("\"" +  network.getId() + "\"");
        int maxCC = network.getBusView().getBusStream().mapToInt(b -> b.getConnectedComponent().getNum()).max().getAsInt();
        String[] colors = Colors.generateColorScale(maxCC + 1);
        Map<String, Node> nodes = new HashMap<>();
        for (Bus b : network.getBusView().getBuses()) {
            long load = Math.round(b.getLoadStream().mapToDouble(Load::getP0).sum());
            long maxGeneration = Math.round(b.getGeneratorStream().mapToDouble(Generator::getMaxP).sum());
            String busId = getBusId(b);
            Node n = new Node()
                    .id(busId)
                    .attr("shape", "oval")
                    .attr("style", "filled")
                    .attr("fontsize", "10")
                    .attr("fillcolor", colors[b.getConnectedComponent().getNum()])
                    .attr("tooltip", "load=" + load + "MW" + NEWLINE + "max generation=" + maxGeneration + "MW" + NEWLINE + "cc=" + b.getConnectedComponent().getNum());
            nodes.put(busId, n);
            graph.node(n);
        }
        for (Branch branch : network.getBranches()) {
            Bus b1 = branch.getTerminal1().getBusView().getBus();
            Bus b2 = branch.getTerminal2().getBusView().getBus();
            if (b1 != null && b2 != null) {
                Node n1 = nodes.get(getBusId(b1));
                Node n2 = nodes.get(getBusId(b2));
                graph.edge(new Edge(n1, n2).attr("tooltip", branch.getName()));
            }
        }
        graph.writeTo(os);
    }
}
