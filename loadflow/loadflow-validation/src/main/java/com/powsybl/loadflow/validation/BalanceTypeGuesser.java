/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BalanceTypeGuesser {

    private static final String NON_EXISTENT_SLACK_ID = "NonExistentSlackBusId";

    private String maxChangeId = "";
    private double maxChange = 0;
    private double sumMaxP = 0;
    private double sumTargetP = 0;
    private double sumP = 0;
    private double cumSumVarKMax = 0;
    private double cumSumVarKTarget = 0;
    private double cumSumVarKHeadroom = 0;
    private int sumChanged = 0;

    private BalanceType balanceType;
    private String slack;

    private double kMax = 0;
    private double kTarget = 0;
    private double kHeadroom = 0;

    public BalanceTypeGuesser() {
        this.balanceType = BalanceType.NONE;
        this.slack = NON_EXISTENT_SLACK_ID;
    }

    public BalanceTypeGuesser(Network network, double threshold) {
        Objects.requireNonNull(network);
        guess(network, threshold);
    }

    private void guess(Network network, double threshold) {
        network.getGeneratorStream().forEach(generator -> {
            computeSums(generator, threshold);
        });
        if (sumChanged > 0) {
            kMax = (sumP - sumTargetP) / sumMaxP;
            kTarget = (sumP - sumTargetP) / sumTargetP;
            kHeadroom = (sumP - sumTargetP) / (sumMaxP - sumTargetP);
            double varKMax = (cumSumVarKMax / sumMaxP) / (Math.max(1E-12, kMax * kMax) - 1);
            double varKTarget = (cumSumVarKTarget / sumTargetP) / (Math.max(1E-12, kTarget * kTarget) - 1);
            double varKHeadroom = (cumSumVarKHeadroom / (sumMaxP - sumTargetP)) / (Math.max(1E-12, kHeadroom * kHeadroom) - 1);
            this.balanceType = getBalanceType(varKMax, varKTarget, varKHeadroom);
        } else {
            this.balanceType = BalanceType.NONE;
            this.slack = maxChangeId;
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

    private void computeSums(Generator generator, double threshold) {
        double p = -generator.getTerminal().getP();
        double targetP = generator.getTargetP();
        double maxP = generator.getMaxP();
        double minP = generator.getMinP();
        if (Math.abs(p - targetP) > maxChange) {
            maxChangeId = generator.getId();
            maxChange = Math.abs(p - targetP);
        }
        if (ValidationUtils.boundedWithin(Math.max(threshold, minP), maxP - threshold, targetP, 0)
                && ValidationUtils.boundedWithin(Math.max(0, minP), maxP, p, -threshold)
                && maxP >= threshold
                && Math.abs(p - targetP) >= threshold) {
            sumMaxP += maxP;
            sumTargetP += targetP;
            sumP +=  p;
            cumSumVarKMax += Math.pow(p - targetP, 2) / maxP;
            cumSumVarKTarget += Math.pow(p - targetP, 2) / targetP;
            cumSumVarKHeadroom += Math.pow(p - targetP, 2) / (maxP + targetP);
            sumChanged++;
        }
    }

    public BalanceType getBalanceType() {
        return balanceType;
    }

    public String getSlack() {
        return slack;
    }

    public double getKMax() {
        return kMax;
    }

    public double getKTarget() {
        return kTarget;
    }

    public double getKHeadroom() {
        return kHeadroom;
    }

}
