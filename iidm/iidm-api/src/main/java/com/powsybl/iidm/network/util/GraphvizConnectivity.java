/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.util.Colors;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTSubgraph;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static com.powsybl.iidm.network.dot.IidmDOTUtils.FILL_COLOR;
import static com.powsybl.iidm.network.dot.IidmDOTUtils.FONT_SIZE;
import static com.powsybl.iidm.network.dot.IidmDOTUtils.LABEL;
import static com.powsybl.iidm.network.dot.IidmDOTUtils.LINE_SEPARATOR;
import static com.powsybl.iidm.network.dot.IidmDOTUtils.SHAPE;
import static com.powsybl.iidm.network.dot.IidmDOTUtils.STYLE;
import static com.powsybl.iidm.network.dot.IidmDOTUtils.TOOL_TIP;
import static com.powsybl.iidm.network.dot.IidmDOTUtils.exportGraph;
import static com.powsybl.iidm.network.dot.Subgraph.DEFAULT_CLUSTER_ATTRIBUTES;

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

    private final Map<String, String> busIdToVertexIdMap = new HashMap<>();

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
        Map<String, Attribute> graphAttributes = new HashMap<>();
        graphAttributes.put(LABEL, DefaultAttribute.createAttribute(network.getId()));
        exportGraph(writer, random, this::exportVertices, this::exportEdges, graphAttributes);
    }

    private void exportVertices(Map<String, Map<String, Attribute>> verticesAttributes,
                                  Map<DefaultEdge, Map<String, Attribute>> edgeAttributes,
                                  Random random,
                                  Graph<String, DefaultEdge> jGraph,
                                  Map<String, DOTSubgraph<String, DefaultEdge>> subgraphs) {
        // create bus color scale
        int maxCC = network.getBusView().getBusStream().mapToInt(b -> b.getConnectedComponent().getNum()).max().orElseThrow();
        String[] colors = Colors.generateColorScale(maxCC + 1, random);
        int nextVertexId = 0;

        for (Bus b : network.getBusView().getBuses()) {
            long load = Math.round(b.getLoadStream().mapToDouble(Load::getP0).sum());
            long maxGeneration = Math.round(b.getGeneratorStream().mapToDouble(Generator::getMaxP).sum());
            String busId = getBusId(b);
            String busVertexId = String.valueOf(nextVertexId++);
            busIdToVertexIdMap.put(busId, busVertexId);
            String tooltip = "load=" + load + "MW" + NEWLINE + "max generation=" + maxGeneration + "MW" + NEWLINE + "cc=" + b.getConnectedComponent().getNum();

            jGraph.addVertex(busVertexId);

            Map<String, Attribute> vertexAttributes = new LinkedHashMap<>();
            vertexAttributes.put(LABEL, DefaultAttribute.createAttribute(busId));
            vertexAttributes.put(SHAPE, DefaultAttribute.createAttribute("ellipse"));
            vertexAttributes.put(STYLE, DefaultAttribute.createAttribute("filled"));
            vertexAttributes.put(FONT_SIZE, DefaultAttribute.createAttribute("10"));
            vertexAttributes.put(FILL_COLOR, DefaultAttribute.createAttribute(colors[b.getConnectedComponent().getNum()]));
            vertexAttributes.put(TOOL_TIP, DefaultAttribute.createAttribute(tooltip));
            if (countryCluster) {
                b.getVoltageLevel()
                    .getSubstation()
                    .flatMap(Substation::getCountry)
                    .ifPresent(country -> {
                        String subgraphId = "cluster_" + country.name();
                        if (!subgraphs.containsKey(subgraphId)) {
                            subgraphs.put(subgraphId,
                                new DOTSubgraph<>(new AsSubgraph<>(jGraph, Set.of(), Set.of()), Map.of(
                                    LABEL, DefaultAttribute.createAttribute(country.name()),
                                    STYLE, DefaultAttribute.createAttribute("rounded")
                                ), DEFAULT_CLUSTER_ATTRIBUTES, true, false));
                        }
                        subgraphs.get(subgraphId).getSubgraph().addVertex(busVertexId);
                    });
            }
            verticesAttributes.put(busVertexId, vertexAttributes);
        }
    }

    private void exportEdges(Map<DefaultEdge, Map<String, Attribute>> edgesAttributes,
                             Graph<String, DefaultEdge> jGraph) {
        for (Branch<?> branch : network.getBranches()) {
            Bus b1 = branch.getTerminal1().getBusView().getBus();
            Bus b2 = branch.getTerminal2().getBusView().getBus();
            if (b1 != null && b2 != null) {
                String bus1Id = busIdToVertexIdMap.get(getBusId(b1));
                String bus2Id = busIdToVertexIdMap.get(getBusId(b2));
                DefaultEdge edge = jGraph.getEdge(bus1Id, bus2Id);
                if (edge == null) {
                    // Create the edge
                    edge = jGraph.addEdge(bus1Id, bus2Id);
                    Map<String, Attribute> edgeAttributes = new LinkedHashMap<>();
                    edgeAttributes.put(LABEL, DefaultAttribute.createAttribute(branch.getId()));
                    edgesAttributes.put(edge, edgeAttributes);
                } else {
                    // Update the label
                    Map<String, Attribute> edgeAttributes = edgesAttributes.get(edge);
                    edgeAttributes.put(LABEL, DefaultAttribute.createAttribute(edgeAttributes.get(LABEL) + LINE_SEPARATOR + branch.getId()));
                }
            }
        }
    }
}
