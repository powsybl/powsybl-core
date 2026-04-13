package com.powsybl.mixed.security.analysis.parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
class MixedModeParametersExtensionTest {
    private MixedModeParametersExtension extension;

    @BeforeEach
    void setUp() {
        extension = new MixedModeParametersExtension();
    }

    @Test
    void testDefaultValues() {
        assertNull(extension.getStaticSimulator());
        assertNull(extension.getDynamicSimulator());
        assertNull(extension.getSwitchCriteria());
    }

    @Test
    void testSetGetStaticSimulator() {
        extension.setStaticSimulator("load-flow");
        assertEquals("load-flow", extension.getStaticSimulator());
    }

    @Test
    void testSetGetDynamicSimulator() {
        extension.setDynamicSimulator("dynaflow");
        assertEquals("dynaflow", extension.getDynamicSimulator());
    }

    @Test
    void testSetGetSwitchCriteria() {
        extension.setSwitchCriteria(Arrays.asList("NON_CONVERGENCE", "LIMIT_VIOLATIONS"));
        assertEquals(2, extension.getSwitchCriteria().size());
        assertTrue(extension.getSwitchCriteria().contains("NON_CONVERGENCE"));
        assertTrue(extension.getSwitchCriteria().contains("LIMIT_VIOLATIONS"));
    }

    @Test
    void testSetEmptySwitchCriteria() {
        extension.setSwitchCriteria(Collections.emptyList());
        assertNotNull(extension.getSwitchCriteria());
        assertEquals(0, extension.getSwitchCriteria().size());
    }

    @Test
    void testSetNullSwitchCriteria() {
        extension.setSwitchCriteria(null);
        assertNull(extension.getSwitchCriteria());
    }

    @Test
    void testCompleteConfiguration() {
        extension.setStaticSimulator("load-flow");
        extension.setDynamicSimulator("dynaflow");
        extension.setSwitchCriteria(Collections.singletonList("NON_CONVERGENCE"));
        assertEquals("load-flow", extension.getStaticSimulator());
        assertEquals("dynaflow", extension.getDynamicSimulator());
        assertEquals(1, extension.getSwitchCriteria().size());
    }
}
