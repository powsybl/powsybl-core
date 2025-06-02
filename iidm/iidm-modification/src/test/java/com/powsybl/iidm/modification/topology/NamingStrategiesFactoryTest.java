package com.powsybl.iidm.modification.topology;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.powsybl.iidm.modification.topology.NamingStrategiesManager.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
class NamingStrategiesFactoryTest {

    @Test
    void testGetDefaultNotNull() {
        NamingStrategy strategy = getDefaultNamingStrategy();
        assertNotNull(strategy);
    }

    @Test
    void testFindByNameDefault() {
        Optional<NamingStrategy> strategy = findNamingStrategyByName("Default");
        assertTrue(strategy.isPresent());
        assertEquals("Default", strategy.get().getName());
    }

    @Test
    void testFindByNameNullAndEmpty() {
        assertFalse(findNamingStrategyByName(null).isPresent());
        assertFalse(findNamingStrategyByName(" ").isPresent());
    }

    @Test
    void testFindAllStrategies() {
        List<NamingStrategy> strategies = findAllNamingStrategies();
        assertNotNull(strategies);
        assertFalse(strategies.isEmpty());
        assertTrue(strategies.stream().anyMatch(s -> "Default".equals(s.getName())));
    }
}
