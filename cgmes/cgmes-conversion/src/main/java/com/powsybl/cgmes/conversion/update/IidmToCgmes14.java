package com.powsybl.cgmes.conversion.update;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.powsybl.cgmes.conversion.update.elements14.*;

public class IidmToCgmes14 extends IidmToCgmes {

    public IidmToCgmes14() {
        this.converter = converter();
    }

    public Map<String, Callable<Map<String, CgmesPredicateDetails>>> converter() {

        final Map<String, Callable<Map<String, CgmesPredicateDetails>>> map = new HashMap<>();
        map.put("GeneratorImpl", () -> GeneratorToSynchronousMachine.converter());
        map.put("LoadImpl", () -> LoadToEnergyConsumer.converter());

        return map;
    }

}
