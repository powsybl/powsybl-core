/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.ContainerType;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TieLineAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Not destructive network merge.
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public final class MergingView implements Network {
    public static final PowsyblException NOT_IMPLEMENTED_EXCEPTION = new PowsyblException("Not implemented exception");

    private static final Logger LOGGER = LoggerFactory.getLogger(MergingView.class);

    /** Indexing of all Identifiable into current merging view */
    private final MergingViewIndex index;

    /** Delegate for Identifiable creation into current merging view */
    private final Network workingNetwork;

    /** To listen events from merging network */
    private final NetworkListener listener = new MergingNetworkListener();

    private final NetworkBusBreakerViewAdapter busBreakerView;

    private final NetworkBusViewAdapter busView;

    /** Constructor */
    private MergingView(final NetworkFactory factory, final String id, final String format) {
        Objects.requireNonNull(factory, "factory is null");

        index = new MergingViewIndex(this);
        busBreakerView = new NetworkBusBreakerViewAdapter(index);
        busView = new NetworkBusViewAdapter(index);
        // Working network will store view informations
        workingNetwork = factory.createNetwork(id, format);
        // Add working network as merging network
        index.checkAndAdd(workingNetwork);
        workingNetwork.addListener(listener);
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
        other.addListener(listener);

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
    public String getName() {
        return workingNetwork.getName();
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
        return index.getNetworkStream()
                .map(n -> n.getIdentifiable(id))
                .filter(Objects::nonNull)
                .map(index::getIdentifiable)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        return index.getIdentifiables();
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
        return getSubstations(Optional.ofNullable(country).map(Country::getName).orElse(null), tsoId, geographicalTags);
    }

    @Override
    public Iterable<Substation> getSubstations(final String country, final String tsoId, final String... geographicalTags) {
        return StreamSupport.stream(getSubstations().spliterator(), false).filter(substation -> {
            if (country != null && !country.equals(substation.getCountry().map(Country::getName).orElse(""))) {
                return false;
            }
            if (tsoId != null && !tsoId.equals(substation.getTso())) {
                return false;
            }
            for (final String tag : geographicalTags) {
                if (!substation.getGeographicalTags().contains(tag)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public Substation getSubstation(final String id) {
        return getSubstationStream()
                .filter(s -> id.equals(s.getId()))
                .findFirst()
                .orElse(null);
    }

    // VoltageLevel
    @Override
    public Network getNetwork() {
        return this;
    }

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
        return getVoltageLevelStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
        return getBatteryStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
        return getVscConverterStationStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
        return getTwoWindingsTransformerStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
    }

    // Switches
    @Override
    public Switch getSwitch(final String id) {
        return getSwitchStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
        return getStaticVarCompensatorStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
        return getShuntCompensatorStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
        return getLoadStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
        return getGeneratorStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
    }

    // BusbarSections
    @Override
    public BusbarSection getBusbarSection(final String id) {
        return getBusbarSectionStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
        return getLccConverterStationStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
        return getHvdcConverterStationStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
    }

    // Branches
    @Override
    public Branch getBranch(final String id) {
        return getBranchStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
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
        return getThreeWindingsTransformerStream().filter(s -> id.equals(s.getId())).findFirst().orElse(null);
    }

    @Override
    public NetworkBusBreakerViewAdapter getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public BusView getBusView() {
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

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------

    @Override
    public VariantManager getVariantManager() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LineAdder newLine() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Line> getLines() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Line> getLineStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getLineCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Line getLine(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TieLineAdder newTieLine() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getDanglingLineCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLine getDanglingLine(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getHvdcLineCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public HvdcLine getHvdcLine(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public HvdcLineAdder newHvdcLine() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void addListener(final NetworkListener listener) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void removeListener(final NetworkListener listener) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }
}
