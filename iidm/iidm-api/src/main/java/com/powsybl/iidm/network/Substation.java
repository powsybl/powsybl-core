/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Set;
import java.util.stream.Stream;

/**
 * A substation is a collection of equipment located at a the same geographical
 * site.
 * <p>It is composed of several voltage levels.
 * <p>A substation is located to one country and belongs to one TSO.
 * <p> To create a substation, see {@link SubstationAdder}
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see VoltageLevel
 * @see Country
 * @see SubstationAdder
 */
public interface Substation extends Container<Substation> {

    Network getNetwork();

    /**
     * Get the country to which the substation belongs.
     */
    Country getCountry();

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

}
