/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.security.dynamic.json.DynamicSecurityDummyExtension;
import com.powsybl.security.results.PreContingencyResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
@AutoService(DynamicSecurityAnalysisProvider.class)
public class DynamicSecurityAnalysisProviderMock implements DynamicSecurityAnalysisProvider {

    @Override
    public CompletableFuture<SecurityAnalysisReport> run(Network network, String workingVariantId, DynamicModelsSupplier dynamicModelsSupplier, ContingenciesProvider contingenciesProvider, DynamicSecurityAnalysisRunParameters runParameters) {
        CompletableFuture<SecurityAnalysisReport> cfSar = mock(CompletableFuture.class);
        SecurityAnalysisReport report = mock(SecurityAnalysisReport.class);
        when(report.getResult()).thenReturn(mock(SecurityAnalysisResult.class));
        when(report.getResult().getPreContingencyResult()).thenReturn(mock(PreContingencyResult.class));
        when(report.getResult().getPreContingencyLimitViolationsResult()).thenReturn(mock(LimitViolationsResult.class));
        when(report.getLogBytes()).thenReturn(Optional.of("Hello world".getBytes()));
        when(cfSar.join()).thenReturn(report);
        try {
            when(cfSar.get()).thenReturn(report);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return cfSar;
    }

    @Override
    public String getName() {
        return "DynamicSecurityAnalysisToolProviderMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.of(new DummySerializer());
    }

    @Override
    public Optional<Extension<DynamicSecurityAnalysisParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.of(new DynamicSecurityDummyExtension());
    }

    @Override
    public List<String> getSpecificParametersNames() {
        return Collections.singletonList("dummy-extension");
    }
}
