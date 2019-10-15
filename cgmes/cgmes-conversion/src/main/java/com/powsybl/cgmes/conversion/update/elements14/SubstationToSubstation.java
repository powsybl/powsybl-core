package com.powsybl.cgmes.conversion.update.elements14;

import java.util.Optional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.ConversionMapper;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;

public class SubstationToSubstation implements ConversionMapper {

    public SubstationToSubstation(IidmChange change) {
        this.change = change;
    }

    @Override
    public Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Multimap<String, CgmesPredicateDetails> map = ArrayListMultimap.create();
        Substation newSubstation = (Substation) change.getIdentifiable();

        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:Substation"));

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
