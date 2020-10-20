/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.FluentIterable;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Not destructive network merge.
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public final class MergingView implements Network, MultiVariantObject {
    private static final Logger LOGGER = LoggerFactory.getLogger(MergingView.class);

    /** Indexing of all Identifiable into current merging view */
    private final MergingViewIndex index;

    /** Delegate for Identifiable creation into current merging view */
    private final Network workingNetwork;

    /** Components managers */
    private final Map<String, ConnectedComponentsManager> connectedComponentsManager = new HashMap<>();
    private final Map<String, SynchronousComponentsManager> synchronousComponentsManager = new HashMap<>();

    /** To listen events from merging network */
    private final NetworkListener mergeDanglingLineListener;
    private final TopologyListener topologyListener;

    static PowsyblException createNotImplementedException() {
        return new PowsyblException("Not implemented exception");
    }

    private boolean fictitious;

    private static class BusBreakerViewAdapter implements Network.BusBreakerView {

        private final MergingViewIndex index;

        BusBreakerViewAdapter(final MergingViewIndex index) {
            this.index = index;
        }

        @Override
        public Bus getBus(final String id) {
            return index.getVoltageLevelStream()
                    .map(vl -> vl.getBusBreakerView().getBus(id))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Iterable<Bus> getBuses() {
            return FluentIterable.from(index.getVoltageLevels())
                    .transformAndConcat(vl -> vl.getBusBreakerView().getBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return index.getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getBusStream());
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return Collections.unmodifiableList(getSwitchStream().collect(Collectors.toList()));
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return index.getNetworkStream()
                    .map(Network::getBusBreakerView)
                    .map(Network.BusBreakerView::getSwitchStream)
                    .flatMap(stream -> stream)
                    .map(index::getSwitch);
        }

        @Override
        public int getSwitchCount() {
            return (int) getSwitchStream().count();
        }
    }

    private final BusBreakerViewAdapter busBreakerView;

    private static class BusViewAdapter implements Network.BusView {

        private final MergingViewIndex index;

        BusViewAdapter(final MergingViewIndex index) {
            this.index = index;
        }

        @Override
        public Iterable<Bus> getBuses() {
            return FluentIterable.from(index.getVoltageLevels())
                    .transformAndConcat(vl -> vl.getBusView().getBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return index.getVoltageLevelStream().flatMap(vl -> vl.getBusView().getBusStream());
        }

        @Override
        public Bus getBus(final String id) {
            return index.getVoltageLevelStream()
                    .map(vl -> vl.getBusView().getBus(id))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Collection<Component> getConnectedComponents() {
            return Collections.unmodifiableList(index.getView().getConnectedComponentsManager().getConnectedComponents());
        }
    }

    private final BusViewAdapter busView;

    /** Variant management for all merged networks */
    private final MergingVariantManager variantManager;

    /** Constructor */
    private MergingView(final NetworkFactory factory, final String id, final String format) {
        Objects.requireNonNull(factory, "factory is null");

        index = new MergingViewIndex(this);
        variantManager = new MergingVariantManager(index);

        connectedComponentsManager.put(VariantManagerConstants.INITIAL_VARIANT_ID, new ConnectedComponentsManager(this));
        synchronousComponentsManager.put(VariantManagerConstants.INITIAL_VARIANT_ID, new SynchronousComponentsManager(this));

        // Listeners creation
        mergeDanglingLineListener = new MergingLineListener(index);
        topologyListener = new TopologyListener(index);
        busBreakerView = new BusBreakerViewAdapter(index);
        busView = new BusViewAdapter(index);
        // Working network will store view informations
        workingNetwork = factory.createNetwork(id, format);
        // Add working network as merging network
        index.checkAndAdd(workingNetwork);
        // Attach listeners
        addInternalListeners(workingNetwork);
    }

    /** Public constructor */
    public static MergingView create(final String id, final String format) {
        return new MergingView(NetworkFactory.findDefault(), id, format);
    }

    @Override
    public void merge(final Network other) {
        Objects.requireNonNull(other, "network is null");

        final long start = System.currentTimeMillis();

        index.checkAndAdd(other);
        addInternalListeners(other);

        LOGGER.info("Merging of {} done in {} ms", other.getId(), System.currentTimeMillis() - start);
    }

    @Override
    public void merge(final Network... others) {
        for (final Network other : others) {
            merge(other);
        }
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.NETWORK;
    }

    @Override
    public String getId() {
        return workingNetwork.getId();
    }

    @Override
    public Optional<String> getOptionalName() {
        return workingNetwork.getOptionalName();
    }

    @Override
    public String getNameOrId() {
        return workingNetwork.getNameOrId();
    }

    @Override
    public DateTime getCaseDate() {
        return workingNetwork.getCaseDate();
    }

    @Override
    public Network setCaseDate(final DateTime date) {
        workingNetwork.setCaseDate(date);
        return this;
    }

    @Override
    public int getForecastDistance() {
        return workingNetwork.getForecastDistance();
    }

    @Override
    public Network setForecastDistance(final int forecastDistance) {
        workingNetwork.setForecastDistance(forecastDistance);
        return this;
    }

    @Override
    public Network getNetwork() {
        return this;
    }

    public Network getNetwork(String id) {
        return index.getNetwork(n -> n.getId().equals(id));
    }

    @Override
    public boolean hasProperty() {
        return index.getNetworkStream()
                .anyMatch(Network::hasProperty);
    }

    @Override
    public boolean hasProperty(final String key) {
        return index.getNetworkStream()
                .anyMatch(n -> n.hasProperty(key));
    }

    @Override
    public String getProperty(final String key) {
        return index.getNetworkStream()
                .map(n -> n.getProperty(key))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getProperty(final String key, final String defaultValue) {
        return index.getNetworkStream()
                .map(n -> n.getProperty(key, defaultValue))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(defaultValue);
    }

    @Override
    public String setProperty(final String key, final String value) {
        index.getNetworkStream().forEach(n -> n.setProperty(key, value));
        return null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return index.getNetworkStream()
                .map(Network::getPropertyNames)
                .flatMap(Set<String>::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isFictitious() {
        return fictitious;
    }

    @Override
    public void setFictitious(boolean fictitious) {
        boolean oldValue = this.fictitious;
        if (oldValue != fictitious) {
            this.fictitious = fictitious;
            index.getNetworkStream().forEach(n -> n.setFictitious(fictitious));
        }
    }

    @Override
    public String getSourceFormat() {
        String format = "hybrid";
        // If all Merging Network has same format
        final Set<String> formats = index.getNetworkStream()
                .map(Network::getSourceFormat)
                .collect(Collectors.toSet());
        if (formats.size() == 1) {
            format = formats.iterator().next();
        }
        return format;
    }

    @Override
    public Set<Country> getCountries() {
        return getSubstationStream()
                .map(Substation::getCountry)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Country.class)));
    }

    @Override
    public int getCountryCount() {
        return getCountries().size();
    }

    @Override
    public Identifiable<?> getIdentifiable(final String id) {
        // Need to cast into Identifiable in order to check MergedLine first
        return Optional.ofNullable((Identifiable) index.getMergedLine(id)).orElse(index.get(n -> n.getIdentifiable(id), index::getIdentifiable));
    }

    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        return index.getIdentifiables();
    }

    @Override
    public <C extends Connectable> Iterable<C> getConnectables(Class<C> clazz) {
        return Collections.unmodifiableCollection(index.getConnectables(clazz));
    }

    @Override
    public <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
        return index.getConnectables(clazz).stream();
    }

    @Override
    public <C extends Connectable> int getConnectableCount(Class<C> clazz) {
        return index.getConnectables(clazz).size();
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return Collections.unmodifiableCollection(index.getConnectables());
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return index.getConnectables().stream();
    }

    @Override
    public int getConnectableCount() {
        return index.getConnectables().size();
    }

    @Override
    public SubstationAdderAdapter newSubstation() {
        return new SubstationAdderAdapter(workingNetwork.newSubstation(), index);
    }

    @Override
    public Iterable<Substation> getSubstations() {
        return Collections.unmodifiableCollection(index.getSubstations());
    }

    @Override
    public Stream<Substation> getSubstationStream() {
        return index.getSubstations().stream();
    }

    @Override
    public int getSubstationCount() {
        return index.getSubstations().size();
    }

    @Override
    public Iterable<Substation> getSubstations(final Country country, final String tsoId, final String... geographicalTags) {
        return getSubstations(Optional.ofNullable(country).map(Country::getName)
                .orElse(null), tsoId, geographicalTags);
    }

    @Override
    public Iterable<Substation> getSubstations(final String country, final String tsoId, final String... geographicalTags) {
        return index.getNetworkStream()
                .map(n -> n.getSubstations(country, tsoId, geographicalTags))
                .flatMap(x -> StreamSupport.stream(x.spliterator(), false))
                .filter(Objects::nonNull)
                .map(index::getSubstation)
                .collect(Collectors.toList());
    }

    @Override
    public Substation getSubstation(final String id) {
        return index.get(n -> n.getSubstation(id), index::getSubstation);
    }

    // VoltageLevel
    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Collections.unmodifiableCollection(index.getVoltageLevels());
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return index.getVoltageLevels().stream();
    }

    @Override
    public int getVoltageLevelCount() {
        return index.getVoltageLevels().size();
    }

    @Override
    public VoltageLevel getVoltageLevel(final String id) {
        return index.get(n -> n.getVoltageLevel(id), index::getVoltageLevel);
    }

    // Battery
    @Override
    public Iterable<Battery> getBatteries() {
        return Collections.unmodifiableCollection(index.getBatteries());
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return index.getBatteries().stream();
    }

    @Override
    public int getBatteryCount() {
        return index.getBatteries().size();
    }

    @Override
    public Battery getBattery(final String id) {
        return index.get(n -> n.getBattery(id), index::getBattery);
    }

    // VscConverterStation
    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Collections.unmodifiableCollection(index.getVscConverterStations());
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return index.getVscConverterStations().stream();
    }

    @Override
    public int getVscConverterStationCount() {
        return index.getVscConverterStations().size();
    }

    @Override
    public VscConverterStation getVscConverterStation(final String id) {
        return index.get(n -> n.getVscConverterStation(id), index::getVscConverterStation);
    }

    // TwoWindingsTransformer
    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Collections.unmodifiableCollection(index.getTwoWindingsTransformers());
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return index.getTwoWindingsTransformers().stream();
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return index.getTwoWindingsTransformers().size();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(final String id) {
        return index.get(n -> n.getTwoWindingsTransformer(id), index::getTwoWindingsTransformer);
    }

    // Switches
    @Override
    public Switch getSwitch(final String id) {
        return index.get(n -> n.getSwitch(id), index::getSwitch);
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Collections.unmodifiableCollection(index.getSwitches());
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return index.getSwitches().stream();
    }

    @Override
    public int getSwitchCount() {
        return index.getSwitches().size();
    }

    // StaticVarCompensator
    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Collections.unmodifiableCollection(index.getStaticVarCompensators());
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return index.getStaticVarCompensators().stream();
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return index.getStaticVarCompensators().size();
    }

    @Override
    public StaticVarCompensator getStaticVarCompensator(final String id) {
        return index.get(n -> n.getStaticVarCompensator(id), index::getStaticVarCompensator);
    }

    // ShuntCompensators
    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Collections.unmodifiableCollection(index.getShuntCompensators());
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return index.getShuntCompensators().stream();
    }

    @Override
    public int getShuntCompensatorCount() {
        return index.getShuntCompensators().size();
    }

    @Override
    public ShuntCompensator getShuntCompensator(final String id) {
        return index.get(n -> n.getShuntCompensator(id), index::getShuntCompensator);
    }

    // Loads
    @Override
    public Iterable<Load> getLoads() {
        return Collections.unmodifiableCollection(index.getLoads());
    }

    @Override
    public Stream<Load> getLoadStream() {
        return index.getLoads().stream();
    }

    @Override
    public int getLoadCount() {
        return index.getLoads().size();
    }

    @Override
    public Load getLoad(final String id) {
        return index.get(n -> n.getLoad(id), index::getLoad);
    }

    // Generators
    @Override
    public Iterable<Generator> getGenerators() {
        return Collections.unmodifiableCollection(index.getGenerators());
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return index.getGenerators().stream();
    }

    @Override
    public int getGeneratorCount() {
        return index.getGenerators().size();
    }

    @Override
    public Generator getGenerator(final String id) {
        return index.get(n -> n.getGenerator(id), index::getGenerator);
    }

    // BusbarSections
    @Override
    public BusbarSection getBusbarSection(final String id) {
        return index.get(n -> n.getBusbarSection(id), index::getBusbarSection);
    }

    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        return Collections.unmodifiableCollection(index.getBusbarSections());
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return index.getBusbarSections().stream();
    }

    @Override
    public int getBusbarSectionCount() {
        return index.getBusbarSections().size();
    }

    // LccConverterStations
    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Collections.unmodifiableCollection(index.getLccConverterStations());
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return index.getLccConverterStations().stream();
    }

    @Override
    public int getLccConverterStationCount() {
        return index.getLccConverterStations().size();
    }

    @Override
    public LccConverterStation getLccConverterStation(final String id) {
        return index.get(n -> n.getLccConverterStation(id), index::getLccConverterStation);
    }

    // HvdcConverterStations
    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        return Collections.unmodifiableCollection(index.getHvdcConverterStations());
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        return index.getHvdcConverterStations().stream();
    }

    @Override
    public int getHvdcConverterStationCount() {
        return index.getHvdcConverterStations().size();
    }

    @Override
    public HvdcConverterStation<?> getHvdcConverterStation(final String id) {
        return index.get(n -> n.getHvdcConverterStation(id), index::getHvdcConverterStation);
    }

    // Branches
    @Override
    public Branch getBranch(final String id) {
        // Need to cast into Branch in order to check MergedLine first
        return Optional.ofNullable((Branch) index.getMergedLine(id)).orElse(index.get(n -> n.getBranch(id), index::getBranch));
    }

    @Override
    public Iterable<Branch> getBranches() {
        return Collections.unmodifiableCollection(index.getBranches());
    }

    @Override
    public Stream<Branch> getBranchStream() {
        return index.getBranches().stream();
    }

    @Override
    public int getBranchCount() {
        return index.getBranches().size();
    }

    // ThreeWindingsTransformers
    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Collections.unmodifiableCollection(index.getThreeWindingsTransformers());
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return index.getThreeWindingsTransformers().stream();
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return index.getThreeWindingsTransformers().size();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(final String id) {
        return index.get(n -> n.getThreeWindingsTransformer(id), index::getThreeWindingsTransformer);
    }

    // Lines
    @Override
    public LineAdder newLine() {
        return new LineAdderAdapter(index);
    }

    @Override
    public Iterable<Line> getLines() {
        return index.getLines();
    }

    @Override
    public Stream<Line> getLineStream() {
        return index.getLineStream();
    }

    @Override
    public int getLineCount() {
        return index.getLineCount();
    }

    @Override
    public Line getLine(final String id) {
        return Optional.ofNullable(index.getMergedLine(id)).orElse(index.get(n -> n.getLine(id), index::getLine));
    }

    // DanglingLines
    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return index.getDanglingLines();
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return index.getDanglingLineStream();
    }

    @Override
    public int getDanglingLineCount() {
        return index.getDanglingLineCount();
    }

    @Override
    public DanglingLine getDanglingLine(final String id) {
        final DanglingLine dl = index.get(n -> n.getDanglingLine(id), index::getDanglingLine);
        return dl == null || index.isMerged(dl) ? null : dl;
    }

    // HvdcLines
    @Override
    public HvdcLineAdder newHvdcLine() {
        return new HvdcLineAdderAdapter(index);
    }

    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        return Collections.unmodifiableCollection(index.getHvdcLines());
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return index.getHvdcLines().stream();
    }

    @Override
    public int getHvdcLineCount() {
        return index.getHvdcLines().size();
    }

    @Override
    public HvdcLine getHvdcLine(final String id) {
        return index.get(n -> n.getHvdcLine(id), index::getHvdcLine);
    }

    @Override
    public Network.BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public Network.BusView getBusView() {
        return busView;
    }

    @Override
    public <E extends Extension<Network>> void addExtension(final Class<? super E> type, final E extension) {
        workingNetwork.addExtension(type, extension);
    }

    @Override
    public <E extends Extension<Network>> E getExtension(final Class<? super E> type) {
        return workingNetwork.getExtension(type);
    }

    @Override
    public <E extends Extension<Network>> E getExtensionByName(final String name) {
        return workingNetwork.getExtensionByName(name);
    }

    @Override
    public <E extends Extension<Network>> boolean removeExtension(final Class<E> type) {
        return workingNetwork.removeExtension(type);
    }

    @Override
    public <E extends Extension<Network>> Collection<E> getExtensions() {
        return workingNetwork.getExtensions();
    }

    @Override
    public String getImplementationName() {
        return "MergingView";
    }

    @Override
    public <E extends Extension<Network>, B extends ExtensionAdder<Network, E>> B newExtension(Class<B> type) {
        return workingNetwork.newExtension(type);
    }

    @Override
    public VariantManager getVariantManager() {
        return variantManager;
    }

    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds) {
        // FIXME(mathbagu)
        for (String targetVariantId : targetVariantIds) {
            connectedComponentsManager.put(targetVariantId, new ConnectedComponentsManager(this));
            synchronousComponentsManager.put(targetVariantId, new SynchronousComponentsManager(this));
        }
    }

    @Override
    public void removeVariant(String variantId) {
        // FIXME(mathbagu)
        connectedComponentsManager.remove(variantId);
        synchronousComponentsManager.remove(variantId);
    }

    private void addInternalListeners(Network network) {
        // Attach all custom listeners
        network.addListener(mergeDanglingLineListener);
        network.addListener(topologyListener);
    }

    ConnectedComponentsManager getConnectedComponentsManager() {
        return connectedComponentsManager.get(getVariantManager().getWorkingVariantId());
    }

    SynchronousComponentsManager getSynchronousComponentsManager() {
        return synchronousComponentsManager.get(getVariantManager().getWorkingVariantId());
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public TieLineAdder newTieLine() {
        throw createNotImplementedException();
    }

    @Override
    public void addListener(final NetworkListener listener) {
        throw createNotImplementedException();
    }

    @Override
    public void removeListener(final NetworkListener listener) {
        throw createNotImplementedException();
    }
}
