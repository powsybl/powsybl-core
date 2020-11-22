/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.ext;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.powsybl.ucte.network.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteNetworkExt implements UcteNetwork {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteNetworkExt.class);

    private final UcteNetwork network;

    private final float lineMinZ;

    private List<UcteSubstation> substations;

    private Map<UcteNodeCode, UcteVoltageLevel> node2voltageLevel;

    public UcteNetworkExt(UcteNetwork network, float lineMinZ) {
        this.network = Objects.requireNonNull(network);
        this.lineMinZ = lineMinZ;
    }

    @Override
    public void setVersion(UcteFormatVersion version) {
        network.setVersion(version);
    }

    @Override
    public UcteFormatVersion getVersion() {
        return network.getVersion();
    }

    @Override
    public List<String> getComments() {
        return network.getComments();
    }

    @Override
    public void addNode(UcteNode node) {
        invalidateSubstations();
        network.addNode(node);
    }

    @Override
    public Collection<UcteNode> getNodes() {
        return network.getNodes();
    }

    @Override
    public UcteNode getNode(UcteNodeCode code) {
        return network.getNode(code);
    }

    @Override
    public void addLine(UcteLine line) {
        invalidateSubstations();
        network.addLine(line);
    }

    @Override
    public Collection<UcteLine> getLines() {
        return network.getLines();
    }

    @Override
    public UcteLine getLine(UcteElementId id) {
        return network.getLine(id);
    }

    @Override
    public void addTransformer(UcteTransformer transformer) {
        invalidateSubstations();
        network.addTransformer(transformer);
    }

    @Override
    public Collection<UcteTransformer> getTransformers() {
        return network.getTransformers();
    }

    @Override
    public UcteTransformer getTransformer(UcteElementId id) {
        return network.getTransformer(id);
    }

    @Override
    public void addRegulation(UcteRegulation regulation) {
        network.addRegulation(regulation);
    }

    @Override
    public Collection<UcteRegulation> getRegulations() {
        return network.getRegulations();
    }

    @Override
    public UcteRegulation getRegulation(UcteElementId transfoId) {
        return network.getRegulation(transfoId);
    }

    private Graph<UcteNodeCode, Object> createSubstationGraph(UcteNetwork network) {
        Graph<UcteNodeCode, Object> graph = new Pseudograph<>(Object.class);
        for (UcteNode node : network.getNodes()) {
            graph.addVertex(node.getCode());
        }

        // in the same substation...
        addEdgeBetweenSameGeographicalSpotNodes(network, Optional.empty(), graph);
        addEdgeBetweenTransformers(network, graph);
        addEdgeForCouplerOrLowImpedanceLine(network, Optional.empty(), graph);

        return graph;
    }

    private Graph<UcteNodeCode, Object> createVoltageLevelGraph(UcteNetwork network, Collection<UcteNodeCode> nodeCodes) {
        Graph<UcteNodeCode, Object> graph = new Pseudograph<>(Object.class);
        for (UcteNodeCode node : nodeCodes) {
            graph.addVertex(node);
        }

        // in the same VL...
        addEdgeBetweenSameGeographicalSpotNodes(network, Optional.of(nodeCodes.stream().collect(Collectors.toList())), graph);
        addEdgeForCouplerOrLowImpedanceLine(network, Optional.of(nodeCodes.stream().collect(Collectors.toSet())), graph);

        return graph;
    }

    private void addEdgeBetweenSameGeographicalSpotNodes(UcteNetwork network, Optional<Collection<UcteNodeCode>> nodeCodesSubset, Graph<UcteNodeCode, Object> graph) {
        // ...nodes with same geographical spot
        Collection<UcteNodeCode> nodeCodes = nodeCodesSubset.isPresent() ? nodeCodesSubset.get() : network.getNodes().stream().map(n -> n.getCode()).collect(Collectors.toList());
        Multimap<String, UcteNodeCode> nodesByGeographicalSpot = Multimaps.index(nodeCodes, nodeCode -> nodeCode.getUcteCountryCode().getUcteCode() + nodeCode.getGeographicalSpot());
        for (Map.Entry<String, Collection<UcteNodeCode>> entry : nodesByGeographicalSpot.asMap().entrySet()) {
            for (UcteNodeCode n1 : entry.getValue()) {
                for (UcteNodeCode n2 : entry.getValue()) {
                    if (n1 != n2) {
                        graph.addEdge(n1, n2);
                    }
                }
            }
        }
    }

    private void addEdgeBetweenTransformers(UcteNetwork network, Graph<UcteNodeCode, Object> graph) {
        // ...nodes connected by a transformer
        for (UcteTransformer tfo : network.getTransformers()) {
            UcteNodeCode nodeCode1 = tfo.getId().getNodeCode1();
            UcteNodeCode nodeCode2 = tfo.getId().getNodeCode2();
            graph.addEdge(nodeCode1, nodeCode2);
        }
    }

    private void addEdgeForCouplerOrLowImpedanceLine(UcteNetwork network, Optional<Set<UcteNodeCode>> nodeCodesSubset, Graph<UcteNodeCode, Object> graph) {
        // ...nodes connected by a coupler or by a low impedance line
        for (UcteLine l : network.getLines()) {
            UcteNodeCode nodeCode1 = l.getId().getNodeCode1();
            UcteNodeCode nodeCode2 = l.getId().getNodeCode2();
            if (nodeCodesSubset.isPresent() && !(nodeCodesSubset.get().contains(nodeCode1) && nodeCodesSubset.get().contains(nodeCode2))) {
                continue;
            }
            if (l.getStatus() == UcteElementStatus.BUSBAR_COUPLER_IN_OPERATION
                    || l.getStatus() == UcteElementStatus.BUSBAR_COUPLER_OUT_OF_OPERATION) {
                graph.addEdge(nodeCode1, nodeCode2);
            } else {
                double z = Math.hypot(l.getResistance(), l.getReactance());
                if (z < lineMinZ) {
                    graph.addEdge(nodeCode1, nodeCode2);
                }
            }
        }
    }

    private void invalidateSubstations() {
        substations = null;
        node2voltageLevel = null;
    }

    private int compareVoltageLevelThenBusbar(UcteNodeCode nodeCode1, UcteNodeCode nodeCode2) {
        int c = Integer.compare(nodeCode2.getVoltageLevelCode().getVoltageLevel(), nodeCode1.getVoltageLevelCode().getVoltageLevel());
        if (c == 0) {
            c = nodeCode2.getBusbar().compareTo(nodeCode1.getBusbar());
        }
        return c;
    }

    private UcteNodeCode getMainNode(Set<UcteNodeCode> nodes) {
        // the main node of the substation is not an xnode and the one with the highest voltage
        // level and the lowest busbar number.
        return nodes.stream()
                .sorted((nodeCode1, nodeCode2) -> {
                    if (nodeCode1.getUcteCountryCode() == UcteCountryCode.XX &&
                            nodeCode2.getUcteCountryCode() != UcteCountryCode.XX) {
                        return 1;
                    } else if (nodeCode1.getUcteCountryCode() != UcteCountryCode.XX &&
                            nodeCode2.getUcteCountryCode() == UcteCountryCode.XX) {
                        return -1;
                    } else {
                        int c = compareVoltageLevelThenBusbar(nodeCode1, nodeCode2);
                        return (c != 0) ? c : nodeCode2.compareTo(nodeCode1); // Alphabetical order to always have the same main node (invariant)
                    }
                })
                .findFirst()
                .orElseThrow(AssertionError::new);
    }

    private void updateSubstation() {
        if (substations == null) {
            LOGGER.trace("Update substations...");
            substations = new ArrayList<>();
            node2voltageLevel = new TreeMap<>();
            Graph<UcteNodeCode, Object> graph = createSubstationGraph(network);
            for (Set<UcteNodeCode> substationNodes : new ConnectivityInspector<>(graph).connectedSets()) {
                UcteNodeCode mainNode = getMainNode(substationNodes);

                Multimap<UcteVoltageLevelCode, UcteNodeCode> nodesByVoltageLevel
                        = Multimaps.index(substationNodes, UcteNodeCode::getVoltageLevelCode);

                String substationName = mainNode.getUcteCountryCode().getUcteCode() + mainNode.getGeographicalSpot();
                List<UcteVoltageLevel> voltageLevels = new ArrayList<>();
                UcteSubstation substation = new UcteSubstation(substationName, voltageLevels);
                substations.add(substation);

                LOGGER.trace("Define substation {}", substationName);

                for (Map.Entry<UcteVoltageLevelCode, Collection<UcteNodeCode>> entry : nodesByVoltageLevel.asMap().entrySet()) {
                    Graph<UcteNodeCode, Object> graphVl = createVoltageLevelGraph(network, entry.getValue());
                    for (Set<UcteNodeCode> voltageLevelNodes : new ConnectivityInspector<>(graphVl).connectedSets()) {
                        UcteVoltageLevelCode vlc = entry.getKey();
                        UcteNodeCode mainVlNode = getMainNode(voltageLevelNodes);
                        String voltageLevelName = mainVlNode.getUcteCountryCode().getUcteCode() + mainVlNode.getGeographicalSpot() + vlc.ordinal();
                        UcteVoltageLevel voltageLevel = new UcteVoltageLevel(voltageLevelName, substation, voltageLevelNodes);
                        voltageLevels.add(voltageLevel);
                        voltageLevelNodes.forEach(voltageLevelNode -> node2voltageLevel.put(voltageLevelNode, voltageLevel));

                        LOGGER.trace("Define voltage level {} as a group of {} nodes", voltageLevelName, voltageLevelNodes);
                    }
                }
            }
        }
    }

    public Collection<UcteSubstation> getSubstations() {
        updateSubstation();
        return substations;
    }

    public UcteVoltageLevel getVoltageLevel(UcteNodeCode code) {
        updateSubstation();
        return node2voltageLevel.get(code);
    }

    @Override
    public void fix() {
        network.fix();
    }

}
