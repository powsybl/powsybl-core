/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.utils;

import com.google.re2j.Matcher;
import org.jgrapht.Graph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.BaseExporter;
import org.jgrapht.nio.ExportException;
import org.jgrapht.nio.GraphExporter;
import org.jgrapht.nio.IntegerIdProvider;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jgrapht.nio.dot.DOTExporter.DEFAULT_GRAPH_ID;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class IidmDOTExporter<V, E> extends BaseExporter<V, E> implements GraphExporter<V, E> {

    protected Supplier<Map<String, Subgraph<V>>> subgraphProvider;

    private final Map<V, String> validatedIds;

    /**
     * Constructs a new DOTExporter object with an integer id provider.
     */
    public IidmDOTExporter() {
        this(new IntegerIdProvider<>());
    }

    /**
     * Constructs a new DOTExporter object with the given id provider. Additional providers such as
     * attributes can be given using the appropriate setter methods.
     *
     * @param vertexIdProvider for generating vertex IDs. Must not be null.
     */
    public IidmDOTExporter(Function<V, String> vertexIdProvider) {
        super(vertexIdProvider);
        this.subgraphProvider = null;
        this.validatedIds = new HashMap<>();
    }

    private static String escapeDoubleQuotes(String labelName) {
        return labelName.replaceAll("\"", Matcher.quoteReplacement("\\\""));
    }

    /**
     * Get the subgraph provider
     *
     * @return the vertex attribute provider as an {@link Optional}
     */
    public Optional<Supplier<Map<String, Subgraph<V>>>> getSubgraphProvider() {
        return subgraphProvider != null ? Optional.of(subgraphProvider) : Optional.empty();
    }

    /**
     * Set the subgraph provider
     *
     * @param subgraphProvider the vertex attribute provider
     */
    public void setSubgraphProvider(Supplier<Map<String, Subgraph<V>>> subgraphProvider) {
        this.subgraphProvider = subgraphProvider;
    }

    /**
     * Exports a graph into a plain text file in DOT format.
     *
     * @param g      the graph to be exported
     * @param writer the writer to which the graph to be exported
     */
    @Override
    public void exportGraph(Graph<V, E> g, Writer writer) {
        PrintWriter out = new PrintWriter(writer);

        out.println(computeHeader(g));

        // graph attributes
        for (Map.Entry<String, Attribute> attr : graphAttributeProvider.orElse(Collections::emptyMap).get().entrySet()) {
            out.print(IidmDOTUtils.INDENT);
            out.print(attr.getKey());
            out.print('=');
            out.print(attr.getValue());
            out.println(";");
        }

        // vertex set
        for (V v : g.vertexSet()) {
            out.print(IidmDOTUtils.INDENT);
            out.print(getVertexID(v));

            getVertexAttributes(v).ifPresent(m -> {
                renderEdgesAndVerticesAttributes(out, m);
            });

            out.println(";");
        }

        String connector = computeConnector(g);

        // edge set
        for (E e : g.edgeSet()) {
            String source = getVertexID(g.getEdgeSource(e));
            String target = getVertexID(g.getEdgeTarget(e));

            out.print(IidmDOTUtils.INDENT);
            out.print(source);
            out.print(connector);
            out.print(target);

            getEdgeAttributes(e).ifPresent(m -> {
                renderEdgesAndVerticesAttributes(out, m);
            });

            out.println(";");
        }

        // subgraphs
        for (Map.Entry<String, Subgraph<V>> subgraphEntry : getSubgraphProvider().orElse(Collections::emptyMap).get().entrySet()) {
            writeSubgraph(out, subgraphEntry.getKey(), subgraphEntry.getValue());
        }

        out.println(computeFooter(g));

        out.flush();
    }

    private void writeSubgraph(PrintWriter out, String subgraphName, Subgraph<V> subgraph) {
        out.println(IidmDOTUtils.INDENT + "subgraph " + subgraphName + " {");
        renderSubgraphAttributes(out, subgraphName, subgraph);
        for (V v : subgraph.getVertices()) {
            out.println(IidmDOTUtils.DOUBLE_INDENT + getVertexID(v) + ";");
        }
        out.println(IidmDOTUtils.INDENT + "}");
    }

    /**
     * Compute the header
     *
     * @param graph the graph
     * @return the header
     */
    private String computeHeader(Graph<V, E> graph) {
        StringBuilder headerBuilder = new StringBuilder();
        if (!graph.getType().isAllowingMultipleEdges()) {
            headerBuilder.append(IidmDOTUtils.DONT_ALLOW_MULTIPLE_EDGES_KEYWORD).append(" ");
        }
        if (graph.getType().isDirected()) {
            headerBuilder.append(IidmDOTUtils.DIRECTED_GRAPH_KEYWORD);
        } else {
            headerBuilder.append(IidmDOTUtils.UNDIRECTED_GRAPH_KEYWORD);
        }
        headerBuilder.append(" ").append(computeGraphId(graph)).append(" {");
        return headerBuilder.toString();
    }

    /**
     * Compute the footer
     *
     * @param graph the graph
     * @return the footer
     */
    private String computeFooter(Graph<V, E> graph) {
        return "}";
    }

    /**
     * Compute the connector
     *
     * @param graph the graph
     * @return the connector
     */
    private String computeConnector(Graph<V, E> graph) {
        StringBuilder connectorBuilder = new StringBuilder();
        if (graph.getType().isDirected()) {
            connectorBuilder.append(" ").append(IidmDOTUtils.DIRECTED_GRAPH_EDGEOP).append(" ");
        } else {
            connectorBuilder.append(" ").append(IidmDOTUtils.UNDIRECTED_GRAPH_EDGEOP).append(" ");
        }
        return connectorBuilder.toString();
    }

    /**
     * Get the id of the graph.
     *
     * @param graph the graph
     * @return the graph id
     */
    private String computeGraphId(Graph<V, E> graph) {
        String graphId = getGraphId().orElse(DEFAULT_GRAPH_ID);
        if (!IidmDOTUtils.isValidID(graphId)) {
            throw new ExportException(
                "Generated graph ID '" + graphId
                    + "' is not valid with respect to the .dot language");
        }
        return graphId;
    }

    private void renderSubgraphAttributes(PrintWriter out, String subgraphName, Subgraph<V> subgraph) {
        Map<String, Attribute> subgraphAttributes = subgraph.getSubgraphAttributes();
        Map<String, Attribute> clusterAttributes = subgraph.getClusterAttributes();
        if (subgraphAttributes.isEmpty() && clusterAttributes.isEmpty()) {
            return;
        }
        out.print(IidmDOTUtils.DOUBLE_INDENT + subgraphName + " [ ");
        for (Map.Entry<String, Attribute> entry : clusterAttributes.entrySet()) {
            String name = entry.getKey();
            renderAttribute(out, name, entry.getValue());
        }
        out.println("];");
        for (Map.Entry<String, Attribute> attr : subgraph.getSubgraphAttributes().entrySet()) {
            out.print(IidmDOTUtils.DOUBLE_INDENT);
            out.print(attr.getKey());
            out.print('=');
            out.print(attr.getValue());
            out.println(";");
        }
    }

    private void renderEdgesAndVerticesAttributes(PrintWriter out, Map<String, Attribute> attributes) {
        if (attributes == null) {
            return;
        }
        renderAttributes(out, attributes);
    }

    private void renderAttributes(PrintWriter out, Map<String, Attribute> attributes) {
        out.print(" [ ");
        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            String name = entry.getKey();
            renderAttribute(out, name, entry.getValue());
        }
        out.print("]");
    }

    private void renderAttribute(PrintWriter out, String attrName, Attribute attribute) {
        out.print(attrName + "=");
        final String attrValue = attribute.getValue();
        if (AttributeType.HTML.equals(attribute.getType())) {
            out.print("<" + attrValue + ">");
        } else if (AttributeType.IDENTIFIER.equals(attribute.getType())) {
            out.print(attrValue);
        } else {
            out.print("\"" + escapeDoubleQuotes(attrValue) + "\"");
        }
        out.print(" ");
    }

    /**
     * Return a valid vertex ID (with respect to the .dot language definition as described in
     * http://www.graphviz.org/doc/info/lang.html
     *
     * <p>
     * Quoted from above mentioned source: An ID is valid if it meets one of the following criteria:
     *
     * <ul>
     * <li>any string of alphabetic characters, underscores or digits, not beginning with a digit;
     * <li>a number [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? );
     * <li>any double-quoted string ("...") possibly containing escaped quotes (\");
     * <li>an HTML string (<...>).
     * </ul>
     *
     * @throws ExportException if the given <code>vertexIDProvider</code> didn't generate a valid
     *                         vertex ID.
     */
    private String getVertexID(V v) {
        String vertexId = validatedIds.get(v);
        if (vertexId == null) {
            /*
             * use the associated id provider for an ID of the given vertex
             */
            vertexId = getVertexId(v);

            /*
             * test if it is a valid ID
             */
            if (!IidmDOTUtils.isValidID(vertexId)) {
                throw new ExportException(
                    "Generated id '" + vertexId + "'for vertex '" + v
                        + "' is not valid with respect to the .dot language");
            }

            validatedIds.put(v, vertexId);
        }
        return vertexId;
    }
}
