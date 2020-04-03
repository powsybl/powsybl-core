/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class MergingViewIndex {

    /** Local storage for merged lines created */
    private final Map<String, MergedLine> mergedLineCached = new WeakHashMap<>();

    /** Local storage for adapters created */
    private final Map<Identifiable, AbstractAdapter<? extends Identifiable>> identifiableCached = new WeakHashMap<>();

    private final Map<Component, AbstractAdapter<? extends Component>> componentCached = new WeakHashMap<>();

    private final Map<Terminal, AbstractAdapter<? extends Terminal>> terminalCached = new WeakHashMap<>();

    private final Map<PhaseTapChanger, AbstractAdapter<? extends PhaseTapChanger>> ptcCached = new WeakHashMap<>();

    private final Map<RatioTapChanger, AbstractAdapter<? extends RatioTapChanger>> rtcCached = new WeakHashMap<>();

    /** Network asked to be merged */
    private final Collection<Network> networks = new ArrayList<>();

    /** Current merging view reference */
    private final MergingView currentView;

    /** Constructor */
    MergingViewIndex(final MergingView currentView) {
        // Keep reference on current view
        this.currentView = Objects.requireNonNull(currentView);
    }

    /** @return current merging view instance */
    MergingView getView() {
        return currentView;
    }

    /** @return stream of merging network */
    Stream<Network> getNetworkStream() {
        return networks.stream();
    }

    boolean contains(String id) {
        return Objects.nonNull(this.currentView.getIdentifiable(id));
    }

    /** Validate all networks added into merging network list */
    void checkAndAdd(final Network other) {
        // Check multi-variants network
        ValidationUtil.checkSingleVariant(other);
        // Check unique identifiable network
        ValidationUtil.checkUniqueIds(other, this);
        // Local storage for mergeable network
        networks.add(other);
        // Manage DanglingLines
        other.getDanglingLineStream().forEach(this::checkNewDanglingLine);
    }

    void checkNewDanglingLine(final DanglingLine dll2) {
        Objects.requireNonNull(dll2, "DanglingLine is null");
        // Manage DanglingLines
        final String code = dll2.getUcteXnodeCode();
        // Find other DanglingLine if exist
        final Set<DanglingLine> danglingLines = getNetworkStream().flatMap(Network::getDanglingLineStream).filter(d -> d.getUcteXnodeCode().equals(code)).collect(Collectors.toSet());
        for (DanglingLine dll1 : danglingLines) {
            if (dll1 != dll2) {
                // Create MergedLine from 2 dangling lines
                mergedLineCached.computeIfAbsent(code, key -> new MergedLine(this, dll1, dll2));
            }
        }
    }

    MergedLine getMergedLineByCode(final String ucteXnodeCode) {
        return mergedLineCached.get(ucteXnodeCode);
    }

    Line getMergedLine(final String id) {
        return mergedLineCached.values().stream()
                .filter(l -> l.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /** @return adapter according to given parameter */
    Identifiable getIdentifiable(final Identifiable identifiable) {
        if (identifiable instanceof Substation) {
            return getSubstation((Substation) identifiable); // container
        } else if (identifiable instanceof Bus) {
            return getBus((Bus) identifiable);
        } else if (identifiable instanceof VoltageLevel) {
            return getVoltageLevel((VoltageLevel) identifiable); // container
        } else if (identifiable instanceof Connectable) {
            return getConnectable((Connectable) identifiable);
        } else if (identifiable instanceof HvdcLine) {
            return getHvdcLine((HvdcLine) identifiable);
        } else if (identifiable instanceof Switch) {
            return getSwitch((Switch) identifiable);
        } else if (identifiable instanceof Network) {
            return currentView;
        } else {
            throw new PowsyblException(identifiable.getClass() + " type is not managed in MergingViewIndex.");
        }
    }

    /** @return all adapters according to all Identifiables */
    Stream<Identifiable<?>> getIdentifiableStream() {
        // Search Identifiables into merging & working networks, and MergedLines
        return Stream.concat(getNetworkStream().map(Network::getIdentifiables)
                        .filter(n -> !(n instanceof Network))
                        .flatMap(Collection::stream),
                mergedLineCached.values().stream());
    }

    /** @return all adapters according to all Identifiables */
    Collection<Identifiable<?>> getIdentifiables() {
        // Search Identifiables into merging & working networks
        return getIdentifiableStream().collect(Collectors.toList());
    }

    /** @return all Adapters according to all Connectables of a given type */
    <C extends Connectable> Collection<C> getConnectables(Class<C> clazz) {
        // Search Connectables of a given type into merging & working networks
        if (clazz == Line.class) {
            return getLines().stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
        } else if (clazz == DanglingLine.class) {
            return getDanglingLines().stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
        } else {
            return getNetworkStream()
                    .flatMap(n -> n.getConnectableStream(clazz))
                    .map(c -> clazz.cast(getConnectable(c)))
                    .collect(Collectors.toList());
        }
    }

    /** @return all Adapters according to all Connectables */
    Collection<Connectable> getConnectables() {
        return getNetworkStream()
                .flatMap(Network::getConnectableStream)
                .map(this::getConnectable)
                .distinct()
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all Substations into merging view */
    Collection<Substation> getSubstations() {
        // Search Substations into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getSubstationStream)
                .map(this::getSubstation)
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all Batteries into merging view */
    Collection<Battery> getBatteries() {
        // Search Batteries into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getBatteryStream)
                .map(this::getBattery)
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all VscConverterStations into merging view */
    Collection<VscConverterStation> getVscConverterStations() {
        // Search VscConverterStation into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getVscConverterStationStream)
                .map(this::getVscConverterStation)
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all TwoWindingsTransformers into merging view */
    Collection<TwoWindingsTransformer> getTwoWindingsTransformers() {
        // Search TwoWindingsTransformer into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getTwoWindingsTransformerStream)
                .map(this::getTwoWindingsTransformer)
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all Switches into merging view */
    Collection<Switch> getSwitches() {
        // Search TwoWindingsTransformer into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getSwitchStream)
                .map(this::getSwitch)
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all StaticVarCompensators into merging view */
    Collection<StaticVarCompensator> getStaticVarCompensators() {
        // Search StaticVarCompensator into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getStaticVarCompensatorStream)
                .map(this::getStaticVarCompensator)
                .collect(Collectors.toList());
    }

    Collection<ShuntCompensator> getShuntCompensators() {
        // Search ShuntCompensator into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getShuntCompensatorStream)
                .map(this::getShuntCompensator)
                .collect(Collectors.toList());
    }

    Collection<VoltageLevel> getVoltageLevels() {
        // Search VoltageLevel into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getVoltageLevelStream)
                .map(this::getVoltageLevel)
                .collect(Collectors.toList());
    }

    Collection<Load> getLoads() {
        // Search Load into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getLoadStream)
                .map(this::getLoad)
                .collect(Collectors.toList());
    }

    Collection<Generator> getGenerators() {
        // Search Generator into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getGeneratorStream)
                .map(this::getGenerator)
                .collect(Collectors.toList());
    }

    Collection<BusbarSection> getBusbarSections() {
        // Search BusbarSection into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getBusbarSectionStream)
                .map(this::getBusbarSection)
                .collect(Collectors.toList());
    }

    Collection<LccConverterStation> getLccConverterStations() {
        // Search LccConverterStation into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getLccConverterStationStream)
                .map(this::getLccConverterStation)
                .collect(Collectors.toList());
    }

    Collection<HvdcConverterStation<?>> getHvdcConverterStations() {
        // Search HvdcConverterStation into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getHvdcConverterStationStream)
                .map(this::getHvdcConverterStation)
                .collect(Collectors.toList());
    }

    Collection<Branch> getBranches() {
        // Search Branch into merging & working networks, and MergedLines
        return Stream.concat(getNetworkStream().flatMap(Network::getBranchStream)
                        .map(this::getBranch),
                mergedLineCached.values().stream())
                .collect(Collectors.toList());
    }

    Collection<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        // Search ThreeWindingsTransformer into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getThreeWindingsTransformerStream)
                .map(this::getThreeWindingsTransformer)
                .collect(Collectors.toList());
    }

    Collection<Bus> getBuses() {
        // Search ThreeWindingsTransformer into merging & working networks
        return getNetworkStream()
                .map(Network::getBusBreakerView)
                .flatMap(Network.BusBreakerView::getBusStream)
                .map(this::getBus)
                .collect(Collectors.toList());
    }

    Collection<Line> getLines() {
        // Search Line into merging & working networks, and MergedLines
        return Stream.concat(getNetworkStream().flatMap(Network::getLineStream)
                        .map(this::getLine),
                mergedLineCached.values().stream())
                .collect(Collectors.toList());
    }

    boolean isMerged(final DanglingLine dl) {
        return mergedLineCached.containsKey(dl.getUcteXnodeCode());
    }

    Collection<DanglingLine> getDanglingLines() {
        // Search DanglingLine into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getDanglingLineStream)
                .filter(dl -> !isMerged(dl))
                .map(this::getDanglingLine)
                .collect(Collectors.toList());
    }

    Collection<HvdcLine> getHvdcLines() {
        // Search HvdcLine into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getHvdcLineStream)
                .map(this::getHvdcLine)
                .collect(Collectors.toList());
    }

    /** @return adapter according to given Substation */
    SubstationAdapter getSubstation(final Substation substation) {
        return substation == null ? null : (SubstationAdapter) identifiableCached.computeIfAbsent(substation, key -> new SubstationAdapter(substation, this));
    }

    /** @return adapter according to given VoltageLevel */
    VoltageLevelAdapter getVoltageLevel(final VoltageLevel vl) {
        return vl == null ? null : (VoltageLevelAdapter) identifiableCached.computeIfAbsent(vl, key -> new VoltageLevelAdapter(vl, this));
    }

    /** @return adapter according to given Switch */
    SwitchAdapter getSwitch(final Switch sw) {
        return sw == null ? null : (SwitchAdapter) identifiableCached.computeIfAbsent(sw, key -> new SwitchAdapter(sw, this));
    }

    /** @return adapter according to given HvdcLine */
    HvdcLineAdapter getHvdcLine(final HvdcLine hvdcLine) {
        return hvdcLine == null ? null : (HvdcLineAdapter) identifiableCached.computeIfAbsent(hvdcLine, key -> new HvdcLineAdapter(hvdcLine, this));
    }

    /** @return adapter according to given Bus */
    BusAdapter getBus(final Bus bus) {
        return bus == null ? null : (BusAdapter) identifiableCached.computeIfAbsent(bus, key -> new BusAdapter(bus, this));
    }

    /** @return adapter according to given TwoWindingsTransformer */
    TwoWindingsTransformerAdapter getTwoWindingsTransformer(final TwoWindingsTransformer twt) {
        return twt == null ? null : (TwoWindingsTransformerAdapter) identifiableCached.computeIfAbsent(twt, key -> new TwoWindingsTransformerAdapter(twt, this));
    }

    ThreeWindingsTransformerAdapter getThreeWindingsTransformer(final ThreeWindingsTransformer twt) {
        return twt == null ? null : (ThreeWindingsTransformerAdapter) identifiableCached.computeIfAbsent(twt, key -> new ThreeWindingsTransformerAdapter(twt, this));
    }

    BusbarSectionAdapter getBusbarSection(final BusbarSection bs) {
        return bs == null ? null : (BusbarSectionAdapter) identifiableCached.computeIfAbsent(bs, key -> new BusbarSectionAdapter(bs, this));
    }

    GeneratorAdapter getGenerator(final Generator generator) {
        return generator == null ? null : (GeneratorAdapter) identifiableCached.computeIfAbsent(generator, key -> new GeneratorAdapter(generator, this));
    }

    LoadAdapter getLoad(final Load load) {
        return load == null ? null : (LoadAdapter) identifiableCached.computeIfAbsent(load, key -> new LoadAdapter(load, this));
    }

    BatteryAdapter getBattery(final Battery battery) {
        return battery == null ? null : (BatteryAdapter) identifiableCached.computeIfAbsent(battery, key -> new BatteryAdapter(battery, this));
    }

    ComponentAdapter getComponent(final Component component) {
        return component == null ? null : (ComponentAdapter) componentCached.computeIfAbsent(component, key -> new ComponentAdapter(component, this));
    }

    TerminalAdapter getTerminal(final Terminal terminal) {
        return terminal == null ? null : (TerminalAdapter) terminalCached.computeIfAbsent(terminal, key -> new TerminalAdapter(terminal, this));
    }

    PhaseTapChangerAdapter getPhaseTapChanger(final PhaseTapChanger ptc) {
        return ptc == null ? null : (PhaseTapChangerAdapter) ptcCached.computeIfAbsent(ptc, key -> new PhaseTapChangerAdapter(ptc, this));
    }

    RatioTapChangerAdapter getRatioTapChanger(final RatioTapChanger rtc) {
        return rtc == null ? null : (RatioTapChangerAdapter) rtcCached.computeIfAbsent(rtc, key -> new RatioTapChangerAdapter(rtc, this));
    }

    Line getLine(final Line line) {
        return line == null ? null : (Line) identifiableCached.computeIfAbsent(line, k -> {
            if (line.isTieLine()) {
                return new TieLineAdapter((TieLine) line, this);
            } else {
                return new LineAdapter(line, this);
            }
        });
    }

    HvdcConverterStation<?> getHvdcConverterStation(final HvdcConverterStation<?> cs) {
        if (cs == null) {
            return null;
        }
        if (cs instanceof LccConverterStation) {
            return getLccConverterStation((LccConverterStation) cs);
        } else if (cs instanceof VscConverterStation) {
            return getVscConverterStation((VscConverterStation) cs);
        } else {
            throw new PowsyblException("Invalid type " + cs.getClass() + " to be adapted");
        }
    }

    VscConverterStationAdapter getVscConverterStation(final VscConverterStation vsc) {
        return vsc == null ? null : (VscConverterStationAdapter) identifiableCached.computeIfAbsent(vsc, key -> new VscConverterStationAdapter((VscConverterStation) key, this));
    }

    LccConverterStationAdapter getLccConverterStation(final LccConverterStation lcc) {
        return lcc == null ? null : (LccConverterStationAdapter) identifiableCached.computeIfAbsent(lcc, key -> new LccConverterStationAdapter((LccConverterStation) key, this));
    }

    ShuntCompensatorAdapter getShuntCompensator(final ShuntCompensator shuntCompensator) {
        return shuntCompensator == null ? null
                : (ShuntCompensatorAdapter) identifiableCached.computeIfAbsent(shuntCompensator, key -> new ShuntCompensatorAdapter((ShuntCompensator) key, this));
    }

    StaticVarCompensatorAdapter getStaticVarCompensator(final StaticVarCompensator svc) {
        return svc == null ? null : (StaticVarCompensatorAdapter) identifiableCached.computeIfAbsent(svc, key -> new StaticVarCompensatorAdapter((StaticVarCompensator) key, this));
    }

    DanglingLineAdapter getDanglingLine(final DanglingLine dll) {
        return dll == null ? null : (DanglingLineAdapter) identifiableCached.computeIfAbsent(dll, key -> new DanglingLineAdapter((DanglingLine) key, this));
    }

    Connectable getConnectable(final Connectable connectable) {
        if (connectable == null) {
            return null;
        }
        switch (connectable.getType()) {
            case BUSBAR_SECTION:
                return getBusbarSection((BusbarSection) connectable);
            case LINE:
            case TWO_WINDINGS_TRANSFORMER:
                return getBranch((Branch) connectable);
            case THREE_WINDINGS_TRANSFORMER:
                return getThreeWindingsTransformer((ThreeWindingsTransformer) connectable);
            case GENERATOR:
                return getGenerator((Generator) connectable);
            case BATTERY:
                return getBattery((Battery) connectable);
            case LOAD:
                return getLoad((Load) connectable);
            case SHUNT_COMPENSATOR:
                return getShuntCompensator((ShuntCompensator) connectable);
            case DANGLING_LINE:
                final DanglingLine danglingLine = (DanglingLine) connectable;
                final Line mergedLine = getMergedLineByCode(danglingLine.getUcteXnodeCode());
                return Objects.nonNull(mergedLine) ? mergedLine : getDanglingLine(danglingLine);
            case STATIC_VAR_COMPENSATOR:
                return getStaticVarCompensator((StaticVarCompensator) connectable);
            case HVDC_CONVERTER_STATION:
                return getHvdcConverterStation((HvdcConverterStation) connectable);
            default:
                throw new AssertionError(connectable.getType().name() + " is not valid to be adapted to connectable");
        }
    }

    Branch getBranch(final Branch b) {
        if (b == null) {
            return null;
        }
        switch (b.getType()) {
            case LINE:
                return getLine((Line) b);
            case TWO_WINDINGS_TRANSFORMER:
                return getTwoWindingsTransformer((TwoWindingsTransformer) b);
            default:
                throw new AssertionError(b.getType().name() + " is not valid to be adapted to branch");
        }
    }

    <T> T get(final Function<? super Network, ? extends T> mapNetworkIndex, final UnaryOperator<T> mapToAdapter) {
        Objects.requireNonNull(mapNetworkIndex, "mapNetworkIndex is null");
        Objects.requireNonNull(mapToAdapter, "mapToAdapter is null");

        return getNetworkStream()
                .map(mapNetworkIndex)
                .filter(Objects::nonNull)
                .map(mapToAdapter)
                .findFirst()
                .orElse(null);
    }

    Network getNetwork(final Predicate<? super Network> filter) {
        return getNetworkStream()
                    .filter(filter)
                    .findFirst()
                    .orElse(null);
    }
}
