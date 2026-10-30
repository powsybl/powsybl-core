/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.GraphConnectivity;
import com.powsybl.math.graph.GraphConnectivityFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
class GraphConnectivityComponentsManager implements ComponentsManager {

    private final GraphConnectivityFactory<Identifiable<?>, Object> connectivityFactory;
    private final NetworkImpl network;

    private final Set<VoltageLevel> invalidatedVoltageLevels = new HashSet<>();

    private GraphConnectivity<Identifiable<?>, Object> synchronousConnectivity;

    GraphConnectivityComponentsManager(NetworkImpl network,
                                       GraphConnectivityFactory<Identifiable<?>, Object> connectivityFactory) {
        this.network = network;
        this.connectivityFactory = connectivityFactory;
    }

    private void updateSynchronousConnectivity() {
        if (synchronousConnectivity == null) {
            synchronousConnectivity = connectivityFactory.create();

            network.getVoltageLevels().forEach(this::addVoltageLevelBuses);
            network.getVoltageLevels().forEach(this::addVoltageLevelIncidentEdges);
        } else if (!invalidatedVoltageLevels.isEmpty()) {
            invalidatedVoltageLevels.forEach(this::addVoltageLevelBuses);
            invalidatedVoltageLevels.forEach(this::addVoltageLevelIncidentEdges);
            invalidatedVoltageLevels.clear();
        }
    }

    private void addVoltageLevelBuses(VoltageLevel vl) {
        for (Bus bus : vl.getBusView().getBuses()) {
            synchronousConnectivity.addVertex(bus);
        }
    }

    private void addVoltageLevelIncidentEdges(VoltageLevel vl) {
        for (Connectable<?> connectable : vl.getConnectables()) {
            switch (connectable) {
                case AbstractConnectableBranch<?> branch -> {
                    Bus bus1 = branch.getTerminal1().getBusView().getBus();
                    Bus bus2 = branch.getTerminal2().getBusView().getBus();
                    if (bus1 != null && bus2 != null) {
                        synchronousConnectivity.addEdge(bus1, bus2, branch);
                    }
                }
                case BoundaryLine bl -> {
                    TieLine tl = bl.getTieLine().orElse(null);
                    if (tl != null) {
                        Bus bus1 = tl.getBoundaryLine1().getTerminal().getBusView().getBus();
                        Bus bus2 = tl.getBoundaryLine2().getTerminal().getBusView().getBus();

                        if (bus1 != null && bus2 != null) {
                            synchronousConnectivity.addEdge(bus1, bus2, tl);
                        }
                    }
                }
                case ThreeWindingsTransformer thwt -> {
                    Bus bus1 = thwt.getLeg1().getTerminal().getBusView().getBus();
                    Bus bus2 = thwt.getLeg2().getTerminal().getBusView().getBus();
                    Bus bus3 = thwt.getLeg3().getTerminal().getBusView().getBus();

                    if (bus1 != null && bus2 != null) {
                        synchronousConnectivity.addEdge(bus1, bus2, thwt.getLeg1());
                    }
                    if (bus2 != null && bus3 != null) {
                        synchronousConnectivity.addEdge(bus2, bus3, thwt.getLeg2());
                    }
                    if (bus3 != null && bus1 != null) {
                        synchronousConnectivity.addEdge(bus3, bus1, thwt.getLeg3());
                    }
                }
                default -> { }
            }
        }
    }

    private void removeVoltageLevel(VoltageLevel vl) {
        for (Connectable<?> connectable : vl.getConnectables()) {
            switch (connectable) {
                case AbstractConnectableBranch<?> branch -> synchronousConnectivity.removeEdge(branch);
                case ThreeWindingsTransformer thwt -> {
                    synchronousConnectivity.removeEdge(thwt.getLeg1());
                    synchronousConnectivity.removeEdge(thwt.getLeg2());
                    synchronousConnectivity.removeEdge(thwt.getLeg3());
                }
                default -> throw new IllegalStateException("Unexpected value: " + connectable);
            }
        }
    }

    @Override
    public void invalidate() {
        invalidatedVoltageLevels.clear();
        synchronousConnectivity = null;
    }

    @Override
    public void voltageLevelAdded(VoltageLevel voltageLevel) {

    }

    @Override
    public void invalidate(VoltageLevel voltageLevel) {
        if (synchronousConnectivity != null) {
            removeVoltageLevel(voltageLevel);
            invalidatedVoltageLevels.add(voltageLevel);
        }
    }

    @Override
    public void voltageLevelRemoved(VoltageLevel voltageLevel) {

    }

    @Override
    public List<Component> getConnectedComponents() {
        updateSynchronousConnectivity();
        return synchronousConnectivity.getConnectedComponents()
                .stream()
                .map(c -> (Component) new ComponentWrapper(c))
                .toList();
    }

    @Override
    public Component getComponent(BusExt bus) {
        Bus busviewBus = switch (bus) {
            case ConfiguredBus cb -> {
                List<BusTerminal> term = cb.getTerminals();
                if (term.isEmpty()) {
                    yield null;
                }
                yield term.getFirst().getBusView().getBus();
            }
            case CalculatedBus mb -> mb;
            case null -> null;
            default -> throw new IllegalStateException("Unexpected value: " + bus);
        };

        if (bus == null) {
            return null;
        }

        updateSynchronousConnectivity();
        com.powsybl.math.graph.Component<Identifiable<?>> comp = synchronousConnectivity.getConnectedComponent(busviewBus);
        if (comp == null) {
            return null;
        }

        return new ComponentWrapper(comp);
    }

    @Override
    public boolean isInMainComponent(BusExt bus) {
        Bus busviewBus = switch (bus) {
            case ConfiguredBus cb -> {
                List<BusTerminal> term = cb.getTerminals();
                if (term.isEmpty()) {
                    yield null;
                }
                yield term.getFirst().getBusView().getBus();
            }
            case CalculatedBus mb -> mb;
            case null -> null;
            default -> throw new IllegalStateException("Unexpected value: " + bus);
        };

        if (bus == null) {
            return false;
        }
        return synchronousConnectivity.getLargestConnectedComponent().contains(busviewBus);
    }

    @Override
    public Component getComponent(DcBusImpl bus) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInMainComponent(DcBusImpl bus) {
        throw new UnsupportedOperationException();
    }

    private record ComponentWrapper(com.powsybl.math.graph.Component<Identifiable<?>> comp) implements Component {
        @Override
        public int getNum() {
            return comp.getNum();
        }

        @Override
        public int getSize() {
            return comp.size();
        }

        @Override
        public Iterable<Bus> getBuses() {
            return () -> getBusStream().iterator();
        }

        @Override
        public Stream<Bus> getBusStream() {
            return comp.stream().filter(b -> b instanceof Bus).map(b -> (Bus) b);
        }

        @Override
        public Iterable<DcBus> getDcBuses() {
            return List.of();
        }

        @Override
        public Stream<DcBus> getDcBusStream() {
            return Stream.empty();
        }
    }
}
