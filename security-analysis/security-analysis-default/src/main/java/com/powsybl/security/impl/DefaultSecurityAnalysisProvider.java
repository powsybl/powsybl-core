/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.impl;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;

import java.util.concurrent.CompletableFuture;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
@AutoService(SecurityAnalysisProvider.class)
public class DefaultSecurityAnalysisProvider implements SecurityAnalysisProvider {

    private static final String PROVIDER_NAME = "DefaultSecurityAnalysis";
    private static final String PROVIDER_VERSION = "1.0";

    @Override
    public CompletableFuture<SecurityAnalysisReport> run(Network network, String workingVariantId, ContingenciesProvider contingenciesProvider, SecurityAnalysisRunParameters runParameters, ReportNode reportNode) {
        DefaultSecurityAnalysis securityAnalysis = new DefaultSecurityAnalysis(network, runParameters.getDetector(),
                runParameters.getFilter(), runParameters.getComputationManager(), runParameters.getMonitors(), reportNode);
        runParameters.getInterceptors().forEach(securityAnalysis::addInterceptor);
        return securityAnalysis.run(workingVariantId, runParameters.getSecurityAnalysisParameters(), contingenciesProvider);
    }

    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getVersion() {
        return PROVIDER_VERSION;
    }
}
