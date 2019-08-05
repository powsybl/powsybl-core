package com.powsybl.cgmes.update.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * For conversion of TwoWindingsTransformer we need to create two additional
 * elements End1 and End2. Both have distinct ID (Subject) and contain reference
 * to the parent PowerTransformer element.
 */
public class TwoWindingsTransformerToPowerTransformer extends IidmToCgmes implements ConversionMapper {

    public TwoWindingsTransformerToPowerTransformer(IidmChange change) {
        super(change);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicatesOnUpdate() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("b", new CgmesPredicateDetails("cim:TransformerWinding.b", "_EQ", false, idEnd1)),
            entry("r", new CgmesPredicateDetails("cim:TransformerWinding.r", "_EQ", false, idEnd1)),
            entry("x", new CgmesPredicateDetails("cim:TransformerWinding.x", "_EQ", false, idEnd1)),
            entry("g", new CgmesPredicateDetails("cim:TransformerWinding.g", "_EQ", false, idEnd1)),
            entry("ratedU1", new CgmesPredicateDetails("cim:TransformerWinding.ratedU", "_EQ", false, idEnd1)),
            entry("ratedU2", new CgmesPredicateDetails("cim:TransformerWinding.ratedU", "_EQ", false, idEnd2)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();

        TwoWindingsTransformer newTwoWindingsTransformer = (TwoWindingsTransformer) change.getIdentifiable();

        String ptId = newTwoWindingsTransformer.getId();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        allCgmesDetails.put(rdfType, "cim:PowerTransformer");

        String name = newTwoWindingsTransformer.getName();
        allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("name"),
            name);

        /**
         * PowerTransformerEnd1
         */
        CgmesPredicateDetails rdfTypeEnd1 = new CgmesPredicateDetails("rdf:type", "_EQ", false, idEnd1);
        allCgmesDetails.put(rdfTypeEnd1, "cim:TransformerWinding");

        CgmesPredicateDetails powerTransformerEnd1 = new CgmesPredicateDetails(
            "cim:TransformerWinding.MemberOf_PowerTransformer",
            "_EQ", true, idEnd1);
        allCgmesDetails.put(powerTransformerEnd1, ptId);

        double b = newTwoWindingsTransformer.getB();
        if (!String.valueOf(b).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("b"),
                String.valueOf(b));
        }

        double r = newTwoWindingsTransformer.getR();
        if (!String.valueOf(r).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("r"),
                String.valueOf(r));
        }

        double x = newTwoWindingsTransformer.getX();
        if (!String.valueOf(x).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("x"),
                String.valueOf(x));
        }

        double g = newTwoWindingsTransformer.getG();
        if (!String.valueOf(g).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("g"),
                String.valueOf(g));
        }

        double ratedU1 = newTwoWindingsTransformer.getRatedU1();
        if (!String.valueOf(ratedU1).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("ratedU1"),
                String.valueOf(ratedU1));
        }

        /**
         * PowerTransformerEnd2
         */

        CgmesPredicateDetails rdfTypeEnd2 = new CgmesPredicateDetails("rdf:type", "_EQ", false, idEnd2);
        allCgmesDetails.put(rdfTypeEnd2, "cim:TransformerWinding");

        CgmesPredicateDetails powerTransformerEnd2 = new CgmesPredicateDetails(
            "cim:TransformerWinding.MemberOf_PowerTransformer",
            "_EQ", true, idEnd2);
        allCgmesDetails.put(powerTransformerEnd2, ptId);

        CgmesPredicateDetails bEnd2 = new CgmesPredicateDetails("cim:TransformerWinding.b", "_EQ", false, idEnd2);
        allCgmesDetails.put(bEnd2, String.valueOf(0.0));

        CgmesPredicateDetails rEnd2 = new CgmesPredicateDetails("cim:TransformerWinding.r", "_EQ", false, idEnd2);
        allCgmesDetails.put(rEnd2, String.valueOf(0.0));

        CgmesPredicateDetails xEnd2 = new CgmesPredicateDetails("cim:TransformerWinding.x", "_EQ", false, idEnd2);
        allCgmesDetails.put(xEnd2, String.valueOf(0.0));

        CgmesPredicateDetails gEnd2 = new CgmesPredicateDetails("cim:TransformerWinding.g", "_EQ", false, idEnd2);
        allCgmesDetails.put(gEnd2, String.valueOf(0.0));

        double ratedU2 = newTwoWindingsTransformer.getRatedU2();
        if (!String.valueOf(ratedU1).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("ratedU2"),
                String.valueOf(ratedU2));
        }

        return allCgmesDetails;
    }

    private static String idEnd1 = UUID.randomUUID().toString();
    private static String idEnd2 = UUID.randomUUID().toString();

}
