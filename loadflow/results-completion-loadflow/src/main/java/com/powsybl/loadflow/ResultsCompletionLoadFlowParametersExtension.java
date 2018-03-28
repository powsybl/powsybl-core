/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.extensions.AbstractExtension;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ResultsCompletionLoadFlowParametersExtension extends AbstractExtension<LoadFlowParameters> {

    public static final float EPSILON_X_DEFAULT = 0.1f;
    public static final boolean APPLY_REACTANCE_CORRECTION_DEFAULT = false;

    private final float epsilonX;
    private final boolean applyReactanceCorrection;

    public ResultsCompletionLoadFlowParametersExtension(float epsilonX, boolean applyReactanceCorrection) {
        this.epsilonX = epsilonX;
        this.applyReactanceCorrection = applyReactanceCorrection;
    }

    public ResultsCompletionLoadFlowParametersExtension() {
        this(EPSILON_X_DEFAULT, APPLY_REACTANCE_CORRECTION_DEFAULT);
    }

    public float getEpsilonX() {
        return epsilonX;
    }

    public boolean isApplyReactanceCorrection() {
        return applyReactanceCorrection;
    }

    @Override
    public String getName() {
        return "Results Completion LoadFlow Extension";
    }

    protected Map<String, Object> toMap() {
        return ImmutableMap.of("epsilonX", epsilonX,
                               "applyReactanceCorrection", applyReactanceCorrection);
    }

    @Override
    public String toString() {
        return toMap().toString();
    }

}
