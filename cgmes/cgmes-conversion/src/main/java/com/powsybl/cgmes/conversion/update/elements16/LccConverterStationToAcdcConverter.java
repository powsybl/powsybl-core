package com.powsybl.cgmes.conversion.update.elements16;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.update.AbstractIidmToCgmes;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.iidm.network.LccConverterStation;

public class LccConverterStationToAcdcConverter extends AbstractIidmToCgmes {
    private LccConverterStationToAcdcConverter() {
    }

    public static Map<String, CgmesPredicateDetails> mapIidmAtrribute() {

        return Collections.unmodifiableMap(Stream.of(
            entry("rdfType", new CgmesPredicateDetails("rdf:type", "_TP", false, "cim:ACDCConverter")))
            .collect(entriesToMap()));

    }

    public Map<String, String> getValues(IidmChange change) {
        if (!(change.getIdentifiable() instanceof LccConverterStation)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        LccConverterStation lccConverterStation = (LccConverterStation) change.getIdentifiable();
        return ImmutableMap.<String, String>builder()
            .put("name", lccConverterStation.getName()).build();
    }
}
