/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.ImportPostProcessor;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ImportPostProcessor.class)
public class ReplaceTieLinesByLinesPostProcessor implements ImportPostProcessor {

    public static final String THROW_EXCEPTION = "iidm.import.post-processor.replace-tie-lines-by-lines.throw-exception";

    private static final Parameter THROW_EXCEPTION_PARAMETER
            = new Parameter(THROW_EXCEPTION, ParameterType.BOOLEAN, "Throw exception if issue while replacing tie lines by lines", Boolean.TRUE);

    private final ParameterDefaultValueConfig defaultValueConfig;

    public ReplaceTieLinesByLinesPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public ReplaceTieLinesByLinesPostProcessor(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    @Override
    public String getName() {
        return "replaceTieLinesByLines";
    }

    @Override
    public void process(Network network, ComputationManager computationManager, ReportNode reportNode) {
        new ReplaceTieLinesByLines().apply(network, Parameter.readBoolean("XIIDM", null, THROW_EXCEPTION_PARAMETER, defaultValueConfig), computationManager, false, reportNode);
    }
}
