/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs.local;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.AppLogger;
import com.powsybl.afs.ext.base.ProjectCase;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.EmptyContingencyListProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.afs.SecurityAnalysisRunner;
import com.powsybl.security.afs.SecurityAnalysisRunningService;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalSecurityAnalysisRunningService implements SecurityAnalysisRunningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalSecurityAnalysisRunningService.class);

    private final String securityAnalysisName;

    public LocalSecurityAnalysisRunningService(String securityAnalysisName) {
        this.securityAnalysisName = securityAnalysisName;
    }

    @Override
    public void run(SecurityAnalysisRunner runner) {
        Objects.requireNonNull(runner);

        ProjectCase aCase = (ProjectCase) runner.getCase().orElseThrow(() -> new AfsException("Invalid case link"));
        ContingenciesProvider contingencyListProvider = runner.getContingencyStore()
                .map(store -> (ContingenciesProvider) store)
                .orElse(new EmptyContingencyListProvider());
        SecurityAnalysisParameters parameters = runner.readParameters();
        ComputationManager computationManager = runner.getFileSystem().getData().getLongTimeExecutionComputationManager();

        UUID taskId = runner.startTask();
        try {
            AppLogger logger = runner.createLogger(taskId);

            logger.log("Loading network...");
            Network network = aCase.getNetwork();

            // add all interceptors
            List<SecurityAnalysisInterceptor> interceptors = new ArrayList<>();
            for (String interceptorName : SecurityAnalysisInterceptors.getExtensionNames()) {
                interceptors.add(SecurityAnalysisInterceptors.createInterceptor(interceptorName));
            }

            logger.log("Running security analysis...");
            SecurityAnalysis.find(securityAnalysisName)
                    .with(network.getVariantManager().getWorkingVariantId())
                    .with(interceptors)
                    .run(network, computationManager, parameters, contingencyListProvider)
                    .handleAsync((result, throwable) -> {
                        if (throwable == null) {
                            logger.log("Security analysis complete, storing results...");
                            runner.writeResult(result);
                        } else {
                            logger.log("Security analysis failed");
                            LOGGER.error(throwable.toString(), throwable);
                        }
                        return null;
                    }).join();
        } finally {
            runner.stopTask(taskId);
        }
    }
}
