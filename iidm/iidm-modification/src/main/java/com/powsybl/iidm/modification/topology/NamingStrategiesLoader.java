package com.powsybl.iidm.modification.topology;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This interface supports the modular architecture of PowSyBL by allowing different modules
 * to contribute their own naming strategies without modifying existing code
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
public interface NamingStrategiesLoader {

    List<NamingStrategy> loadNamingStrategies();

    NamingStrategy getDefault();

    Optional<NamingStrategy> findByName(String name);

    Set<String> getAvailableStrategyNames();

    default List<NamingStrategy> findAll() {
        return loadNamingStrategies();
    }
}
