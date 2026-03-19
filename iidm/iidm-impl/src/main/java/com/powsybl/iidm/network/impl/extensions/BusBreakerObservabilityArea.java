/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class BusBreakerObservabilityArea extends AbstractExtension<VoltageLevel> implements ObservabilityArea {

    private static final Logger LOG = LoggerFactory.getLogger(BusBreakerObservabilityArea.class);
    private static final String NOT_SUPPORTED_IN_BB_TOPOLOGY = "Not supported in a bus breaker topology";

    static class NodeBreakerViewImpl implements NodeBreakerView {

        @Override
        public Map<Integer, AreaCharacteristics> getObservabilityAreaByNode() {
            throw new UnsupportedOperationException(NOT_SUPPORTED_IN_BB_TOPOLOGY);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(int node) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_IN_BB_TOPOLOGY);
        }
    }

    static class BusBreakerViewImpl implements BusBreakerView {

        private final Map<String, BusBreakerAreaCharacteristics> observabilityAreas = new HashMap<>();

        BusBreakerViewImpl(Map<String, BusBreakerAreaCharacteristics> observabilityAreas) {
            this.observabilityAreas.putAll(observabilityAreas);
        }

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus() {
            return getObservabilityAreaByBus(true);
        }

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus(boolean throwException) {
            return Collections.unmodifiableMap(observabilityAreas);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId) {
            return getObservabilityArea(busId, true);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId, boolean throwException) {
            return observabilityAreas.get(busId);
        }
    }

    class BusViewImpl implements BusView {

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus() {
            return getObservabilityAreaByBus(true);
        }

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus(boolean throwException) {
            Map<String, AreaCharacteristics> observabilityAreaByBusViewBus = new HashMap<>();
            busBreakerView.observabilityAreas.forEach((key, value) -> {
                String busViewBusId = voltageLevel.getBusView().getMergedBus(key).getId();
                if (observabilityAreaByBusViewBus.containsKey(busViewBusId)) {
                    AreaCharacteristics previous = observabilityAreaByBusViewBus.get(busViewBusId);
                    if (previous != value) {
                        LOG.error("Inconsistent observabilities areas: bus {} is associated " +
                                "to different area numbers and/or status. Some will be lost.", busViewBusId);
                        if (throwException) {
                            throw new PowsyblException("Inconsistent observabilities areas: bus " + busViewBusId + " is associated " +
                                    "to different area numbers and/or status");
                        }
                    }
                } else {
                    observabilityAreaByBusViewBus.put(busViewBusId, value);
                }
            });
            return observabilityAreaByBusViewBus;
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId) {
            return getObservabilityArea(busId, true);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId, boolean throwException) {
            AreaCharacteristics characteristics = null;
            for (Map.Entry<String, BusBreakerAreaCharacteristics> e : busBreakerView.observabilityAreas.entrySet()) {
                Bus bus = voltageLevel.getBusBreakerView().getBus(e.getKey());
                if (bus == null) {
                    LOG.error("Inconsistent observabilities areas: bus {} does not exist anymore in bus-breaker view", e.getKey());
                    if (throwException) {
                        throw new PowsyblException("Inconsistent observabilities areas: bus " + e.getKey() + " does not exist anymore in bus-breaker view");
                    }
                    continue;
                }
                if (voltageLevel.getBusView().getMergedBus(e.getKey()).getId().equals(busId)) {
                    if (characteristics != null && characteristics != e.getValue()) {
                        LOG.error("Inconsistent observabilities areas: bus {} is associated " +
                                "to different area numbers and/or status. Some will be lost.", busId);
                        if (throwException) {
                            throw new PowsyblException("Inconsistent observabilities areas: bus " + busId + " is associated " +
                                    "to different area numbers and/or status");
                        }
                    } else {
                        characteristics = e.getValue();
                    }
                }
            }
            return characteristics;
        }
    }

    private final NodeBreakerViewImpl nodeBreakerView;
    private final BusBreakerViewImpl busBreakerView;
    private final BusViewImpl busView;

    private final VoltageLevel voltageLevel;

    BusBreakerObservabilityArea(Map<String, AbstractAreaCharacteristics.Characteristics> observabilityAreasByBusView,
                                Map<String, AbstractAreaCharacteristics.Characteristics> observabilityAreasByBusBreakerView,
                                List<Set<String>> busBreakerViewBusesByBus, VoltageLevel voltageLevel) {
        nodeBreakerView = new NodeBreakerViewImpl();
        busBreakerView = new BusBreakerViewImpl(convert(observabilityAreasByBusBreakerView, observabilityAreasByBusView, busBreakerViewBusesByBus, voltageLevel));
        busView = new BusViewImpl();
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
    }

    private static Map<String, BusBreakerAreaCharacteristics> convert(Map<String, AbstractAreaCharacteristics.Characteristics> observabilityAreaByBusBreakerViewBus,
                                                                      Map<String, AbstractAreaCharacteristics.Characteristics> observabilityAreaByBusViewBus,
                                                                      List<Set<String>> busBreakerViewBusesByBus, VoltageLevel voltageLevel) {
        Map<String, BusBreakerAreaCharacteristics> observabilityAreas = new HashMap<>();
        if (observabilityAreaByBusBreakerViewBus.isEmpty()) {
            for (Map.Entry<String, AbstractAreaCharacteristics.Characteristics> e : observabilityAreaByBusViewBus.entrySet()) {
                String busId = e.getKey();
                Bus bus = voltageLevel.getBusView().getBus(busId);
                if (bus == null) {
                    throw new PowsyblException("Bus-view bus " + busId + " does not exist");
                }
                BusBreakerAreaCharacteristics c = new BusBreakerAreaCharacteristics(voltageLevel.getBusBreakerView().getBusStreamFromBusViewBusId(busId).map(Identifiable::getId).collect(Collectors.toSet()), e.getValue(), voltageLevel);
                voltageLevel.getBusBreakerView().getBusStreamFromBusViewBusId(busId)
                        .forEach(b -> observabilityAreas.put(b.getId(), c));
            }
        } else {
            for (Set<String> buses : busBreakerViewBusesByBus) {
                BusBreakerAreaCharacteristics area = new BusBreakerAreaCharacteristics(buses, observabilityAreaByBusBreakerViewBus.get(buses.iterator().next()), voltageLevel);
                buses.forEach(busId -> observabilityAreas.put(busId, area));
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
        return busBreakerView.getObservabilityArea(terminal.getBusBreakerView().getBus().getId());
    }

    @Override
    public Collection<AreaCharacteristics> getObservabilityAreas() {
        return new HashSet<>(busBreakerView.observabilityAreas.values());
    }

    @Override
    public boolean isConsistentWithTopology() {
        Collection<Set<String>> trueBuses = voltageLevel.getBusView().getBusStream().map(b -> voltageLevel.getBusBreakerView().getBusesFromBusViewBusId(b.getId()))
                .filter(buses -> !buses.isEmpty())
                .map(buses -> {
                    Set<String> ids = new HashSet<>();
                    buses.forEach(b -> ids.add(b.getId()));
                    return ids;
                })
                .collect(Collectors.toSet());
        for (Set<String> buses : busBreakerView.observabilityAreas.values().stream().map(c -> c.getBusBreakerData().getBusIds()).collect(Collectors.toSet())) {
            if (trueBuses.stream().noneMatch(b -> b.containsAll(buses))) {
                return false;
            }
        }
        for (Set<String> buses : trueBuses) {
            if (busBreakerView.observabilityAreas.values().stream().map(c -> c.getBusBreakerData().getBusIds()).noneMatch(b -> b.containsAll(buses))) {
                return false;
            }
        }
        return true;
    }
}
