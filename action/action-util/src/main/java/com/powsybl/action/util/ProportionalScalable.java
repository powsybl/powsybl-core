/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Scalable that divides scale proportionally between multiple scalable.
 * Scale may be iterative or not.
 * If the iterative mode is activated, the residues due to scalable saturation is divided between the
 * other scalable composing the ProportionalScalable.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class ProportionalScalable extends AbstractCompoundScalable {
    private static final double EPSILON = 1e-2;

    private final class ScalablePercentage {
        private final Scalable scalable;
        private final float percentage;
        private boolean saturated;
        private double iterationPercentage;

        private ScalablePercentage(Scalable scalable, float percentage) {
            this.scalable = scalable;
            this.percentage = percentage;
            this.saturated = false;
            this.iterationPercentage = percentage;
        }

        Scalable getScalable() {
            return scalable;
        }

        float getPercentage() {
            return percentage;
        }

        boolean isSaturated() {
            return saturated;
        }

        boolean notSaturated() {
            return !saturated;
        }

        void setSaturated(boolean saturated) {
            this.saturated = saturated;
        }

        double getIterationPercentage() {
            return iterationPercentage;
        }

        void setIterationPercentage(double iterationPercentage) {
            this.iterationPercentage = iterationPercentage;
        }
    }

    private final List<ScalablePercentage> scalablePercentageList;

    private final boolean iterative;

    ProportionalScalable(List<Float> percentages, List<Scalable> scalables) {
        this(percentages, scalables, false);
    }

    ProportionalScalable(List<Float> percentages, List<Scalable> scalables, boolean iterative) {
        checkPercentages(percentages, scalables);
        this.scalablePercentageList = new ArrayList<>();
        for (int i = 0; i < scalables.size(); i++) {
            this.scalablePercentageList.add(new ScalablePercentage(scalables.get(i), percentages.get(i)));
        }
        this.iterative = iterative;
    }

    Collection<Scalable> getScalables() {
        return scalablePercentageList.stream().map(ScalablePercentage::getScalable).collect(Collectors.toList());
    }

    private static void checkPercentages(List<Float> percentages, List<Scalable> scalables) {
        Objects.requireNonNull(percentages);
        Objects.requireNonNull(scalables);

        if (scalables.size() != percentages.size()) {
            throw new IllegalArgumentException("percentage and scalable list must have the same size");
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
            throw new AssertionError(String.format("Error in proportional scalable ventilation. Sum of percentages must be equals to 100 (%.2f)", iterationPercentagesSum));
        }
    }

    private void updateIterationPercentages() {
        double unsaturatedPercentagesSum = scalablePercentageList.stream().filter(ScalablePercentage::notSaturated).mapToDouble(ScalablePercentage::getIterationPercentage).sum();
        scalablePercentageList.forEach(scalablePercentage -> {
            if (scalablePercentage.isSaturated()) {
                scalablePercentage.setIterationPercentage(0);
            } else {
                scalablePercentage.setIterationPercentage(scalablePercentage.getIterationPercentage() / unsaturatedPercentagesSum * 100);
            }
        });
    }

    private double iterativeScale(Network n, double asked, ScalingConvention scalingConvention, boolean constantPowerFactor) {
        double done = 0;
        while (Math.abs(asked - done) > EPSILON && notSaturated()) {
            checkIterationPercentages();
            done += scaleIteration(n, asked - done, scalingConvention, constantPowerFactor);
            updateIterationPercentages();
        }
        return done;
    }

    private double scaleIteration(Network n, double asked, ScalingConvention scalingConvention, boolean constantPowerFactor) {
        double done = 0;
        for (ScalablePercentage scalablePercentage : scalablePercentageList) {
            Scalable s = scalablePercentage.getScalable();
            double iterationPercentage = scalablePercentage.getIterationPercentage();
            double askedOnScalable = iterationPercentage / 100 * asked;
            double doneOnScalable = 0;
            if (constantPowerFactor && s instanceof LoadScalable) {
                doneOnScalable = s.scaleWithConstantPowerFactor(n, askedOnScalable);
            } else {
                doneOnScalable = s.scale(n, askedOnScalable, scalingConvention);
            }
            if (Math.abs(doneOnScalable - askedOnScalable) > EPSILON) {
                scalablePercentage.setSaturated(true);
            }
            done += doneOnScalable;
        }
        return done;
    }

    @Override
    public double scaleWithConstantPowerFactor(Network n, double asked) {
        return scaleConstantPowerFactor(n, asked, ScalingConvention.GENERATOR);
    }

    public double scaleConstantPowerFactor(Network n, double asked, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);
        reinitIterationPercentage();
        if (iterative) {
            return iterativeScale(n, asked, scalingConvention, true);
        } else {
            return scaleIteration(n, asked, scalingConvention, true);
        }
    }

    @Override
    public double scale(Network n, double asked, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);
        reinitIterationPercentage();
        if (iterative) {
            return iterativeScale(n, asked, scalingConvention, false);
        } else {
            return scaleIteration(n, asked, scalingConvention, false);
        }
    }

    private void reinitIterationPercentage() {
        scalablePercentageList.forEach(scalablePercentage -> {
            scalablePercentage.setSaturated(false);
            scalablePercentage.setIterationPercentage(scalablePercentage.getPercentage());
        });
    }

}

