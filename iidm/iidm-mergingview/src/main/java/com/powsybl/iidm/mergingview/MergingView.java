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

    /** Constructor */
    private MergingView(final NetworkFactory factory, final String id, final String format) {
        Objects.requireNonNull(factory, "factory is null");

        index = new MergingViewIndex(this);
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
                .filter(s -> id.compareTo(s.getId()) == 0)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Network getNetwork() {
        return this;
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------

    @Override
    public VariantManager getVariantManager() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getVoltageLevelCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevel getVoltageLevel(final String id) {
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
    public Branch getBranch(final String branchId) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Branch> getBranches() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Branch> getBranchStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getBranchCount() {
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
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Generator> getGenerators() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getGeneratorCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Generator getGenerator(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Battery> getBatteries() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getBatteryCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Battery getBattery(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Load> getLoads() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Load> getLoadStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getLoadCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Load getLoad(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getShuntCompensatorCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ShuntCompensator getShuntCompensator(final String id) {
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
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getStaticVarCompensatorCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensator getStaticVarCompensator(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Switch getSwitch(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Switch> getSwitches() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getSwitchCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public BusbarSection getBusbarSection(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getBusbarSectionCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getHvdcConverterStationCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public HvdcConverterStation<?> getHvdcConverterStation(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getLccConverterStationCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LccConverterStation getLccConverterStation(final String id) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getVscConverterStationCount() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VscConverterStation getVscConverterStation(final String id) {
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
    public BusBreakerView getBusBreakerView() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public BusView getBusView() {
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

    @Override
    public <E extends Extension<Network>> void addExtension(final Class<? super E> type, final E extension) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <E extends Extension<Network>> E getExtension(final Class<? super E> type) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <E extends Extension<Network>> E getExtensionByName(final String name) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <E extends Extension<Network>> boolean removeExtension(final Class<E> type) {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <E extends Extension<Network>> Collection<E> getExtensions() {
        throw NOT_IMPLEMENTED_EXCEPTION;
    }
}
