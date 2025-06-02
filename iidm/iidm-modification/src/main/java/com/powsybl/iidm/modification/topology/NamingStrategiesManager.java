package com.powsybl.iidm.modification.topology;

import java.util.List;
import java.util.Optional;

/**
 * Manager for dynamically discovering and managing naming strategies.
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
public final class NamingStrategiesManager {

    private static final NamingStrategiesLoader STRATEGIES_LOADER = new NamingStrategiesServiceLoader();

    private NamingStrategiesManager() {
    }

    private static final class DefaultStrategyHolder {
        private static final NamingStrategy DEFAULT_STRATEGY = STRATEGIES_LOADER.getDefaultNamingStrategy();

        private DefaultStrategyHolder() {
        }
    }

    public static NamingStrategy getDefaultNamingStrategy() {
        return DefaultStrategyHolder.DEFAULT_STRATEGY;
    }

    public static Optional<NamingStrategy> findNamingStrategyByName(String name) {
        return STRATEGIES_LOADER.findNamingStrategyByName(name);
    }

    public static List<NamingStrategy> findAllNamingStrategies() {
        return STRATEGIES_LOADER.loadNamingStrategies();
    }
}
