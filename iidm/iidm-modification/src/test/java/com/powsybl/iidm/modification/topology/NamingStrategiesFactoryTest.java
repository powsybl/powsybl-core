package com.powsybl.iidm.modification.topology;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
class NamingStrategiesFactoryTest {

    @Test
    void testGetDefaultNotNull() {
        NamingStrategy strategy = NamingStrategiesFactory.getDefault();
        assertNotNull(strategy);
    }

    @Test
    void testFindByNameDefault() {
        Optional<NamingStrategy> strategy = NamingStrategiesFactory.findByName("Default");
        assertTrue(strategy.isPresent());
        assertEquals("Default", strategy.get().getName());
    }

    @Test
    void testFindByNameNullAndEmpty() {
        assertFalse(NamingStrategiesFactory.findByName(null).isPresent());
        assertFalse(NamingStrategiesFactory.findByName("NonExistent").isPresent());
        assertFalse(NamingStrategiesFactory.findByName(" ").isPresent());
    }

    @Test
    void testFindAllStrategies() {
        List<NamingStrategy> strategies = NamingStrategiesFactory.findAll();
        assertNotNull(strategies);
        assertFalse(strategies.isEmpty());
        assertTrue(strategies.stream().anyMatch(s -> "Default".equals(s.getName())));
    }
}
