/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import com.google.common.collect.Iterables;
import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.ConnectedComponent;
import eu.itesla_project.iidm.network.DanglingLine;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.ShuntCompensator;
import eu.itesla_project.iidm.network.ThreeWindingsTransformer;
import eu.itesla_project.iidm.network.TopologyVisitor;
import eu.itesla_project.iidm.network.TwoWindingsTransformer;
import eu.itesla_project.iidm.network.VoltageLevel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class MergedBus extends IdentifiableImpl implements CalculatedBus {

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
            throw new ITeslaException("Bus has been invalidated");
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
    public float getV() {
        checkValidity();
        for (Bus b : buses) {
            if (!Float.isNaN(b.getV())) {
                return b.getV();
            }
        }
        return Float.NaN;
    }

    @Override
    public BusExt setV(float v) {
        checkValidity();
        for (ConfiguredBus bus : buses) {
            bus.setV(v);
        }
        return this;
    }

    @Override
    public float getAngle() {
        checkValidity();
        for (Bus b : buses) {
            if (!Float.isNaN(b.getAngle())) {
                return b.getAngle();
            }
        }
        return Float.NaN;
    }

    @Override
    public BusExt setAngle(float angle) {
        checkValidity();
        for (ConfiguredBus bus : buses) {
            bus.setAngle(angle);
        }
        return this;
    }

    @Override
    public float getP() {
        checkValidity();
        float p = 0;
        for (Bus b : buses) {
            p += b.getP();
        }
        return p;
    }

    @Override
    public float getQ() {
        checkValidity();
        float q = 0;
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
    public ConnectedComponent getConnectedComponent() {
        checkValidity();
        for (Bus b : buses) {
            ConnectedComponent cc = b.getConnectedComponent();
            if (cc != null) {
                return cc;
            }
        }
        throw new RuntimeException("Should not happened");
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
    public Iterable<TwoWindingsTransformer> getTwoWindingTransformers() {
        checkValidity();
        List<Iterable<TwoWindingsTransformer>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getTwoWindingTransformers());
        }
        return Iterables.concat(iterables);
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
    public Iterable<Generator> getGenerators() {
        checkValidity();
        List<Iterable<Generator>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getGenerators());
        }
        return Iterables.concat(iterables);
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
    public Iterable<ShuntCompensator> getShunts() {
        checkValidity();
        List<Iterable<ShuntCompensator>> iterables = new ArrayList<>(buses.size());
        for (ConfiguredBus bus : buses) {
            iterables.add(bus.getShunts());
        }
        return Iterables.concat(iterables);
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
