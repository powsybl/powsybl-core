package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.util.Networks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class NodeBreakerObservabilityArea extends AbstractExtension<VoltageLevel> implements ObservabilityArea {

    private static final Logger LOG = LoggerFactory.getLogger(NodeBreakerObservabilityArea.class);

    static class NodeBreakerViewImpl implements NodeBreakerView {

        private final Map<Integer, NodeBreakerAreaCharacteristics> observabilityAreas = new HashMap<>();

        NodeBreakerViewImpl(Map<Integer, NodeBreakerAreaCharacteristics> observabilityAreas, List<Set<Integer>> nodesByBus) {
            this.observabilityAreas.putAll(observabilityAreas);
        }

        @Override
        public Map<Integer, AreaCharacteristics> getObservabilityAreaByNode() {
            return Collections.unmodifiableMap(observabilityAreas);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(int node) {
            return observabilityAreas.get(node);
        }
    }

    class BusBreakerViewImpl implements BusBreakerView {

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus() {
            return getObservabilityAreaByBus(true);
        }

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus(boolean throwException) {
            Map<String, AreaCharacteristics> observabilityAreaByBus = new HashMap<>();
            Networks.getNodesByBus(voltageLevel).forEach((busId, nodes) -> {
                AreaCharacteristics characteristics = NodeBreakerObservabilityArea.this.getObservabilityArea(busId, nodes, throwException);
                for (Bus bus : voltageLevel.getBusBreakerView().getBusesFromBusViewBusId(busId)) {
                    observabilityAreaByBus.put(bus.getId(), characteristics);
                }
            });
            return observabilityAreaByBus;
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId) {
            return getObservabilityArea(busId, true);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId, boolean throwException) {
            Bus bus = voltageLevel.getBusBreakerView().getBus(busId);
            if (busId == null) {
                return null;
            }
            Bus busViewBus = bus.getConnectedTerminalStream().map(t -> t.getBusView().getBus()).filter(Objects::nonNull).findFirst().orElse(null);
            if (busViewBus == null) {
                return null;
            }
            String busViewBusId = busViewBus.getId();
            return NodeBreakerObservabilityArea.this.getObservabilityArea(busViewBusId, Networks.getNodesByBus(voltageLevel).get(busViewBusId), throwException);
        }
    }

    class BusViewImpl implements BusView {

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus() {
            return getObservabilityAreaByBus(true);
        }

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus(boolean throwException) {
            Map<String, AreaCharacteristics> observabilityAreaByBus = new HashMap<>();
            Networks.getNodesByBus(voltageLevel)
                    .forEach((busId, nodes) -> observabilityAreaByBus.put(busId, NodeBreakerObservabilityArea.this.getObservabilityArea(busId, nodes, throwException)));
            return observabilityAreaByBus;
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId) {
            return getObservabilityArea(busId, true);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId, boolean throwException) {
            return NodeBreakerObservabilityArea.this.getObservabilityArea(busId, Networks.getNodesByBus(voltageLevel).get(busId), throwException);
        }
    }

    private final NodeBreakerViewImpl nodeBreakerView;
    private final BusBreakerViewImpl busBreakerView;
    private final BusViewImpl busView;

    private final VoltageLevel voltageLevel;

    NodeBreakerObservabilityArea(Map<Integer, AbstractAreaCharacteristics.Characteristics> observabilityAreasByNode,
                                 Map<String, AbstractAreaCharacteristics.Characteristics> observabilityAreasByBus, List<Set<Integer>> nodesByBus, VoltageLevel voltageLevel) {
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
        this.nodeBreakerView = new NodeBreakerViewImpl(convert(observabilityAreasByNode, observabilityAreasByBus, nodesByBus, voltageLevel), nodesByBus);
        this.busBreakerView = new BusBreakerViewImpl();
        this.busView = new BusViewImpl();
    }

    private static Map<Integer, NodeBreakerAreaCharacteristics> convert(Map<Integer, AbstractAreaCharacteristics.Characteristics> observabilityAreasByNode,
                                                                        Map<String, AbstractAreaCharacteristics.Characteristics> observabilityAreaByBus,
                                                                        List<Set<Integer>> nodesByBus,
                                                                        VoltageLevel voltageLevel) {
        Map<Integer, NodeBreakerAreaCharacteristics> observabilityAreas = new HashMap<>();
        if (observabilityAreasByNode.isEmpty()) {
            for (Map.Entry<String, AbstractAreaCharacteristics.Characteristics> e : observabilityAreaByBus.entrySet()) {
                String busId = e.getKey();
                Bus bus = voltageLevel.getBusView().getBus(busId);
                if (bus == null) {
                    throw new PowsyblException("Bus-view bus " + busId + " does not exist");
                }
                Map<String, Set<Integer>> nodesByBusMap = Networks.getNodesByBus(voltageLevel);
                NodeBreakerAreaCharacteristics c = new NodeBreakerAreaCharacteristics(nodesByBusMap.get(busId), e.getValue(), voltageLevel);
                nodesByBusMap.get(busId).forEach(node -> observabilityAreas.put(node, c));
            }
        } else {
            for (Set<Integer> nodes : nodesByBus) {
                NodeBreakerAreaCharacteristics c = new NodeBreakerAreaCharacteristics(nodes, observabilityAreasByNode.get(nodes.iterator().next()), voltageLevel);
                nodes.forEach(node -> observabilityAreas.put(node, c));
            }
        }
        return observabilityAreas;
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public BusView getBusView() {
        return busView;
    }

    @Override
    public AreaCharacteristics getObservabilityArea(Terminal terminal) {
        return nodeBreakerView.getObservabilityArea(terminal.getNodeBreakerView().getNode());
    }

    @Override
    public Collection<AreaCharacteristics> getObservabilityAreas() {
        return new HashSet<>(nodeBreakerView.observabilityAreas.values());
    }

    @Override
    public boolean isConsistentWithTopology() {
        Collection<Set<Integer>> trueNodes = Networks.getNodesByBus(voltageLevel).values();
        for (Set<Integer> nodes : nodeBreakerView.observabilityAreas.values().stream().map(c -> c.getNodeBreakerData().getNodes()).collect(Collectors.toSet())) {
            if (trueNodes.stream().noneMatch(n -> n.containsAll(nodes))) {
                return false;
            }
        }
        for (Set<Integer> nodes : trueNodes) {
            if (nodeBreakerView.observabilityAreas.values().stream().map(c -> c.getNodeBreakerData().getNodes()).noneMatch(n -> n.containsAll(nodes))) {
                return false;
            }
        }
        return true;
    }

    private AreaCharacteristics getObservabilityArea(String busId, Set<Integer> nodes, boolean throwException) {
        AreaCharacteristics value = null;
        for (int node : nodes) {
            AreaCharacteristics tmp = nodeBreakerView.observabilityAreas.get(node);
            if (value != null) {
                if (tmp == null) {
                    LOG.error("Inconsistent observability areas: only part of nodes of bus-view bus {} are defined", busId);
                    if (throwException) {
                        throw new PowsyblException("Inconsistent observability areas: only part of nodes of bus-view bus " + busId + " are defined");
                    }
                    continue;
                }
                if (value != tmp) {
                    LOG.error("Inconsistent observability areas: bus-view bus {} has different area numbers and/or status. Some will be lost.", busId);
                    if (throwException) {
                        throw new PowsyblException("Inconsistent observability areas: bus-view bus " + busId + " has different area numbers and/or status");
                    }
                }
            } else {
                value = tmp;
            }
        }
        return value;
    }
}
