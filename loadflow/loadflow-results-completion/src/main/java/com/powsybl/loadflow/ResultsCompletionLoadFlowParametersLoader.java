/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import java.util.Objects;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
@AutoService(LoadFlowParameters.ConfigLoader.class)
public class ResultsCompletionLoadFlowParametersLoader implements LoadFlowParameters.ConfigLoader<ResultsCompletionLoadFlowParametersExtension> {

    @Override
    public ResultsCompletionLoadFlowParametersExtension load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        float epsilonX = ResultsCompletionLoadFlowParametersExtension.EPSILON_X_DEFAULT;
        boolean applyReactanceCorrection = ResultsCompletionLoadFlowParametersExtension.APPLY_REACTANCE_CORRECTION_DEFAULT;
        ModuleConfig config = platformConfig.getModuleConfigIfExists("results-completion-loadflow-parameters");
        if (config != null) {
            epsilonX = config.getFloatProperty("epsilon-x", ResultsCompletionLoadFlowParametersExtension.EPSILON_X_DEFAULT);
            applyReactanceCorrection = config.getBooleanProperty("apply-reactance-correction", ResultsCompletionLoadFlowParametersExtension.APPLY_REACTANCE_CORRECTION_DEFAULT);
        }
        return new ResultsCompletionLoadFlowParametersExtension(epsilonX, applyReactanceCorrection);
    }

    @Override
    public String getExtensionName() {
        return "Results Completion LoadFlow Extension";
    }

    @Override
    public String getCategoryName() {
        return "loadflow-parameters";
    }

    @Override
    public Class<? super ResultsCompletionLoadFlowParametersExtension> getExtensionClass() {
        return ResultsCompletionLoadFlowParametersExtension.class;
    }

}
