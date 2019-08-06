package com.powsybl.cgmes.update.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.Line;

public class LineToACLineSegment extends IidmToCgmes implements ConversionMapper {

    public LineToACLineSegment(IidmChange change) {
        super(change);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicatesOnUpdate() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("r", new CgmesPredicateDetails("cim:Conductor.r", "_EQ", false)),
            entry("x", new CgmesPredicateDetails("cim:Conductor.x", "_EQ", false)),
            entry("b1", new CgmesPredicateDetails("cim:Conductor.bch", "_EQ", false)),
            entry("b2", new CgmesPredicateDetails("cim:Conductor.bch", "_EQ", false)),
            entry("g1", new CgmesPredicateDetails("cim:Conductor.gch", "_EQ", false)),
            entry("g2", new CgmesPredicateDetails("cim:Conductor.gch", "_EQ", false)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();
        Line newLine = (Line) change.getIdentifiable();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        allCgmesDetails.put(rdfType, "cim:ACLineSegment");

        String name = newLine.getName();
        if (name != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("name"),
                name);
        }

        double r = newLine.getR();
        if (!String.valueOf(r).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("r"),
                String.valueOf(r));
        }

        double x = newLine.getX();
        if (!String.valueOf(x).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("x"),
                String.valueOf(x));
        }

        double b1 = newLine.getB1();
        double b2 = newLine.getB2();
        if (!String.valueOf(b1).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("b1"),
                String.valueOf(b1 + b2));
        }

        double g1 = newLine.getG1();
        double g2 = newLine.getG2();
        if (!String.valueOf(g1).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("g1"),
                String.valueOf(g1 + g2));
        }

        return allCgmesDetails;
    }

}
