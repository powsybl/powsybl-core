package com.powsybl.hybrid.security.analysis.parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/** @author Riad Benradi {@literal <riad.benradi at rte-france.com>}*/
class HybridModeParametersExtensionTest {
    private HybridModeParametersExtension extension;

    @BeforeEach
    void setUp() {
        extension = new HybridModeParametersExtension();
    }

    @Test
    void testDefaultValues() {
        assertNull(extension.getFirstProviderName());
        assertNull(extension.getSecondProviderName());
    }

    @Test
    void testSetGetFirstProviderName() {
        extension.setFirstProviderName("OpenLoadFlow");
        assertEquals("OpenLoadFlow", extension.getFirstProviderName());
    }

    @Test
    void testSetGetSecondProviderName() {
        extension.setSecondProviderName("Dynaflow");
        assertEquals("Dynaflow", extension.getSecondProviderName());
    }

    @Test
    void testCompleteConfiguration() {
        extension.setFirstProviderName("OpenLoadFlow");
        extension.setSecondProviderName("Dynaflow");
        assertEquals("OpenLoadFlow", extension.getFirstProviderName());
        assertEquals("Dynaflow", extension.getSecondProviderName());
    }
}
