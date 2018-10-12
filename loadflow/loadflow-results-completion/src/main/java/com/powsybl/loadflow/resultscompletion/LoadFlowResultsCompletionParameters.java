/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LoadFlowResultsCompletionParameters {

    public static final float EPSILON_X_DEFAULT = 0.1f;
    public static final boolean APPLY_REACTANCE_CORRECTION_DEFAULT = false;

    private final float epsilonX;
    private final boolean applyReactanceCorrection;

    public static LoadFlowResultsCompletionParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static LoadFlowResultsCompletionParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        float epsilonX = LoadFlowResultsCompletionParameters.EPSILON_X_DEFAULT;
        boolean applyReactanceCorrection = LoadFlowResultsCompletionParameters.APPLY_REACTANCE_CORRECTION_DEFAULT;
        ModuleConfig config = platformConfig.getModuleConfigIfExists("loadflow-results-completion-parameters");
        if (config != null) {
            epsilonX = config.getFloatProperty("epsilon-x", LoadFlowResultsCompletionParameters.EPSILON_X_DEFAULT);
            applyReactanceCorrection = config.getBooleanProperty("apply-reactance-correction", LoadFlowResultsCompletionParameters.APPLY_REACTANCE_CORRECTION_DEFAULT);
        }
        return new LoadFlowResultsCompletionParameters(epsilonX, applyReactanceCorrection);
    }

    public LoadFlowResultsCompletionParameters(float epsilonX, boolean applyReactanceCorrection) {
        this.epsilonX = epsilonX;
        this.applyReactanceCorrection = applyReactanceCorrection;
    }

    public LoadFlowResultsCompletionParameters() {
        this(EPSILON_X_DEFAULT, APPLY_REACTANCE_CORRECTION_DEFAULT);
    }

    public float getEpsilonX() {
        return epsilonX;
    }

    public boolean isApplyReactanceCorrection() {
        return applyReactanceCorrection;
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
