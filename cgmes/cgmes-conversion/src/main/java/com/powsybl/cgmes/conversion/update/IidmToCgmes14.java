package com.powsybl.cgmes.conversion.update;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.update.elements14.*;

public class IidmToCgmes14 extends IidmToCgmes {

    public IidmToCgmes14() {
        this.converter = converter();
    }

    public Map<String, Map<String, CgmesPredicateDetails>> converter() {
        return ImmutableMap.<String, Map<String, CgmesPredicateDetails>>builder()
            .put("GeneratorImpl", GeneratorToSynchronousMachine.mapIidmAtrribute())
            .put("LoadImpl", LoadToEnergyConsumer.mapIidmAtrribute()).build();
    }
}
