/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.ImportPostProcessor;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@AutoService(ImportPostProcessor.class)
public class ReplaceIdsByNamesPostProcessor implements ImportPostProcessor {

    public static final String NAME = "replaceIdsByNames";

    private final PlatformConfig platformConfig;

    public ReplaceIdsByNamesPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public ReplaceIdsByNamesPostProcessor(PlatformConfig platformConfig) {
        this.platformConfig = Objects.requireNonNull(platformConfig);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, ComputationManager computationManager, ReportNode reportNode) {
        new ReplaceIdsByNames(platformConfig).apply(network, false, computationManager, reportNode);
    }
}
