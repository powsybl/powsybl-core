/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A power network model.
 *
 * To create a new empty network with default implementation:
 *<pre>
 *    Network n = Network.create("test", "test");
 *</pre>
 *
 * <p>The network is initially created with one variant identified by
 * <code>VariantManagerConstants.INITIAL_VARIANT_ID</code>. {@link VariantManager} is
 * responsible for variant management and is accessible from the network thanks
 * to {@link #getVariantManager()}.
 *
 * <p>Instances of <code>Network</code> are not thread safe except for attributes
 * depending of the variant (always specified in the javadoc) if
 * {@link VariantManager#allowVariantMultiThreadAccess(boolean)} is set to true.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see NetworkFactory
 * @see VariantManager
 */
public interface Network extends Container<Network> {

    /**
     * A global bus/breaker view of the network.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    interface BusBreakerView {

        /**
         * Get all buses.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Iterable<Bus> getBuses();

        /**
         * Get all buses.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Stream<Bus> getBusStream();

        /**
         * Get all switches
         */
        Iterable<Switch> getSwitches();

        /**
         * Get all switches.
         */
        Stream<Switch> getSwitchStream();

        /**
         * Get the switch count.
         */
        int getSwitchCount();

        /**
         * Get a Bus.
         */
        default Bus getBus(String id) {
            throw new PowsyblException("Method should be overridden in the current implementation");
        }
    }

    /**
     * A global bus view of the network.
     */
    interface BusView {

        /**
         * Get all buses.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Iterable<Bus> getBuses();

        /**
         * Get all buses.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Stream<Bus> getBusStream();

        /**
         * Get a Bus.
         */
        default Bus getBus(String id) {
            throw new PowsyblException("Method should be overridden in the current implementation");
        }

        /**
         * Get all connected compoments.
         * <p>
         * Depends on the working variant.
         * @see VariantManager
         */
        Collection<Component> getConnectedComponents();

    }

    /**
     * Create an empty network using default implementation.
     *
     * @param id id of the network
     * @param sourceFormat source  format
     * @return an empty network
     */
    static Network create(String id, String sourceFormat) {
        return NetworkFactory.findDefault().createNetwork(id, sourceFormat);
    }

    /**
     * Just being able to name method create et not createNetwork. Create is not available in {@link NetworkFactory} for backward
     * compatibility reason. To cleanup when {@link NetworkFactory#create(String, String)} will be removed.
     */
    interface PrettyNetworkFactory {

        Network create(String id, String sourceFormat);
    }

    /**
     * Get network factory named {@code name}.
     *
     * @param name name of the {@link NetworkFactory}
     * @return network factory with  the given name
     */
    static PrettyNetworkFactory with(String name) {
        return (id, sourceFormat) -> NetworkFactory.find(name).createNetwork(id, sourceFormat);
    }

    /**
     * Get the date that the network represents.
     */
    DateTime getCaseDate();

    /**
     * Set the date that the network represents.
     * @throws IllegalArgumentException if date is null.
     */
    Network setCaseDate(DateTime date);

    /**
     * Get the forecast distance in minutes.
     * <p>Example: 0 for a snapshot, 6*60 to 30*60 for a DACF.
     */
    int getForecastDistance();

    Network setForecastDistance(int forecastDistance);

    /**
     * Get the source format.
     * @return the source format
     */
    String getSourceFormat();

    /**
     * Get the variant manager of the network.
     */
    VariantManager getVariantManager();

    /**
     * Get all countries.
     */
    Set<Country> getCountries();

    /**
     * Get the country count.
     */
    int getCountryCount();

    /**
     * Get a builder to create a new substation.
     */
    SubstationAdder newSubstation();

    /**
     * Get all substations.
     */
    Iterable<Substation> getSubstations();

    /**
     * Get all substations.
     */
    Stream<Substation> getSubstationStream();

    /**
     * Get the substation count.
     */
    int getSubstationCount();

    /**
     * Get substation located in a specific county, TSO and marked with a list
     * of geographical tag.
     *
     * @param country the country, if <code>null</code> there is no
     *                  filtering on countries
     * @param tsoId the id of the TSO, if <code>null</code> there is no
     *                  filtering on TSOs
     * @param geographicalTags a list a geographical tags
     */
    Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags);

    /**
     * Get substation located in a specific county, TSO and marked with a list
     * of geographical tag.
     *
     * @param country the country name, if empty string, the filtering will be on
     *                  substations without country, if <code>null</code> there is no
     *                  filtering on countries
     * @param tsoId the id of the TSO, if <code>null</code> there is no
     *                  filtering on TSOs
     * @param geographicalTags a list a geographical tags
     */
    Iterable<Substation> getSubstations(String country, String tsoId, String... geographicalTags);

    /**
     * Get a substation.
     *
     * @param id the id of the substation
     */
    Substation getSubstation(String id);

    /**
     * Get all substation voltage levels.
     */
    Iterable<VoltageLevel> getVoltageLevels();

    /**
     * Get all substation voltage levels.
     */
    Stream<VoltageLevel> getVoltageLevelStream();

    /**
     * Get the voltage level count.
     */
    int getVoltageLevelCount();

    /**
     * Get a substation voltage level.
     *
     * @param id the id of the substation voltage level
     */
    VoltageLevel getVoltageLevel(String id);

    /**
     * Get a builder to create a new AC line.
     */
    LineAdder newLine();

    /**
     * Get all AC lines.
     */
    Iterable<Line> getLines();

    /**
     * Get a branch
     * @param branchId the id of the branch
     */
    Branch getBranch(String branchId);

    /**
     * Get all branches
     */
    Iterable<Branch> getBranches();

    /**
     * Get all branches
     */
    Stream<Branch> getBranchStream();

    /**
     * Get the branch count.
     */
    int getBranchCount();

    /**
     * Get all AC lines.
     */
    Stream<Line> getLineStream();

    /**
     * Get the AC line count.
     */
    int getLineCount();

    /**
     * Get a AC line.
     *
     * @param id the name of the AC line
     */
    Line getLine(String id);

    /**
     * Get a builder to create a new AC tie line.
     */
    TieLineAdder newTieLine();

    /**
     * Get all two windings transformers.
     */
    Iterable<TwoWindingsTransformer> getTwoWindingsTransformers();

    /**
     * Get all two windings transformers.
     */
    Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream();

    /**
     * Get the two windings transformer count.
     */
    int getTwoWindingsTransformerCount();

    /**
     * Get a two windings transformer.
     *
     * @param id the id of the two windings transformer
     */
    TwoWindingsTransformer getTwoWindingsTransformer(String id);

    /**
     * Get all 3 windings transformers.
     */
    Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers();

    /**
     * Get all 3 windings transformers.
     */
    Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream();

    /**
     * Get the 3 windings transformer count.
     */
    int getThreeWindingsTransformerCount();

    /**
     * Get a 3 windings transformer.
     *
     * @param id the id of the 3 windings transformer
     */
    ThreeWindingsTransformer getThreeWindingsTransformer(String id);

    /**
     * Get all generators.
     */
    Iterable<Generator> getGenerators();

    /**
     * Get all generators.
     */
    Stream<Generator> getGeneratorStream();

    /**
     * Get the generator count.
     */
    int getGeneratorCount();

    /**
     * Get a generator.
     *
     * @param id the id of the generator
     */
    Generator getGenerator(String id);

    /**
     * Get all batteries.
     */
    Iterable<Battery> getBatteries();

    /**
     * Get all batteries.
     */
    Stream<Battery> getBatteryStream();

    /**
     * Get the battery count.
     */
    int getBatteryCount();

    /**
     * Get a battery.
     *
     * @param id the id of the battery
     */
    Battery getBattery(String id);

    /**
     * Get all loads.
     */
    Iterable<Load> getLoads();

    /**
     * Get all loads.
     */
    Stream<Load> getLoadStream();

    /**
     * Get the load count.
     */
    int getLoadCount();

    /**
     * Get a load.
     *
     * @param id the id the load
     */
    Load getLoad(String id);

    /**
     * Get all compensator shunts.
     */
    Iterable<ShuntCompensator> getShuntCompensators();

    /**
     * Get all compensator shunts.
     */
    Stream<ShuntCompensator> getShuntCompensatorStream();

    /**
     * Get the shunt count.
     */
    int getShuntCompensatorCount();

    /**
     * Get a compensator shunt.
     *
     * @param id the id of the compensator shunt
     */
    ShuntCompensator getShuntCompensator(String id);

    /**
     * Get all dangling lines.
     */
    Iterable<DanglingLine> getDanglingLines();

    /**
     * Get all dangling lines.
     */
    Stream<DanglingLine> getDanglingLineStream();

    /**
     * Get the dangling line count.
     */
    int getDanglingLineCount();

    /**
     * Get a dangling line.
     *
     * @param id the id of the dangling line
     */
    DanglingLine getDanglingLine(String id);

    /**
     * Get all static var compensators.
     */
    Iterable<StaticVarCompensator> getStaticVarCompensators();

    /**
     * Get all static var compensators.
     */
    Stream<StaticVarCompensator> getStaticVarCompensatorStream();

    /**
     * Get the static var compensator count.
     */
    int getStaticVarCompensatorCount();

    /**
     * Get a static var compensator.
     *
     * @param id the id of the static var compensator
     */
    StaticVarCompensator getStaticVarCompensator(String id);

    /**
     * Get a switch from its id.
     * @param id id of the switch
     * @return the switch
     */
    Switch getSwitch(String id);

    /**
     * Get all switches.
     * @return all switches
     */
    Iterable<Switch> getSwitches();

    /**
     * Get all switches.
     * @return all switches
     */
    Stream<Switch> getSwitchStream();

    /**
     * Get the switch count.
     *
     * @return the switch count
     */
    int getSwitchCount();

    /**
     * Get a busbar section from its id.
     * @param id id of the busbar section
     * @return the busbar section
     */
    BusbarSection getBusbarSection(String id);

    /**
     * Get all busbar sections.
     * @return all busbar sections
     */
    Iterable<BusbarSection> getBusbarSections();

    /**
     * Get all busbar sections.
     * @return all busbar sections
     */
    Stream<BusbarSection> getBusbarSectionStream();

    /**
     * Get the busbar section count.
     * @return the busbar section count.
     */
    int getBusbarSectionCount();

    /**
     * Get all HVDC converter stations.
     * @return all HVDC converter stations
     */
    Iterable<HvdcConverterStation<?>> getHvdcConverterStations();

    /**
     * Get all HVDC converter stations.
     * @return all HVDC converter stations
     */
    Stream<HvdcConverterStation<?>> getHvdcConverterStationStream();

    /**
     * Get HVDC converter stations count.
     * @return HVDC converter station count
     */
    int getHvdcConverterStationCount();

    /**
     * Get an HVDC converter station.
     * @param id the id of the HVDC converter station
     * @return the HVDC converter station or null if not found
     */
    HvdcConverterStation<?> getHvdcConverterStation(String id);

    /**
     * Get all LCC converter stations.
     * @return all LCC converter stations
     */
    Iterable<LccConverterStation> getLccConverterStations();

    /**
     * Get all LCC converter stations.
     * @return all LCC converter stations
     */
    Stream<LccConverterStation> getLccConverterStationStream();

    /**
     * Get LCC converter stations count.
     * @return LCC converter station count
     */
    int getLccConverterStationCount();

    /**
     * Get an LCC converter station.
     * @param id the id of the LCC converter station
     * @return the LCC converter station or null if not found
     */
    LccConverterStation getLccConverterStation(String id);

    /**
     * Get all VSC converter stations.
     * @return all VSC converter stations
     */
    Iterable<VscConverterStation> getVscConverterStations();

    /**
     * Get all VSC converter stations.
     * @return all VSC converter stations
     */
    Stream<VscConverterStation> getVscConverterStationStream();

    /**
     * Get VSC converter stations count.
     * @return VSC converter station count
     */
    int getVscConverterStationCount();

    /**
     * Get an VSC converter station.
     * @param id the id of the VSC converter station
     * @return the VSC converter station or null if not found
     */
    VscConverterStation getVscConverterStation(String id);

    /**
     * Get all HVDC lines.
     * @return all HVDC lines
     */
    Iterable<HvdcLine> getHvdcLines();

    /**
     * Get all HVDC lines.
     * @return all HVDC lines
     */
    Stream<HvdcLine> getHvdcLineStream();

    /**
     * Get HVDC lines count.
     * @return HVDC lines count
     */
    int getHvdcLineCount();

    /**
     * Get an HVDC line.
     * @param id the id of the HVDC line
     * @return the HVDC line or null if not found
     */
    HvdcLine getHvdcLine(String id);

    /**
     * Get an HVDC line from a converter station
     * @param converterStation a HVDC converter station
     * @return the HVDC line or null if not found
     */
    default HvdcLine getHvdcLine(HvdcConverterStation converterStation) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Get a builder to create a new HVDC line.
     * @return a builder to create a new HVDC line
     */
    HvdcLineAdder newHvdcLine();

    /**
     * Get a equipment.
     *
     * @param id the id of the equipment
     */
    Identifiable<?> getIdentifiable(String id);

    /**
     * Get all identifiables of the network.
     *
     * @return all identifiables of the network
     */
    Collection<Identifiable<?>> getIdentifiables();

    /**
     * Get all connectables of the network for a given type
     *
     * @param clazz connectable type class
     * @return all the connectables of the given type
     */
    default <C extends Connectable> Iterable<C> getConnectables(Class<C> clazz) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a stream of all connectables of the network for a given type
     *
     * @param clazz connectable type class
     * @return a stream of all the connectables of the given type
     */
    default <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
        throw new UnsupportedOperationException();
    }

    /**
     * Count the connectables of the network for a given type
     *
     * @param clazz connectable type class
     * @return the count of all the connectables of the given type
     */
    default <C extends Connectable> int getConnectableCount(Class<C> clazz) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get all connectables of the network
     *
     * @return all the connectables
     */
    default Iterable<Connectable> getConnectables() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a stream of all connectables of the network
     *
     * @return a stream of all the connectables
     */
    default Stream<Connectable> getConnectableStream() {
        throw new UnsupportedOperationException();
    }

    /**
     * Count the connectables of the network
     *
     * @return the count of all the connectables
     */
    default int getConnectableCount() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a bus/breaker view of the network.
     */
    BusBreakerView getBusBreakerView();

    /**
     * Get a bus view of the network.
     */
    BusView getBusView();

    /**
     * Merge with an other network. At the end of the merge the other network
     * is empty.
     * @param other the other network
     */
    void merge(Network other);

    void merge(Network... others);

    void addListener(NetworkListener listener);

    void removeListener(NetworkListener listener);
}
