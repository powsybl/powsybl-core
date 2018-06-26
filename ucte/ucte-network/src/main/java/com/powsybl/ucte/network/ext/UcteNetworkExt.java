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
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
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

    private UndirectedGraph<UcteNodeCode, Object> createSubstationGraph(UcteNetwork network) {
        UndirectedGraph<UcteNodeCode, Object> graph = new Pseudograph<>(Object.class);
        for (UcteNode node : network.getNodes()) {
            graph.addVertex(node.getCode());
        }

        // in the same substation...
        addEdgeBetweenSameGeographicalSpotNodes(network, graph);
        addEdgeBetweenTransformers(network, graph);
        addEdgeForCouplerOrLowImpedanceLine(network, graph);

        return graph;
    }

    private void addEdgeBetweenSameGeographicalSpotNodes(UcteNetwork network, UndirectedGraph<UcteNodeCode, Object> graph) {
        // ...nodes with same geographical spot
        Multimap<String, UcteNode> nodesByGeographicalSpot = Multimaps.index(network.getNodes(), node -> node.getCode().getUcteCountryCode() + node.getCode().getGeographicalSpot());
        for (Map.Entry<String, Collection<UcteNode>> entry : nodesByGeographicalSpot.asMap().entrySet()) {
            for (UcteNode n1 : entry.getValue()) {
                for (UcteNode n2 : entry.getValue()) {
                    if (n1 != n2) {
                        graph.addEdge(n1.getCode(), n2.getCode());
                    }
                }
            }
        }
    }

    private void addEdgeBetweenTransformers(UcteNetwork network, UndirectedGraph<UcteNodeCode, Object> graph) {
        // ...nodes connected by a transformer
        for (UcteTransformer tfo : network.getTransformers()) {
            UcteNodeCode nodeCode1 = tfo.getId().getNodeCode1();
            UcteNodeCode nodeCode2 = tfo.getId().getNodeCode2();
            graph.addEdge(nodeCode1, nodeCode2);
        }
    }

    private void addEdgeForCouplerOrLowImpedanceLine(UcteNetwork network, UndirectedGraph<UcteNodeCode, Object> graph) {
        // ...nodes connected by a coupler or by a low impedance line
        for (UcteLine l : network.getLines()) {
            UcteNodeCode nodeCode1 = l.getId().getNodeCode1();
            UcteNodeCode nodeCode2 = l.getId().getNodeCode2();
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

    private void updateSubstation() {
        if (substations == null) {
            LOGGER.trace("Update substations...");
            substations = new ArrayList<>();
            node2voltageLevel = new HashMap<>();
            UndirectedGraph<UcteNodeCode, Object> graph = createSubstationGraph(network);
            for (Set<UcteNodeCode> substationNodes : new ConnectivityInspector<>(graph).connectedSets()) {
                // the main node of the substation is not an xnode and the one with the highest voltage
                // level and the lowest busbar number.
                UcteNodeCode mainNode = substationNodes.stream()
                        .sorted((nodeCode1, nodeCode2) -> {
                            if (nodeCode1.getUcteCountryCode() == UcteCountryCode.XX &&
                                    nodeCode2.getUcteCountryCode() != UcteCountryCode.XX) {
                                return 1;
                            } else if (nodeCode1.getUcteCountryCode() != UcteCountryCode.XX &&
                                    nodeCode2.getUcteCountryCode() == UcteCountryCode.XX) {
                                return -1;
                            } else {
                                return compareVoltageLevelThenBusbar(nodeCode1, nodeCode2);
                            }
                        })
                        .findFirst()
                        .orElseThrow(AssertionError::new);

                Multimap<UcteVoltageLevelCode, UcteNodeCode> nodesByVoltageLevel
                        = Multimaps.index(substationNodes, UcteNodeCode::getVoltageLevelCode);

                String substationName = mainNode.getUcteCountryCode().getUcteCode() + mainNode.getGeographicalSpot();
                List<UcteVoltageLevel> voltageLevels = new ArrayList<>();
                UcteSubstation substation = new UcteSubstation(substationName, voltageLevels);
                substations.add(substation);

                LOGGER.trace("Define substation {}", substationName);

                for (Map.Entry<UcteVoltageLevelCode, Collection<UcteNodeCode>> entry : nodesByVoltageLevel.asMap().entrySet()) {
                    UcteVoltageLevelCode vlc = entry.getKey();
                    Collection<UcteNodeCode> voltageLevelNodes = entry.getValue();
                    String voltageLevelName = mainNode.getUcteCountryCode().getUcteCode() + mainNode.getGeographicalSpot() + vlc.ordinal();
                    UcteVoltageLevel voltageLevel = new UcteVoltageLevel(voltageLevelName, substation, voltageLevelNodes);
                    voltageLevels.add(voltageLevel);
                    voltageLevelNodes.forEach(voltageLevelNode -> node2voltageLevel.put(voltageLevelNode, voltageLevel));

                    LOGGER.trace("Define voltage level {} as a group of {} nodes", voltageLevelName, voltageLevelNodes);
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
