package com.powsybl.security.json;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.powsybl.security.json.JsonSecurityAnalysisParametersTest.*;

@AutoService(SecurityAnalysisProvider.class)
public class SecurityAnalysisProviderMock implements SecurityAnalysisProvider {

    @Override
    public CompletableFuture<SecurityAnalysisReport> run(Network network, String workingVariantId, ContingenciesProvider contingenciesProvider, SecurityAnalysisRunParameters runParameters, ReportNode reportNode) {
        return null;
    }

    @Override
    public Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.of(new DummySerializer());
    }

    @Override
    public Optional<Extension<SecurityAnalysisParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.of(new DummyExtension());
    }

    @Override
    public String getName() {
        return "SecurityAnalysisMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public Optional<Extension<SecurityAnalysisParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.of(new DummyExtension());
    }

    @Override
    public List<String> getSpecificParametersNames() {
        return Collections.singletonList("dummy-extension");
    }
}
