/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.primitives.Ints;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.util.Colors;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.impl.utils.IidmDOTExporter;
import com.powsybl.iidm.network.impl.utils.Subgraph;
import com.powsybl.iidm.network.util.ShortIdDictionary;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractTopologyModel implements TopologyModel {

    public static final int DEFAULT_NODE_INDEX_LIMIT = 1000;

    public static final int NODE_INDEX_LIMIT = loadNodeIndexLimit(PlatformConfig.defaultConfig());

    protected final VoltageLevelExt voltageLevel;

    protected AbstractTopologyModel(VoltageLevelExt voltageLevel) {
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
    }

    protected static int loadNodeIndexLimit(PlatformConfig platformConfig) {
        return platformConfig
                .getOptionalModuleConfig("iidm")
                .map(moduleConfig -> moduleConfig.getIntProperty("node-index-limit", DEFAULT_NODE_INDEX_LIMIT))
                .orElse(DEFAULT_NODE_INDEX_LIMIT);
    }

    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    protected static void addNextTerminals(TerminalExt otherTerminal, List<TerminalExt> nextTerminals) {
        Objects.requireNonNull(otherTerminal);
        Objects.requireNonNull(nextTerminals);
        Connectable<?> otherConnectable = otherTerminal.getConnectable();
        if (otherConnectable instanceof Branch<?> branch) {
            if (branch.getTerminal1() == otherTerminal) {
                nextTerminals.add((TerminalExt) branch.getTerminal2());
            } else if (branch.getTerminal2() == otherTerminal) {
                nextTerminals.add((TerminalExt) branch.getTerminal1());
            } else {
                throw new IllegalStateException();
            }
        } else if (otherConnectable instanceof ThreeWindingsTransformer ttc) {
            if (ttc.getLeg1().getTerminal() == otherTerminal) {
                nextTerminals.add((TerminalExt) ttc.getLeg2().getTerminal());
                nextTerminals.add((TerminalExt) ttc.getLeg3().getTerminal());
            } else if (ttc.getLeg2().getTerminal() == otherTerminal) {
                nextTerminals.add((TerminalExt) ttc.getLeg1().getTerminal());
                nextTerminals.add((TerminalExt) ttc.getLeg3().getTerminal());
            } else if (ttc.getLeg3().getTerminal() == otherTerminal) {
                nextTerminals.add((TerminalExt) ttc.getLeg1().getTerminal());
                nextTerminals.add((TerminalExt) ttc.getLeg2().getTerminal());
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public void invalidateCache() {
        invalidateCache(false);
    }

    public abstract Iterable<Terminal> getTerminals();

    public abstract Stream<Terminal> getTerminalStream();

    public <T extends Connectable> Iterable<T> getConnectables(Class<T> clazz) {
        Iterable<Terminal> terminals = getTerminals();
        return FluentIterable.from(terminals)
                .transform(Terminal::getConnectable)
                .filter(clazz)
                .toSet();
    }

    public <T extends Connectable> Stream<T> getConnectableStream(Class<T> clazz) {
        return getTerminalStream()
                .map(Terminal::getConnectable)
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .distinct();
    }

    public <T extends Connectable> int getConnectableCount(Class<T> clazz) {
        return Ints.checkedCast(getConnectableStream(clazz).count());
    }

    public Iterable<Connectable> getConnectables() {
        return FluentIterable.from(getTerminals())
                .transform(Terminal::getConnectable)
                .toSet();
    }

    public Stream<Connectable> getConnectableStream() {
        return getTerminalStream()
                .map(Terminal::getConnectable)
                .distinct();
    }

    public abstract VoltageLevelExt.NodeBreakerViewExt getNodeBreakerView();

    public abstract VoltageLevelExt.BusBreakerViewExt getBusBreakerView();

    public abstract VoltageLevelExt.BusViewExt getBusView();

    public abstract Iterable<Switch> getSwitches();

    public abstract int getSwitchCount();

    public abstract TopologyKind getTopologyKind();

    public abstract void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex);

    public abstract void reduceVariantArraySize(int number);

    public abstract void deleteVariantArrayElement(int index);

    public abstract void allocateVariantArrayElement(int[] indexes, int sourceIndex);

    protected abstract void removeTopology();

    public abstract void printTopology();

    public abstract void printTopology(PrintStream out, ShortIdDictionary dict);

    public abstract void exportTopology(Path file) throws IOException;

    public abstract void exportTopology(Writer writer);

    public void exportTopology(Writer writer, Random random) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(random);

        // Initialize the JgraphT graph and the attributes, nodes and edges map
        Graph<String, DefaultEdge> jGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        Map<String, Attribute> graphAttributes = new HashMap<>();
        Map<String, Map<String, Attribute>> vertexAttributes = new HashMap<>();
        Map<DefaultEdge, Map<String, Attribute>> edgeAttributes = new HashMap<>();
        Map<String, Subgraph<String>> subgraphs = new HashMap<>();

        // Compute the attributes, nodes and edges
        graphAttributes.put("compound", DefaultAttribute.createAttribute("true"));
        exportVertices(vertexAttributes, edgeAttributes, random, jGraph, subgraphs);
        exportEdges(edgeAttributes, jGraph);

        // Set the exporter
        IidmDOTExporter<String, DefaultEdge> exporter = new IidmDOTExporter<>(v -> v);
        exporter.setGraphAttributeProvider(() -> graphAttributes);
        exporter.setVertexAttributeProvider(vertexAttributes::get);
        exporter.setEdgeAttributeProvider(edgeAttributes::get);
        if (!subgraphs.isEmpty()) {
            exporter.setSubgraphProvider(() -> subgraphs);
        }

        exporter.exportGraph(jGraph, writer);
    }

    protected abstract void exportVertices(Map<String, Map<String, Attribute>> vertexAttributes,
                                           Map<DefaultEdge, Map<String, Attribute>> edgeAttributes,
                                           Random random,
                                           Graph<String, DefaultEdge> jGraph,
                                           Map<String, Subgraph<String>> subgraphs);

    protected abstract void exportEdges(Map<DefaultEdge, Map<String, Attribute>> edgeAttributes,
                                        Graph<String, DefaultEdge> jGraph);

    protected Map<String, String> createBusColorScale(Random random, List<String> busIds) {
        Map<String, String> busColor = new HashMap<>();
        String[] colors = Colors.generateColorScale(busIds.size(), random);
        for (int i = 0; i < busIds.size(); i++) {
            busColor.put(busIds.get(i), colors[i]);
        }
        return busColor;
    }
}
