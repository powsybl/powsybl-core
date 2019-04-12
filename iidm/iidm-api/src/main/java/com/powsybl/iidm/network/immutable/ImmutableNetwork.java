/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An immutable {@link Network}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * Usage example:
 * <pre> {@code
 *      Network network = ...; // a mutable network
 *      Network immutable = ImmutableNetwork.of(network);
 * }</pre>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ImmutableNetwork extends AbstractImmutableIdentifiable<Network> implements Network {

    public static final PowsyblException UNMODIFIABLE_EXCEPTION = new PowsyblException("Unmodifiable identifiable");

    private ImmutableNetwork(Network identifiable) {
        super(identifiable);
    }

    public static ImmutableNetwork of(Network network) {
        return new ImmutableNetwork(Objects.requireNonNull(network));
    }

    static PowsyblException createUnmodifiableNetworkException() {
        return UNMODIFIABLE_EXCEPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateTime getCaseDate() {
        return identifiable.getCaseDate();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public Network setCaseDate(DateTime date) {
        throw createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getForecastDistance() {
        return identifiable.getForecastDistance();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public Network setForecastDistance(int forecastDistance) {
        throw createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSourceFormat() {
        return identifiable.getSourceFormat();
    }

    /**
     * {@inheritDoc}
     * @return an {@link ImmutableVariantManager}
     */
    @Override
    public VariantManager getVariantManager() {
        return cache.getVariantManager(identifiable.getVariantManager());
    }

    /**
     * {@inheritDoc}
     * @return an unmodifiable set of {@link Country}
     */
    @Override
    public Set<Country> getCountries() {
        return Collections.unmodifiableSet(new HashSet<>(identifiable.getCountries()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCountryCount() {
        return identifiable.getCountryCount();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public SubstationAdder newSubstation() {
        throw createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * Substations are wrapped in {@link ImmutableSubstation}.
     */
    @Override
    public Iterable<Substation> getSubstations() {
        return Iterables.transform(identifiable.getSubstations(), cache::getSubstation);
    }

    /**
     * {@inheritDoc}
     * Substations are wrapped in {@link ImmutableSubstation}.
     */
    @Override
    public Stream<Substation> getSubstationStream() {
        return identifiable.getSubstationStream().map(cache::getSubstation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSubstationCount() {
        return identifiable.getSubstationCount();
    }

    /**
     * {@inheritDoc}
     * Substations are wrapped in {@link ImmutableSubstation}.
     */
    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        return Iterables.transform(identifiable.getSubstations(country, tsoId, geographicalTags), cache::getSubstation);
    }

    /**
     * {@inheritDoc}
     * Substations are wrapped in {@link ImmutableSubstation}.
     */
    @Override
    public Substation getSubstation(String id) {
        return cache.getSubstation(identifiable.getSubstation(id));
    }

    /**
     * {@inheritDoc}
     * VoltageLevels are wrapped in {@link ImmutableVoltageLevel}.
     */
    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Iterables.transform(identifiable.getVoltageLevels(), cache::getVoltageLevel);
    }

    /**
     * {@inheritDoc}
     * VoltageLevels are wrapped in {@link ImmutableVoltageLevel}.
     */
    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return identifiable.getVoltageLevelStream().map(cache::getVoltageLevel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVoltageLevelCount() {
        return identifiable.getVoltageLevelCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableVoltageLevel}
     */
    @Override
    public VoltageLevel getVoltageLevel(String id) {
        return cache.getVoltageLevel(identifiable.getVoltageLevel(id));
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public LineAdder newLine() {
        throw createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * Lines are wrapped in {@link ImmutableLine}.
     */
    @Override
    public Iterable<Line> getLines() {
        return Iterables.transform(identifiable.getLines(), cache::getLine);
    }

    /**
     * {@inheritDoc}
     * The returned branch is immutable.
     */
    @Override
    public Branch getBranch(String branchId) {
        return cache.getBranch(identifiable.getBranch(branchId));
    }

    /**
     * {@inheritDoc}
     * The returned branches are immutable.
     */
    @Override
    public Iterable<Branch> getBranches() {
        return Iterables.transform(identifiable.getBranches(), cache::getBranch);
    }

    /**
     * {@inheritDoc}
     * The returned branches are immutable.
     */
    @Override
    public Stream<Branch> getBranchStream() {
        return identifiable.getBranchStream().map(cache::getBranch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBranchCount() {
        return identifiable.getBranchCount();
    }

    /**
     * {@inheritDoc}
     * Lines are wrapped in {@link ImmutableLine}.
     */
    @Override
    public Stream<Line> getLineStream() {
        return identifiable.getLineStream().map(cache::getLine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLineCount() {
        return identifiable.getLineCount();
    }

    /**
     *
     * {@inheritDoc}
     * @return Returns an {@link ImmutableLine}
     */
    @Override
    public Line getLine(String id) {
        return cache.getLine(identifiable.getLine(id));
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public TieLineAdder newTieLine() {
        throw createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * TwoWindingsTransformers are wrapped in {@link ImmutableTwoWindingsTransformer}.
     */
    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(identifiable.getTwoWindingsTransformers(), cache::getTwoWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     * TwoWindingsTransformers are wrapped in {@link ImmutableTwoWindingsTransformer}.
     */
    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return identifiable.getTwoWindingsTransformerStream().map(cache::getTwoWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTwoWindingsTransformerCount() {
        return identifiable.getTwoWindingsTransformerCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableTwoWindingsTransformer}.
     */
    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        return cache.getTwoWindingsTransformer(identifiable.getTwoWindingsTransformer(id));
    }

    /**
     * {@inheritDoc}
     * ThreeWindingsTransformers are wrapped in {@link ImmutableThreeWindingsTransformer}.
     */
    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(identifiable.getThreeWindingsTransformers(), cache::getThreeWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     * ThreeWindingsTransformers are wrapped in {@link ImmutableThreeWindingsTransformer}.
     */
    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return identifiable.getThreeWindingsTransformerStream().map(cache::getThreeWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getThreeWindingsTransformerCount() {
        return identifiable.getThreeWindingsTransformerCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableThreeWindingsTransformer}.
     */
    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        return cache.getThreeWindingsTransformer(identifiable.getThreeWindingsTransformer(id));
    }

    /**
     * {@inheritDoc}
     * Generators are wrapped in {@link ImmutableGenerator}.
     */
    @Override
    public Iterable<Generator> getGenerators() {
        return Iterables.transform(identifiable.getGenerators(), cache::getGenerator);
    }

    /**
     * {@inheritDoc}
     * Generators are wrapped in {@link ImmutableGenerator}.
     */
    @Override
    public Stream<Generator> getGeneratorStream() {
        return identifiable.getGeneratorStream().map(cache::getGenerator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGeneratorCount() {
        return identifiable.getGeneratorCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableGenerator}.
     */
    @Override
    public Generator getGenerator(String id) {
        return cache.getGenerator(identifiable.getGenerator(id));
    }

    /**
     * {@inheritDoc}
     * Batteries are wrapped in {@link ImmutableBattery}.
     */
    @Override
    public Iterable<Battery> getBatteries() {
        return Iterables.transform(identifiable.getBatteries(), cache::getBattery);
    }

    /**
     * {@inheritDoc}
     * Batteries are wrapped in {@link ImmutableBattery}.
     */
    @Override
    public Stream<Battery> getBatteryStream() {
        return identifiable.getBatteryStream().map(cache::getBattery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBatteryCount() {
        return identifiable.getBatteryCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableBattery}
     */
    @Override
    public Battery getBattery(String id) {
        return cache.getBattery(identifiable.getBattery(id));
    }

    /**
     * {@inheritDoc}
     * Loads are wrapped in {@link ImmutableLoad}.
     */
    @Override
    public Iterable<Load> getLoads() {
        return Iterables.transform(identifiable.getLoads(), cache::getLoad);
    }

    /**
     * {@inheritDoc}
     * Loads are wrapped in {@link ImmutableLoad}.
     */
    @Override
    public Stream<Load> getLoadStream() {
        return identifiable.getLoadStream().map(cache::getLoad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLoadCount() {
        return identifiable.getLoadCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableLoad}.
     */
    @Override
    public Load getLoad(String id) {
        return cache.getLoad(identifiable.getLoad(id));
    }

    /**
     * {@inheritDoc}
     * ShuntCompensators are wrapped in {@link ImmutableShuntCompensator}.
     */
    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Iterables.transform(identifiable.getShuntCompensators(), cache::getShuntCompensator);
    }

    /**
     * {@inheritDoc}
     * ShuntCompensators are wrapped in {@link ImmutableShuntCompensator}.
     */
    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return identifiable.getShuntCompensatorStream().map(cache::getShuntCompensator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getShuntCompensatorCount() {
        return identifiable.getShuntCompensatorCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableShuntCompensator}.
     */
    @Override
    public ShuntCompensator getShuntCompensator(String id) {
        return cache.getShuntCompensator(identifiable.getShuntCompensator(id));
    }

    /**
     * {@inheritDoc}
     * DanglingLines are wrapped in {@link ImmutableDanglingLine}.
     */
    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return Iterables.transform(identifiable.getDanglingLines(), cache::getDanglingLine);
    }

    /**
     * {@inheritDoc}
     * DanglingLines are wrapped in {@link ImmutableDanglingLine}.
     */
    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return identifiable.getDanglingLineStream().map(cache::getDanglingLine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDanglingLineCount() {
        return identifiable.getDanglingLineCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableDanglingLine}.
     */
    @Override
    public DanglingLine getDanglingLine(String id) {
        return cache.getDanglingLine(identifiable.getDanglingLine(id));
    }

    /**
     * {@inheritDoc}
     * StaticVarCompensators are wrapped in {@link ImmutableStaticVarCompensator}.
     */
    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Iterables.transform(identifiable.getStaticVarCompensators(), cache::getStaticVarCompensator);
    }

    /**
     * {@inheritDoc}
     * StaticVarCompensators are wrapped in {@link ImmutableStaticVarCompensator}.
     */
    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return identifiable.getStaticVarCompensatorStream().map(cache::getStaticVarCompensator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStaticVarCompensatorCount() {
        return identifiable.getStaticVarCompensatorCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableStaticVarCompensator}.
     */
    @Override
    public StaticVarCompensator getStaticVarCompensator(String id) {
        return cache.getStaticVarCompensator(identifiable.getStaticVarCompensator(id));
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableSwitch}.
     */
    @Override
    public Switch getSwitch(String id) {
        return cache.getSwitch(identifiable.getSwitch(id));
    }

    /**
     * {@inheritDoc}
     * Switches are wrapped in {@link ImmutableSwitch}.
     */
    @Override
    public Iterable<Switch> getSwitches() {
        return Iterables.transform(identifiable.getSwitches(), cache::getSwitch);
    }

    /**
     * {@inheritDoc}
     * Switches are wrapped in {@link ImmutableSwitch}.
     */
    @Override
    public Stream<Switch> getSwitchStream() {
        return identifiable.getSwitchStream().map(cache::getSwitch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSwitchCount() {
        return identifiable.getSwitchCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableBusbarSection}.
     */
    @Override
    public BusbarSection getBusbarSection(String id) {
        return cache.getBusbarSection(identifiable.getBusbarSection(id));
    }

    /**
     * {@inheritDoc}
     * BusbarSections are wrapped in {@link ImmutableBusbarSection}.
     */
    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        return Iterables.transform(identifiable.getBusbarSections(), cache::getBusbarSection);
    }

    /**
     * {@inheritDoc}
     * BusbarSections are wrapped in {@link ImmutableBusbarSection}.
     */
    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return identifiable.getBusbarSectionStream().map(cache::getBusbarSection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBusbarSectionCount() {
        return identifiable.getBusbarSectionCount();
    }

    /**
     * {@inheritDoc}
     * HvdcConverterStations are wrapped in {@link ImmutableLccConverterStation} or {@link ImmutableVscConverterStation}.
     */
    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        return Iterables.transform(identifiable.getHvdcConverterStations(), cache::getHvdcConverterStation);
    }


    /**
     * {@inheritDoc}
     * HvdcConverterStations are wrapped in {@link ImmutableLccConverterStation} or {@link ImmutableVscConverterStation}.
     */
    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        return identifiable.getHvdcConverterStationStream().map(cache::getHvdcConverterStation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHvdcConverterStationCount() {
        return identifiable.getHvdcConverterStationCount();
    }

    @Override
    public HvdcConverterStation<?> getHvdcConverterStation(String id) {
        return identifiable.getHvdcConverterStation(id);
    }

    /**
     * {@inheritDoc}
     * LccConverterStations are wrapped in {@link ImmutableLccConverterStation}.
     */
    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Iterables.transform(identifiable.getLccConverterStations(), cache::getLccConverterStation);
    }

    /**
     * {@inheritDoc}
     * LccConverterStations are wrapped in {@link ImmutableLccConverterStation}.
     */
    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return identifiable.getLccConverterStationStream().map(cache::getLccConverterStation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLccConverterStationCount() {
        return identifiable.getLccConverterStationCount();
    }

    /**
     * {@inheritDoc}
     * LccConverterStations are wrapped in {@link ImmutableLccConverterStation}.
     */
    @Override
    public LccConverterStation getLccConverterStation(String id) {
        return cache.getLccConverterStation(identifiable.getLccConverterStation(id));
    }

    /**
     * {@inheritDoc}
     * VscConverterStations are wrapped in {@link ImmutableVscConverterStation}.
     */
    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Iterables.transform(identifiable.getVscConverterStations(), cache::getVscConverterStation);
    }

    /**
     * {@inheritDoc}
     * VscConverterStations are wrapped in {@link ImmutableVscConverterStation}.
     */
    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return identifiable.getVscConverterStationStream().map(cache::getVscConverterStation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVscConverterStationCount() {
        return identifiable.getVscConverterStationCount();
    }

    /**
     * {@inheritDoc}
     * VscConverterStations are wrapped in {@link ImmutableVscConverterStation}.
     */
    @Override
    public VscConverterStation getVscConverterStation(String id) {
        return cache.getVscConverterStation(identifiable.getVscConverterStation(id));
    }

    /**
     * {@inheritDoc}
     * HvdcLines are wrapped in {@link ImmutableHvdcLine}.
     */
    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        return Iterables.transform(identifiable.getHvdcLines(), cache::getHvdcLine);
    }

    /**
     * {@inheritDoc}
     * HvdcLines are wrapped in {@link ImmutableHvdcLine}.
     */
    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return identifiable.getHvdcLineStream().map(cache::getHvdcLine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHvdcLineCount() {
        return identifiable.getHvdcLineCount();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableHvdcLine}.
     */
    @Override
    public HvdcLine getHvdcLine(String id) {
        return cache.getHvdcLine(identifiable.getHvdcLine(id));
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public HvdcLineAdder newHvdcLine() {
        throw createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * @return an immutable identifiable
     */
    @Override
    public Identifiable<?> getIdentifiable(String id) {
        return cache.getIdentifiable(identifiable.getIdentifiable(id));
    }

    /**
     * {@inheritDoc}
     * @return an unmodifiable collections of immutable identifiable objects
     */
    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        Set<Identifiable<?>> res = new HashSet<>();
        for (Identifiable i : identifiable.getIdentifiables()) {
            res.add(cache.getIdentifiable(i));
        }
        return Collections.unmodifiableSet(res);
    }

    /**
     * {@inheritDoc}
     * The return-value is wrapped into immutable.
     */
    @Override
    public BusBreakerView getBusBreakerView() {
        return new BusBreakerView() {

            final BusBreakerView busBreakerView = identifiable.getBusBreakerView();

            /**
             * {@inheritDoc}
             * Buses are wrapped in {@link ImmutableBus}.
             */
            @Override
            public Iterable<Bus> getBuses() {
                return Iterables.transform(busBreakerView.getBuses(), cache::getBus);
            }

            /**
             * {@inheritDoc}
             * Buses are wrapped in {@link ImmutableBus}.
             */
            @Override
            public Stream<Bus> getBusStream() {
                return busBreakerView.getBusStream().map(cache::getBus);
            }

            /**
             * {@inheritDoc}
             * Switches are wrapped in {@link ImmutableSwitch}.
             */
            @Override
            public Iterable<Switch> getSwitches() {
                return Iterables.transform(busBreakerView.getSwitches(), cache::getSwitch);
            }

            /**
             * {@inheritDoc}
             * Switches are wrapped in {@link ImmutableSwitch}.
             */
            @Override
            public Stream<Switch> getSwitchStream() {
                return busBreakerView.getSwitchStream().map(cache::getSwitch);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int getSwitchCount() {
                return busBreakerView.getSwitchCount();
            }
        };
    }

    /**
     * {@inheritDoc}
     * The return-value is wrapped into immutable.
     */
    @Override
    public BusView getBusView() {
        return new BusView() {

            /**
             * {@inheritDoc}
             * Buses are wrapped in {@link ImmutableBus}.
             */
            @Override
            public Iterable<Bus> getBuses() {
                return Iterables.transform(identifiable.getBusView().getBuses(), cache::getBus);
            }

            /**
             * {@inheritDoc}
             * Buses are wrapped in {@link ImmutableBus}.
             */
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

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void merge(Network other) {
        throw createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void merge(Network... others) {
        throw createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(NetworkListener listener) {
        identifiable.addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(NetworkListener listener) {
        identifiable.removeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerType getContainerType() {
        return identifiable.getContainerType();
    }
}
