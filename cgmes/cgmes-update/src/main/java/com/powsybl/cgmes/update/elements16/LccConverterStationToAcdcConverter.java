package com.powsybl.cgmes.update.elements16;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.iidm.network.LccConverterStation;

public class LccConverterStationToAcdcConverter implements ConversionMapper {

    public LccConverterStationToAcdcConverter(IidmChange change) {
        this.change = change;
    }

    @Override
    public Map<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Map<String, CgmesPredicateDetails> map = new HashMap<>();
        LccConverterStation newLccConverterStation = (LccConverterStation) change.getIdentifiable();

        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_TP", false, "cim:ACDCConverter"));

        String name = newLccConverterStation.getName();
        if (name != null) {
            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));
        }
        return map;
    }

    private IidmChange change;
}
