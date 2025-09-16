/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.ref.RefChain;
import com.powsybl.iidm.network.DcBus;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.math.graph.UndirectedGraphImpl;
import com.powsybl.math.graph.UndirectedGraphListener;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcTopologyModel implements MultiVariantObject {

    public static final int DEFAULT_DC_NODE_INDEX_LIMIT = 1000;
    private static final String NOT_FOUND_IN_THE_NETWORK = "' not found in the network";

    public static final int DC_NODE_INDEX_LIMIT = loadDcNodeIndexLimit(PlatformConfig.defaultConfig());

    private Ref<NetworkImpl> networkRef;
    private Ref<SubnetworkImpl> subnetworkRef;

    private final UndirectedGraphImpl<DcNodeImpl, DcSwitchImpl> graph = new UndirectedGraphImpl<>(DC_NODE_INDEX_LIMIT);

    /* DcNodes indexed by vertex number */
    private final Map<String, Integer> dcNodes = new HashMap<>();

    /* DcSwitches indexed by edge number */
    private final Map<String, Integer> dcSwitches = new HashMap<>();

    private final VariantArray<DcTopologyModel.VariantImpl> variants;

    public DcTopologyModel(RefChain<NetworkImpl> networkRef, RefChain<SubnetworkImpl> subnetworkRef) {
        this.networkRef = Objects.requireNonNull(networkRef);
        this.subnetworkRef = Objects.requireNonNull(subnetworkRef);
        // the ref object of the variant array is the same as the current object
        variants = new VariantArray<>(networkRef, DcTopologyModel.VariantImpl::new);
        // invalidate topology and connected components
        graph.addListener(new UndirectedGraphListener<>() {
            @Override
            public void vertexAdded(int v) {
                invalidateCache();
            }

            @Override
            public void vertexObjectSet(int v, DcNodeImpl obj) {
                invalidateCache();
            }

            @Override
            public void vertexRemoved(int v, DcNodeImpl obj) {
                invalidateCache();
            }

            @Override
            public void allVerticesRemoved() {
                invalidateCache();
            }

            @Override
            public void edgeAdded(int e, DcSwitchImpl obj) {
                invalidateCache();
            }

            @Override
            public void edgeBeforeRemoval(int e, DcSwitchImpl obj) {
                // Nothing to do
            }

            @Override
            public void edgeRemoved(int e, DcSwitchImpl obj) {
                invalidateCache();
            }

            @Override
            public void allEdgesBeforeRemoval(Collection<DcSwitchImpl> obj) {
                // Nothing to do
            }

            @Override
            public void allEdgesRemoved(Collection<DcSwitchImpl> obj) {
                invalidateCache();
            }
        });
    }

    void updateRef(Ref<NetworkImpl> networkRef, Ref<SubnetworkImpl> subnetworkRef) {
        this.networkRef = networkRef;
        this.subnetworkRef = subnetworkRef;
    }

    protected NetworkImpl getNetwork() {
        return networkRef.get();
    }

    protected static int loadDcNodeIndexLimit(PlatformConfig platformConfig) {
        return platformConfig
                .getOptionalModuleConfig("iidm")
                .map(moduleConfig -> moduleConfig.getIntProperty("dc-node-index-limit", DEFAULT_DC_NODE_INDEX_LIMIT))
                .orElse(DEFAULT_DC_NODE_INDEX_LIMIT);
    }

    private Integer getVertex(String dcNodeId) {
        Objects.requireNonNull(dcNodeId, "DC Node id is null");
        Integer v = dcNodes.get(dcNodeId);
        if (v == null) {
            throw new PowsyblException("DC Node '" + dcNodeId + NOT_FOUND_IN_THE_NETWORK);
        }
        return v;
    }

    void addDcNodeToTopology(DcNodeImpl dcNode) {
        int v = graph.addVertex();
        graph.setVertexObject(v, dcNode);
        dcNodes.put(dcNode.getId(), v);
    }

    void removeDcNode(String dcNodeId) {
        Integer v = dcNodes.get(dcNodeId);
        if (v == null) {
            throw new PowsyblException("DC Node '" + dcNodeId + NOT_FOUND_IN_THE_NETWORK);
        }
        dcNodes.remove(dcNodeId);
        graph.removeVertex(v);
    }

    void addDcSwitchToTopology(DcSwitchImpl dcSwitch, String dcNode1, String dcNode2) {
        int v1 = getVertex(dcNode1);
        int v2 = getVertex(dcNode2);
        int e = graph.addEdge(v1, v2, dcSwitch);
        dcSwitches.put(dcSwitch.getId(), e);
    }

    void removeDcSwitch(String dcSwitchId) {
        Integer e = dcSwitches.get(dcSwitchId);
        if (e == null) {
            throw new PowsyblException("DC Switch '" + dcSwitchId + NOT_FOUND_IN_THE_NETWORK);
        }
        dcSwitches.remove(dcSwitchId);
        graph.removeEdge(e);
    }

    void addAndDetachSubnetworkDcTopologyModel(SubnetworkImpl subnetwork) {
        // called by network flatten, the subnetwork DC topology model is merged into this DC topology model.
        subnetwork.getDcNodeStream().forEach(dcNode -> addDcNodeToTopology((DcNodeImpl) dcNode));
        subnetwork.getDcSwitchStream().forEach(dcSwitch -> addDcSwitchToTopology((DcSwitchImpl) dcSwitch, dcSwitch.getDcNode1().getId(), dcSwitch.getDcNode2().getId()));

        subnetwork.detachDcTopologyModel(); // to remove refs
    }

    /**
     * DcBus topology cache
     *
     * @param dcBuses DC bus by id
     * @param dcNodeIdToDcBus DC node ID to DC bus mapping
     */
    private record DcBusCache(Map<String, DcBusImpl> dcBuses, Map<String, DcBusImpl> dcNodeIdToDcBus) {

        private Collection<DcBusImpl> getDcBuses() {
            return dcBuses.values();
        }

        private int getDcBusCount() {
            return dcBuses.size();
        }

        private DcBusImpl getDcBus(String id) {
            return dcBuses.get(id);
        }

        private DcBusImpl getDcBusOfDcNode(String dcNodeId) {
            return dcNodeIdToDcBus.get(dcNodeId);
        }
    }

    public Iterable<DcBus> getDcBuses() {
        return Collections.unmodifiableCollection(calculatedDcBusTopology.getDcBuses());
    }

    public int getDcBusCount() {
        return calculatedDcBusTopology.getDcBusCount();
    }

    public Stream<DcBus> getDcBusStream() {
        return calculatedDcBusTopology.getDcBuses().stream().map(Function.identity());
    }

    public DcBusImpl getDcBus(String id) {
        return calculatedDcBusTopology.getDcBus(id);
    }

    public DcBusImpl getDcBusOfDcNode(String dcNodeId) {
        return calculatedDcBusTopology.getDcBusOfDcNode(dcNodeId);
    }

    /**
     * DcBus topology calculated from DcNode-s and DcSwitch-es
     */
    class CalculatedDcBusTopology {

        private static boolean isDcBusValid(Set<DcNodeImpl> dcNodeSet) {
            // DcBus is valid if at least one DcConnectable connected, i.e. there is at least one connected DcTerminal
            return dcNodeSet.stream().flatMap(DcNode::getConnectedDcTerminalStream).findAny().isPresent();
        }

        private DcBusImpl createDcBus(Set<DcNodeImpl> dcNodeSet) {
            if (dcNodeSet == null || dcNodeSet.isEmpty()) {
                throw new PowsyblException("DC Node set is null or empty");
            }
            var node = dcNodeSet.stream().min(Comparator.comparing(DcNodeImpl::getId)).orElseThrow();
            String dcBusId = Identifiables.getUniqueId(node.getId() + "_dcBus", getNetwork().getIndex()::contains);
            String dcBusName = node.getOptionalName().orElse(null);
            return new DcBusImpl(networkRef, subnetworkRef, dcBusId, dcBusName, dcNodeSet);
        }

        private DcBusCache getCache() {
            if (variants.get().cache != null) {
                // valid cache exists
                return variants.get().cache;
            }

            Map<String, DcBusImpl> dcBuses = new LinkedHashMap<>();

            // mapping between DC nodes ID and DC buses
            Map<String, DcBusImpl> dcNodeIdToDcBus = new HashMap<>();

            boolean[] encountered = new boolean[graph.getVertexCapacity()];
            Arrays.fill(encountered, false);
            for (int v : graph.getVertices()) {
                if (!encountered[v]) {
                    final Set<DcNodeImpl> dcNodeSet = new LinkedHashSet<>(1);
                    dcNodeSet.add(graph.getVertexObject(v));
                    graph.traverse(v, TraversalType.DEPTH_FIRST, (v1, e, v2) -> {
                        DcSwitchImpl dcSwitch = graph.getEdgeObject(e);
                        if (dcSwitch.isOpen()) {
                            return TraverseResult.TERMINATE_PATH;
                        } else {
                            dcNodeSet.add(graph.getVertexObject(v2));
                            return TraverseResult.CONTINUE;
                        }
                    }, encountered);

                    if (isDcBusValid(dcNodeSet)) {
                        DcBusImpl dcBus = createDcBus(dcNodeSet);
                        dcBuses.put(dcBus.getId(), dcBus);
                        dcNodeSet.forEach(dcNode -> dcNodeIdToDcBus.put(dcNode.getId(), dcBus));
                    }
                }
            }

            variants.get().cache = new DcBusCache(dcBuses, dcNodeIdToDcBus);
            return variants.get().cache;
        }

        private void invalidateCache() {
            // detach buses
            if (variants.get().cache != null) {
                for (DcBusImpl bus : variants.get().cache.getDcBuses()) {
                    bus.invalidate();
                }
                variants.get().cache = null;
            }
        }

        private Collection<DcBusImpl> getDcBuses() {
            return getCache().getDcBuses();
        }

        private int getDcBusCount() {
            return getCache().getDcBusCount();
        }

        private DcBusImpl getDcBus(String dcBusId) {
            return getCache().getDcBus(dcBusId);
        }

        private DcBusImpl getDcBusOfDcNode(String dcNodeId) {
            return getCache().getDcBusOfDcNode(dcNodeId);
        }

    }

    final CalculatedDcBusTopology calculatedDcBusTopology
            = new CalculatedDcBusTopology();

    private static final class VariantImpl implements Variant {

        private DcBusCache cache;

        private VariantImpl() {
        }

        @Override
        public DcTopologyModel.VariantImpl copy() {
            return new DcTopologyModel.VariantImpl();
        }
    }

    public void invalidateCache() {
        calculatedDcBusTopology.invalidateCache();
        // DC topology does not affect at all AC topology,
        // synchronous (ac) components do not need to be invalidated,
        // only dc components and connected (ac+dc) components are invalidated.
        getNetwork().getConnectedComponentsManager().invalidate();
        getNetwork().getDcComponentsManager().invalidate();
    }

    public void attach(DcTerminalImpl dcTerminal) {
        DcNodeImpl dcNode = (DcNodeImpl) dcTerminal.getDcNode();
        dcNode.addDcTerminal(dcTerminal);
        invalidateAllVariantsCache();
    }

    public void detach(DcTerminalImpl dcTerminal) {
        DcNodeImpl dcNode = (DcNodeImpl) dcTerminal.getDcNode();
        dcNode.removeDcTerminal(dcTerminal);
        invalidateAllVariantsCache();
    }

    public void invalidateAllVariantsCache() {
        networkRef.get().getVariantManager().forEachVariant(this::invalidateCache);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        variants.push(number, () -> variants.copy(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        variants.pop(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        variants.delete(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        variants.allocate(indexes, () -> variants.copy(sourceIndex));
    }
}
