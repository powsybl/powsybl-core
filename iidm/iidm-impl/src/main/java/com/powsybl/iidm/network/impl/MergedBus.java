/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class MergedBus extends AbstractIdentifiable<Bus> implements CalculatedBus {

    private final Set<ConfiguredBus> buses;

    private boolean valid = true;

    MergedBus(String id, String name, boolean fictitious, Set<ConfiguredBus> buses) {
        super(id, name, fictitious);
        if (buses.isEmpty()) {
            throw new IllegalArgumentException("buses is empty");
        }
        this.buses = buses;
    }

    private void checkValidity() {
        if (!valid) {
            throw new PowsyblException("Bus has been invalidated");
        }
    }

    @Override
    public boolean isInMainConnectedComponent() {
        Optional<ConfiguredBus> bus = buses.stream().findFirst();
        return bus.isPresent() && bus.get().isInMainConnectedComponent();
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        Optional<ConfiguredBus> bus = buses.stream().findFirst();
        return bus.isPresent() && bus.get().isInMainSynchronousComponent();
    }

    @Override
    public int getConnectedTerminalCount() {
        checkValidity();
        return buses.stream().mapToInt(ConfiguredBus::getConnectedTerminalCount).sum();
    }

    @Override
    public Iterable<TerminalExt> getConnectedTerminals() {
        checkValidity();
        return buses.stream().map(ConfiguredBus::getConnectedTerminals).reduce(Iterables::concat).orElse(Collections.emptyList());
    }

    @Override
    public Stream<TerminalExt> getConnectedTerminalStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getConnectedTerminalStream);
    }

    @Override
    public void invalidate() {
        valid = false;
        buses.clear();
    }

    @Override
    public NetworkImpl getNetwork() {
        return (NetworkImpl) getVoltageLevel().getNetwork();
    }

    @Override
    public Network getParentNetwork() {
        return getVoltageLevel().getParentNetwork();
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        checkValidity();
        return buses.iterator().next().getVoltageLevel();
    }

    @Override
    public double getV() {
        checkValidity();
        for (Bus b : buses) {
            if (!Double.isNaN(b.getV())) {
                return b.getV();
            }
        }
        return Double.NaN;
    }

    @Override
    public BusExt setV(double v) {
        checkValidity();
        for (ConfiguredBus bus : buses) {
            bus.setV(v);
        }
        return this;
    }

    @Override
    public double getAngle() {
        checkValidity();
        for (Bus b : buses) {
            if (!Double.isNaN(b.getAngle())) {
                return b.getAngle();
            }
        }
        return Double.NaN;
    }

    @Override
    public BusExt setAngle(double angle) {
        checkValidity();
        for (ConfiguredBus bus : buses) {
            bus.setAngle(angle);
        }
        return this;
    }

    @Override
    public double getP() {
        checkValidity();
        double p = 0;
        for (Bus b : buses) {
            p += b.getP();
        }
        return p;
    }

    @Override
    public double getQ() {
        checkValidity();
        double q = 0;
        for (Bus b : buses) {
            q += b.getQ();
        }
        return q;
    }

    @Override
    public double getFictitiousP0() {
        return buses.stream().map(Bus::getFictitiousP0).reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousP0(double p0) {
        buses.forEach(b -> b.setFictitiousP0(0.0));
        buses.iterator().next().setFictitiousP0(p0);
        return this;
    }

    @Override
    public double getFictitiousQ0() {
        return buses.stream().map(Bus::getFictitiousQ0).reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousQ0(double q0) {
        buses.forEach(b -> b.setFictitiousQ0(0.0));
        buses.iterator().next().setFictitiousQ0(q0);
        return this;
    }

    @Override
    public void setConnectedComponentNumber(int connectedComponentNumber) {
        checkValidity();
        for (ConfiguredBus bus : buses) {
            bus.setConnectedComponentNumber(connectedComponentNumber);
        }
    }

    @Override
    public Component getConnectedComponent() {
        checkValidity();
        for (Bus b : buses) {
            Component cc = b.getConnectedComponent();
            if (cc != null) {
                return cc;
            }
        }
        throw new IllegalStateException("Should not happen");
    }

    @Override
    public void setSynchronousComponentNumber(int componentNumber) {
        checkValidity();
        for (ConfiguredBus bus : buses) {
            bus.setSynchronousComponentNumber(componentNumber);
        }
    }

    @Override
    public Component getSynchronousComponent() {
        checkValidity();
        for (Bus b : buses) {
            Component sc = b.getSynchronousComponent();
            if (sc != null) {
                return sc;
            }
        }
        throw new IllegalStateException("Should not happen");
    }

    @Override
    public Iterable<Line> getLines() {
        checkValidity();
        List<Iterable<Line>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getLines());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<Line> getLineStream() {
        checkValidity();
        return buses.stream().flatMap(Bus::getLineStream);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        checkValidity();
        List<Iterable<TwoWindingsTransformer>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getTwoWindingsTransformers());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getTwoWindingsTransformerStream);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        checkValidity();
        List<Iterable<ThreeWindingsTransformer>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getThreeWindingsTransformers());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getThreeWindingsTransformerStream);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        checkValidity();
        List<Iterable<Generator>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getGenerators());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getGeneratorStream);
    }

    @Override
    public Iterable<Battery> getBatteries() {
        checkValidity();
        List<Iterable<Battery>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getBatteries());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getBatteryStream);
    }

    @Override
    public Iterable<Load> getLoads() {
        checkValidity();
        List<Iterable<Load>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getLoads());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<Load> getLoadStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getLoadStream);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        checkValidity();
        List<Iterable<ShuntCompensator>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getShuntCompensators());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getShuntCompensatorStream);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        checkValidity();
        List<Iterable<DanglingLine>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getDanglingLines(danglingLineFilter));
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        checkValidity();
        return buses.stream().flatMap(configuredBus -> configuredBus.getDanglingLineStream(danglingLineFilter));
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        checkValidity();
        List<Iterable<StaticVarCompensator>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getStaticVarCompensators());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getStaticVarCompensatorStream);
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        checkValidity();
        List<Iterable<LccConverterStation>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getLccConverterStations());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getLccConverterStationStream);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        checkValidity();
        List<Iterable<VscConverterStation>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getVscConverterStations());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getVscConverterStationStream);
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        checkValidity();
        for (ConfiguredBus bus : buses) {
            bus.visitConnectedEquipments(visitor);
        }
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        checkValidity();
        for (ConfiguredBus bus : buses) {
            bus.visitConnectedOrConnectableEquipments(visitor);
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Bus";
    }
}
