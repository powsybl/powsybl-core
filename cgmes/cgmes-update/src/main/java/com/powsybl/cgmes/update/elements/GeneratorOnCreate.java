package com.powsybl.cgmes.update.elements;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.iidm.network.Generator;

public class GeneratorOnCreate extends IidmToCgmes implements ConversionOnCreate {
    public GeneratorOnCreate(IidmChange change) {
        super(change);
    }

    @Override
    public Map<CgmesPredicateDetails, String> getIdentifiableAttributes() {

        Map<CgmesPredicateDetails, String> mapContextPredicateValue = new HashMap<CgmesPredicateDetails, String>();

        Generator newGenerator = (Generator) change.getIdentifiable();
        Map<String, Object> iidmToCgmesMapper = generatorToSynchronousMachine();

        mapContextPredicateValue.put((CgmesPredicateDetails) iidmToCgmesMapper.get("rdfType"),
            "SynchronousMachine");

        String name = newGenerator.getName();
        if (name != null) {
            mapContextPredicateValue.put((CgmesPredicateDetails) iidmToCgmesMapper.get("name"),
                name);
        }

        Double ratedS = newGenerator.getRatedS();
        if (ratedS != null) {
            mapContextPredicateValue.put((CgmesPredicateDetails) iidmToCgmesMapper.get("ratedS"),
                ratedS.toString());
        }

        return mapContextPredicateValue;
    }

}
