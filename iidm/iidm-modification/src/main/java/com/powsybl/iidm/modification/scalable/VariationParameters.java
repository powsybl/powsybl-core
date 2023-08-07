/*
 * Copyright (c) 2023. , All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.modification.scalable;

public class VariationParameters {

    public enum DistributionMode {
        PROPORTIONAL_TO_TARGETP,
        PROPORTIONAL_TO_PMAX,
        PROPORTIONAL_TO_DIFF_PMAX_TARGETP,
        PROPORTIONAL_TO_DIFF_TARGETP_PMIN,
        PROPORTIONAL_TO_P0,
        REGULAR_DISTRIBUTION
    }

    public enum VariationType {
        DELTA_P,
        TARGET_P
    }

    public enum ReactiveVariationMode {
        CONSTANT_Q,
        TAN_PHI_FIXED
    }

    private final DistributionMode distributionMode;
    private final VariationType variationType;
    private final Double variationValue;
    private final ReactiveVariationMode reactiveVariationMode;

    public VariationParameters(DistributionMode distributionMode, VariationType variationType, Double variationValue, ReactiveVariationMode reactiveVariationMode) {
        this.distributionMode = distributionMode;
        this.variationType = variationType;
        this.variationValue = variationValue;
        this.reactiveVariationMode = reactiveVariationMode;
    }

    public VariationParameters(DistributionMode distributionMode, VariationType variationType, Double variationValue) {
        this.distributionMode = distributionMode;
        this.variationType = variationType;
        this.variationValue = variationValue;
        this.reactiveVariationMode = null;
    }

    public DistributionMode getDistributionMode() {
        return distributionMode;
    }

    public VariationType getVariationType() {
        return variationType;
    }

    public Double getVariationValue() {
        return variationValue;
    }

    public ReactiveVariationMode getReactiveVariationMode() {
        return reactiveVariationMode;
    }
}
