/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.databind.Module;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.contingency.json.ContingencyJsonModule;
import com.powsybl.security.*;
import com.powsybl.security.results.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractSecurityAnalysisJsonModule extends ContingencyJsonModule {

    private final transient List<SecurityAnalysisJsonPlugin> plugins;

    protected static List<SecurityAnalysisJsonPlugin> getServices() {
        return new ServiceLoaderCache<>(SecurityAnalysisJsonPlugin.class).getServices();
    }

    protected AbstractSecurityAnalysisJsonModule(Collection<SecurityAnalysisJsonPlugin> plugins) {
        Objects.requireNonNull(plugins);
        this.plugins = List.copyOf(plugins);
        addSerializer(SecurityAnalysisResult.class, new SecurityAnalysisResultSerializer());
        addSerializer(NetworkMetadata.class, new NetworkMetadataSerializer());
        addSerializer(PostContingencyResult.class, new PostContingencyResultSerializer());
        addSerializer(LimitViolationsResult.class, new LimitViolationsResultSerializer());
        addSerializer(PreContingencyResult.class, new PreContingencyResultSerializer());
        addSerializer(BusResult.class, new BusResultSerializer());
        addSerializer(BranchResult.class, new BranchResultSerializer());
        addSerializer(ThreeWindingsTransformerResult.class, new ThreeWindingsTransformerResultSerializer());
        addSerializer(OperatorStrategyResult.class, new OperatorStrategyResultSerializer());
        addSerializer(OperatorStrategyResult.ConditionalActionsResult.class, new ConditionalActionsResultSerializer());
        addSerializer(ConnectivityResult.class, new ConnectivityResultSerializer());

        addDeserializer(SecurityAnalysisResult.class, new SecurityAnalysisResultDeserializer());
        addDeserializer(NetworkMetadata.class, new NetworkMetadataDeserializer());
        addDeserializer(PostContingencyResult.class, new PostContingencyResultDeserializer());
        addDeserializer(LimitViolationsResult.class, new LimitViolationResultDeserializer());
        addDeserializer(PreContingencyResult.class, new PreContingencyResultDeserializer());
        addDeserializer(BusResult.class, new BusResultDeserializer());
        addDeserializer(BranchResult.class, new BranchResultDeserializer());
        addDeserializer(ThreeWindingsTransformerResult.class, new ThreeWindingsTransformerResultDeserializer());
        addDeserializer(SecurityAnalysisParameters.class, new SecurityAnalysisParametersDeserializer());
        addDeserializer(OperatorStrategyResult.class, new OperatorStrategyResultDeserializer());
        addDeserializer(OperatorStrategyResult.ConditionalActionsResult.class, new ConditionalActionsResultDeserializer());
        addDeserializer(NetworkResult.class, new NetworkResultDeserializer());
        addDeserializer(ConnectivityResult.class, new ConnectivityResultDeserializer());
    }

    @Override
    public Iterable<? extends Module> getDependencies() {
        return () -> plugins.stream()
                .flatMap(plugin -> plugin.getJsonModules().stream())
                .iterator();
    }
}
