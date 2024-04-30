/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.DELTA_P;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
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
     * If the injection can be found in given network, it is added to the injections list.
     *
     * @param network network
     * @return network injections used in the scalable
     */
    List<Injection> filterInjections(Network network);

    /**
     * Scale the given network using Generator convention by default.
     * The actual scaling value may be different to the one asked if
     * the Scalable limit is reached. If the scalable is disconnected,
     * the scaling value will be 0.
     *
     * @param n network
     * @param asked value asked to adjust the scalable active power
     * @param parameters specific parameters used to scale
     * @return the actual value of the scalable active power adjustment
     */
    double scale(Network n, double asked, ScalingParameters parameters);

    default double scale(Network n, double asked) {
        return scale(n, asked, new ScalingParameters());
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

    /**
     * create DanglingLineScalable with id.
     * The generator scaling convention is used by default.
     */
    static DanglingLineScalable onDanglingLine(String id) {
        return new DanglingLineScalable(id);
    }

    /**
     * create DanglingLineScalable with id and the scaling convention that will be used.
     */
    static DanglingLineScalable onDanglingLine(String id, ScalingConvention scalingConvention) {
        return new DanglingLineScalable(id, scalingConvention);
    }

    /**
     * create DanglingLineScalable with id, min and max power values for scaling.
     * The generator scaling convention is used by default.
     */
    static DanglingLineScalable onDanglingLine(String id, double minValue, double maxValue) {
        return new DanglingLineScalable(id, minValue, maxValue);
    }

    /**
     * create DanglingLineScalable with id, min and max power values for scaling and the scaling convention that will be used.
     */
    static DanglingLineScalable onDanglingLine(String id, double minValue, double maxValue, ScalingConvention scalingConvention) {
        return new DanglingLineScalable(id, minValue, maxValue, scalingConvention);
    }

    static Scalable scalable(String id) {
        return new ScalableAdapter(id);
    }

    static List<Scalable> scalables(String... ids) {
        return Arrays.stream(ids).map(ScalableAdapter::new).collect(Collectors.toList());
    }

    static ProportionalScalable proportional(List<? extends Injection> injections, ProportionalScalable.DistributionMode distributionMode) {
        return new ProportionalScalable(injections, distributionMode);
    }

    static ProportionalScalable proportional(List<? extends Injection> injections, ProportionalScalable.DistributionMode distributionMode, double minValue, double maxValue) {
        return new ProportionalScalable(injections, distributionMode, minValue, maxValue);
    }

    static ProportionalScalable proportional(List<Double> percentages, List<Scalable> scalables) {
        return new ProportionalScalable(percentages, scalables);
    }

    static ProportionalScalable proportional(List<Double> percentages, List<Scalable> scalables, double minValue, double maxValue) {
        return new ProportionalScalable(percentages, scalables, minValue, maxValue);
    }

    static ProportionalScalable proportional(double percentage, Scalable scalable) {
        return new ProportionalScalable(Collections.singletonList(percentage), Collections.singletonList(scalable));
    }

    static ProportionalScalable proportional(double percentage1, Scalable scalable1, double percentage2, Scalable scalable2) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2),
                                        Arrays.asList(scalable1, scalable2));
    }

    static ProportionalScalable proportional(double percentage1, Scalable scalable1, double percentage2, Scalable scalable2, double percentage3, Scalable scalable3) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2, percentage3),
                                        Arrays.asList(scalable1, scalable2, scalable3));
    }

    static ProportionalScalable proportional(double percentage1, Scalable scalable1, double percentage2, Scalable scalable2, double percentage3, Scalable scalable3, double percentage4, Scalable scalable4) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2, percentage3, percentage4),
                                        Arrays.asList(scalable1, scalable2, scalable3, scalable4));
    }

    static ProportionalScalable proportional(double percentage1, Scalable scalable1, double percentage2, Scalable scalable2, double percentage3, Scalable scalable3, double percentage4, Scalable scalable4, double percentage5, Scalable scalable5) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2, percentage3, percentage4, percentage5),
                                        Arrays.asList(scalable1, scalable2, scalable3, scalable4, scalable5));
    }

    static StackScalable stack(Injection<?>... injections) {
        List<Scalable> injectionScalables = Arrays.stream(injections).map(ScalableAdapter::new).collect(Collectors.toList());
        return new StackScalable(injectionScalables);
    }

    static StackScalable stack(double minValue, double maxValue, Injection<?>... injections) {
        List<Scalable> injectionScalables = Arrays.stream(injections).map(ScalableAdapter::new).collect(Collectors.toList());
        return new StackScalable(injectionScalables, minValue, maxValue);
    }

    static StackScalable stack(List<? extends Injection<?>> injections) {
        List<Scalable> injectionScalables = injections.stream().map(ScalableAdapter::new).collect(Collectors.toList());
        return new StackScalable(injectionScalables);
    }

    static StackScalable stack(List<? extends Injection<?>> injections, double minValue, double maxValue) {
        List<Scalable> injectionScalables = injections.stream().map(ScalableAdapter::new).collect(Collectors.toList());
        return new StackScalable(injectionScalables, minValue, maxValue);
    }

    static StackScalable stack(Scalable... scalables) {
        return new StackScalable(scalables);
    }

    static StackScalable stack(double minValue, double maxValue, Scalable... scalables) {
        return new StackScalable(minValue, maxValue, scalables);
    }

    static StackScalable stack(String... ids) {
        List<Scalable> identifierScalables = Arrays.stream(ids).map(ScalableAdapter::new).collect(Collectors.toList());
        return new StackScalable(identifierScalables);
    }

    static StackScalable stack(double minValue, double maxValue, String... ids) {
        List<Scalable> identifierScalables = Arrays.stream(ids).map(ScalableAdapter::new).collect(Collectors.toList());
        return new StackScalable(identifierScalables, minValue, maxValue);
    }

    static UpDownScalable upDown(Scalable upScalable, Scalable downScalable) {
        return new UpDownScalable(upScalable, downScalable);
    }

    static UpDownScalable upDown(Scalable upScalable, Scalable downScalable, double minValue, double maxValue) {
        return new UpDownScalable(upScalable, downScalable, minValue, maxValue);
    }

    /**
     * Returns the value that has to be added to the network, depending on the type of variation chosen in the parameters
     * @param scalingParameters Scaling parameters including a variation type (DELTA_P or TARGET_P)
     * @param askedValue value of scaling asked on the scalable
     * @param currentGlobalPower current global power in the network
     * @return the variation value if the type is DELTA_P, else the difference between the variation value and the current global value sum
     */
    static double getVariationAsked(ScalingParameters scalingParameters, double askedValue, double currentGlobalPower) {
        return scalingParameters.getScalingType() == DELTA_P
            ? askedValue
            : askedValue - currentGlobalPower;
    }

    /**
     * Returns the current power value for the injections corresponding to this Scalable
     * @param network Network in which the injections are defined
     * @param asked value of scaling asked on the scalable. This is used to know in which direction we want to scale for UpDownScalables.
     * @param scalingConvention The value is computed either with Generator or Load convention according to this parameter.
     * @return the current power value
     */
    double getSteadyStatePower(Network network, double asked, ScalingConvention scalingConvention);
}
