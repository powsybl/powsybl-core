package com.powsybl.iidm.modification.topology;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
class NamingStrategiesTest {

    @Test
    void testGetDefaultNotNull() {
        NamingStrategy strategy = new DefaultNamingStrategy();
        assertNotNull(strategy);
    }

    @Test
    void testFindByNameDefault() {
        NamingStrategiesServiceLoader loader = new NamingStrategiesServiceLoader();
        Optional<NamingStrategy> strategy = loader.findNamingStrategyByName("Default");
        assertTrue(strategy.isPresent());
        assertEquals("Default", strategy.get().getName());
    }

    @Test
    void testFindAllNamingStrategies() {
        NamingStrategiesServiceLoader loader = new NamingStrategiesServiceLoader();
        List<NamingStrategy> strategies = loader.findAllNamingStrategies();
        assertNotNull(strategies);
        assertFalse(strategies.isEmpty());
        assertTrue(strategies.stream().anyMatch(s -> "Default".equals(s.getName())));
    }
}
