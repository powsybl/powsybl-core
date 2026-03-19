package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.extensions.ObservabilityAreaAdder;

import java.util.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class ObservabilityAreaAdderImpl extends AbstractExtensionAdder<VoltageLevel, ObservabilityArea>
        implements ObservabilityAreaAdder {

    private final Map<String, AbstractAreaCharacteristics.Characteristics> observabilityAreaByBusViewBus = new HashMap<>();
    private final Map<String, AbstractAreaCharacteristics.Characteristics> observabilityAreaByBusBreakerViewBus = new HashMap<>();
    private final Map<Integer, AbstractAreaCharacteristics.Characteristics> observabilityAreaByNodes = new HashMap<>();

    private final List<Set<Integer>> nodesByBus = new ArrayList<>();
    private final List<Set<String>> busBreakerViewBusesByBusViewBus = new ArrayList<>();

    ObservabilityAreaAdderImpl(VoltageLevel extendable) {
        super(extendable);
    }

    @Override
    protected ObservabilityArea createExtension(VoltageLevel voltageLevel) {
        int nonEmptyMap = (observabilityAreaByNodes.isEmpty() ? 0 : 1) + (observabilityAreaByBusBreakerViewBus.isEmpty() ? 0 : 1) + (observabilityAreaByBusViewBus.isEmpty() ? 0 : 1);
        if (nonEmptyMap > 1) {
            throw new PowsyblException("Observability areas must be exclusively filled by bus-view buses OR nodes, not both");
        }
        if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            if (!observabilityAreaByBusBreakerViewBus.isEmpty()) {
                throw new PowsyblException("Observability areas must be exclusively filled by bus-view buses or nodes in node-breaker voltage levels");
            }
            return new NodeBreakerObservabilityArea(observabilityAreaByNodes, observabilityAreaByBusViewBus, nodesByBus, voltageLevel);
        } else if (voltageLevel.getTopologyKind() == TopologyKind.BUS_BREAKER) {
            if (!observabilityAreaByNodes.isEmpty()) {
                throw new PowsyblException("Observability areas must be exclusively filled by bus-view buses or bus-breaker-view buses in bus-breaker voltage levels");
            }
            return new BusBreakerObservabilityArea(observabilityAreaByBusViewBus, observabilityAreaByBusBreakerViewBus, busBreakerViewBusesByBusViewBus, voltageLevel);
        }
        throw new AssertionError("Unexpected voltage level " + voltageLevel.getId() + " topology: " + voltageLevel.getTopologyKind());
    }

    @Override
    public ObservabilityAreaAdder withObservabilityAreaByBusViewBus(String busViewBusId, int observabilityAreaNumber, ObservabilityArea.ObservabilityStatus status) {
        observabilityAreaByBusViewBus.put(busViewBusId, new AbstractAreaCharacteristics.Characteristics(observabilityAreaNumber, status));
        return this;
    }

    @Override
    public ObservabilityAreaAdder withObservabilityAreaByBusBreakerViewBuses(Set<String> busBreakerViewBusIds, int observabilityAreaNumber, ObservabilityArea.ObservabilityStatus status) {
        AbstractAreaCharacteristics.Characteristics characteristics = new AbstractAreaCharacteristics.Characteristics(observabilityAreaNumber, status);
        busBreakerViewBusIds.forEach(busId -> observabilityAreaByBusBreakerViewBus.put(busId, characteristics));
        busBreakerViewBusesByBusViewBus.add(busBreakerViewBusIds);
        return this;
    }

    @Override
    public ObservabilityAreaAdder withObservabilityAreaByNodes(Set<Integer> nodes, int observabilityAreaNumber, ObservabilityArea.ObservabilityStatus status) {
        AbstractAreaCharacteristics.Characteristics characteristics = new AbstractAreaCharacteristics.Characteristics(observabilityAreaNumber, status);
        nodes.forEach(n -> observabilityAreaByNodes.put(n, characteristics));
        nodesByBus.add(nodes);
        return this;
    }
}
