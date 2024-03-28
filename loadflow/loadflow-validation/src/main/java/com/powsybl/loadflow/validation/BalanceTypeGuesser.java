/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import static java.util.Comparator.comparingDouble;

import java.util.AbstractMap.SimpleEntry;
import java.util.Objects;
import java.util.stream.Stream;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;

/**
 *
 * On creation, tries to guess the balancing method used by the computation, from the differences between
 * the computed power outputs and initial target power.
 * This is necessary since the balancing method actually used is not known.
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class BalanceTypeGuesser {

    private String slackCandidate;
    private double maxActivePowerDifference = 0;

    /**
     * Compute balancing ratio assuming balancing proportional to P max.
     */
    private final KComputation kMaxComputation = new KComputation();

    /**
     * Compute balancing ratio assuming balancing proportional to target P.
     */
    private final KComputation kTargetComputation = new KComputation();

    /**
     * Compute balancing ratio assuming balancing proportional to headroom.
     */
    private final KComputation kHeadroomComputation = new KComputation();

    private BalanceType balanceType;
    private String slack;

    public BalanceTypeGuesser() {
        this.balanceType = BalanceType.NONE;
    }

    /**
     * Tries to guess the balancing method for the provided network.
     *
     * @param threshold: Under this threshold, a power deviation will not be seen as significant (not taken into account in the ratio computation).
     */
    public BalanceTypeGuesser(Network network, double threshold) {
        Objects.requireNonNull(network);
        guess(network, threshold);
    }

    private void guess(Network network, double threshold) {
        // Number of generators that significantly moved
        int movedGeneratorsCount = network.getGeneratorStream().mapToInt(generator -> isMovedGenerator(generator, threshold)).sum();
        if (movedGeneratorsCount > 0) {
            this.balanceType = getBalanceType(kMaxComputation.getVarK(), kTargetComputation.getVarK(), kHeadroomComputation.getVarK());
        } else {
            this.balanceType = BalanceType.NONE;
            this.slack = slackCandidate;
        }
    }

    private BalanceType getBalanceType(double varKMax, double varKTarget, double varKHeadroom) {
        return Stream.of(new SimpleEntry<>(BalanceType.PROPORTIONAL_TO_GENERATION_P_MAX, varKMax),
                         new SimpleEntry<>(BalanceType.PROPORTIONAL_TO_GENERATION_P, varKTarget),
                         new SimpleEntry<>(BalanceType.PROPORTIONAL_TO_GENERATION_HEADROOM, varKHeadroom))
                     .min(comparingDouble(SimpleEntry::getValue))
                     .map(SimpleEntry::getKey)
                     .orElse(BalanceType.NONE);
    }

    private int isMovedGenerator(Generator generator, double threshold) {
        double p = -generator.getTerminal().getP();
        double targetP = generator.getTargetP();
        double maxP = generator.getMaxP();
        double minP = generator.getMinP();
        if (Math.abs(p - targetP) > maxActivePowerDifference) {
            slackCandidate = generator.getId();
            maxActivePowerDifference = Math.abs(p - targetP);
        }
        if (ValidationUtils.boundedWithin(Math.max(threshold, minP), maxP - threshold, targetP, 0)
                && ValidationUtils.boundedWithin(Math.max(0, minP), maxP, p, -threshold)
                && maxP >= threshold
                && Math.abs(p - targetP) >= threshold) {
            kMaxComputation.addGeneratorValues(p, targetP, maxP);
            kTargetComputation.addGeneratorValues(p, targetP, targetP);
            kHeadroomComputation.addGeneratorValues(p, targetP, maxP - targetP);
            return 1;
        }
        return 0;
    }

    /**
     * The balance type identified as most likely (leading to the smallest variance around theoretical values).
     */
    public BalanceType getBalanceType() {
        return balanceType;
    }

    /**
     * The identified slack generator, if any.
     */
    public String getSlack() {
        return slack;
    }

    /**
     * The balancing ratio value, assuming balancing proportional to P max.
     */
    public double getKMax() {
        return kMaxComputation.getK();
    }

    /**
     * The balancing ratio value, assuming balancing proportional to target P.
     */
    public double getKTarget() {
        return kTargetComputation.getK();
    }

    /**
     * The balancing ratio value, assuming balancing proportional to P headroom.
     */
    public double getKHeadroom() {
        return kHeadroomComputation.getK();
    }

}
