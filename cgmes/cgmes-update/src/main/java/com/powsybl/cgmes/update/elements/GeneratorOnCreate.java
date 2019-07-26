package com.powsybl.cgmes.update.elements;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.cgmes.update.MapTriplestorePredicateToContext;
import com.powsybl.iidm.network.Generator;

public class GeneratorOnCreate extends IidmToCgmes implements ConversionOnCreate {
    public GeneratorOnCreate(IidmChange change) {
        super(change);
    }

    @Override
    public Map<MapTriplestorePredicateToContext, String> getIdentifiableAttributes() {

        Map<MapTriplestorePredicateToContext, String> mapContextPredicateValue = new HashMap<MapTriplestorePredicateToContext, String>();

        Generator newGenerator = (Generator) change.getIdentifiable();
        Map<String, Object> iidmToCgmesMapper = generatorToSynchronousMachine();

        mapContextPredicateValue.put((MapTriplestorePredicateToContext) iidmToCgmesMapper.get("rdfType"),
            "SynchronousMachine");

        String name = newGenerator.getName();
        if (name != null) {
            mapContextPredicateValue.put((MapTriplestorePredicateToContext) iidmToCgmesMapper.get("name"),
                name);
        }

        Double ratedS = newGenerator.getRatedS();
        if (ratedS != null) {
            mapContextPredicateValue.put((MapTriplestorePredicateToContext) iidmToCgmesMapper.get("ratedS"),
                ratedS.toString());
        }

        return mapContextPredicateValue;
    }

}
