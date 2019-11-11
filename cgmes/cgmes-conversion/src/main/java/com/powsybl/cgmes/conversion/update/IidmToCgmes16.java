package com.powsybl.cgmes.conversion.update;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.powsybl.cgmes.conversion.update.elements16.*;

public class IidmToCgmes16 extends IidmToCgmes {

    public IidmToCgmes16() {
        this.converter = converter();
    }

    public Map<String, Callable<Map<String, CgmesPredicateDetails>>> converter() {

        // map Identifiable Instance with its method for IIDM - CGMES conversion
        final Map<String, Callable<Map<String, CgmesPredicateDetails>>> map = new HashMap<>();
        map.put("GeneratorImpl", () -> GeneratorToSynchronousMachine.converter());
        map.put("LoadImpl", () -> LoadToEnergyConsumer.converter());


        return map;
    }
}
