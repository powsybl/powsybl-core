/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.DELTA_P;

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

    static ProportionalScalable proportional(List<Float> percentages, List<Scalable> scalables) {
        return new ProportionalScalable(percentages, scalables);
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

    static UpDownScalable upDown(Scalable upScalable, Scalable downScalable) {
        return new UpDownScalable(upScalable, downScalable);
    }

    /**
     * Returns the value that has to be added to the network, depending on the type of variation chosen in the parameters
     * @param scalingParameters Scaling parameters including a variation type (DELTA_P or TARGET_P) and a variation value
     * @param sum current global value
     * @return the variation value if the type is DELTA_P, else the difference between the variation value and the current global value sum
     */
    static double getVariationAsked(ScalingParameters scalingParameters, AtomicReference<Double> sum) {
        return scalingParameters.getScalingType() == DELTA_P
            ? scalingParameters.getScalingValue()
            : scalingParameters.getScalingValue() - sum.get();
    }

    public static void createReport(Reporter reporter, String reporterKey, String message, TypedValue errorSeverity) {
        reporter.report(Report.builder()
            .withKey(reporterKey)
            .withDefaultMessage(message)
            .withSeverity(errorSeverity)
            .build());
    }

    /**
     * Computes and applies a scaling variation of power on a list of loads, using variation parameters defined by the user.
     * Depending on the distribution mode chosen, the distribution percentage for each load will be computed differently:
     * <ul>
     *     <li>PROPORTIONAL_TO_P0: P0 divided by the sum of all the P0</li>
     *     <li>REGULAR_DISTRIBUTION: 100% divided by the number of loads</li>
     * </ul>
     * If the global sum computed for the PROPORTIONAL_TO_P0 mode is at zero, the system will default to the REGULAR_DISTRIBUTION mode.
     * @param network The network on which the scaling variation is applied
     * @param subReporter The reporter
     * @param scalingParameters The parameters for the scaling
     * @param loads The loads on which the scaling will be done
     * @return the value of the power that was finally allocated on the loads
     */
    static double scaleOnLoads(Network network,
                                      Reporter subReporter,
                                      ScalingParameters scalingParameters,
                                      Collection<Load> loads) {
        // Check that scalingParameters is coherent with the type of elements given
        if (scalingParameters.getScalingConvention() != ScalingConvention.LOAD) {
            throw new PowsyblException(String.format("Scaling convention in the parameters cannot be %s for loads", scalingParameters.getScalingConvention()));
        }

        // Initialisation of the ProportionalScalable
        ProportionalScalable proportionalScalable = new ProportionalScalable();

        // Global current value
        AtomicReference<Double> sumP0 = new AtomicReference<>(0D);

        // The variation mode chosen changes how the percentages are computed
        switch (scalingParameters.getDistributionMode()) {
            case PROPORTIONAL_TO_P0 -> {
                // Proportional to the P0 of the loads
                loads.forEach(load ->
                    sumP0.set(sumP0.get() + load.getP0())
                );
                if (sumP0.get() > 0.0) {
                    loads.forEach(load ->
                        proportionalScalable.getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onLoad(load.getId()), (float) (load.getP0() * 100.0 / sumP0.get())))
                    );
                } else {
                    // If no power is currently configured, a regular distribution is used
                    loads.forEach(load -> {
                        sumP0.set(sumP0.get() + load.getP0());
                        proportionalScalable.getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onLoad(load.getId()), (float) (100.0 / loads.size())));
                    });
                }
            }
            case REGULAR_DISTRIBUTION ->
                // Each load get the same
                loads.forEach(load -> {
                    sumP0.set(sumP0.get() + load.getP0());
                    proportionalScalable.getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onLoad(load.getId()), (float) (100.0 / loads.size())));
                });
            default ->
                throw new IllegalArgumentException(String.format("Variation mode cannot be %s for LoadScalables", scalingParameters.getDistributionMode()));
        }

        // Variation asked globally
        double variationAsked = Scalable.getVariationAsked(scalingParameters, sumP0);

        // Do the repartition
        double variationDone = proportionalScalable.scale(network, variationAsked, scalingParameters);

        // Report
        Scalable.createReport(subReporter,
            "scalingApplied",
            String.format("Successfully scaled on loads using mode %s with a variation value asked of %s. Variation done is %s",
                scalingParameters.getDistributionMode(), variationAsked, variationDone),
            TypedValue.INFO_SEVERITY);
        return variationDone;
    }

    /**
     * Computes and applies a scaling variation of power on a list of generators, using variation parameters defined by the user.
     * Depending on the distribution mode chosen, the distribution percentage for each generator will be computed differently:
     * <ul>
     *     <li>PROPORTIONAL_TO_TARGETP: targetP divided by the sum of all the targetP</li>
     *     <li>PROPORTIONAL_TO_PMAX: Pmax divided by the sum of all the Pmax</li>
     *     <li>PROPORTIONAL_TO_DIFF_PMAX_TARGETP: available power (Pmax - targetP) divided by the sum of all the available power</li>
     *     <li>PROPORTIONAL_TO_DIFF_TARGETP_PMIN: used power (targetP - Pmin) divided by the sum of all the used power</li>
     *     <li>REGULAR_DISTRIBUTION: 100% divided by the number of generators</li>
     *     <li>STACKING_UP: generators are fully powered one after the other until the global power asked is reached</li>
     * </ul>
     * If the global sum computed for the chosen distribution mode is at zero, the system will default to the REGULAR_DISTRIBUTION mode.
     * @param network The network on which the scaling variation is applied
     * @param subReporter The reporter
     * @param scalingParameters The parameters for the scaling
     * @param generators The generators on which the scaling will be done
     * @return the value of the power that was finally allocated on the generators
     */
    static double scaleOnGenerators(Network network,
                                           Reporter subReporter,
                                           ScalingParameters scalingParameters,
                                           Collection<Generator> generators) {
        // Check that scalingParameters is coherent with the type of elements given
        if (scalingParameters.getScalingConvention() != ScalingConvention.GENERATOR) {
            throw new PowsyblException(String.format("Scaling convention in the parameters cannot be %s for generators", scalingParameters.getScalingConvention()));
        }

        // Initialisation of the ProportionalScalable
        Scalable scalable;

        // Global current power
        AtomicReference<Double> sumTargetP = new AtomicReference<>(0D);

        // The variation mode chosen changes how the percentages are computed
        switch (scalingParameters.getDistributionMode()) {
            case PROPORTIONAL_TO_TARGETP -> {
                // Proportional to the target power of each generator
                scalable = new ProportionalScalable();
                generators.forEach(generator ->
                    sumTargetP.set(sumTargetP.get() + generator.getTargetP())
                );
                if (sumTargetP.get() > 0.0) {
                    generators.forEach(generator ->
                        ((ProportionalScalable) scalable).getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) (generator.getTargetP() * 100.0 / sumTargetP.get()))));
                } else {
                    // If no power is currently configured, a regular distribution is used
                    generators.forEach(generator ->
                        ((ProportionalScalable) scalable).getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) (100.0 / generators.size())))
                    );
                }
            }
            case PROPORTIONAL_TO_PMAX -> {
                // Proportional to the maximal power of each generator
                scalable = new ProportionalScalable();

                // Global max power
                AtomicReference<Double> sumPMax = new AtomicReference<>(0D);
                generators.forEach(generator -> {
                    sumTargetP.set(sumTargetP.get() + generator.getTargetP());
                    sumPMax.set(sumPMax.get() + generator.getMaxP());
                });
                generators.forEach(generator ->
                    ((ProportionalScalable) scalable).getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) (generator.getMaxP() * 100.0 / sumPMax.get()))));
            }
            case PROPORTIONAL_TO_DIFF_PMAX_TARGETP -> {
                // Proportional to the available power (Pmax - targetP) of each generator
                scalable = new ProportionalScalable();

                // Global available power
                AtomicReference<Double> sumAvailableP = new AtomicReference<>(0D);
                generators.forEach(generator -> {
                    sumTargetP.set(sumTargetP.get() + generator.getTargetP());
                    sumAvailableP.set(sumAvailableP.get() + (generator.getMaxP() - generator.getTargetP()));
                });
                if (sumAvailableP.get() > 0.0) {
                    generators.forEach(generator ->
                        ((ProportionalScalable) scalable).getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) ((generator.getMaxP() - generator.getTargetP()) * 100.0 / sumAvailableP.get()))));
                } else {
                    // If no power is currently available, a regular distribution is used
                    generators.forEach(generator ->
                        ((ProportionalScalable) scalable).getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) (100.0 / generators.size())))
                    );
                }
            }
            case PROPORTIONAL_TO_DIFF_TARGETP_PMIN -> {
                // Proportional to the used power (targetP - Pmin) of each generator
                scalable = new ProportionalScalable();

                // Global used power
                AtomicReference<Double> sumUsedP = new AtomicReference<>(0D);
                generators.forEach(generator -> {
                    sumTargetP.set(sumTargetP.get() + generator.getTargetP());
                    sumUsedP.set(sumUsedP.get() + (generator.getTargetP() - generator.getMinP()));
                });
                if (sumUsedP.get() > 0.0) {
                    generators.forEach(generator ->
                        ((ProportionalScalable) scalable).getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) ((generator.getTargetP() - generator.getMinP()) * 100.0 / sumUsedP.get()))));
                } else {
                    // If no power is currently used, a regular distribution is used
                    generators.forEach(generator ->
                        ((ProportionalScalable) scalable).getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) (100.0 / generators.size())))
                    );
                }
            }
            case REGULAR_DISTRIBUTION -> {
                // Each load get the same
                scalable = new ProportionalScalable();
                generators.forEach(generator -> {
                    sumTargetP.set(sumTargetP.get() + generator.getTargetP());
                    ((ProportionalScalable) scalable).getScalablePercentageList().add(new ProportionalScalable.ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) (100.0 / generators.size())));
                });
            }
            case STACKING_UP ->
                // Fully charges generators one after the other until the power asked is reached
                scalable = stack(generators.stream().map(generator -> {
                    sumTargetP.set(sumTargetP.get() + generator.getTargetP());
                    return Scalable.onGenerator(generator.getId());
                }).toArray(Scalable[]::new));
            default ->
                throw new IllegalArgumentException(String.format("Variation mode cannot be %s for GeneratorScalables", scalingParameters.getDistributionMode()));
        }

        // Variation asked globally
        double variationAsked = getVariationAsked(scalingParameters, sumTargetP);

        // Adapt the asked value if needed - only useful if using a proportional scalable
        if (scalable instanceof ProportionalScalable proportionalScalable) {
            variationAsked = proportionalScalable.resizeAskedForVentilation(network, variationAsked, scalingParameters);
        }

        // Do the repartition
        double variationDone = scalable.scale(
            network,
            variationAsked,
            scalingParameters);

        // Report
        Scalable.createReport(subReporter,
            "scalingApplied",
            String.format("Successfully scaled on generators using mode %s with a variation value asked of %s. Variation done is %s",
                scalingParameters.getDistributionMode(), variationAsked, variationDone),
            TypedValue.INFO_SEVERITY);

        return variationDone;
    }
}
