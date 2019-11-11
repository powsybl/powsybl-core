package com.powsybl.cgmes.conversion.update;

import java.util.Map;
import java.util.concurrent.Callable;
import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.model.CgmesModel;

public class IidmToCgmes {

    public IidmToCgmes() {

    }

    public IidmToCgmes(String cimVersion) {
        this.cimVersion = cimVersion;

        if (cimVersion.equals("cim14#")) {
            iidmToCgmes = new IidmToCgmes14();
        } else {
            iidmToCgmes = new IidmToCgmes16();
        }
    }

    CgmesPredicateDetails convert(IidmChange change, CgmesModel cgmes) throws Exception {
        if (!(change instanceof IidmChangeOnUpdate)) {
            throw new ConversionException("Can't process requested modification");
        }
        // Getting IIDM - CGMES conversion map from converter:
        CgmesPredicateDetails converted = iidmToCgmes.converter
            .get(getIidmInstanceName(change))
            .call()
            .get(change.getAttribute());
        converted.setValue(change.getNewValueString());

        return converted;
    }

    private String getIidmInstanceName(IidmChange change) {
        return change.getIdentifiable().getClass().getSimpleName();
    }

    public static Map<String, CgmesPredicateDetails> genericConverter(String value) {
        final String RDF_TYPE = "rdf:type";
        return ImmutableMap.of(
            "rdfTypeSsh", new CgmesPredicateDetails(RDF_TYPE, "_SSH", false, value),
            "rdfTypeEq", new CgmesPredicateDetails(RDF_TYPE, "_EQ", false, value),
            "rdfTypeSv", new CgmesPredicateDetails(RDF_TYPE, "_SV", false, value),
            "name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, value));
    }

    private String cimVersion;
    private IidmToCgmes iidmToCgmes;
    public Map<String, Callable<Map<String, CgmesPredicateDetails>>> converter;
}
