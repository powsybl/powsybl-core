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
        return ImmutableVariantManager.of(identifiable.getVariantManager());
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
        return Iterables.transform(identifiable.getSubstations(), ImmutableSubstation::ofNullable);
    }

    @Override
    public Stream<Substation> getSubstationStream() {
        return identifiable.getSubstationStream().map(ImmutableSubstation::ofNullable);
    }

    @Override
    public int getSubstationCount() {
        return identifiable.getSubstationCount();
    }

    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        return Iterables.transform(identifiable.getSubstations(country, tsoId, geographicalTags), ImmutableSubstation::ofNullable);
    }

    @Override
    public Substation getSubstation(String id) {
        return ImmutableSubstation.ofNullable(identifiable.getSubstation(id));
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Iterables.transform(identifiable.getVoltageLevels(), ImmutableVoltageLevel::ofNullable);
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return identifiable.getVoltageLevelStream().map(ImmutableVoltageLevel::ofNullable);
    }

    @Override
    public int getVoltageLevelCount() {
        return identifiable.getVoltageLevelCount();
    }

    @Override
    public VoltageLevel getVoltageLevel(String id) {
        return ImmutableVoltageLevel.ofNullable(identifiable.getVoltageLevel(id));
    }

    @Override
    public LineAdder newLine() {
        throw createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<Line> getLines() {
        return Iterables.transform(identifiable.getLines(), ImmutableFactory::ofNullableLine);
    }

    @Override
    public Branch getBranch(String branchId) {
        return ImmutableFactory.ofNullableBranch(identifiable.getBranch(branchId));
    }

    @Override
    public Iterable<Branch> getBranches() {
        return Iterables.transform(identifiable.getBranches(), ImmutableFactory::ofNullableBranch);
    }

    @Override
    public Stream<Branch> getBranchStream() {
        return identifiable.getBranchStream().map(ImmutableFactory::ofNullableBranch);
    }

    @Override
    public int getBranchCount() {
        return identifiable.getBranchCount();
    }

    @Override
    public Stream<Line> getLineStream() {
        return identifiable.getLineStream().map(ImmutableFactory::ofNullableLine);
    }

    @Override
    public int getLineCount() {
        return identifiable.getLineCount();
    }

    @Override
    public Line getLine(String id) {
        return ImmutableFactory.ofNullableLine(identifiable.getLine(id));
    }

    @Override
    public TieLineAdder newTieLine() {
        throw createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(identifiable.getTwoWindingsTransformers(), ImmutableTwoWindingsTransformer::ofNullable);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return identifiable.getTwoWindingsTransformerStream().map(ImmutableTwoWindingsTransformer::ofNullable);
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return identifiable.getTwoWindingsTransformerCount();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        return ImmutableTwoWindingsTransformer.ofNullable(identifiable.getTwoWindingsTransformer(id));
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(identifiable.getThreeWindingsTransformers(), ImmutableThreeWindingsTransformer::ofNullable);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return identifiable.getThreeWindingsTransformerStream().map(ImmutableThreeWindingsTransformer::ofNullable);
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return identifiable.getThreeWindingsTransformerCount();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        return ImmutableThreeWindingsTransformer.ofNullable(identifiable.getThreeWindingsTransformer(id));
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(identifiable.getGenerators(), ImmutableGenerator::ofNullable);
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return identifiable.getGeneratorStream().map(ImmutableGenerator::ofNullable);
    }

    @Override
    public int getGeneratorCount() {
        return identifiable.getGeneratorCount();
    }

    @Override
    public Generator getGenerator(String id) {
        return ImmutableGenerator.ofNullable(identifiable.getGenerator(id));
    }

    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(identifiable.getLoads(), ImmutableLoad::ofNullable);
    }

    @Override
    public Stream<Load> getLoadStream() {
        return identifiable.getLoadStream().map(ImmutableLoad::ofNullable);
    }

    @Override
    public int getLoadCount() {
        return identifiable.getLoadCount();
    }

    @Override
    public Load getLoad(String id) {
        return ImmutableLoad.ofNullable(identifiable.getLoad(id));
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(identifiable.getShuntCompensators(), ImmutableShuntCompensator::ofNullable);
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return identifiable.getShuntCompensatorStream().map(ImmutableShuntCompensator::ofNullable);
    }

    @Override
    public int getShuntCompensatorCount() {
        return identifiable.getShuntCompensatorCount();
    }

    @Override
    public ShuntCompensator getShuntCompensator(String id) {
        return ImmutableShuntCompensator.ofNullable(identifiable.getShuntCompensator(id));
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return Iterables.transform(identifiable.getDanglingLines(), ImmutableDanglingLine::ofNullable);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return identifiable.getDanglingLineStream().map(ImmutableDanglingLine::ofNullable);
    }

    @Override
    public int getDanglingLineCount() {
        return identifiable.getDanglingLineCount();
    }

    @Override
    public DanglingLine getDanglingLine(String id) {
        return ImmutableDanglingLine.ofNullable(identifiable.getDanglingLine(id));
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(identifiable.getStaticVarCompensators(), ImmutableStaticVarCompensator::ofNullable);
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return identifiable.getStaticVarCompensatorStream().map(ImmutableStaticVarCompensator::ofNullable);
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return identifiable.getStaticVarCompensatorCount();
    }

    @Override
    public StaticVarCompensator getStaticVarCompensator(String id) {
        return ImmutableStaticVarCompensator.ofNullable(identifiable.getStaticVarCompensator(id));
    }

    @Override
    public Switch getSwitch(String id) {
        return ImmutableSwitch.ofNullable(identifiable.getSwitch(id));
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Iterables.transform(identifiable.getSwitches(), ImmutableSwitch::ofNullable);
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return identifiable.getSwitchStream().map(ImmutableSwitch::ofNullable);
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
        return Iterables.transform(identifiable.getHvdcConverterStations(), ImmutableFactory::ofNullableHvdcConverterStation);
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        return identifiable.getHvdcConverterStationStream().map(ImmutableFactory::ofNullableHvdcConverterStation);
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
        return Iterables.transform(identifiable.getLccConverterStations(), ImmutableLccConverterStation::ofNullable);
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return identifiable.getLccConverterStationStream().map(ImmutableLccConverterStation::ofNullable);
    }

    @Override
    public int getLccConverterStationCount() {
        return identifiable.getLccConverterStationCount();
    }

    @Override
    public LccConverterStation getLccConverterStation(String id) {
        return ImmutableLccConverterStation.ofNullable(identifiable.getLccConverterStation(id));
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(identifiable.getVscConverterStations(), ImmutableVscConverterStation::ofNullable);
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return identifiable.getVscConverterStationStream().map(ImmutableVscConverterStation::ofNullable);
    }

    @Override
    public int getVscConverterStationCount() {
        return identifiable.getVscConverterStationCount();
    }

    @Override
    public VscConverterStation getVscConverterStation(String id) {
        return ImmutableVscConverterStation.ofNullable(identifiable.getVscConverterStation(id));
    }

    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        return Iterables.transform(identifiable.getHvdcLines(), ImmutableHvdcLine::ofNullable);
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return identifiable.getHvdcLineStream().map(ImmutableHvdcLine::ofNullable);
    }

    @Override
    public int getHvdcLineCount() {
        return identifiable.getHvdcLineCount();
    }

    @Override
    public HvdcLine getHvdcLine(String id) {
        return ImmutableHvdcLine.ofNullable(identifiable.getHvdcLine(id));
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
                return Iterables.transform(busBreakerView.getBuses(), ImmutableBus::ofNullable);
            }

            @Override
            public Stream<Bus> getBusStream() {
                return busBreakerView.getBusStream().map(ImmutableBus::ofNullable);
            }

            @Override
            public Iterable<Switch> getSwitches() {
                return Iterables.transform(busBreakerView.getSwitches(), ImmutableSwitch::ofNullable);
            }

            @Override
            public Stream<Switch> getSwitchStream() {
                return busBreakerView.getSwitchStream().map(ImmutableSwitch::ofNullable);
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
                return Iterables.transform(identifiable.getBusView().getBuses(), ImmutableBus::ofNullable);
            }

            @Override
            public Stream<Bus> getBusStream() {
                return identifiable.getBusView().getBusStream().map(ImmutableBus::ofNullable);
            }

            @Override
            public Collection<Component> getConnectedComponents() {
                return identifiable.getBusView().getConnectedComponents().stream().map(ImmutableComponent::ofNullable).collect(Collectors.toList());
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
