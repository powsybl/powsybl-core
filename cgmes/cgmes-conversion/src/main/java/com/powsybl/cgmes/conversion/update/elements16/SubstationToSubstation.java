package com.powsybl.cgmes.conversion.update.elements16;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.update.AbstractIidmToCgmes;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.ConversionMapper;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;

public class SubstationToSubstation extends AbstractIidmToCgmes implements ConversionMapper {

    private SubstationToSubstation() {
    }

    public static Map<String, CgmesPredicateDetails> converter() {
        return  Collections.unmodifiableMap(Stream.of(
            entry("country", new CgmesPredicateDetails("cim:Substation.Region", "_EQ", true, value)),
            entry("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:Substation")))
            .collect(entriesToMap()));
    }

    static Map<String, String> getValues(IidmChange change, CgmesModel cgmes) {
        if (!(change.getIdentifiable() instanceof Substation)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        Substation substation = (Substation) change.getIdentifiable();
        Optional<Country> country = substation.getCountry();
        if (! country.isPresent()) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        return ImmutableMap.of(
            "name", substation.getName(),
            "country", country.get().toString());
    }

}
