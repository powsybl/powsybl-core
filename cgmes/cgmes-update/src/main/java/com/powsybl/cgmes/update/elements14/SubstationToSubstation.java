package com.powsybl.cgmes.update.elements14;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes14;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;

public class SubstationToSubstation extends IidmToCgmes14 implements ConversionMapper {

    public SubstationToSubstation(IidmChange change) {
        super(change);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("subRegionName", new CgmesPredicateDetails("cim:SubGeographicalRegion.Region", "_EQ", true)),
            entry("country", new CgmesPredicateDetails("cim:Substation.Region", "_EQ", true)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();

        Substation newSubstation = (Substation) change.getIdentifiable();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        allCgmesDetails.put(rdfType, "cim:Substation");

        String name = newSubstation.getName();
        if (name != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("name"),
                name);
        }

        Optional<Country> country = newSubstation.getCountry();
        if (country.isPresent()) {
            allCgmesDetails
                .put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("country"),
                    country.get().toString());
        }

        return allCgmesDetails;
    }

}
