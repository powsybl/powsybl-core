/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.RESPECT_OF_DISTRIBUTION;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.RESPECT_OF_VOLUME_ASKED;

/**
 * Scalable that divides scale proportionally between multiple scalable.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class ProportionalScalable extends AbstractCompoundScalable {
    private static final double EPSILON = 1e-2;
    private static final String GENERIC_SCALABLE_CLASS_ERROR = "Unable to create a scalable from %s";
    private static final String GENERIC_INCONSISTENCY_ERROR = "Variable %s inconsistent with injection type %s";

    public enum DistributionMode {
        PROPORTIONAL_TO_TARGETP,
        PROPORTIONAL_TO_PMAX,
        PROPORTIONAL_TO_DIFF_PMAX_TARGETP,
        PROPORTIONAL_TO_DIFF_TARGETP_PMIN,
        PROPORTIONAL_TO_P0,
        UNIFORM_DISTRIBUTION
    }

    static final class ScalablePercentage {
        private final Scalable scalable;
        private final double percentage;
        private double iterationPercentage;

        ScalablePercentage(Scalable scalable, double percentage) {
            this.scalable = scalable;
            this.percentage = percentage;
            this.iterationPercentage = percentage;
        }

        Scalable getScalable() {
            return scalable;
        }

        double getPercentage() {
            return percentage;
        }

        boolean isSaturated() {
            return iterationPercentage == 0.0;
        }

        boolean notSaturated() {
            return iterationPercentage != 0.0;
        }

        double getIterationPercentage() {
            return iterationPercentage;
        }

        void setIterationPercentage(double iterationPercentage) {
            this.iterationPercentage = iterationPercentage;
        }
    }

    private final List<ScalablePercentage> scalablePercentageList;

    ProportionalScalable(List<Double> percentages, List<Scalable> scalables) {
        this(percentages, scalables, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    ProportionalScalable(List<Double> percentages, List<Scalable> scalables, double minValue, double maxValue) {
        checkPercentages(percentages, scalables);
        this.scalablePercentageList = new ArrayList<>();
        for (int i = 0; i < scalables.size(); i++) {
            this.scalablePercentageList.add(new ScalablePercentage(scalables.get(i), percentages.get(i)));
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public ProportionalScalable(List<? extends Injection> injections, DistributionMode distributionMode) {
        this(injections, distributionMode, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public ProportionalScalable(List<? extends Injection> injections, DistributionMode distributionMode, double minValue, double maxValue) {
        // Create the scalable for each injection
        List<Scalable> injectionScalables = injections.stream().map(ScalableAdapter::new).collect(Collectors.toList());

        // Compute the sum of every individual power
        double totalDistribution = computeTotalDistribution(injections, distributionMode);

        // In some cases, a regular distribution is equivalent to the nominal distribution :
        // - PROPORTIONAL_TO_P0 : if no power is currently configured
        // - PROPORTIONAL_TO_TARGETP : if no power is currently configured
        // - PROPORTIONAL_TO_DIFF_PMAX_TARGETP : if no power is currently available
        // - PROPORTIONAL_TO_DIFF_TARGETP_PMIN : if no power is currently used
        DistributionMode finalDistributionMode;
        double finalTotalDistribution;
        if (totalDistribution == 0.0 &&
            (distributionMode == DistributionMode.PROPORTIONAL_TO_P0
                || distributionMode == DistributionMode.PROPORTIONAL_TO_TARGETP
                || distributionMode == DistributionMode.PROPORTIONAL_TO_DIFF_PMAX_TARGETP
                || distributionMode == DistributionMode.PROPORTIONAL_TO_DIFF_TARGETP_PMIN)) {
            finalDistributionMode = DistributionMode.UNIFORM_DISTRIBUTION;
            finalTotalDistribution = computeTotalDistribution(injections, finalDistributionMode);
        } else {
            finalDistributionMode = distributionMode;
            finalTotalDistribution = totalDistribution;
        }

        // Compute the percentages for each injection
        List<Double> percentages = injections.stream().map(injection -> getIndividualDistribution(injection, finalDistributionMode) * 100.0 / finalTotalDistribution).toList();
        checkPercentages(percentages, injectionScalables);

        // Create the list of ScalablePercentage
        this.scalablePercentageList = new ArrayList<>();
        for (int i = 0; i < injectionScalables.size(); i++) {
            this.scalablePercentageList.add(new ScalablePercentage(injectionScalables.get(i), percentages.get(i)));
        }

        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    private double computeTotalDistribution(List<? extends Injection> injections, DistributionMode distributionMode) {
        return injections.stream().mapToDouble(injection -> getIndividualDistribution(injection, distributionMode)).sum();
    }

    private double getIndividualDistribution(Injection<?> injection, DistributionMode distributionMode) {
        // Check the injection type
        checkInjectionClass(injection);

        // Get the injection value according to the distribution mode
        return switch (distributionMode) {
            case PROPORTIONAL_TO_TARGETP -> getTargetP(injection);
            case PROPORTIONAL_TO_P0 -> getP0(injection);
            case PROPORTIONAL_TO_PMAX -> getMaxP(injection);
            case PROPORTIONAL_TO_DIFF_PMAX_TARGETP -> getMaxP(injection) - getTargetP(injection);
            case PROPORTIONAL_TO_DIFF_TARGETP_PMIN -> getTargetP(injection) - getMinP(injection);
            case UNIFORM_DISTRIBUTION -> 1;
        };
    }

    private void checkInjectionClass(Injection<?> injection) {
        if (!(injection instanceof Generator
            || injection instanceof Load
            || injection instanceof DanglingLine)) {
            throw new PowsyblException(String.format(GENERIC_SCALABLE_CLASS_ERROR, injection.getClass()));
        }
    }

    private double getTargetP(Injection<?> injection) {
        if (injection instanceof Generator generator) {
            return generator.getTargetP();
        } else {
            throw new PowsyblException(String.format(GENERIC_INCONSISTENCY_ERROR,
                "TargetP", injection.getClass()));
        }
    }

    private double getP0(Injection<?> injection) {
        if (injection instanceof Load load) {
            return load.getP0();
        } else if (injection instanceof DanglingLine danglingLine) {
            return danglingLine.getP0();
        } else {
            throw new PowsyblException(String.format(GENERIC_INCONSISTENCY_ERROR,
                "P0", injection.getClass()));
        }
    }

    private double getMaxP(Injection<?> injection) {
        if (injection instanceof Generator generator) {
            return generator.getMaxP();
        } else {
            throw new PowsyblException(String.format(GENERIC_INCONSISTENCY_ERROR,
                "MaxP", injection.getClass()));
        }
    }

    private double getMinP(Injection<?> injection) {
        if (injection instanceof Generator generator) {
            return generator.getMinP();
        } else {
            throw new PowsyblException(String.format(GENERIC_INCONSISTENCY_ERROR,
                "MinP", injection.getClass()));
        }
    }

    Collection<Scalable> getScalables() {
        return scalablePercentageList.stream().map(ScalablePercentage::getScalable).toList();
    }

    private static void checkPercentages(List<Double> percentages, List<Scalable> scalables) {
        Objects.requireNonNull(percentages);
        Objects.requireNonNull(scalables);

        if (scalables.size() != percentages.size()) {
            throw new IllegalArgumentException("percentage and scalable list must have the same size");
        }
        if (scalables.isEmpty()) {
            return;
        }
        if (percentages.stream().anyMatch(p -> Double.isNaN(p))) {
            throw new IllegalArgumentException("There is at least one undefined percentage");
        }
        double sum = percentages.stream().mapToDouble(Double::valueOf).sum();
        if (Math.abs(100 - sum) > EPSILON) {
            throw new IllegalArgumentException(String.format("Sum of percentages must be equals to 100 (%.2f)", sum));
        }
    }

    private boolean notSaturated() {
        return scalablePercentageList.stream().anyMatch(ScalablePercentage::notSaturated);
    }

    private void checkIterationPercentages() {
        double iterationPercentagesSum = scalablePercentageList.stream().mapToDouble(ScalablePercentage::getIterationPercentage).sum();
        if (Math.abs(100 - iterationPercentagesSum) > EPSILON) {
            throw new IllegalStateException(String.format("Error in proportional scalable ventilation. Sum of percentages must be equals to 100 (%.2f)", iterationPercentagesSum));
        }
    }

    private void updateIterationPercentages() {
        double unsaturatedPercentagesSum = scalablePercentageList.stream().filter(ScalablePercentage::notSaturated).mapToDouble(ScalablePercentage::getIterationPercentage).sum();
        scalablePercentageList.forEach(scalablePercentage -> {
            if (!scalablePercentage.isSaturated()) {
                scalablePercentage.setIterationPercentage(scalablePercentage.getIterationPercentage() / unsaturatedPercentagesSum * 100);
            }
        });
    }

    private double iterativeScale(Network n, double asked, ScalingParameters parameters) {
        double done = 0;
        while (Math.abs(asked - done) > EPSILON && notSaturated()) {
            checkIterationPercentages();
            done += scaleIteration(n, asked - done, parameters);
            updateIterationPercentages();
        }
        return done;
    }

    private double scaleIteration(Network n, double asked, ScalingParameters parameters) {
        double done = 0;
        for (ScalablePercentage scalablePercentage : scalablePercentageList) {
            Scalable s = scalablePercentage.getScalable();
            double iterationPercentage = scalablePercentage.getIterationPercentage();
            if (iterationPercentage == 0.0) {
                // no need to go further
                continue;
            }
            double askedOnScalable = iterationPercentage / 100 * asked;
            double doneOnScalable = s.scale(n, askedOnScalable, parameters);

            // check if scalable reached limit
            double scalableMin = s.minimumValue(n, parameters.getScalingConvention());
            double scalableMax = s.maximumValue(n, parameters.getScalingConvention());
            double scalableP = s.getSteadyStatePower(n, asked, parameters.getScalingConvention());
            boolean saturated = asked > 0 ?
                    Math.abs(scalableMax - scalableP) < EPSILON / 100 :
                    Math.abs(scalableMin - scalableP) < EPSILON / 100;
            if (doneOnScalable == 0 || saturated) {
                // If didn't move now (tested by a perfect zero equality), it won't move in subsequent iterations for sure.
                // If reached saturation, can exclude right now to avoid earlier another iteration.
                scalablePercentage.setIterationPercentage(0);
            }
            done += doneOnScalable;
        }
        return done;
    }

    @Override
    public double scale(Network n, double asked, ScalingParameters parameters) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(parameters);

        // Compute the current power value
        double currentGlobalPower = getSteadyStatePower(n, asked, parameters.getScalingConvention());

        // Variation asked
        double variationAsked = Scalable.getVariationAsked(parameters, asked, currentGlobalPower);

        double boundedVariation = getBoundedVariation(variationAsked, currentGlobalPower, parameters.getScalingConvention());

        // Adapt the asked value if needed - only used in RESPECT_OF_DISTRIBUTION mode
        if (parameters.getPriority() == RESPECT_OF_DISTRIBUTION) {
            boundedVariation = resizeAskedForFixedDistribution(n, boundedVariation, parameters);
        }

        reinitIterationPercentage();
        if (parameters.getPriority() == RESPECT_OF_VOLUME_ASKED) {
            return iterativeScale(n, boundedVariation, parameters);
        } else {
            return scaleIteration(n, boundedVariation, parameters);
        }
    }

    private void reinitIterationPercentage() {
        scalablePercentageList.forEach(scalablePercentage -> scalablePercentage.setIterationPercentage(scalablePercentage.getPercentage()));
    }

    /**
     * Compute the power that can be scaled on the network while keeping the distribution percentages valid.
     * This method is only used on generators, using a GeneratorScalable or a ScalableAdapter, and when the
     * scaling priority is RESPECT_OF_DISTRIBUTION.
     * @param asked power that shall be scaled on the network
     * @param network network on which the scaling shall be done
     * @return the effective power value that can be safely scaled while keeping the distribution percentages valid
     */
    double resizeAskedForFixedDistribution(Network network, double asked, ScalingParameters scalingParameters) {
        AtomicReference<Double> resizingPercentage = new AtomicReference<>(1.0);
        scalablePercentageList.forEach(scalablePercentage -> {
            if (scalablePercentage.getScalable() instanceof GeneratorScalable generatorScalable) {
                resizingPercentage.set(Math.min(
                    generatorScalable.availablePowerInPercentageOfAsked(network, asked, scalablePercentage.getPercentage(), scalingParameters.getScalingConvention()),
                    resizingPercentage.get()));
            } else if (scalablePercentage.getScalable() instanceof ScalableAdapter scalableAdapter) {
                resizingPercentage.set(Math.min(
                    scalableAdapter.availablePowerInPercentageOfAsked(network, asked, scalablePercentage.getPercentage(), scalingParameters.getScalingConvention()),
                    resizingPercentage.get()));
            } else {
                throw new PowsyblException(String.format("RESPECT_OF_DISTRIBUTION mode can only be used with ScalableAdapter or GeneratorScalable, not %s",
                    scalablePercentage.getScalable().getClass()));
            }
        });
        return asked * resizingPercentage.get();
    }

    @Override
    public double getSteadyStatePower(Network network, double asked, ScalingConvention scalingConvention) {
        return scalablePercentageList.stream().mapToDouble(scalablePercentage -> scalablePercentage.getScalable().getSteadyStatePower(network, asked, scalingConvention)).sum();
    }

}

