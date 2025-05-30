package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of NamingStrategiesLoader using ServiceLoaderCache.
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
public class NamingStrategiesServiceLoader implements NamingStrategiesLoader {
    private static final ServiceLoaderCache<NamingStrategy> NAMING_STRATEGY_CACHE = new ServiceLoaderCache<>(NamingStrategy.class);

    @Override
    public List<NamingStrategy> loadNamingStrategies() {
        return NAMING_STRATEGY_CACHE.getServices();
    }

    @Override
    public NamingStrategy getDefault() {
        List<NamingStrategy> strategies = loadNamingStrategies();

        return strategies.stream()
                .filter(strategy -> NamingStrategyConstants.NAME.equals(strategy.getName()))
                .findFirst()
                .orElse(strategies.get(0));
    }

    @Override
    public Optional<NamingStrategy> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        return loadNamingStrategies().stream()
                .filter(strategy -> name.equals(strategy.getName()))
                .findFirst();
    }

    @Override
    public Set<String> getAvailableStrategyNames() {
        return loadNamingStrategies().stream()
                .map(NamingStrategy::getName)
                .collect(Collectors.toSet());
    }
}
