/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion;

import com.powsybl.commons.config.PlatformConfig;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class LoadFlowResultsCompletionParameters {

    public static final float EPSILON_X_DEFAULT = 0.1f;
    public static final boolean APPLY_REACTANCE_CORRECTION_DEFAULT = false;
    public static final double Z0_THRESHOLD_DIFF_VOLTAGE_ANGLE = 1e-6;

    private final float epsilonX;
    private final boolean applyReactanceCorrection;
    private final double z0ThresholdDiffVoltageAngle;

    public static LoadFlowResultsCompletionParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static LoadFlowResultsCompletionParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        return platformConfig.getOptionalModuleConfig("loadflow-results-completion-parameters")
                .map(config -> {
                    float epsilonX = config.getFloatProperty("epsilon-x", LoadFlowResultsCompletionParameters.EPSILON_X_DEFAULT);
                    boolean applyReactanceCorrection = config.getBooleanProperty("apply-reactance-correction", LoadFlowResultsCompletionParameters.APPLY_REACTANCE_CORRECTION_DEFAULT);
                    double z0ThresholdDiffVoltageAngle = config.getDoubleProperty("z0-threshold-diff-voltage-angle", LoadFlowResultsCompletionParameters.Z0_THRESHOLD_DIFF_VOLTAGE_ANGLE);
                    return new LoadFlowResultsCompletionParameters(epsilonX, applyReactanceCorrection, z0ThresholdDiffVoltageAngle);
                })
                .orElseGet(() -> new LoadFlowResultsCompletionParameters(LoadFlowResultsCompletionParameters.EPSILON_X_DEFAULT, LoadFlowResultsCompletionParameters.APPLY_REACTANCE_CORRECTION_DEFAULT, LoadFlowResultsCompletionParameters.Z0_THRESHOLD_DIFF_VOLTAGE_ANGLE));
    }

    public LoadFlowResultsCompletionParameters(float epsilonX, boolean applyReactanceCorrection, double z0ThresholdDiffVoltageAngle) {
        this.epsilonX = epsilonX;
        this.applyReactanceCorrection = applyReactanceCorrection;
        this.z0ThresholdDiffVoltageAngle = z0ThresholdDiffVoltageAngle;
    }

    public LoadFlowResultsCompletionParameters() {
        this(EPSILON_X_DEFAULT, APPLY_REACTANCE_CORRECTION_DEFAULT, Z0_THRESHOLD_DIFF_VOLTAGE_ANGLE);
    }

    public float getEpsilonX() {
        return epsilonX;
    }

    public boolean isApplyReactanceCorrection() {
        return applyReactanceCorrection;
    }

    public double getZ0ThresholdDiffVoltageAngle() {
        return z0ThresholdDiffVoltageAngle;
    }

    protected Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("epsilonX", epsilonX);
        map.put("applyReactanceCorrection", applyReactanceCorrection);
        map.put("z0ThresholdDiffVoltageAngle", z0ThresholdDiffVoltageAngle);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        return toMap().toString();
    }

}
