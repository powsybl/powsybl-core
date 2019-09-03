package com.powsybl.cgmes.update.elements14;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;

public class SubstationToSubstation implements ConversionMapper {

    public SubstationToSubstation(IidmChange change) {
        this.change = change;
    }

    @Override
    public Map<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Map<String, CgmesPredicateDetails> map = new HashMap<>();
        Substation newSubstation = (Substation) change.getIdentifiable();

        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_TP", false, "cim:Substation"));

        String name = newSubstation.getName();
        if (name != null) {
            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));
        }

        Optional<Country> country = newSubstation.getCountry();
        if (country.isPresent()) {
            map.put("country",
                new CgmesPredicateDetails("cim:Substation.Region", "_EQ", true, country.get().toString()));
        }
        // TODO elena fix Region/SubRegion/Country
        if (country.isPresent()) {
            map.put("subRegionName",
                new CgmesPredicateDetails("cim:SubGeographicalRegion.Region", "_EQ", true, country.get().toString()));
        }

        return map;
    }

    private IidmChange change;
}
