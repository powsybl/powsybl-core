package com.powsybl.cgmes.update.elements16;

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
    public Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("r", new CgmesPredicateDetails("cim:ACLineSegment.r", "_EQ", false)),
            entry("x", new CgmesPredicateDetails("cim:ACLineSegment.x", "_EQ", false)),
            entry("bch", new CgmesPredicateDetails("cim:ACLineSegment.bch", "_EQ", false)),
            entry("gch", new CgmesPredicateDetails("cim:ACLineSegment.gch", "_EQ", false)))
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
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("name"),
                name);
        }

        double r = newLine.getR();
        if (!String.valueOf(r).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("r"),
                String.valueOf(r));
        }

        double x = newLine.getX();
        if (!String.valueOf(x).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("x"),
                String.valueOf(x));
        }

        double bch = newLine.getB1();
        if (!String.valueOf(bch).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("bch"),
                String.valueOf(bch));
        }

        double gch = newLine.getG1();
        if (!String.valueOf(bch).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("gch"),
                String.valueOf(gch));
        }

        return allCgmesDetails;
    }

}
