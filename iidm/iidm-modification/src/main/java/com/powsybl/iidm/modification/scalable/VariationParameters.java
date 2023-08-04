/*
 * Copyright (c) 2023. , All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.modification.scalable;

public class VariationParameters {

    public enum VariationMode {
        PROPORTIONAL_TO_TARGETP,
        PROPORTIONAL_TO_PMAX,
        PROPORTIONAL_TO_DIFF_PMAX_TARGETP,
        PROPORTIONAL_TO_DIFF_TARGETP_PMIN,
        PROPORTIONAL_TO_P0,
        REGULAR_DISTRIBUTION
    }

    private VariationMode variationMode;


    public VariationParameters(VariationMode variationMode) {
        this.variationMode = variationMode;
    }

    public VariationMode getVariationMode() {
        return variationMode;
    }

    public void setVariationMode(VariationMode variationMode) {
        this.variationMode = variationMode;
    }
}
