package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.util.ServiceLoaderCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<NamingStrategy> strategies = loader.loadNamingStrategies();

        assertNotNull(strategies);
        assertFalse(strategies.isEmpty());

        assertTrue(strategies.stream().anyMatch(s -> "Default".equals(s.getName())));
    }

    @Test
    void testGetDefault() {
        NamingStrategy defaultStrategy = loader.getDefault();

        assertNotNull(defaultStrategy);
        assertNotNull(defaultStrategy.getName());
        assertFalse(defaultStrategy.getName().trim().isEmpty());
    }

    @Test
    void testGetDefaultPrefersDefault() {
        NamingStrategy defaultStrategy = loader.getDefault();

        List<NamingStrategy> allStrategies = loader.loadNamingStrategies();
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
        Optional<NamingStrategy> found = loader.findByName("Default");
        assertTrue(found.isPresent());
        assertEquals("Default", found.get().getName());

        Optional<NamingStrategy> notFound = loader.findByName("NonExistent");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindAll() {
        List<NamingStrategy> strategies1 = loader.loadNamingStrategies();
        List<NamingStrategy> strategies2 = loader.findAll();

        assertEquals(strategies1.size(), strategies2.size());
        assertTrue(strategies1.containsAll(strategies2));
    }

    @Test
    void testGetAvailableStrategyNames() {
        List<NamingStrategy> strategies = loader.loadNamingStrategies();
        Set<String> expectedNames = strategies.stream()
                .map(NamingStrategy::getName)
                .collect(Collectors.toSet());

        Set<String> actualNames = loader.getAvailableStrategyNames();

        assertEquals(expectedNames, actualNames);
    }

    @Test
    void testNoDuplicateNames() {
        List<NamingStrategy> strategies = loader.loadNamingStrategies();

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
    void testClearCache() {
        List<NamingStrategy> services1 = cache.getServices();
        cache.clearCache();
        List<NamingStrategy> services2 = cache.getServices();

        assertNotSame(services1, services2);
        assertEquals(services1.size(), services2.size());
    }

    @Test
    void testCompleteIntegration() {
        NamingStrategy strategyFromFactory = NamingStrategiesFactory.getDefault();

        NamingStrategiesServiceLoader serviceLoader = new NamingStrategiesServiceLoader();
        NamingStrategy strategyFromLoader = serviceLoader.getDefault();

        assertEquals(strategyFromFactory.getName(), strategyFromLoader.getName());
    }

    @Test
    void testConsistencyBetweenMethods() {
        NamingStrategy defaultStrategy = NamingStrategiesFactory.getDefault();
        Optional<NamingStrategy> foundStrategy = NamingStrategiesFactory.findByName("Default");
        List<NamingStrategy> allStrategies = NamingStrategiesFactory.findAll();

        assertTrue(foundStrategy.isPresent());
        assertEquals(defaultStrategy.getName(), foundStrategy.get().getName());
        assertTrue(allStrategies.contains(defaultStrategy));
    }

    @Test
    void testPerformanceNoUnnecessaryReloading() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            NamingStrategiesFactory.getDefault();
            NamingStrategiesFactory.findAll();
        }

        long duration = System.currentTimeMillis() - startTime;

        assertTrue(duration < 1000, "Operations should be fast due to caching");
    }
}
