/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractVoltageLevel extends AbstractIdentifiable<VoltageLevel> implements VoltageLevelExt {

    private final SubstationImpl substation;

    private double nominalV;

    private double lowVoltageLimit;

    private double highVoltageLimit;

    AbstractVoltageLevel(String id, String name, boolean fictitious, SubstationImpl substation,
                         double nominalV, double lowVoltageLimit, double highVoltageLimit) {
        super(id, name, fictitious);
        this.substation = substation;
        this.nominalV = nominalV;
        this.lowVoltageLimit = lowVoltageLimit;
        this.highVoltageLimit = highVoltageLimit;
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.VOLTAGE_LEVEL;
    }

    @Override
    public SubstationImpl getSubstation() {
        return substation;
    }

    @Override
    public NetworkImpl getNetwork() {
        return substation.getNetwork();
    }

    private void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    @Override
    public double getNominalV() {
        return nominalV;
    }

    @Override
    public VoltageLevelExt setNominalV(double nominalV) {
        ValidationUtil.checkNominalV(this, nominalV);
        double oldValue = this.nominalV;
        this.nominalV = nominalV;
        notifyUpdate("nominalV", oldValue, nominalV);
        return this;
    }

    @Override
    public double getLowVoltageLimit() {
        return lowVoltageLimit;
    }

    @Override
    public VoltageLevel setLowVoltageLimit(double lowVoltageLimit) {
        ValidationUtil.checkVoltageLimits(this, lowVoltageLimit, highVoltageLimit);
        double oldValue = this.lowVoltageLimit;
        this.lowVoltageLimit = lowVoltageLimit;
        notifyUpdate("lowVoltageLimit", oldValue, lowVoltageLimit);
        return this;
    }

    @Override
    public double getHighVoltageLimit() {
        return highVoltageLimit;
    }

    @Override
    public VoltageLevel setHighVoltageLimit(double highVoltageLimit) {
        ValidationUtil.checkVoltageLimits(this, lowVoltageLimit, highVoltageLimit);
        double oldValue = this.highVoltageLimit;
        this.highVoltageLimit = highVoltageLimit;
        notifyUpdate("highVoltageLimit", oldValue, highVoltageLimit);
        return this;
    }

    @Override
    public <T extends Connectable> T getConnectable(String id, Class<T> aClass) {
        // the fastest way to get the equipment is to look in the index
        // and then check if it is connected to this substation
        T connectable = substation.getNetwork().getIndex().get(id, aClass);
        if (connectable == null) {
            return null;
        } else if (connectable instanceof Injection) {
            return ((Injection) connectable).getTerminal().getVoltageLevel() == this
                    ? connectable : null;
        } else if (connectable instanceof Branch) {
            return ((Branch) connectable).getTerminal1().getVoltageLevel() == this
                    || ((Branch) connectable).getTerminal2().getVoltageLevel() == this
                    ? connectable : null;
        } else if (connectable instanceof ThreeWindingsTransformer) {
            return ((ThreeWindingsTransformer) connectable).getLeg1().getTerminal().getVoltageLevel() == this
                    || ((ThreeWindingsTransformer) connectable).getLeg2().getTerminal().getVoltageLevel() == this
                    || ((ThreeWindingsTransformer) connectable).getLeg3().getTerminal().getVoltageLevel() == this
                    ? connectable : null;
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public <T extends Connectable> Iterable<T> getConnectables(Class<T> clazz) {
        Iterable<Terminal> terminals = getTerminals();
        return FluentIterable.from(terminals)
                .transform(Terminal::getConnectable)
                .filter(clazz)
                .toSet();
    }

    @Override
    public <T extends Connectable> Stream<T> getConnectableStream(Class<T> clazz) {
        return getTerminalStream()
                .map(Terminal::getConnectable)
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .distinct();
    }

    @Override
    public <T extends Connectable> int getConnectableCount(Class<T> clazz) {
        return Ints.checkedCast(getConnectableStream(clazz).count());
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return FluentIterable.from(getTerminals())
                .transform(Terminal::getConnectable)
                .toSet();
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return getTerminalStream()
                .map(Terminal::getConnectable)
                .distinct();
    }

    @Override
    public int getConnectableCount() {
        return Ints.checkedCast(getConnectableStream().count());
    }

    @Override
    public GeneratorAdder newGenerator() {
        return new GeneratorAdderImpl(this);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getConnectables(Generator.class);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getConnectableStream(Generator.class);
    }

    @Override
    public int getGeneratorCount() {
        return getConnectableCount(Generator.class);
    }

    @Override
    public BatteryAdder newBattery() {
        return new BatteryAdderImpl(this);
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getConnectables(Battery.class);
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getConnectableStream(Battery.class);
    }

    @Override
    public int getBatteryCount() {
        return getConnectableCount(Battery.class);
    }

    @Override
    public LoadAdder newLoad() {
        return new LoadAdderImpl(this);
    }

    @Override
    public Iterable<Load> getLoads() {
        return getConnectables(Load.class);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getConnectableStream(Load.class);
    }

    @Override
    public int getLoadCount() {
        return getConnectableCount(Load.class);
    }

    @Override
    public ShuntCompensatorAdder newShuntCompensator() {
        return new ShuntCompensatorAdderImpl(this);
    }

    @Override
    public int getShuntCompensatorCount() {
        return getConnectableCount(ShuntCompensator.class);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getConnectables(ShuntCompensator.class);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getConnectableStream(ShuntCompensator.class);
    }

    @Override
    public DanglingLineAdder newDanglingLine() {
        return new DanglingLineAdderImpl(this);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return getConnectables(DanglingLine.class);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return getConnectableStream(DanglingLine.class);
    }

    @Override
    public int getDanglingLineCount() {
        return getConnectableCount(DanglingLine.class);
    }

    @Override
    public StaticVarCompensatorAdderImpl newStaticVarCompensator() {
        return new StaticVarCompensatorAdderImpl(this);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getConnectables(StaticVarCompensator.class);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getConnectableStream(StaticVarCompensator.class);
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return getConnectableCount(StaticVarCompensator.class);
    }

    @Override
    public int getVscConverterStationCount() {
        return getConnectableCount(VscConverterStation.class);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return getConnectables(VscConverterStation.class);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getConnectableStream(VscConverterStation.class);
    }

    @Override
    public VscConverterStationAdder newVscConverterStation() {
        return new VscConverterStationAdderImpl(this);
    }

    @Override
    public int getLccConverterStationCount() {
        return getConnectableCount(LccConverterStation.class);
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getConnectables(LccConverterStation.class);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getConnectableStream(LccConverterStation.class);
    }

    @Override
    public LccConverterStationAdder newLccConverterStation() {
        return new LccConverterStationAdderImpl(this);
    }

    @Override
    protected String getTypeDescription() {
        return "Voltage level";
    }

    protected abstract Iterable<Terminal> getTerminals();

    protected abstract Stream<Terminal> getTerminalStream();

    @Override
    public void visitEquipments(TopologyVisitor visitor) {
        AbstractBus.visitEquipments(getTerminals(), visitor);
    }

    protected static void addNextTerminals(TerminalExt otherTerminal, List<TerminalExt> nextTerminals) {
        Objects.requireNonNull(otherTerminal);
        Objects.requireNonNull(nextTerminals);
        Connectable otherConnectable = otherTerminal.getConnectable();
        if (otherConnectable instanceof Branch) {
            Branch branch = (Branch) otherConnectable;
            if (branch.getTerminal1() == otherTerminal) {
                nextTerminals.add((TerminalExt) branch.getTerminal2());
            } else if (branch.getTerminal2() == otherTerminal) {
                nextTerminals.add((TerminalExt) branch.getTerminal1());
            } else {
                throw new AssertionError();
            }
        } else if (otherConnectable instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer ttc = (ThreeWindingsTransformer) otherConnectable;
            if (ttc.getLeg1().getTerminal() == otherTerminal) {
                nextTerminals.add((TerminalExt) ttc.getLeg2().getTerminal());
                nextTerminals.add((TerminalExt) ttc.getLeg3().getTerminal());
            } else if (ttc.getLeg2().getTerminal() == otherTerminal) {
                nextTerminals.add((TerminalExt) ttc.getLeg1().getTerminal());
                nextTerminals.add((TerminalExt) ttc.getLeg3().getTerminal());
            } else if (ttc.getLeg3().getTerminal() == otherTerminal) {
                nextTerminals.add((TerminalExt) ttc.getLeg1().getTerminal());
                nextTerminals.add((TerminalExt) ttc.getLeg2().getTerminal());
            } else {
                throw new AssertionError();
            }
        }
    }

    @Override
    public void remove() {
        VoltageLevels.checkRemovability(this);

        // Remove all connectables
        List<Connectable> connectables = Lists.newArrayList(getConnectables());
        for (Connectable connectable : connectables) {
            connectable.remove();
        }

        // Remove the topology
        removeTopology();

        // Remove this voltage level from the network
        getSubstation().remove(this);
        getNetwork().getIndex().remove(this);

        getNetwork().getListeners().notifyRemoval(this);
    }

    protected abstract void removeTopology();
}
