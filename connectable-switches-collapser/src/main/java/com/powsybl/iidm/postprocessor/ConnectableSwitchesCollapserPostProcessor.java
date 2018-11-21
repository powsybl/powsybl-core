/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.postprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.google.common.collect.Maps;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.ImportPostProcessor;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyVisitor;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
@AutoService(ImportPostProcessor.class)
public class ConnectableSwitchesCollapserPostProcessor implements ImportPostProcessor {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ConnectableSwitchesCollapserPostProcessor.class);

    @Override
    public String getName() {
        return "connectableSwitchesCollapser";
    }

    @Override
    public void process(Network network, ComputationManager cm) throws Exception {
        Objects.requireNonNull(network);
        LOGGER.info("Execute {} post processor on network {}", getName(), network.getId());
        removeBreakers(network);
    }

    private int removeBreakers(Network network) {
        AtomicInteger removedSwitches = new AtomicInteger(0);

        network.getVoltageLevelStream().forEach(vl -> {

            int removed = removeBreakers(network, vl);
            removedSwitches.addAndGet(removed);

            while (removed > 0) {
                removed = removeBreakers(network, vl);
                removedSwitches.addAndGet(removed);
            }
            removed = removeDoubleSwitchBuses(network, vl);
            removedSwitches.addAndGet(removed);

            while (removed > 0) {
                removed = removeDoubleSwitchBuses(network, vl);
                removedSwitches.addAndGet(removed);
            }
        });
        LOGGER.info("Removed switches: " + removedSwitches.get());
        return removedSwitches.get();
    }

    private int removeBreakers(Network network, VoltageLevel vl) {
        AtomicInteger removed = new AtomicInteger();

        Map<String, List<String>> busMap = getBusSwitchMap(vl);

        List<String> singleSwitchBuses = busMap.entrySet().stream()
                .filter(e -> e.getValue().size() == 1).map(r -> r.getKey()).collect(Collectors.toList());

        List<Switch> switchList = busMap.entrySet().stream()
                .filter(e -> e.getValue().size() == 1).map(e -> vl.getBusBreakerView().getSwitch(e.getValue().get(0))).distinct().collect(Collectors.toList());

        switchList.forEach(sw -> {
            Bus b1 = vl.getBusBreakerView().getBus1(sw.getId());
            Bus b2 = vl.getBusBreakerView().getBus2(sw.getId());
            Bus toRemove = null;
            Bus toKeep = null;
            if (singleSwitchBuses.contains(b1.getId())
                    && singleSwitchBuses.contains(b2.getId())) {
                if (compareBusToRemove(b1, b2)) {
                    toRemove = b1;
                    toKeep = b2;
                } else {
                    toRemove = b2;
                    toKeep = b1;
                }
            } else if (singleSwitchBuses.contains(b1.getId())) {
                toRemove = b1;
                toKeep = b2;
            } else if (singleSwitchBuses.contains(b2.getId())) {
                toRemove = b2;
                toKeep = b1;
            }
            if (toRemove != null) {
                if (this.removeSwitchandBus(vl, sw, toRemove, toKeep)) {
                    removed.incrementAndGet();
                }
                if (toKeep.getConnectedTerminalCount() == 0) {
                    try {
                        vl.getBusBreakerView().removeBus(toKeep.getId());
                    } catch (Exception ex) {
                        LOGGER.debug("Not removed disconnected bus " + toKeep.getId());
                    }
                }
            }
        });
        return removed.get();
    }

    private int removeDoubleSwitchBuses(Network network, VoltageLevel vl) {
        AtomicInteger removed = new AtomicInteger();
        Map<String, List<String>> busMap = getBusSwitchMap(vl);

        Map<String, List<String>> doubleSwitchBuses = busMap.entrySet().stream()
                .filter(e -> e.getValue().size() == 2)
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        doubleSwitchBuses.forEach((b, swl) -> {
            Bus bus = vl.getBusBreakerView().getBus(b);
            Switch s1 = vl.getBusBreakerView().getSwitch(swl.get(0));
            Switch s2 = vl.getBusBreakerView().getSwitch(swl.get(1));

            List<Terminal> terminals = getTerminals(bus);

            if (s1 != null && s2 != null && terminals.size() == 0) {
                Switch switchToKeep;
                Switch switchToRemove;

                if (compareSwitchToRemove(vl, bus, s1, s2)) {
                    switchToRemove = s1;
                    switchToKeep = s2;
                } else {
                    switchToRemove = s2;
                    switchToKeep = s1;
                }

                Bus busToKeep = vl.getBusBreakerView().getBus1(switchToRemove.getId()).equals(bus)
                        ? vl.getBusBreakerView().getBus2(switchToRemove.getId())
                        : vl.getBusBreakerView().getBus1(switchToRemove.getId());

                boolean reconnectBus1 = vl.getBusBreakerView().getBus1(switchToKeep.getId())
                        .equals(bus) ? true : false;

                vl.getBusBreakerView().removeSwitch(switchToRemove.getId());

                String bus1 = reconnectBus1 ? busToKeep.getId()
                        : vl.getBusBreakerView().getBus1(switchToKeep.getId()).getId();
                String bus2 = reconnectBus1
                        ? vl.getBusBreakerView().getBus2(switchToKeep.getId()).getId()
                        : busToKeep.getId();

                vl.getBusBreakerView().removeSwitch(switchToKeep.getId());
                vl.getBusBreakerView().removeBus(bus.getId());
                vl.getBusBreakerView().newSwitch().setId(switchToKeep.getId())
                        .setName(switchToKeep.getName()).setFictitious(switchToKeep.isFictitious())
                        .setOpen(switchToKeep.isOpen() || switchToRemove.isOpen()).setBus1(bus1)
                        .setBus2(bus2).add();
                removed.incrementAndGet();
            }
        });
        return removed.get();
    }

    private Map<String, List<String>> getBusSwitchMap(VoltageLevel vl) {

        return vl.getBusBreakerView().getSwitchStream().flatMap(s -> Stream.of(
                Maps.immutableEntry(vl.getBusBreakerView().getBus1(s.getId()).getId(), s.getId()),
                Maps.immutableEntry(vl.getBusBreakerView().getBus2(s.getId()).getId(), s.getId())))
                            .collect(Collectors.toMap(a -> a.getKey(),
                                a -> new ArrayList<>(Collections.singletonList(a.getValue())), (o, n) -> {
                                    o.addAll(n);
                                    return o;
                                }, HashMap::new));
    }

    private boolean compareSwitchToRemove(VoltageLevel vl, Bus bus, Switch s1, Switch s2) {

        if (s1.isFictitious()) {
            return true;
        }
        if (s2.isFictitious()) {
            return false;
        }
        if (s1.getKind() != s2.getKind()) {
            return s1.getKind() == SwitchKind.BREAKER;
        }
        Bus b1 = vl.getBusBreakerView().getBus1(s1.getId()).equals(bus)
                ? vl.getBusBreakerView().getBus2(s1.getId())
                : vl.getBusBreakerView().getBus1(s1.getId());
        Bus b2 = vl.getBusBreakerView().getBus1(s2.getId()).equals(bus)
                ? vl.getBusBreakerView().getBus2(s2.getId())
                : vl.getBusBreakerView().getBus1(s2.getId());

        if (b1.getAngle() != b2.getAngle()) {
            return b1.getAngle() < b2.getAngle();
        }
        return s1.getId().compareTo(s2.getId()) < 0;
    }

    private boolean compareBusToRemove(Bus b1, Bus b2) {
        if (b1.getConnectedTerminalCount() == b2.getConnectedTerminalCount()) {
            long score1 = b1.getTwoWindingTransformerStream().count()
                    + b1.getThreeWindingTransformerStream().count() + b1.getLineStream().count();
            long score2 = b2.getTwoWindingTransformerStream().count()
                    + b2.getThreeWindingTransformerStream().count() + b2.getLineStream().count();
            return score1 <= score2;
        } else {
            return b1.getConnectedTerminalCount() < b2.getConnectedTerminalCount();
        }
    }

    private List<Terminal> getTerminals(Bus b) {
        List<Terminal> terminals = new ArrayList();

        b.visitConnectedOrConnectableEquipments(new TopologyVisitor() {
            @Override
            public void visitBusbarSection(BusbarSection section) {
                terminals.add(section.getTerminal());
            }

            @Override
            public void visitLine(Line line, Line.Side side) {
                terminals.add(line.getTerminal(side));
            }

            @Override
            public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer,
                    TwoWindingsTransformer.Side side) {
                terminals.add(transformer.getTerminal(side));
            }

            @Override
            public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer,
                    ThreeWindingsTransformer.Side side) {
                terminals.add(transformer.getTerminal(side));
            }

            @Override
            public void visitGenerator(Generator generator) {
                terminals.add(generator.getTerminal());
            }

            @Override
            public void visitLoad(Load load) {
                terminals.add(load.getTerminal());
            }

            @Override
            public void visitShuntCompensator(ShuntCompensator sc) {
                terminals.add(sc.getTerminal());
            }

            @Override
            public void visitDanglingLine(DanglingLine danglingLine) {
                terminals.add(danglingLine.getTerminal());
            }

            @Override
            public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
                terminals.add(staticVarCompensator.getTerminal());
            }

            @Override
            public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
                terminals.add(converterStation.getTerminal());
            }
        });
        return terminals;
    }

    private boolean removeSwitchandBus(VoltageLevel vl, Switch s, Bus toRemove, Bus toKeep) {
        List<Terminal> terminals = getTerminals(toRemove);
        if (terminals.size() <= 1) {
            terminals.forEach(t -> {
                boolean reconnect = false;
                if (t.isConnected()) {
                    t.disconnect();
                    reconnect = true;
                }
                t.getBusBreakerView().setConnectableBus(toKeep.getId());

                if (!s.isOpen() && reconnect) {
                    t.connect();
                }
            });
            vl.getBusBreakerView().removeSwitch(s.getId());
            vl.getBusBreakerView().removeBus(toRemove.getId());
            return true;
        }
        return false;
    }


}
