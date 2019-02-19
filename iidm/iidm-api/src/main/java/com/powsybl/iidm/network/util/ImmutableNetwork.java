/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ImmutableNetwork extends AbstractImmutableIdentifiable<Network> implements Network {

    static final PowsyblException UNMODIFIABLE_EXCEPTION = new PowsyblException("Unmodifiable identifiable");

    private static final Map<Network, ImmutableNetwork> CACHE = new HashMap<>();

    private final ImmutableCacheIndex cache = new ImmutableCacheIndex();

    private ImmutableNetwork(Network identifiable) {
        super(identifiable);
    }

    public static ImmutableNetwork of(Network identifiable) {
        return CACHE.computeIfAbsent(identifiable, k -> new ImmutableNetwork(identifiable));
    }

    static PowsyblException createUnmodifiableNetworkException() {
        return UNMODIFIABLE_EXCEPTION;
    }

    @Override
    public DateTime getCaseDate() {
        return identifiable.getCaseDate();
    }

    @Override
    public Network setCaseDate(DateTime date) {
        throw createUnmodifiableNetworkException();
    }

    @Override
    public int getForecastDistance() {
        return identifiable.getForecastDistance();
    }

    @Override
    public Network setForecastDistance(int forecastDistance) {
        throw createUnmodifiableNetworkException();
    }

    @Override
    public String getSourceFormat() {
        return identifiable.getSourceFormat();
    }

    @Override
    public VariantManager getVariantManager() {
        return cache.getVariantManager(identifiable.getVariantManager());
    }

    @Override
    public Set<Country> getCountries() {
        return identifiable.getCountries();
    }

    @Override
    public int getCountryCount() {
        return identifiable.getCountryCount();
    }

    @Override
    public SubstationAdder newSubstation() {
        throw createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<Substation> getSubstations() {
        return Iterables.transform(identifiable.getSubstations(), cache::getSubstation);
    }

    @Override
    public Stream<Substation> getSubstationStream() {
        return identifiable.getSubstationStream().map(cache::getSubstation);
    }

    @Override
    public int getSubstationCount() {
        return identifiable.getSubstationCount();
    }

    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        return Iterables.transform(identifiable.getSubstations(country, tsoId, geographicalTags), cache::getSubstation);
    }

    @Override
    public Substation getSubstation(String id) {
        return cache.getSubstation(identifiable.getSubstation(id));
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Iterables.transform(identifiable.getVoltageLevels(), cache::getVoltageLevel);
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return identifiable.getVoltageLevelStream().map(cache::getVoltageLevel);
    }

    @Override
    public int getVoltageLevelCount() {
        return identifiable.getVoltageLevelCount();
    }

    @Override
    public VoltageLevel getVoltageLevel(String id) {
        return cache.getVoltageLevel(identifiable.getVoltageLevel(id));
    }

    @Override
    public LineAdder newLine() {
        throw createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<Line> getLines() {
        return Iterables.transform(identifiable.getLines(), cache::getLine);
    }

    @Override
    public Branch getBranch(String branchId) {
        return cache.getBranch(identifiable.getBranch(branchId));
    }

    @Override
    public Iterable<Branch> getBranches() {
        return Iterables.transform(identifiable.getBranches(), cache::getBranch);
    }

    @Override
    public Stream<Branch> getBranchStream() {
        return identifiable.getBranchStream().map(cache::getBranch);
    }

    @Override
    public int getBranchCount() {
        return identifiable.getBranchCount();
    }

    @Override
    public Stream<Line> getLineStream() {
        return identifiable.getLineStream().map(cache::getLine);
    }

    @Override
    public int getLineCount() {
        return identifiable.getLineCount();
    }

    @Override
    public Line getLine(String id) {
        return cache.getLine(identifiable.getLine(id));
    }

    @Override
    public TieLineAdder newTieLine() {
        throw createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(identifiable.getTwoWindingsTransformers(), cache::getTwoWindingsTransformer);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return identifiable.getTwoWindingsTransformerStream().map(cache::getTwoWindingsTransformer);
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return identifiable.getTwoWindingsTransformerCount();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        return cache.getTwoWindingsTransformer(identifiable.getTwoWindingsTransformer(id));
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(identifiable.getThreeWindingsTransformers(), cache::getThreeWindingsTransformer);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return identifiable.getThreeWindingsTransformerStream().map(cache::getThreeWindingsTransformer);
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return identifiable.getThreeWindingsTransformerCount();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        return cache.getThreeWindingsTransformer(identifiable.getThreeWindingsTransformer(id));
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(identifiable.getGenerators(), cache::getGenerator);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return identifiable.getGeneratorStream().map(cache::getGenerator);
    }

    @Override
    public int getGeneratorCount() {
        return identifiable.getGeneratorCount();
    }

    @Override
    public Generator getGenerator(String id) {
        return cache.getGenerator(identifiable.getGenerator(id));
    }

    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(identifiable.getLoads(), cache::getLoad);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return identifiable.getLoadStream().map(cache::getLoad);
    }

    @Override
    public int getLoadCount() {
        return identifiable.getLoadCount();
    }

    @Override
    public Load getLoad(String id) {
        return cache.getLoad(identifiable.getLoad(id));
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(identifiable.getShuntCompensators(), cache::getShuntCompensator);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return identifiable.getShuntCompensatorStream().map(cache::getShuntCompensator);
    }

    @Override
    public int getShuntCompensatorCount() {
        return identifiable.getShuntCompensatorCount();
    }

    @Override
    public ShuntCompensator getShuntCompensator(String id) {
        return cache.getShuntCompensator(identifiable.getShuntCompensator(id));
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return Iterables.transform(identifiable.getDanglingLines(), cache::getDanglingLine);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return identifiable.getDanglingLineStream().map(cache::getDanglingLine);
    }

    @Override
    public int getDanglingLineCount() {
        return identifiable.getDanglingLineCount();
    }

    @Override
    public DanglingLine getDanglingLine(String id) {
        return cache.getDanglingLine(identifiable.getDanglingLine(id));
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(identifiable.getStaticVarCompensators(), cache::getStaticVarCompensator);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return identifiable.getStaticVarCompensatorStream().map(cache::getStaticVarCompensator);
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return identifiable.getStaticVarCompensatorCount();
    }

    @Override
    public StaticVarCompensator getStaticVarCompensator(String id) {
        return cache.getStaticVarCompensator(identifiable.getStaticVarCompensator(id));
    }

    @Override
    public Switch getSwitch(String id) {
        return cache.getSwitch(identifiable.getSwitch(id));
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Iterables.transform(identifiable.getSwitches(), cache::getSwitch);
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return identifiable.getSwitchStream().map(cache::getSwitch);
    }

    @Override
    public int getSwitchCount() {
        return identifiable.getSwitchCount();
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        return identifiable.getBusbarSection(id);
    }

    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        return identifiable.getBusbarSections();
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return identifiable.getBusbarSectionStream();
    }

    @Override
    public int getBusbarSectionCount() {
        return identifiable.getBusbarSectionCount();
    }

    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        return Iterables.transform(identifiable.getHvdcConverterStations(), cache::getHvdcConverterStation);
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        return identifiable.getHvdcConverterStationStream().map(cache::getHvdcConverterStation);
    }

    @Override
    public int getHvdcConverterStationCount() {
        return identifiable.getHvdcConverterStationCount();
    }

    @Override
    public HvdcConverterStation<?> getHvdcConverterStation(String id) {
        return identifiable.getHvdcConverterStation(id);
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Iterables.transform(identifiable.getLccConverterStations(), cache::getLccConverterStation);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return identifiable.getLccConverterStationStream().map(cache::getLccConverterStation);
    }

    @Override
    public int getLccConverterStationCount() {
        return identifiable.getLccConverterStationCount();
    }

    @Override
    public LccConverterStation getLccConverterStation(String id) {
        return cache.getLccConverterStation(identifiable.getLccConverterStation(id));
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(identifiable.getVscConverterStations(), cache::getVscConverterStation);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return identifiable.getVscConverterStationStream().map(cache::getVscConverterStation);
    }

    @Override
    public int getVscConverterStationCount() {
        return identifiable.getVscConverterStationCount();
    }

    @Override
    public VscConverterStation getVscConverterStation(String id) {
        return cache.getVscConverterStation(identifiable.getVscConverterStation(id));
    }

    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        return Iterables.transform(identifiable.getHvdcLines(), cache::getHvdcLine);
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return identifiable.getHvdcLineStream().map(cache::getHvdcLine);
    }

    @Override
    public int getHvdcLineCount() {
        return identifiable.getHvdcLineCount();
    }

    @Override
    public HvdcLine getHvdcLine(String id) {
        return cache.getHvdcLine(identifiable.getHvdcLine(id));
    }

    @Override
    public HvdcLineAdder newHvdcLine() {
        throw createUnmodifiableNetworkException();
    }

    @Override
    public Identifiable<?> getIdentifiable(String id) {
        return identifiable.getIdentifiable(id);
    }

    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        return identifiable.getIdentifiables();
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return new BusBreakerView() {

            final BusBreakerView busBreakerView = identifiable.getBusBreakerView();

            @Override
            public Iterable<Bus> getBuses() {
                return Iterables.transform(busBreakerView.getBuses(), cache::getBus);
            }

            @Override
            public Stream<Bus> getBusStream() {
                return busBreakerView.getBusStream().map(cache::getBus);
            }

            @Override
            public Iterable<Switch> getSwitches() {
                return Iterables.transform(busBreakerView.getSwitches(), cache::getSwitch);
            }

            @Override
            public Stream<Switch> getSwitchStream() {
                return busBreakerView.getSwitchStream().map(cache::getSwitch);
            }

            @Override
            public int getSwitchCount() {
                return busBreakerView.getSwitchCount();
            }
        };
    }

    @Override
    public BusView getBusView() {
        return new BusView() {
            @Override
            public Iterable<Bus> getBuses() {
                return Iterables.transform(identifiable.getBusView().getBuses(), cache::getBus);
            }

            @Override
            public Stream<Bus> getBusStream() {
                return identifiable.getBusView().getBusStream().map(cache::getBus);
            }

            @Override
            public Collection<Component> getConnectedComponents() {
                return identifiable.getBusView().getConnectedComponents().stream().map(cache::getComponent).collect(Collectors.toList());
            }
        };
    }

    @Override
    public void merge(Network other) {
        throw createUnmodifiableNetworkException();
    }

    @Override
    public void merge(Network... others) {
        throw createUnmodifiableNetworkException();
    }

    @Override
    public void addListener(NetworkListener listener) {
        identifiable.addListener(listener);
    }

    @Override
    public void removeListener(NetworkListener listener) {
        identifiable.removeListener(listener);
    }

    @Override
    public ContainerType getContainerType() {
        return identifiable.getContainerType();
    }
}
