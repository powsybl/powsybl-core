/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ShortIdDictionary;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class VoltageLevelImpl extends AbstractIdentifiable<VoltageLevel> implements VoltageLevelExt {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;

    private final SubstationImpl substation;

    private double nominalV;

    private double lowVoltageLimit;

    private double highVoltageLimit;

    private AbstractTopologyModel topologyModel;

    /** Areas associated to this VoltageLevel, with at most one area for each area type */
    private final Set<Area> areas = new LinkedHashSet<>();

    private boolean removed = false;

    VoltageLevelImpl(String id, String name, boolean fictitious, SubstationImpl substation, Ref<NetworkImpl> networkRef,
                     Ref<SubnetworkImpl> subnetworkRef, double nominalV, double lowVoltageLimit, double highVoltageLimit,
                     TopologyKind topologyKind) {
        super(id, name, fictitious);
        this.substation = substation;
        this.networkRef = networkRef;
        this.subnetworkRef = subnetworkRef;
        this.nominalV = nominalV;
        this.lowVoltageLimit = lowVoltageLimit;
        this.highVoltageLimit = highVoltageLimit;
        this.topologyModel = switch (Objects.requireNonNull(topologyKind)) {
            case NODE_BREAKER -> new NodeBreakerTopologyModel(this);
            case BUS_BREAKER -> new BusBreakerTopologyModel(this);
        };
    }

    @Override
    public AbstractTopologyModel getTopologyModel() {
        return topologyModel;
    }

    @Override
    public String getSubnetworkId() {
        return Optional.ofNullable(subnetworkRef.get()).map(Identifiable::getId).orElse(null);
    }

    @Override
    public Ref<NetworkImpl> getNetworkRef() {
        return networkRef;
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.VOLTAGE_LEVEL;
    }

    @Override
    public Optional<Substation> getSubstation() {
        if (removed) {
            throw new PowsyblException("Cannot access substation of removed voltage level " + id);
        }
        return Optional.ofNullable(substation);
    }

    @Override
    public Iterable<Area> getAreas() {
        if (removed) {
            throwAreasRemovedVoltageLevel();
        }
        return areas;
    }

    @Override
    public Stream<Area> getAreasStream() {
        if (removed) {
            throwAreasRemovedVoltageLevel();
        }
        return areas.stream();
    }

    @Override
    public Optional<Area> getArea(String areaType) {
        Objects.requireNonNull(areaType);
        if (removed) {
            throwAreasRemovedVoltageLevel();
        }
        return areas.stream().filter(area -> Objects.equals(area.getAreaType(), areaType)).findFirst();
    }

    private void throwAreasRemovedVoltageLevel() {
        throw new PowsyblException("Cannot access areas of removed voltage level " + id);
    }

    @Override
    public void addArea(Area area) {
        Objects.requireNonNull(area);
        if (removed) {
            throw new PowsyblException("Cannot add areas to removed voltage level " + id);
        }
        if (areas.contains(area)) {
            // Nothing to do
            return;
        }

        // Check that the VoltageLevel belongs to the same network or subnetwork
        if (area.getParentNetwork() != getParentNetwork()) {
            throw new PowsyblException("VoltageLevel " + getId() + " cannot be added to Area " + area.getId() + ". It does not belong to the same network or subnetwork.");
        }

        // Check if the voltageLevel is already in another Area of the same type
        final Optional<Area> previousArea = getArea(area.getAreaType());
        if (previousArea.isPresent() && previousArea.get() != area) {
            // This instance already has a different area with the same AreaType
            throw new PowsyblException("VoltageLevel " + getId() + " is already in Area of the same type=" + previousArea.get().getAreaType() + " with id=" + previousArea.get().getId());
        }
        // No conflict, add the area to this voltageLevel and vice versa
        areas.add(area);
        area.addVoltageLevel(this);
    }

    @Override
    public void removeArea(Area area) {
        Objects.requireNonNull(area);
        if (removed) {
            throw new PowsyblException("Cannot remove areas from removed voltage level " + id);
        }
        areas.remove(area);
        area.removeVoltageLevel(this);
    }

    @Override
    public Substation getNullableSubstation() {
        return substation;
    }

    @Override
    public NodeBreakerViewExt getNodeBreakerView() {
        return topologyModel.getNodeBreakerView();
    }

    @Override
    public BusBreakerViewExt getBusBreakerView() {
        return topologyModel.getBusBreakerView();
    }

    @Override
    public BusViewExt getBusView() {
        return topologyModel.getBusView();
    }

    @Override
    public NetworkImpl getNetwork() {
        if (removed) {
            throw new PowsyblException("Cannot access network of removed voltage level " + id);
        }
        return Optional.ofNullable(networkRef)
                .map(Ref::get)
                .orElseGet(() -> Optional.ofNullable(substation)
                        .map(SubstationImpl::getNetwork)
                        .orElseThrow(() -> new PowsyblException(String.format("Voltage level %s has no container", id))));
    }

    @Override
    public NetworkExt getParentNetwork() {
        SubnetworkImpl subnetwork = subnetworkRef.get();
        return subnetwork != null ? subnetwork : getNetwork();
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
        T connectable = getNetwork().getIndex().get(id, aClass);
        if (connectable == null) {
            return null;
        } else if (connectable instanceof Injection<?> injection) {
            return injection.getTerminal().getVoltageLevel() == this
                    ? connectable : null;
        } else if (connectable instanceof Branch<?> branch) {
            return branch.getTerminal1().getVoltageLevel() == this
                    || branch.getTerminal2().getVoltageLevel() == this
                    ? connectable : null;
        } else if (connectable instanceof ThreeWindingsTransformer twt) {
            return twt.getLeg1().getTerminal().getVoltageLevel() == this
                    || twt.getLeg2().getTerminal().getVoltageLevel() == this
                    || twt.getLeg3().getTerminal().getVoltageLevel() == this
                    ? connectable : null;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public <T extends Connectable> Iterable<T> getConnectables(Class<T> clazz) {
        return topologyModel.getConnectables(clazz);
    }

    @Override
    public <T extends Connectable> Stream<T> getConnectableStream(Class<T> clazz) {
        return topologyModel.getConnectableStream(clazz);
    }

    @Override
    public <T extends Connectable> int getConnectableCount(Class<T> clazz) {
        return topologyModel.getConnectableCount(clazz);
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return topologyModel.getConnectables();
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return topologyModel.getConnectableStream();
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
    public Iterable<Switch> getSwitches() {
        return topologyModel.getSwitches();
    }

    @Override
    public int getSwitchCount() {
        return topologyModel.getSwitchCount();
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
    public Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream(danglingLineFilter).collect(Collectors.toList());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        return getConnectableStream(DanglingLine.class).filter(danglingLineFilter.getPredicate());
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
    public int getLineCount() {
        return getConnectableCount(Line.class);
    }

    @Override
    public Iterable<Line> getLines() {
        return getConnectables(Line.class);
    }

    @Override
    public Stream<Line> getLineStream() {
        return getConnectableStream(Line.class);
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return getConnectableCount(TwoWindingsTransformer.class);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getConnectables(TwoWindingsTransformer.class);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getConnectableStream(TwoWindingsTransformer.class);
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return getConnectableCount(ThreeWindingsTransformer.class);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getConnectables(ThreeWindingsTransformer.class);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getConnectableStream(ThreeWindingsTransformer.class);
    }

    @Override
    public GroundAdder newGround() {
        return new GroundAdderImpl(this);
    }

    @Override
    public Iterable<Ground> getGrounds() {
        return getConnectables(Ground.class);
    }

    @Override
    public Stream<Ground> getGroundStream() {
        return getConnectableStream(Ground.class);
    }

    @Override
    public int getGroundCount() {
        return getConnectableCount(Ground.class);
    }

    @Override
    protected String getTypeDescription() {
        return "Voltage level";
    }

    @Override
    public void visitEquipments(TopologyVisitor visitor) {
        AbstractBus.visitEquipments(topologyModel.getTerminals(), visitor);
    }

    @Override
    public TopologyKind getTopologyKind() {
        return topologyModel.getTopologyKind();
    }

    @Override
    public void printTopology() {
        topologyModel.printTopology();
    }

    @Override
    public void printTopology(PrintStream out, ShortIdDictionary dict) {
        topologyModel.printTopology(out, dict);
    }

    @Override
    public void exportTopology(Path file) throws IOException {
        topologyModel.exportTopology(file);
    }

    @Override
    public void exportTopology(Writer writer, Random random) {
        topologyModel.exportTopology(writer, random);
    }

    @Override
    public void exportTopology(Writer writer) {
        topologyModel.exportTopology(writer);
    }

    @Override
    public void remove() {
        VoltageLevels.checkRemovability(this);

        NetworkImpl network = getNetwork();
        network.getListeners().notifyBeforeRemoval(this);

        // Remove all connectables
        List<Connectable> connectables = Lists.newArrayList(getConnectables());
        for (Connectable connectable : connectables) {
            connectable.remove();
        }

        // Remove the topology
        topologyModel.removeTopology();

        // Remove this voltage level from the areas
        getAreas().forEach(area -> area.removeVoltageLevel(this));
        // Remove this voltage level from the network
        getSubstation().map(SubstationImpl.class::cast).ifPresent(s -> s.remove(this));
        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
        removed = true;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        topologyModel.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        topologyModel.reduceVariantArraySize(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        topologyModel.deleteVariantArrayElement(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        topologyModel.allocateVariantArrayElement(indexes, sourceIndex);
    }

    @Override
    public void convertToTopology(TopologyKind newTopologyKind) {
        Objects.requireNonNull(newTopologyKind);
        if (newTopologyKind != topologyModel.getTopologyKind()) {
            if (newTopologyKind == TopologyKind.NODE_BREAKER) {
                throw new PowsyblException("Topology model conversion from bus/breaker to node/breaker not yet supported");
            } else if (newTopologyKind == TopologyKind.BUS_BREAKER) {
                BusBreakerTopologyModel newTopologyModel = new BusBreakerTopologyModel(this);

                for (Bus bus : topologyModel.getBusBreakerView().getBuses()) {
                    // no notification, this is just a mutation of a calculation bus to a configured bus
                    ConfiguredBusImpl configuredBus = new ConfiguredBusImpl(bus.getId(), bus.getOptionalName().orElse(null), bus.isFictitious(), this);
                    newTopologyModel.addBus(configuredBus);
                }

                // transfer retained switches
                for (Switch sw : topologyModel.getBusBreakerView().getSwitchStream().toList()) {
                    String busId1 = topologyModel.getBusBreakerView().getBus1(sw.getId()).getId();
                    String busId2 = topologyModel.getBusBreakerView().getBus2(sw.getId()).getId();
                    // no notification, this is just a transfer
                    ((NodeBreakerTopologyModel) topologyModel).removeSwitchFromTopology(sw.getId(), false);
                    newTopologyModel.addSwitchToTopology((SwitchImpl) sw, busId1, busId2);
                }

                // reconnect all connectable to new topology model
                // first store all bus/breaker topological infos associated to this terminal because we will start moving
                // terminal from old mode to new one, it will modify the old topology model
                List<Triple<TerminalExt, Bus, Boolean>> oldTopologyModelInfos = new ArrayList<>();
                for (Terminal oldTerminal : topologyModel.getTerminals()) {
                    if (oldTerminal.getConnectable().getType() != IdentifiableType.BUSBAR_SECTION) {
                        Bus connectableBus = oldTerminal.getBusBreakerView().getConnectableBus();
                        boolean connected = oldTerminal.isConnected();
                        oldTopologyModelInfos.add(Triple.of((TerminalExt) oldTerminal, connectableBus, connected));
                    }
                }

                for (var triple : oldTopologyModelInfos) {
                    TerminalExt oldTerminalExt = triple.getLeft();
                    Bus connectableBus = triple.getMiddle();
                    boolean connected = triple.getRight();

                    // if there is no way to find a connectable bus, remove the connectable
                    // an alternative would be to connect them all to a new trash configured bus
                    if (connectableBus != null) {
                        AbstractConnectable<?> connectable = oldTerminalExt.getConnectable();

                        // create the new terminal with new type
                        TerminalExt newTerminalExt = new TerminalBuilder(networkRef, this, oldTerminalExt.getSide())
                                .setBus(connected ? connectableBus.getId() : null)
                                .setConnectableBus(connectableBus.getId())
                                .build();

                        // first attach new terminal to connectable and to new topology model
                        newTerminalExt.setConnectable(connectable);
                        newTopologyModel.attach(newTerminalExt, false);

                        // then we can detach the old terminal
                        TopologyPoint oldTopologyPoint = oldTerminalExt.getTopologyPoint();
                        topologyModel.detach(oldTerminalExt);

                        // replace the old terminal by the new terminal in the connectable
                        connectable.replaceTerminal(oldTerminalExt, oldTopologyPoint, newTerminalExt, false);
                    } else {
                        // here keep the removal notification
                        oldTerminalExt.getConnectable().remove();
                    }
                }

                // also here keep the notification for remaining switches and internal connection removal
                topologyModel.removeTopology();

                TopologyKind oldTopologyKind = topologyModel.getTopologyKind();
                topologyModel = newTopologyModel;

                notifyUpdate("topologyKind", oldTopologyKind, newTopologyKind);
            }
        }
    }
}
