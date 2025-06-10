package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.List;
import java.util.Optional;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
public class NamingStrategiesServiceLoader {

    private static final ServiceLoaderCache<NamingStrategy> NAMING_STRATEGY_CACHE = new ServiceLoaderCache<>(NamingStrategy.class);

    public List<NamingStrategy> findAllNamingStrategies() {
        return NAMING_STRATEGY_CACHE.getServices();
    }

    public Optional<NamingStrategy> findNamingStrategyByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        return findAllNamingStrategies().stream()
                .filter(strategy -> name.equals(strategy.getName()))
                .findFirst();
    }
}
