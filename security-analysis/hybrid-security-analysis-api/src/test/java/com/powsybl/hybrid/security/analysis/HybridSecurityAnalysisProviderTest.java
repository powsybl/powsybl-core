package com.powsybl.hybrid.security.analysis;

import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.hybrid.security.analysis.parameters.HybridModeParametersExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisRunParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** @author Riad Benradi {@literal <riad.benradi at rte-france.com>}*/

class HybridSecurityAnalysisProviderTest {
    @Mock
    private Network network;
    @Mock
    private ContingenciesProvider contingenciesProvider;
    @Mock
    private SecurityAnalysisRunParameters runParameters;
    private HybridSecurityAnalysisProvider provider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new HybridSecurityAnalysisProvider();
    }

    @Test
    void testProviderName() {
        assertEquals("HybridSecurityAnalysis", provider.getName());
    }

    @Test
    void testProviderVersion() {
        String version = provider.getVersion();
        assertNull(version);
    }

    @Test
    void testRunMissingExtensionThrows() {
        SecurityAnalysisParameters mockSaParams = mock(SecurityAnalysisParameters.class);
        when(runParameters.getSecurityAnalysisParameters()).thenReturn(mockSaParams);
        when(mockSaParams.getExtension(HybridModeParametersExtension.class)).thenReturn(null);
        // This will try to load from PlatformConfig.defaultConfig() which should fail in test env if not found
        assertThrows(Exception.class, () ->
            provider.run(network, "main", contingenciesProvider, runParameters)
        );
    }
}
