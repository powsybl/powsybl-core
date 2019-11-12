package com.powsybl.cgmes.conversion.update;

import java.util.ArrayList;
import java.util.List;
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

    List<CgmesPredicateDetails> convert(IidmChange change) throws Exception {
        if (!(change instanceof IidmChangeOnUpdate)) {
            throw new ConversionException("Can't process requested modification");
        }
        // Getting IIDM - CGMES conversion map from converter:
        List<CgmesPredicateDetails> list = new ArrayList<CgmesPredicateDetails>();

        CgmesPredicateDetails converted = iidmToCgmes.converter
            .get(getIidmInstanceName(change))
            .get(change.getAttribute());
        if (converted != null) {
            converted.setValue(change.getNewValueString());
            list.add(converted);
        }

        return list;
    }

    private String getIidmInstanceName(IidmChange change) {
        return change.getIdentifiable().getClass().getSimpleName();
    }

    public static Map<String, CgmesPredicateDetails> genericConverter(String value) {
        final String RDF_TYPE = "rdf:type";
        final String NAME = "cim:IdentifiedObject.name";
        return ImmutableMap.<String, CgmesPredicateDetails>builder()
            .put("rdfTypeSsh", new CgmesPredicateDetails(RDF_TYPE, "_SSH", false, value))
            .put("rdfTypeEq", new CgmesPredicateDetails(RDF_TYPE, "_EQ", false, value))
            .put("rdfTypeSv", new CgmesPredicateDetails(RDF_TYPE, "_SV", false, value))
            .put("rdfTypeTp", new CgmesPredicateDetails(RDF_TYPE, "_TP", false, value))
            .put("nameSsh", new CgmesPredicateDetails(NAME, "_SSH", false, value))
            .put("nameEq", new CgmesPredicateDetails(NAME, "_EQ", false, value))
            .put("nameSv", new CgmesPredicateDetails(NAME, "_SV", false, value))
            .put("nameTp", new CgmesPredicateDetails(NAME, "_TP", false, value))
            .build();
    }

    public static final String SUBSTATION_IMPL = "SubstationImpl";
    public static final String BUSBREAKER_VOLTAGELEVEL = "BusBreakerVoltageLevel";
    public static final String TWOWINDINGS_TRANSFORMER_IMPL = "TwoWindingsTransformerImpl";
    public static final String CONFIGUREDBUS_IMPL = "ConfiguredBusImpl";
    public static final String GENERATOR_IMPL = "GeneratorImpl";
    public static final String LOAD_IMPL = "LoadImpl";
    public static final String LCCCONVERTER_STATION_IMPL = "LccConverterStationImpl";
    public static final String LINE_IMPL = "LineImpl";
    public static final String SHUNTCOMPENSATOR_IMPL = "ShuntCompensatorImpl";

    private String cimVersion;
    private IidmToCgmes iidmToCgmes;
    public Map<String, Map<String, CgmesPredicateDetails>> converter;
}
