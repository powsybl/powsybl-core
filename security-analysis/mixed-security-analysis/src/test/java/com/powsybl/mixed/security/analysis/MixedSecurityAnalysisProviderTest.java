package com.powsybl.mixed.security.analysis;

import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.mixed.security.analysis.parameters.MixedModeParametersExtension;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisRunParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MixedSecurityAnalysisProviderTest {
    @Mock
    private Network network;
    @Mock
    private ContingenciesProvider contingenciesProvider;
    @Mock
    private SecurityAnalysisRunParameters runParameters;
    private MixedSecurityAnalysisProvider provider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new MixedSecurityAnalysisProvider();
    }

    @Test
    void testProviderName() {
        assertEquals("MixedSecurityAnalysis", provider.getName());
    }

    @Test
    void testProviderVersion() {
        String version = provider.getVersion();
        assertNotNull(version);
        assertFalse(version.isEmpty());
    }

    @Test
    void testRunMissingExtensionThrows() {
        SecurityAnalysisParameters mockSaParams = mock(SecurityAnalysisParameters.class);
        when(runParameters.getSecurityAnalysisParameters()).thenReturn(mockSaParams);
        when(mockSaParams.getExtension(MixedModeParametersExtension.class)).thenReturn(null);
        assertThrows(Exception.class, () ->
            provider.run(network, "main", contingenciesProvider, runParameters).join()
        );
    }
}
