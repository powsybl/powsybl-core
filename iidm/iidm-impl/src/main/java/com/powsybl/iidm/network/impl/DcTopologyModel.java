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
import com.powsybl.iidm.network.Network;
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

    public static final int DC_NODE_INDEX_LIMIT = loadDcNodeIndexLimit(PlatformConfig.defaultConfig());

    private Ref<NetworkImpl> networkRef;
    private Ref<SubnetworkImpl> subnetworkRef;

    private final UndirectedGraphImpl<DcNodeImpl, DcSwitchImpl> graph = new UndirectedGraphImpl<>(DC_NODE_INDEX_LIMIT);

    /* DcNodes indexed by vertex number */
    private final Map<String, Integer> dcNodes = new HashMap<>();

    /* DcSwitches indexed by edge number */
    private final Map<String, Integer> dcSwitches = new HashMap<>();

    protected final VariantArray<DcTopologyModel.VariantImpl> variants;

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
                // Nothing to do, notifications are handled properly in removeSwitch
            }

            @Override
            public void edgeRemoved(int e, DcSwitchImpl obj) {
                invalidateCache();
            }

            @Override
            public void allEdgesBeforeRemoval(Collection<DcSwitchImpl> obj) {
                // Nothing to do, notifications are handled properly in removeAllSwitches
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

    private Network getParentNetwork() {
        return Optional.ofNullable((Network) subnetworkRef.get()).orElse(getNetwork());
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
            throw new PowsyblException("DC Node " + dcNodeId
                    + " not found in the network");
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
            throw new PowsyblException("DC Node '" + dcNodeId
                    + " not found in the network");
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
            throw new PowsyblException("DC Switch '" + dcSwitchId
                    + " not found in the network");
        }
        dcSwitches.remove(dcSwitchId);
        graph.removeEdge(e);
    }

    /**
     * DCBus topology cache
     */
    private static final class DcBusCache {

        /* DC bus by id */
        private final Map<String, DcBusImpl> dcBuses;

        /* DC node to DC bus mapping */
        private final Map<DcNodeImpl, DcBusImpl> mapping;

        private DcBusCache(Map<String, DcBusImpl> dcBuses, Map<DcNodeImpl, DcBusImpl> mapping) {
            this.dcBuses = dcBuses;
            this.mapping = mapping;
        }

        private Collection<DcBusImpl> getDcBuses() {
            return dcBuses.values();
        }

        private int getDcBusCount() {
            return dcBuses.size();
        }

        private DcBusImpl getDcBus(String id) {
            return dcBuses.get(id);
        }

        private DcBusImpl getDcBus(DcNodeImpl dcNode) {
            return mapping.get(dcNode);
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
        return calculatedDcBusTopology.getDcBus(id, false);
    }

    public DcBusImpl getDcBusOfDcNode(String dcNodeId) {
        DcNodeImpl dcNode = (DcNodeImpl) getNetwork().getDcNode(dcNodeId);
        return calculatedDcBusTopology.getDcBus(dcNode);
    }

    /**
     * DcBus topology calculated from DcNode-s and DcSwitch-es
     */
    class CalculatedDcBusTopology {

        private DcBusImpl createDcBus(Set<DcNodeImpl> dcNodeSet) {
            if (dcNodeSet == null || dcNodeSet.isEmpty()) {
                throw new PowsyblException("DC Node set is null or empty");
            }
            var node = dcNodeSet.stream().min(Comparator.comparing(DcNodeImpl::getId)).orElseThrow();
            String dcBusId = Identifiables.getUniqueId(node.getId() + "_dcBus", getNetwork().getIndex()::contains);
            String dcBusName = node.getOptionalName().orElse(null);
            return new DcBusImpl(networkRef, subnetworkRef, dcBusId, dcBusName);
        }

        private void updateCache() {
            if (variants.get().cache != null) {
                return;
            }

            Map<String, DcBusImpl> dcBuses = new LinkedHashMap<>();

            // mapping between configured buses and merged buses
            Map<DcNodeImpl, DcBusImpl> mapping = new IdentityHashMap<>();

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

                    DcBusImpl dcBus = createDcBus(dcNodeSet);
                    dcBuses.put(dcBus.getId(), dcBus);
                    dcNodeSet.forEach(dcNode -> mapping.put(dcNode, dcBus));
                }
            }

            variants.get().cache = new DcBusCache(dcBuses, mapping);
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
            updateCache();
            return variants.get().cache.getDcBuses();
        }

        private int getDcBusCount() {
            updateCache();
            return variants.get().cache.getDcBusCount();
        }

        private DcBusImpl getDcBus(String dcBusId, boolean throwException) {
            updateCache();
            DcBusImpl bus = variants.get().cache.getDcBus(dcBusId);
            if (throwException && bus == null) {
                throw new PowsyblException("Bus " + dcBusId
                        + " not found in network "
                        + getParentNetwork().getId());
            }
            return bus;
        }

        DcBusImpl getDcBus(DcNodeImpl dcNode) {
            Objects.requireNonNull(dcNode, "dcNode is null");
            updateCache();
            return variants.get().cache.getDcBus(dcNode);
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
        // TODO normally this is not needed and can just be deleted getNetwork().getBusView().invalidateCache();
        // TODO normally this is not needed and can just be deleted getNetwork().getBusBreakerView().invalidateCache();
        getNetwork().getConnectedComponentsManager().invalidate();
        // TODO normally this is not needed and can just be deleted getNetwork().getSynchronousComponentsManager().invalidate();
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
