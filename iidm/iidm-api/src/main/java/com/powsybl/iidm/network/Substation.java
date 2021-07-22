/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A substation is a collection of equipments located at a the same geographical
 * site.
 * <p>It is composed of several voltage levels.
 * <p>A substation is located to one country and belongs to one TSO.
 * <p> To create a substation, see {@link SubstationAdder}
 *
 * <p>
 *  Characteristics
 * </p>
 *
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *         <tr>
 *             <th style="border: 1px solid black">Attribute</th>
 *             <th style="border: 1px solid black">Type</th>
 *             <th style="border: 1px solid black">Unit</th>
 *             <th style="border: 1px solid black">Required</th>
 *             <th style="border: 1px solid black">Defaut value</th>
 *             <th style="border: 1px solid black">Description</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td style="border: 1px solid black">Id</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Unique identifier of the substation</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the substation</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Country</td>
 *             <td style="border: 1px solid black">Countrye</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The country where this substation is located</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Tso</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The TSO this substation belongs to</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">GeoraphicalTags</td>
 *             <td style="border: 1px solid black">List of String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">A list of geographical tags</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see VoltageLevel
 * @see Country
 * @see SubstationAdder
 */
public interface Substation extends Container<Substation> {

    Network getNetwork();

    /**
     * Get an optional containing the country to which the substation belongs if it is defined, Optional.empty() if not.
     */
    Optional<Country> getCountry();

    /**
     * Get country to which the substation belongs if it is defined, null otherwise.
     * @return the country to which the substation belongs if it is defined, null otherwise.
     */
    Country getNullableCountry();

    Substation setCountry(Country country);

    /**
     * Get the TSO to which the substation belongs.
     */
    String getTso();

    Substation setTso(String tso);

    /**
     * Get a builder to create a new voltage level in the substation.
     */
    VoltageLevelAdder newVoltageLevel();

    /**
     * Get a builder to create a new voltage level in the substation. The builder is initialized with all the values of the given voltage level.
     */
    default VoltageLevelAdder newVoltageLevel(VoltageLevel voltageLevel) {
        return newVoltageLevel()
                .setFictitious(voltageLevel.isFictitious())
                .setNominalV(voltageLevel.getNominalV())
                .setLowVoltageLimit(voltageLevel.getLowVoltageLimit())
                .setHighVoltageLimit(voltageLevel.getHighVoltageLimit())
                .setTopologyKind(voltageLevel.getTopologyKind());
    }

    /**
     * Get the voltage levels of the substation.
     */
    Iterable<VoltageLevel> getVoltageLevels();

    /**
     * Get the voltage levels of the substation.
     */
    Stream<VoltageLevel> getVoltageLevelStream();

    /**
     * Get a builder to create a new two windings transformer in the substation.
     */
    TwoWindingsTransformerAdder newTwoWindingsTransformer();

    /**
     * Get a builder to create a new two windings transformer in the substation. The builder is initialized with all the values of the given two windings transformer.
     */
    default TwoWindingsTransformerAdder newTwoWindingsTransformer(TwoWindingsTransformer twt) {
        return newTwoWindingsTransformer()
                .setFictitious(twt.isFictitious())
                .setR(twt.getR())
                .setX(twt.getX())
                .setG(twt.getG())
                .setB(twt.getB())
                .setRatedU1(twt.getRatedU1())
                .setRatedU2(twt.getRatedU2())
                .setRatedS(twt.getRatedS());
    }

    /**
     * Get the two windings transformers connected to the substation.
     */
    Iterable<TwoWindingsTransformer> getTwoWindingsTransformers();

    /**
     * Get the two windings transformers connected to the substation.
     */
    Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream();

    /**
     * Get the two windings transformers count
     */
    int getTwoWindingsTransformerCount();

    /**
     * Get a builder to create a new 3 windings transformer in the substation.
     */
    ThreeWindingsTransformerAdder newThreeWindingsTransformer();

    /**
     * Get a builder to create a new 3 windings transformer in the substation. The builder is initialized with all the values of a the given 3 windings transformer.
     */
    default ThreeWindingsTransformerAdder newThreeWindingsTransformer(ThreeWindingsTransformer twt) {
        return newThreeWindingsTransformer()
                .setFictitious(twt.isFictitious())
                .setRatedU0(twt.getRatedU0());
        // not possible to set incomplete legs here, user has to implement it on custom implementation
    }

    /**
     * Get the 3 windings transformers connected to the substation.
     */
    Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers();

    /**
     * Get the 3 windings transformers connected to the substation.
     */
    Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream();

    /**
     * Get the three windings transformers count
     */
    int getThreeWindingsTransformerCount();

    /**
     * Get geographical tags associated to the substation.
     */
    Set<String> getGeographicalTags();

    /**
     * Associate a new geographical tag to the substation.
     */
    Substation addGeographicalTag(String tag);

    /**
     * Remove this substation from the network.
     */
    default void remove() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
