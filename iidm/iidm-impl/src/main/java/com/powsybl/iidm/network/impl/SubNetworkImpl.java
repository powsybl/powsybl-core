/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class SubNetworkImpl extends AbstractNetwork {

    private final NetworkImpl parent;

    SubNetworkImpl(NetworkImpl parent, String id, String name, String sourceFormat) {
        super(id, name, sourceFormat);
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public Collection<Network> getSubNetworks() {
        return Collections.emptyList();
    }

    @Override
    public Network getSubNetwork(String id) {
        return null;
    }

    @Override
    public NetworkImpl getNetwork() {
        return parent;
    }

    @Override
    public VariantManager getVariantManager() {
        return parent.getVariantManager();
    }

    @Override
    public Set<Country> getCountries() {
        return parent.getSubstationStream()
                .filter(s -> s.getClosestNetwork() == this)
                .map(s -> s.getCountry().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public int getCountryCount() {
        return (int) parent.getSubstationStream()
                .filter(s -> s.getClosestNetwork() == this)
                .map(s -> s.getCountry().orElse(null))
                .filter(Objects::nonNull)
                .count();
    }

    @Override
    public SubstationAdder newSubstation() {
        return parent.newSubstation(id);
    }

    @Override
    public Iterable<Substation> getSubstations() {
        return getSubstationStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Substation> getSubstationStream() {
        return parent.getSubstationStream().filter(s -> s.getClosestNetwork() == this);
    }

    @Override
    public int getSubstationCount() {
        return (int) getSubstationStream().count();
    }

    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        return StreamSupport.stream(parent.getSubstations(country, tsoId, geographicalTags).spliterator(), false)
                .filter(s -> s.getClosestNetwork() == this)
                .collect(Collectors.toSet());
    }

    @Override
    public Iterable<Substation> getSubstations(String country, String tsoId, String... geographicalTags) {
        return StreamSupport.stream(parent.getSubstations(country, tsoId, geographicalTags).spliterator(), false)
                .filter(s -> s.getClosestNetwork() == this)
                .collect(Collectors.toSet());
    }

    @Override
    public Substation getSubstation(String id) {
        Substation s = parent.getSubstation(id);
        if (s != null && s.getClosestNetwork() == this) {
            return s;
        }
        return null;
    }

    @Override
    public VoltageLevelAdder newVoltageLevel() {
        return parent.newVoltageLevel(id);
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return getVoltageLevelStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return parent.getVoltageLevelStream().filter(vl -> vl.getClosestNetwork() == this);
    }

    @Override
    public int getVoltageLevelCount() {
        return (int) getVoltageLevelStream().count();
    }

    @Override
    public VoltageLevel getVoltageLevel(String id) {
        VoltageLevel vl = parent.getVoltageLevel(id);
        if (vl != null && vl.getClosestNetwork() == this) {
            return vl;
        }
        return null;
    }

    @Override
    public LineAdder newLine() {
        return parent.newLine(id);
    }

    @Override
    public Iterable<Line> getLines() {
        return getLineStream().collect(Collectors.toSet());
    }

    @Override
    public Branch<?> getBranch(String branchId) {
        Branch<?> b = parent.getBranch(branchId);
        if (b != null && b.getClosestNetwork() == this) {
            return b;
        }
        return null;
    }

    @Override
    public Iterable<Branch> getBranches() {
        return getBranchStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Branch> getBranchStream() {
        return parent.getBranchStream()
                .filter(b -> b.getClosestNetwork() == this);
    }

    @Override
    public int getBranchCount() {
        return (int) getBranchStream().count();
    }

    @Override
    public Stream<Line> getLineStream() {
        return parent.getLineStream()
                .filter(l -> l.getClosestNetwork() == this);
    }

    @Override
    public int getLineCount() {
        return (int) getLineStream().count();
    }

    @Override
    public Line getLine(String id) {
        Line l = parent.getLine(id);
        if (l != null && l.getClosestNetwork() == this) {
            return l;
        }
        return null;
    }

    @Override
    public TieLineAdder newTieLine() {
        return parent.newTieLine(); // FIXME
    }

    @Override
    public TwoWindingsTransformerAdder newTwoWindingsTransformer() {
        return parent.newTwoWindingsTransformer(); // FIXME
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getTwoWindingsTransformerStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return parent.getTwoWindingsTransformerStream()
                .filter(t -> t.getClosestNetwork() == this);
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return (int) getTwoWindingsTransformerStream().count();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        TwoWindingsTransformer twt = parent.getTwoWindingsTransformer(id);
        if (twt != null && twt.getClosestNetwork() == this) {
            return twt;
        }
        return null;
    }

    @Override
    public ThreeWindingsTransformerAdder newThreeWindingsTransformer() {
        return parent.newThreeWindingsTransformer(); // FIXME
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getThreeWindingsTransformerStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return parent.getThreeWindingsTransformerStream()
                .filter(t -> t.getClosestNetwork() == this);
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return (int) getThreeWindingsTransformerStream().count();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        ThreeWindingsTransformer twt = parent.getThreeWindingsTransformer(id);
        if (twt != null && twt.getClosestNetwork() == this) {
            return twt;
        }
        return null;
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getGeneratorStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return parent.getGeneratorStream().filter(g -> g.getClosestNetwork() == this);
    }

    @Override
    public int getGeneratorCount() {
        return (int) getGeneratorStream().count();
    }

    @Override
    public Generator getGenerator(String id) {
        Generator g = parent.getGenerator(id);
        if (g != null && g.getClosestNetwork() == this) {
            return g;
        }
        return null;
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getBatteryStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return parent.getBatteryStream().filter(b -> b.getClosestNetwork() == this);
    }

    @Override
    public int getBatteryCount() {
        return (int) getBatteryStream().count();
    }

    @Override
    public Battery getBattery(String id) {
        Battery b = parent.getBattery(id);
        if (b != null && b.getClosestNetwork() == this) {
            return b;
        }
        return null;
    }

    @Override
    public Iterable<Load> getLoads() {
        return getLoadStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Load> getLoadStream() {
        return parent.getLoadStream().filter(l -> l.getClosestNetwork() == this);
    }

    @Override
    public int getLoadCount() {
        return (int) getLoadStream().count();
    }

    @Override
    public Load getLoad(String id) {
        Load l = parent.getLoad(id);
        if (l != null && l.getClosestNetwork() == this) {
            return l;
        }
        return null;
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getShuntCompensatorStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return parent.getShuntCompensatorStream().filter(s -> s.getClosestNetwork() == this);
    }

    @Override
    public int getShuntCompensatorCount() {
        return (int) getShuntCompensatorStream().count();
    }

    @Override
    public ShuntCompensator getShuntCompensator(String id) {
        ShuntCompensator s = parent.getShuntCompensator(id);
        if (s != null && s.getClosestNetwork() == this) {
            return s;
        }
        return null;
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return getDanglingLineStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return parent.getDanglingLineStream().filter(dl -> dl.getClosestNetwork() == this); // TODO add split tie line / line
    }

    @Override
    public int getDanglingLineCount() {
        return (int) getDanglingLineStream().count();
    }

    @Override
    public DanglingLine getDanglingLine(String id) {
        DanglingLine dl = parent.getDanglingLine(id);
        if (dl != null && dl.getClosestNetwork() == this) {
            return dl;
        }
        // TODO
        // Line l = parent.getLine(id);
        // if (l.getTerminal1().getVoltageLevel().getClosestNetwork() == this || l.getTerminal2().getVoltageLevel().getClosestNetwork() == this)
        // transform to dangling line on the correct side
        return null;
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getStaticVarCompensatorStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return parent.getStaticVarCompensatorStream().filter(s -> s.getClosestNetwork() == this);
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return (int) getStaticVarCompensatorStream().count();
    }

    @Override
    public StaticVarCompensator getStaticVarCompensator(String id) {
        StaticVarCompensator s = parent.getStaticVarCompensator(id);
        if (s != null && s.getClosestNetwork() == this) {
            return s;
        }
        return null;
    }

    @Override
    public Switch getSwitch(String id) {
        Switch s = parent.getSwitch(id);
        if (s != null && s.getClosestNetwork() == this) {
            return s;
        }
        return null;
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return getSwitchStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return parent.getSwitchStream().filter(s -> s.getClosestNetwork() == this);
    }

    @Override
    public int getSwitchCount() {
        return (int) getSwitchStream().count();
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        BusbarSection b = parent.getBusbarSection(id);
        if (b != null && b.getClosestNetwork() == this) {
            return b;
        }
        return null;
    }

    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        return getBusbarSectionStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return parent.getBusbarSectionStream().filter(b -> b.getClosestNetwork() == this);
    }

    @Override
    public int getBusbarSectionCount() {
        return (int) getBusbarSectionStream().count();
    }

    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        return getHvdcConverterStationStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        return parent.getHvdcConverterStationStream().filter(s -> s.getClosestNetwork() == this);
    }

    @Override
    public int getHvdcConverterStationCount() {
        return (int) getHvdcConverterStationStream().count();
    }

    @Override
    public HvdcConverterStation<?> getHvdcConverterStation(String id) {
        HvdcConverterStation<?> s = parent.getHvdcConverterStation(id);
        if (s != null && s.getClosestNetwork() == this) {
            return s;
        }
        return null;
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getLccConverterStationStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return parent.getLccConverterStationStream().filter(s -> s.getClosestNetwork() == this);
    }

    @Override
    public int getLccConverterStationCount() {
        return (int) getLccConverterStationStream().count();
    }

    @Override
    public LccConverterStation getLccConverterStation(String id) {
        LccConverterStation s = parent.getLccConverterStation(id);
        if (s != null && s.getClosestNetwork() == this) {
            return s;
        }
        return null;
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return getVscConverterStationStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return parent.getVscConverterStationStream().filter(s -> s.getClosestNetwork() == this);
    }

    @Override
    public int getVscConverterStationCount() {
        return (int) getVscConverterStationStream().count();
    }

    @Override
    public VscConverterStation getVscConverterStation(String id) {
        VscConverterStation s = parent.getVscConverterStation(id);
        if (s != null && s.getClosestNetwork() == this) {
            return s;
        }
        return null;
    }

    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        return getHvdcLineStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return parent.getHvdcLineStream().filter(l -> l.getClosestNetwork() == this);
    }

    @Override
    public int getHvdcLineCount() {
        return (int) getHvdcLineStream().count();
    }

    @Override
    public HvdcLine getHvdcLine(String id) {
        HvdcLine l = parent.getHvdcLine(id);
        if (l != null && l.getClosestNetwork() == this) {
            return l;
        }
        return null;
    }

    @Override
    public HvdcLine getHvdcLine(HvdcConverterStation converterStation) {
        if (converterStation.getClosestNetwork() == this) {
            return getHvdcLineStream()
                    .filter(l -> l.getConverterStation1() == converterStation || l.getConverterStation2() == converterStation)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public HvdcLineAdder newHvdcLine() {
        return parent.newHvdcLine(); // FIXME
    }

    @Override
    public Identifiable<?> getIdentifiable(String id) {
        Identifiable<?> i = parent.getIdentifiable(id);
        if (i != null && i.getClosestNetwork() == this) {
            return i;
        }
        return null;
    }

    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        return parent.getIdentifiables().stream().filter(i -> i.getClosestNetwork() == this).collect(Collectors.toSet());
    }

    @Override
    public <C extends Connectable> Iterable<C> getConnectables(Class<C> clazz) {
        return getConnectableStream(clazz).collect(Collectors.toSet());
    }

    @Override
    public <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
        return parent.getConnectableStream(clazz).filter(c -> c.getClosestNetwork() == this);
    }

    @Override
    public <C extends Connectable> int getConnectableCount(Class<C> clazz) {
        return (int) getConnectableStream(clazz).count();
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return getConnectableStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return parent.getConnectableStream().filter(c -> c.getClosestNetwork() == this);
    }

    @Override
    public Connectable<?> getConnectable(String id) {
        Connectable<?> c = parent.getConnectable(id);
        if (c != null && c.getClosestNetwork() == this) {
            return c;
        }
        return null;
    }

    @Override
    public int getConnectableCount() {
        return (int) getConnectableStream().count();
    }

    class BusBreakerViewImpl implements BusBreakerView {

        @Override
        public Iterable<Bus> getBuses() {
            return getBusStream().collect(Collectors.toSet());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return parent.getBusBreakerView().getBusStream().filter(b -> b.getClosestNetwork() == SubNetworkImpl.this);
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return getSwitchStream().collect(Collectors.toSet());
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return parent.getBusBreakerView().getSwitchStream().filter(s -> s.getClosestNetwork() == SubNetworkImpl.this);
        }

        @Override
        public int getSwitchCount() {
            return (int) getSwitchStream().count();
        }

        @Override
        public Bus getBus(String id) {
            Bus b = parent.getBusBreakerView().getBus(id);
            if (b != null && b.getClosestNetwork() == SubNetworkImpl.this) {
                return b;
            }
            return null;
        }
    }

    private final BusBreakerViewImpl busBreakerView = new BusBreakerViewImpl();

    @Override
    public BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    class BusViewImpl implements BusView {

        @Override
        public Iterable<Bus> getBuses() {
            return getBusStream().collect(Collectors.toSet());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return parent.getBusView().getBusStream().filter(b -> b.getClosestNetwork() == SubNetworkImpl.this);
        }

        @Override
        public Bus getBus(String id) {
            Bus b = parent.getBusView().getBus(id);
            if (b != null && b.getClosestNetwork() == SubNetworkImpl.this) {
                return b;
            }
            return null;
        }

        @Override
        public Collection<Component> getConnectedComponents() {
            return parent.getBusView().getConnectedComponents().stream()
                    .filter(c -> c.getBusStream().anyMatch(b -> b.getClosestNetwork() == SubNetworkImpl.this))
                    .map(c -> new SubComponent(c, SubNetworkImpl.this))
                    .collect(Collectors.toSet());
        }

        @Override
        public Collection<Component> getSynchronousComponents() {
            return parent.getBusView().getSynchronousComponents().stream()
                    .filter(c -> c.getBusStream().anyMatch(b -> b.getClosestNetwork() == SubNetworkImpl.this))
                    .map(c -> new SubComponent(c, SubNetworkImpl.this))
                    .collect(Collectors.toSet());
        }
    }

    private final BusViewImpl busView = new BusViewImpl();

    @Override
    public BusView getBusView() {
        return busView;
    }

    @Override
    public void merge(Network other) {
        throw new UnsupportedOperationException("Network " + id + " is already merged in network " + parent.getId());
    }

    @Override
    public void merge(Network... others) {
        throw new UnsupportedOperationException("Network " + id + " is already merged in network " + parent.getId());
    }

    @Override
    public void addListener(NetworkListener listener) {
        parent.addListener(listener);
    }

    @Override
    public void removeListener(NetworkListener listener) {
        parent.removeListener(listener);
    }

    @Override
    public ValidationLevel runValidationChecks() {
        return parent.runValidationChecks();
    }

    @Override
    public ValidationLevel runValidationChecks(boolean throwsException) {
        return parent.runValidationChecks(throwsException);
    }

    @Override
    public ValidationLevel runValidationChecks(boolean throwsException, Reporter reporter) {
        return parent.runValidationChecks(throwsException, reporter);
    }

    @Override
    public ValidationLevel getValidationLevel() {
        return parent.getValidationLevel();
    }

    @Override
    public Network setMinimumAcceptableValidationLevel(ValidationLevel validationLevel) {
        return parent.setMinimumAcceptableValidationLevel(validationLevel);
    }

    @Override
    public <I extends Identifiable<I>> Stream<I> getIdentifiableStream(IdentifiableType identifiableType) {
        return (Stream<I>) parent.getIdentifiableStream(identifiableType).filter(i -> i.getClosestNetwork() == this);
    }
}
