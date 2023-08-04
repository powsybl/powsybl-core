/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Scalable that divides scale proportionally between multiple scalable.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class ProportionalScalable extends AbstractCompoundScalable {
    private static final double EPSILON = 1e-2;

    private static final class ScalablePercentage {
        private final Scalable scalable;
        private final float percentage;
        private double iterationPercentage;

        private ScalablePercentage(Scalable scalable, float percentage) {
            this.scalable = scalable;
            this.percentage = percentage;
            this.iterationPercentage = percentage;
        }

        Scalable getScalable() {
            return scalable;
        }

        float getPercentage() {
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

    private ProportionalScalable(){
        // Initialisation if the list
        this.scalablePercentageList = new ArrayList<>();
    }

    ProportionalScalable(List<Float> percentages, List<Scalable> scalables) {
        checkPercentages(percentages, scalables);
        this.scalablePercentageList = new ArrayList<>();
        for (int i = 0; i < scalables.size(); i++) {
            this.scalablePercentageList.add(new ScalablePercentage(scalables.get(i), percentages.get(i)));
        }
    }

    public ProportionalScalable onLoads(VariationParameters variationParameters, Collection<Load> loads) {
        // Initialisation of the ProportionalScalable
        ProportionalScalable proportionalScalable = new ProportionalScalable();

        // The variation mode chosen changes how the percentages are computed
        switch (variationParameters.getVariationMode()) {
            case PROPORTIONAL_TO_P0 -> {
                // Proportional to the P0 of the loads
                AtomicReference<Double> sumP0 = new AtomicReference<>(0D);
                loads.forEach(load ->
                    sumP0.set(sumP0.get() + load.getP0())
                );
                loads.forEach(load ->
                    proportionalScalable.scalablePercentageList.add(new ScalablePercentage(Scalable.onLoad(load.getId()), (float) (load.getP0() * 100.0 / sumP0.get()))));
            }
            case REGULAR_DISTRIBUTION ->
                // Each load get the same
                loads.forEach(load ->
                    proportionalScalable.scalablePercentageList.add(new ScalablePercentage(Scalable.onLoad(load.getId()), (float) (100.0 / loads.size()))));
            default ->
                throw new IllegalArgumentException(String.format("Variation mode cannot be %s for LoadScalables", variationParameters.getVariationMode()));
        }
        return proportionalScalable;
    }

    public ProportionalScalable onGenerators(VariationParameters variationParameters, Collection<Generator> generators) {
        // Initialisation of the ProportionalScalable
        ProportionalScalable proportionalScalable = new ProportionalScalable();

        // The variation mode chosen changes how the percentages are computed
        switch (variationParameters.getVariationMode()) {
            case PROPORTIONAL_TO_TARGETP -> {
                // Proportional to the target power of each generator
                AtomicReference<Double> sumP0 = new AtomicReference<>(0D);
                generators.forEach(generator ->
                    sumP0.set(sumP0.get() + generator.getTargetP())
                );
                generators.forEach(generator ->
                    proportionalScalable.scalablePercentageList.add(new ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) (generator.getTargetP() * 100.0 / sumP0.get()))));
            }
            case PROPORTIONAL_TO_PMAX -> {
                // Proportional to the maximal power of each generator
                AtomicReference<Double> sumP0 = new AtomicReference<>(0D);
                generators.forEach(generator ->
                    sumP0.set(sumP0.get() + generator.getMaxP())
                );
                generators.forEach(generator ->
                    proportionalScalable.scalablePercentageList.add(new ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) (generator.getMaxP() * 100.0 / sumP0.get()))));
            }
            case PROPORTIONAL_TO_DIFF_PMAX_TARGETP -> {
                // Proportional to the available power (Pmax - targetP) of each generator
                AtomicReference<Double> sumP0 = new AtomicReference<>(0D);
                generators.forEach(generator ->
                    sumP0.set(sumP0.get() + (generator.getMaxP() - generator.getTargetP()))
                );
                generators.forEach(generator ->
                    proportionalScalable.scalablePercentageList.add(new ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) ((generator.getMaxP() - generator.getTargetP()) * 100.0 / sumP0.get()))));
            }
            case PROPORTIONAL_TO_DIFF_TARGETP_PMIN -> {
                // Proportional to the used power (targetP - Pmin) of each generator
                AtomicReference<Double> sumP0 = new AtomicReference<>(0D);
                generators.forEach(generator ->
                    sumP0.set(sumP0.get() + (generator.getTargetP() - generator.getMinP()))
                );
                generators.forEach(generator ->
                    proportionalScalable.scalablePercentageList.add(new ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) ((generator.getTargetP() - generator.getMinP()) * 100.0 / sumP0.get()))));
            }
            case REGULAR_DISTRIBUTION ->
                // Each load get the same
                generators.forEach(generator ->
                    proportionalScalable.scalablePercentageList.add(new ScalablePercentage(Scalable.onGenerator(generator.getId()), (float) (100.0 / generators.size()))));
            default ->
                throw new IllegalArgumentException(String.format("Variation mode cannot be %s for LoadScalables", variationParameters.getVariationMode()));
        }
        return proportionalScalable;
    }

    Collection<Scalable> getScalables() {
        return scalablePercentageList.stream().map(ScalablePercentage::getScalable).toList();
    }

    private static void checkPercentages(List<Float> percentages, List<Scalable> scalables) {
        Objects.requireNonNull(percentages);
        Objects.requireNonNull(scalables);

        if (scalables.size() != percentages.size()) {
            throw new IllegalArgumentException("percentage and scalable list must have the same size");
        }
        if (scalables.isEmpty()) {
            return;
        }
        if (percentages.stream().anyMatch(p -> Float.isNaN(p))) {
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
            double askedOnScalable = iterationPercentage / 100 * asked;
            double doneOnScalable = 0;
            doneOnScalable = s.scale(n, askedOnScalable, parameters);
            if (Math.abs(doneOnScalable - askedOnScalable) > EPSILON) {
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
        reinitIterationPercentage();
        if (parameters.isIterative()) {
            return iterativeScale(n, asked, parameters);
        } else {
            return scaleIteration(n, asked, parameters);
        }
    }

    private void reinitIterationPercentage() {
        scalablePercentageList.forEach(scalablePercentage -> scalablePercentage.setIterationPercentage(scalablePercentage.getPercentage()));
    }

}

