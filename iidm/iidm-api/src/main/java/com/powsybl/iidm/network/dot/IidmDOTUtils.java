/*
 * Copyright (c) 2025-2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.dot;

import com.powsybl.commons.util.Colors;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.dot.DOTSubgraph;

import java.io.Writer;
import java.util.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IidmDOTUtils {
    private IidmDOTUtils() {
        // Utility class
    }

    /** Line separator. */
    public static final String LINE_SEPARATOR = "\\l";
    /** Attributes */
    public static final String FILL_COLOR = "fillcolor";
    public static final String FONT_SIZE = "fontsize";
    public static final String LABEL = "label";
    public static final String SHAPE = "shape";
    public static final String STYLE = "style";
    public static final String TOOL_TIP = "tooltip";

    private static final Map<String, Attribute> DEFAULT_SUBGRAPH_ATTRIBUTES = Map.of(
        "pencolor", DefaultAttribute.createAttribute("transparent"));
    private static final Map<String, Attribute> DEFAULT_CLUSTER_ATTRIBUTES = initializeAttributes();

    public static Map<String, String> createBusColorScale(Random random, List<String> busIds) {
        Map<String, String> busColor = new HashMap<>();
        String[] colors = Colors.generateColorScale(busIds.size(), random);
        for (int i = 0; i < busIds.size(); i++) {
            busColor.put(busIds.get(i), colors[i]);
        }
        return busColor;
    }

    public static void exportGraph(Writer writer, Random random,
                                   VertexExporter vertexExporter,
                                   EdgeExporter edgeExporter,
                                   Map<String, Attribute> graphAttributes) {
        // Initialize the JgraphT graph and the attributes, nodes and edges map
        Graph<String, DefaultEdge> jGraph = new DirectedPseudograph<>(DefaultEdge.class);
        Map<String, Map<String, Attribute>> vertexAttributes = new HashMap<>();
        Map<DefaultEdge, Map<String, Attribute>> edgeAttributes = new HashMap<>();
        Map<String, DOTSubgraph<String, DefaultEdge>> subgraphs = new HashMap<>();

        // Compute the attributes, nodes and edges
        graphAttributes.put("compound", DefaultAttribute.createAttribute("true"));
        vertexExporter.exportVertices(vertexAttributes, edgeAttributes, random, jGraph, subgraphs);
        edgeExporter.exportEdges(edgeAttributes, jGraph);

        // Set the exporter
        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(v -> v);
        exporter.setGraphAttributeProvider(() -> graphAttributes);
        exporter.setVertexAttributeProvider(vertexAttributes::get);
        exporter.setEdgeAttributeProvider(edgeAttributes::get);
        if (!subgraphs.isEmpty()) {
            exporter.setSubgraphProvider(() -> subgraphs);
        }

        exporter.exportGraph(jGraph, writer);
    }

    public static Map<String, Attribute> getDefaultSubgraphAttributes() {
        return DEFAULT_SUBGRAPH_ATTRIBUTES;
    }

    public static Map<String, Attribute> getDefaultClusterAttributes() {
        return DEFAULT_CLUSTER_ATTRIBUTES;
    }

    private static Map<String, Attribute> initializeAttributes() {
        Map<String, Attribute> map = new LinkedHashMap<>();
        map.put(LABEL, DefaultAttribute.createAttribute(""));
        map.put(SHAPE, DefaultAttribute.createAttribute("point"));
        map.put(STYLE, DefaultAttribute.createAttribute("invis"));
        return Collections.unmodifiableMap(map);
    }

    @FunctionalInterface
    public interface VertexExporter {
        void exportVertices(
            Map<String, Map<String, Attribute>> vertexAttributes,
            Map<DefaultEdge, Map<String, Attribute>> edgeAttributes,
            Random random,
            Graph<String, DefaultEdge> jGraph,
            Map<String, DOTSubgraph<String, DefaultEdge>> subgraphs
        );
    }

    @FunctionalInterface
    public interface EdgeExporter {
        void exportEdges(
            Map<DefaultEdge, Map<String, Attribute>> edgeAttributes,
            Graph<String, DefaultEdge> jGraph
        );
    }
}
