/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.util.Colors;
import com.powsybl.iidm.network.*;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

/**
 * Example to generate a svg from the dot file:
 * sfdp -Goverlap=prism -Tsvg -o /tmp/a.svg  /tmp/a.dot
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GraphvizConnectivity {

    private static final String NEWLINE = "&#13;&#10;";

    private final Network network;

    private final Random random;

    public GraphvizConnectivity(Network network) {
        this(network, new SecureRandom());
    }

    public GraphvizConnectivity(Network network, Random random) {
        this.network = Objects.requireNonNull(network);
        this.random = Objects.requireNonNull(random);
    }

    private static String getBusId(Bus bus) {
        return bus.getId().replace('-', '_').replace("=", "_");
    }

    public void write(Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            write(writer);
        }
    }

    public void write(Writer writer) {
        Objects.requireNonNull(writer);
        MutableGraph graph = mutGraph(network.getId());
        int maxCC = network.getBusView().getBusStream().mapToInt(b -> b.getConnectedComponent().getNum()).max().getAsInt();
        String[] colors = Colors.generateColorScale(maxCC + 1, random);
        Map<String, MutableNode> nodes = new HashMap<>();
        for (Bus b : network.getBusView().getBuses()) {
            long load = Math.round(b.getLoadStream().mapToDouble(Load::getP0).sum());
            long maxGeneration = Math.round(b.getGeneratorStream().mapToDouble(Generator::getMaxP).sum());
            String busId = getBusId(b);
            MutableNode n = mutNode(Label.of(busId))
                    .attrs()
                    .add(Shape.ELLIPSE)
                    .add(Style.FILLED)
                    .add(Font.size(10))
                    .add(Color.rgb(colors[b.getConnectedComponent().getNum()]).fill())
                    .add("tooltip", "load=" + load + "MW" + NEWLINE + "max generation=" + maxGeneration + "MW" + NEWLINE + "cc=" + b.getConnectedComponent().getNum());
            nodes.put(busId, n);
            graph.add(n);
        }
        for (Branch branch : network.getBranches()) {
            Bus b1 = branch.getTerminal1().getBusView().getBus();
            Bus b2 = branch.getTerminal2().getBusView().getBus();
            if (b1 != null && b2 != null) {
                MutableNode n1 = nodes.get(getBusId(b1));
                MutableNode n2 = nodes.get(getBusId(b2));
                n1.addLink(n1.linkTo(n2).with("tooltip", branch.getName()));
            }
        }
        try {
            writer.write(graph.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
