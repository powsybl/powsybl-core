package com.powsybl.cgmes.update.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * For conversion of TwoWindingsTransformer we need to create two additional
 * elements End1 and End2. Both have distinct ID (Subject) and contain reference
 * to the parent PowerTransformer element.
 */
public class TwoWindingsTransformerToPowerTransformer extends IidmToCgmes implements ConversionOnCreate {

    public TwoWindingsTransformerToPowerTransformer(IidmChange change) {
        super(change);
    }

    public static Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("b", new CgmesPredicateDetails("cim:PowerTransformerEnd.b", "_EQ", false, newIdEnd1)),
            entry("r", new CgmesPredicateDetails("cim:PowerTransformerEnd.r", "_EQ", false, newIdEnd1)),
            entry("x", new CgmesPredicateDetails("cim:PowerTransformerEnd.x", "_EQ", false, newIdEnd1)),
            entry("g", new CgmesPredicateDetails("cim:PowerTransformerEnd.g", "_EQ", false, newIdEnd1)),
            entry("ratedU1", new CgmesPredicateDetails("cim:PowerTransformerEnd.ratedU", "_EQ", false, newIdEnd1)),
            entry("ratedU2", new CgmesPredicateDetails("cim:PowerTransformerEnd.ratedU", "_EQ", false, newIdEnd2)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getIdentifiableAttributes() {

        Map<CgmesPredicateDetails, String> mapCgmesPredicateDetails = new HashMap<CgmesPredicateDetails, String>();

        TwoWindingsTransformer newTwoWindingsTransformer = (TwoWindingsTransformer) change.getIdentifiable();
        Map<String, Object> iidmToCgmesMapper = mapIidmToCgmesPredicates();

        String ptId = newTwoWindingsTransformer.getId();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        mapCgmesPredicateDetails.put(rdfType, "cim:PowerTransformer");

        /**
         * PowerTransformerEnd1
         */
        CgmesPredicateDetails rdfTypeEnd1 = new CgmesPredicateDetails("rdf:type", "_EQ", false, newIdEnd1);
        mapCgmesPredicateDetails.put(rdfTypeEnd1, "cim:PowerTransformerEnd");

        CgmesPredicateDetails nameEnd1 = new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false,
            newIdEnd1);

        CgmesPredicateDetails powerTransformerEnd1 = new CgmesPredicateDetails(
            "cim:PowerTransformerEnd.PowerTransformer",
            "_EQ", true, newIdEnd1);
        mapCgmesPredicateDetails.put(powerTransformerEnd1, ptId);

        double b = newTwoWindingsTransformer.getB();
        if (!String.valueOf(b).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("b"),
                String.valueOf(b));
        }

        double r = newTwoWindingsTransformer.getR();
        if (!String.valueOf(r).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("r"),
                String.valueOf(r));
        }

        double x = newTwoWindingsTransformer.getX();
        if (!String.valueOf(x).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("x"),
                String.valueOf(x));
        }

        double g = newTwoWindingsTransformer.getG();
        if (!String.valueOf(g).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("g"),
                String.valueOf(g));
        }

        double ratedU1 = newTwoWindingsTransformer.getRatedU1();
        if (!String.valueOf(ratedU1).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("ratedU1"),
                String.valueOf(ratedU1));
        }

        /**
         * PowerTransformerEnd2
         */

        CgmesPredicateDetails rdfTypeEnd2 = new CgmesPredicateDetails("rdf:type", "_EQ", false, newIdEnd2);
        mapCgmesPredicateDetails.put(rdfTypeEnd2, "cim:PowerTransformerEnd");

        CgmesPredicateDetails nameEnd2 = new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false,
            newIdEnd2);

        CgmesPredicateDetails powerTransformerEnd2 = new CgmesPredicateDetails(
            "cim:PowerTransformerEnd.PowerTransformer",
            "_EQ", true, newIdEnd2);
        mapCgmesPredicateDetails.put(powerTransformerEnd2, ptId);
        
        CgmesPredicateDetails bEnd2 = new CgmesPredicateDetails("cim:PowerTransformerEnd.b", "_EQ", false, newIdEnd2);
        mapCgmesPredicateDetails.put(bEnd2, String.valueOf(0.0));

        /**
         * Value of Name is common for all:
         */

        String name = newTwoWindingsTransformer.getName();
        if (name != null) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("name"),
                name);
            mapCgmesPredicateDetails.put(nameEnd1, name);
            mapCgmesPredicateDetails.put(nameEnd2, name);
        }

        return mapCgmesPredicateDetails;
    }

    private static String newIdEnd1 = UUID.randomUUID().toString();
    private static String newIdEnd2 = UUID.randomUUID().toString();

}
