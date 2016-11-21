/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractVoltageLevel extends IdentifiableImpl<VoltageLevel> implements VoltageLevelExt {

    private final SubstationImpl substation;

    private float nominalV;

    private float lowVoltageLimit;

    private float highVoltageLimit;

    AbstractVoltageLevel(String id, String name, SubstationImpl substation,
                         float nominalV, float lowVoltageLimit, float highVoltageLimit) {
        super(id, name);
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
    public float getNominalV() {
        return nominalV;
    }

    @Override
    public VoltageLevelExt setNominalV(float nominalV) {
        ValidationUtil.checkNominalV(this, nominalV);
        float oldValue = this.nominalV;
        this.nominalV = nominalV;
        notifyUpdate("nominalV", oldValue, nominalV);
        return this;
    }

    @Override
    public float getLowVoltageLimit() {
        return lowVoltageLimit;
    }

    @Override
    public VoltageLevel setLowVoltageLimit(float lowVoltageLimit) {
        ValidationUtil.checkVoltageLimits(this, lowVoltageLimit, highVoltageLimit);
        float oldValue = this.lowVoltageLimit;
        this.lowVoltageLimit = lowVoltageLimit;
        notifyUpdate("lowVoltageLimit", oldValue, lowVoltageLimit);
        return this;
    }

    @Override
    public float getHighVoltageLimit() {
        return highVoltageLimit;
    }

    @Override
    public VoltageLevel setHighVoltageLimit(float highVoltageLimit) {
        ValidationUtil.checkVoltageLimits(this, lowVoltageLimit, highVoltageLimit);
        float oldValue = this.highVoltageLimit;
        this.highVoltageLimit = highVoltageLimit;
        notifyUpdate("highVoltageLimit", oldValue, highVoltageLimit);
        return this;
    }

    @Override
    public <T extends Connectable> T getConnectable(String id, Class<T> aClass) {
        // the fastest way to get the equipment is to look in the object store
        // and then check if it is connected to this substation
        T connectable = substation.getNetwork().getObjectStore().get(id, aClass);
        if (connectable == null) {
            return null;
        } else if (connectable instanceof SingleTerminalConnectable) {
            return ((SingleTerminalConnectable) connectable).getTerminal().getVoltageLevel() == this
                    ? connectable : null;
        } else if (connectable instanceof TwoTerminalsConnectable) {
            return ((TwoTerminalsConnectable) connectable).getTerminal1().getVoltageLevel() == this
                    || ((TwoTerminalsConnectable) connectable).getTerminal2().getVoltageLevel() == this
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
    public GeneratorAdder newGenerator() {
        return new GeneratorAdderImpl(this);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getConnectables(Generator.class);
    }

    @Override
    public int getGeneratorCount() {
        return getConnectableCount(Generator.class);
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
    public int getLoadCount() {
        return getConnectableCount(Load.class);
    }

    @Override
    public ShuntCompensatorAdder newShunt() {
        return new ShuntCompensatorAdderImpl(this);
    }

    @Override
    public int getShuntCount() {
        return getConnectableCount(ShuntCompensator.class);
    }

    @Override
    public Iterable<ShuntCompensator> getShunts() {
        return getConnectables(ShuntCompensator.class);
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
    public LccConverterStationAdder newLccConverterStation() {
        return new LccConverterStationAdderImpl(this);
    }

    @Override
    protected String getTypeDescription() {
        return "Voltage level";
    }

    protected abstract Iterable<Terminal> getTerminals();

    @Override
    public void visitEquipments(TopologyVisitor visitor) {
        AbstractBus.visitEquipments(getTerminals(), visitor);
    }

    protected static void addNextTerminals(TerminalExt otherTerminal, List<TerminalExt> nextTerminals) {
        Objects.requireNonNull(otherTerminal);
        Objects.requireNonNull(nextTerminals);
        Connectable otherConnectable = otherTerminal.getConnectable();
        if (otherConnectable instanceof TwoTerminalsConnectable) {
            TwoTerminalsConnectable ttc = (TwoTerminalsConnectable) otherConnectable;
            if (ttc.getTerminal1() == otherTerminal) {
                nextTerminals.add((TerminalExt) ttc.getTerminal2());
            } else if (ttc.getTerminal2() == otherTerminal) {
                nextTerminals.add(((TerminalExt) ttc.getTerminal1()));
            } else {
                throw new AssertionError();
            }
        } else if (otherConnectable instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer ttc = (ThreeWindingsTransformer) otherConnectable;
            if (ttc.getLeg1().getTerminal() == otherTerminal) {
                nextTerminals.add(((TerminalExt) ttc.getLeg2().getTerminal()));
                nextTerminals.add(((TerminalExt) ttc.getLeg3().getTerminal()));
            } else if (ttc.getLeg2().getTerminal() == otherTerminal) {
                nextTerminals.add(((TerminalExt) ttc.getLeg1().getTerminal()));
                nextTerminals.add(((TerminalExt) ttc.getLeg3().getTerminal()));
            } else if (ttc.getLeg3().getTerminal() == otherTerminal) {
                nextTerminals.add(((TerminalExt) ttc.getLeg1().getTerminal()));
                nextTerminals.add(((TerminalExt) ttc.getLeg2().getTerminal()));
            } else {
                throw new AssertionError();
            }
        }
    }
}
