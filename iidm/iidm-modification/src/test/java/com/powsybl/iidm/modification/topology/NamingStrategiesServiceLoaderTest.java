package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.util.ServiceLoaderCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
class NamingStrategiesServiceLoaderTest {

    private NamingStrategiesServiceLoader loader;
    private ServiceLoaderCache<NamingStrategy> cache;

    @BeforeEach
    void setUp() {
        loader = new NamingStrategiesServiceLoader();
        cache = new ServiceLoaderCache<>(NamingStrategy.class);
    }

    @Test
    void testLoadNamingStrategies() {
        List<NamingStrategy> strategies = loader.findAllNamingStrategies();

        assertNotNull(strategies);
        assertFalse(strategies.isEmpty());

        assertTrue(strategies.stream().anyMatch(s -> "Default".equals(s.getName())));
    }

    @Test
    void testGetDefault() {
        NamingStrategy defaultStrategy = new DefaultNamingStrategy();

        assertNotNull(defaultStrategy);
        assertNotNull(defaultStrategy.getName());
        assertFalse(defaultStrategy.getName().trim().isEmpty());
    }

    @Test
    void testGetDefaultPrefersDefault() {
        NamingStrategy defaultStrategy = new DefaultNamingStrategy();

        List<NamingStrategy> allStrategies = loader.findAllNamingStrategies();
        if (allStrategies.size() > 1) {
            boolean hasDefaultStrategy = allStrategies.stream()
                    .anyMatch(s -> "Default".equals(s.getName()));

            if (hasDefaultStrategy) {
                assertEquals("Default", defaultStrategy.getName());
            }
        }
    }

    @Test
    void testFindByName() {
        Optional<NamingStrategy> found = loader.findNamingStrategyByName("Default");
        assertTrue(found.isPresent());
        assertEquals("Default", found.get().getName());

        Optional<NamingStrategy> notFound = loader.findNamingStrategyByName("NonExistent");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testNoDuplicateNames() {
        List<NamingStrategy> strategies = loader.findAllNamingStrategies();

        Set<String> names = new HashSet<>();
        for (NamingStrategy strategy : strategies) {
            String name = strategy.getName();
            assertNotNull(name, "Strategy name should not be null");
            assertFalse(name.trim().isEmpty(), "Strategy name should not be empty");
            assertTrue(names.add(name), "Duplicate strategy name found: " + name);
        }
    }

    @Test
    void testConstructorNullClass() {
        assertThrows(NullPointerException.class, () ->
                new ServiceLoaderCache<>(null));
    }

    @Test
    void testGetServicesNotNull() {
        List<NamingStrategy> services = cache.getServices();
        assertNotNull(services);
    }

    @Test
    void testGetServicesCaching() {
        List<NamingStrategy> services1 = cache.getServices();
        List<NamingStrategy> services2 = cache.getServices();

        assertSame(services1, services2, "Should return the same cached instance");
    }

    @Test
    void testConsistencyBetweenMethods() {
        NamingStrategy defaultStrategy = new DefaultNamingStrategy();
        Optional<NamingStrategy> foundStrategy = loader.findNamingStrategyByName("Default");
        List<NamingStrategy> allStrategies = loader.findAllNamingStrategies();

        assertTrue(allStrategies.stream().anyMatch(strategy -> strategy.getName().equals(defaultStrategy.getName())));
        assertTrue(foundStrategy.isPresent());
        assertEquals(defaultStrategy.getName(), foundStrategy.get().getName());
    }

    @Test
    void testFindByNameNullAndEmpty() {
        assertFalse(loader.findNamingStrategyByName(null).isPresent());
        assertFalse(loader.findNamingStrategyByName(" ").isPresent());
    }
}
