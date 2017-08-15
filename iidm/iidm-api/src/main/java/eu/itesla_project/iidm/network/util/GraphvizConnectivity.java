/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.util;

import com.google.common.collect.Iterables;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.TwoTerminalsConnectable;
import org.kohsuke.graphviz.Edge;
import org.kohsuke.graphviz.Graph;
import org.kohsuke.graphviz.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.StreamSupport;

/**
 * Example to generate a svg from the dot file:
 * sfdp -Goverlap=prism -Tsvg -o /tmp/a.svg  /tmp/a.dot
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GraphvizConnectivity {

    private static final String NEWLINE = "&#13;&#10;";

    private static final double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;

    private static String[] generateColorScale(Network network) {
        int maxCC = StreamSupport.stream(network.getBusView().getBuses().spliterator(), false).mapToInt(b -> b.getConnectedComponent().getNum()).max().getAsInt();
        String[] colors = new String[maxCC+1];
        Random ramdom = new Random();
        for (Bus bus : network.getBusView().getBuses()) {
            double h = ramdom.nextDouble();
            h += GOLDEN_RATIO_CONJUGATE;
            h %= 1;
            long[] rgb = hsvToRgb(h, 0.5, 0.95);
            String hex = String.format("#%02x%02x%02x", rgb[0], rgb[1], rgb[2]).toUpperCase();
            colors[bus.getConnectedComponent().getNum()] = hex;
        }
        return colors;
    }

    private static long[] hsvToRgb(double h, double s, double v) {
        int h_i = (int) Math.floor(h * 6);
        double f = h * 6 - h_i;
        double p = v * (1 - s);
        double q = v * (1 - f * s);
        double t = v * (1 - (1 - f) * s);
        double r, g, b;
        switch (h_i) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
                r = v;
                g = p;
                b = q;
                break;
            default:
                throw new AssertionError();
        }
        return new long[] { Math.round(r * 256), Math.round(g * 256), Math.round(b * 256) };
    }

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

    public void write(OutputStream os) throws IOException {
        Graph graph = new Graph().id("\"" +  network.getId() + "\"");
        String[] colors = generateColorScale(network);
        Map<String, Node> nodes = new HashMap<>();
        for (Bus b : network.getBusView().getBuses()) {
            long load = Math.round(StreamSupport.stream(b.getLoads().spliterator(), false).mapToDouble(l -> l.getP0()).sum());
            long maxGeneration = Math.round(StreamSupport.stream(b.getGenerators().spliterator(), false).mapToDouble(l -> l.getMaxP()).sum());
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
        for (TwoTerminalsConnectable branch : Iterables.concat(network.getLines(), network.getTwoWindingsTransformers())) {
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
