package com.powsybl.iidm.modification.topology;

import java.util.List;
import java.util.Optional;

/**
 * Loader interface for dynamically discovering and managing naming strategies.
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
public final class NamingStrategiesFactory {

    private static final NamingStrategiesLoader STRATEGIES_LOADER = new NamingStrategiesServiceLoader();

    private NamingStrategiesFactory() {
    }

    private static final class DefaultStrategyHolder {
        private static final NamingStrategy DEFAULT_STRATEGY = STRATEGIES_LOADER.getDefault();

        private DefaultStrategyHolder() {
        }
    }

    public static NamingStrategy getDefault() {
        return DefaultStrategyHolder.DEFAULT_STRATEGY;
    }

    public static Optional<NamingStrategy> findByName(String name) {
        return STRATEGIES_LOADER.findByName(name);
    }

    public static List<NamingStrategy> findAll() {
        return STRATEGIES_LOADER.loadNamingStrategies();
    }
}
