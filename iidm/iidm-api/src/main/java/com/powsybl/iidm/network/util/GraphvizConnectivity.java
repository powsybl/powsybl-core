/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.util.Colors;
import com.powsybl.iidm.network.*;
import org.anarres.graphviz.builder.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Example to generate a svg from the dot file:
 * sfdp -Goverlap=prism -Tsvg -o /tmp/a.svg  /tmp/a.dot
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class GraphvizConnectivity {

    private static final String NEWLINE = "&#13;&#10;";

    private final Network network;

    private final Random random;

    private boolean countryCluster = false;

    public GraphvizConnectivity(Network network) {
        this(network, new SecureRandom());
    }

    public GraphvizConnectivity(Network network, Random random) {
        this.network = Objects.requireNonNull(network);
        this.random = Objects.requireNonNull(random);
    }

    public GraphvizConnectivity setCountryCluster(boolean countryCluster) {
        this.countryCluster = countryCluster;
        return this;
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
        GraphVizGraph graph = new GraphVizGraph().label(network.getId());
        GraphVizScope scope = new GraphVizScope.Impl();
        int maxCC = network.getBusView().getBusStream().mapToInt(b -> b.getConnectedComponent().getNum()).max().getAsInt();
        String[] colors = Colors.generateColorScale(maxCC + 1, random);
        for (Bus b : network.getBusView().getBuses()) {
            long load = Math.round(b.getLoadStream().mapToDouble(Load::getP0).sum());
            long maxGeneration = Math.round(b.getGeneratorStream().mapToDouble(Generator::getMaxP).sum());
            String busId = getBusId(b);
            String tooltip = "load=" + load + "MW" + NEWLINE + "max generation=" + maxGeneration + "MW" + NEWLINE + "cc=" + b.getConnectedComponent().getNum();
            GraphVizNode node = graph.node(scope, busId).label(busId)
                    .attr(GraphVizAttribute.shape, "ellipse")
                    .attr(GraphVizAttribute.style, "filled")
                    .attr(GraphVizAttribute.fontsize, "10")
                    .attr(GraphVizAttribute.fillcolor, colors[b.getConnectedComponent().getNum()])
                    .attr(GraphVizAttribute.tooltip, tooltip);
            if (countryCluster) {
                b.getVoltageLevel().getSubstation().flatMap(Substation::getCountry)
                        .ifPresent(country -> graph.cluster(scope, country)
                        .label(country.name())
                        .attr(GraphVizAttribute.style, "rounded")
                        .add(node));
            }
        }
        for (Branch<?> branch : network.getBranches()) {
            Bus b1 = branch.getTerminal1().getBusView().getBus();
            Bus b2 = branch.getTerminal2().getBusView().getBus();
            if (b1 != null && b2 != null) {
                GraphVizEdge edge = graph.edge(scope, getBusId(b1), getBusId(b2));
                // to workaround the multigraph lack of support, we add one line to the label per branch
                edge.label().append(branch.getId()).append(System.lineSeparator());
            }
        }
        try {
            graph.writeTo(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
