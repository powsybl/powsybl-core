/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.ImportPostProcessor;
import com.powsybl.iidm.network.Network;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
@AutoService(ImportPostProcessor.class)
public class ResultsCompletionLoadFlowPostProcessor implements ImportPostProcessor {

    public static final String NAME = "resultsCompletionLf";

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultsCompletionLoadFlowPostProcessor.class);

    private final LoadFlowParameters parameters;

    public ResultsCompletionLoadFlowPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public ResultsCompletionLoadFlowPostProcessor(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        parameters = LoadFlowParameters.load(platformConfig);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, ComputationManager computationManager) throws Exception {
        Objects.requireNonNull(network);
        LOGGER.info("Execute {} post processor on network {}", getName(), network.getId());
        new ResultsCompletionLoadFlow(network).run(parameters);
    }

}
