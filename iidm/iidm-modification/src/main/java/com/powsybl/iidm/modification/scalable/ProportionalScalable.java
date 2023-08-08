/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.powsybl.iidm.modification.scalable.ScalingParameters.DistributionMode.STACKING_UP;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.VENTILATION;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.VOLUME;

/**
 * Scalable that divides scale proportionally between multiple scalable.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class ProportionalScalable extends AbstractCompoundScalable {
    private static final double EPSILON = 1e-2;

    static final class ScalablePercentage {
        private final Scalable scalable;
        private final float percentage;
        private double iterationPercentage;

        ScalablePercentage(Scalable scalable, float percentage) {
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

    ProportionalScalable() {
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

    Collection<Scalable> getScalables() {
        return scalablePercentageList.stream().map(ScalablePercentage::getScalable).toList();
    }

    List<ScalablePercentage> getScalablePercentageList() {
        return scalablePercentageList;
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
            double doneOnScalable = s.scale(n, askedOnScalable, parameters);
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
        if (parameters.getPriority() == VOLUME) {
            return iterativeScale(n, asked, parameters);
        } else {
            return scaleIteration(n, asked, parameters);
        }
    }

    private void reinitIterationPercentage() {
        scalablePercentageList.forEach(scalablePercentage -> scalablePercentage.setIterationPercentage(scalablePercentage.getPercentage()));
    }

    /**
     * Compute the power that can be scaled on the network while keeping the ventilation percentages valid.
     * This method is only used if the distribution is not STACKING_UP and if the scaling priority is VENTILATION.
     * @param asked power that shall be scaled on the network
     * @param scalingParameters scaling parameters
     * @param network network on which the scaling shall be done
     * @return the effective power value that can be safely scaled while keeping the ventilation percentages valid
     */
    double resizeAskedForVentilation(Network network, double asked, ScalingParameters scalingParameters) {
        if (scalingParameters.getDistributionMode() != STACKING_UP && scalingParameters.getPriority() == VENTILATION) {
            AtomicReference<Double> resizingPercentage = new AtomicReference<>(1.0);
            scalablePercentageList.forEach(scalablePercentage ->
                resizingPercentage.set(Math.min(((GeneratorScalable) scalablePercentage.getScalable()).availablePowerInPercentageOfAsked(network, asked, scalablePercentage.getPercentage()), resizingPercentage.get()))
            );
            return asked * resizingPercentage.get();
        } else {
            return asked;
        }
    }

}

