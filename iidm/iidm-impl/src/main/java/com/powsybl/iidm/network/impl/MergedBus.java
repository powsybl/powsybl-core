/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class MergedBus extends AbstractIdentifiable<Bus> implements CalculatedBus {

    private final Set<ConfiguredBus> buses;

    private boolean valid = true;

    MergedBus(String id, Set<ConfiguredBus> buses) {
        super(id, null);
        if (buses.size() < 1) {
            throw new IllegalArgumentException("buses.size() < 1");
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
        for (ConfiguredBus bus : buses) {
            return bus.isInMainConnectedComponent();
        }
        return false;
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        for (ConfiguredBus bus : buses) {
            return bus.isInMainSynchronousComponent();
        }
        return false;
    }

    @Override
    public int getConnectedTerminalCount() {
        checkValidity();
        int count = 0;
        for (ConfiguredBus bus : buses) {
            count += bus.getTerminalCount();
        }
        return count;
    }

    @Override
    public Iterable<TerminalExt> getConnectedTerminals() {
        checkValidity();
        List<Iterable<TerminalExt>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getConnectedTerminals());
        }
        return Iterables.concat(iterables);
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
        throw new AssertionError("Should not happen");
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
        throw new AssertionError("Should not happen");
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
    public Iterable<TwoWindingsTransformer> getTwoWindingTransformers() {
        checkValidity();
        List<Iterable<TwoWindingsTransformer>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getTwoWindingTransformers());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingTransformerStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getTwoWindingTransformerStream);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingTransformers() {
        checkValidity();
        List<Iterable<ThreeWindingsTransformer>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getThreeWindingTransformers());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingTransformerStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getThreeWindingTransformerStream);
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

    /**
     * @deprecated Use {@link #getShuntCompensators()} instead.
     */
    @Override
    @Deprecated
    public Iterable<ShuntCompensator> getShunts() {
        return getShuntCompensators();
    }

    /**
     * @deprecated Use {@link #getShuntCompensatorStream()} instead.
     */
    @Override
    @Deprecated
    public Stream<ShuntCompensator> getShuntStream() {
        return getShuntCompensatorStream();
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
    public Iterable<DanglingLine> getDanglingLines() {
        checkValidity();
        List<Iterable<DanglingLine>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getDanglingLines());
        }
        return Iterables.concat(iterables);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        checkValidity();
        return buses.stream().flatMap(ConfiguredBus::getDanglingLineStream);
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
