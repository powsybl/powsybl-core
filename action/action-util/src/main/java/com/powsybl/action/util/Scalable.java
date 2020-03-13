/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Ameni Walha <ameni.walha at rte-france.com>
 */
public interface Scalable {

    /**
     * Sign convention usable for scaling.
     */
    enum ScalingConvention {
        /**
         * Generator convention means that a positive scale will
         * imply an increase of generators target power and a decrease
         * of load consumption.
         */
        GENERATOR,

        /**
         * Load convention means that a positive scale will
         * imply a decrease of generators target power and an increase
         * of load consumption.
         */
        LOAD,
    }

    /**
     * Get the constant active power in MW injected at the network.
     */
    double initialValue(Network n);

    /**
     * Set the constant active power to zero.
     */
    void reset(Network n);

    /**
     * Get the maximal active power in MW.
     * Uses Generator convention by default
     */
    double maximumValue(Network n);

    /**
     * Get the minimal active power in MW.
     * Uses Generator convention by default
     */
    double minimumValue(Network n);

    /**
     * Get the maximal active power in MW with scaling convention.
     * @see ScalingConvention
     */
    double maximumValue(Network n, ScalingConvention scalingConvention);

    /**
     * Get the minimal active power in MW with scaling convention.
     * @see ScalingConvention
     */
    double minimumValue(Network n, ScalingConvention scalingConvention);

    /**
     * @deprecated listGenerators should be replaced by filterInjections
     */
    @Deprecated
    void listGenerators(Network n, List<Generator> generators, List<String> notFoundGenerators);

    /**
     * @deprecated listGenerators should be replaced by filterInjections
     */
    @Deprecated
    List<Generator> listGenerators(Network n, List<String> notFoundGenerators);

    /**
     * @deprecated listGenerators should be replaced by filterInjections
     */
    @Deprecated
    List<Generator> listGenerators(Network n);

    /**
     * Scans all the expected injections of the scalable.
     * If the injection can be found in given network, it is added the the injections list.
     * Otherwise, its identifier is added to the "notFound" list.
     *
     * @param network network
     * @param injections network injections used in the scalable
     * @param notFound expected injections not found in the network
     */
    void filterInjections(Network network, List<Injection> injections, List<String> notFound);


    /**
     * Scans all the expected injections of the scalable.
     * If the injection can be found in given network, it is added the the injections list.
     * Otherwise, its identifier is added to the "notFound" list.
     *
     * @param network network
     * @param notFound expected injections not found in the network
     * @return network injections used in the scalable
     */
    List<Injection> filterInjections(Network network, List<String> notFound);


    /**
     * Scans all the expected injections of the scalable.
     * If the injection can be found in given network, it is added the the injections list.
     *
     * @param network network
     * @return network injections used in the scalable
     */
    List<Injection> filterInjections(Network network);

    /**
     * Scale the given network using Generator convention by default.
     * The actual scaling value may be different to the one asked, if
     * the Scalable limit is reached.
     *
     * @param n network
     * @param asked value asked to adjust the scalable active power
     * @return the actual value of the scalable active power adjustment
     */
    double scale(Network n, double asked);

    /**
     * Scale the given network.
     * The actual scaling value may be different to the one asked, if
     * the Scalable limit is reached.
     *
     * @param n network
     * @param asked value asked to adjust the scalable active power
     * @param scalingConvention power convention used for scaling
     * @return the actual value of the scalable active power adjustment
     * @see ScalingConvention
     */
    double scale(Network n, double asked, ScalingConvention scalingConvention);

    /**
     * @deprecated gen should be replaced by onGenerator
     */
    @Deprecated
    static GeneratorScalable gen(String id) {
        return new GeneratorScalable(id);
    }

    /**
     * create GeneratorScalable with id
     */
    static GeneratorScalable onGenerator(String id) {
        return new GeneratorScalable(id);
    }

    /**
     * create GeneratorScalable with id, min and max power values for scaling
     */
    static GeneratorScalable onGenerator(String id, double minValue, double maxValue) {
        return new GeneratorScalable(id, minValue, maxValue);
    }

    /**
     * create LoadScalable with id
     */
    static LoadScalable onLoad(String id) {
        return new LoadScalable(id);
    }

    /**
     * create LoadScalable with id, min and max power values for scaling
     */
    static LoadScalable onLoad(String id, double minValue, double maxValue) {
        return new LoadScalable(id, minValue, maxValue);
    }

    static Scalable scalable(String id) {
        return new ScalableAdapter(id);
    }

    static List<Scalable> scalables(String... ids) {
        return Arrays.stream(ids).map(ScalableAdapter::new).collect(Collectors.toList());
    }

    static ProportionalScalable proportional(List<Float> percentages, List<Scalable> scalables) {
        return new ProportionalScalable(percentages, scalables);
    }

    static ProportionalScalable proportional(List<Float> percentages, List<Scalable> scalables, boolean iterative) {
        return new ProportionalScalable(percentages, scalables, iterative);
    }

    static ProportionalScalable proportional(float percentage, Scalable scalable) {
        return new ProportionalScalable(Collections.singletonList(percentage), Collections.singletonList(scalable));
    }

    static ProportionalScalable proportional(float percentage1, Scalable scalable1, float percentage2, Scalable scalable2) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2),
                                        Arrays.asList(scalable1, scalable2));
    }

    static ProportionalScalable proportional(float percentage1, Scalable scalable1, float percentage2, Scalable scalable2, float percentage3, Scalable scalable3) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2, percentage3),
                                        Arrays.asList(scalable1, scalable2, scalable3));
    }

    static ProportionalScalable proportional(float percentage1, Scalable scalable1, float percentage2, Scalable scalable2, float percentage3, Scalable scalable3, float percentage4, Scalable scalable4) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2, percentage3, percentage4),
                                        Arrays.asList(scalable1, scalable2, scalable3, scalable4));
    }

    static ProportionalScalable proportional(float percentage1, Scalable scalable1, float percentage2, Scalable scalable2, float percentage3, Scalable scalable3, float percentage4, Scalable scalable4, float percentage5, Scalable scalable5) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2, percentage3, percentage4, percentage5),
                                        Arrays.asList(scalable1, scalable2, scalable3, scalable4, scalable5));
    }

    static StackScalable stack(Scalable... scalables) {
        return new StackScalable(scalables);
    }

    static StackScalable stack(String... ids) {
        List<Scalable> identifierScalables = Arrays.stream(ids).map(ScalableAdapter::new).collect(Collectors.toList());
        return new StackScalable(identifierScalables);
    }
}
