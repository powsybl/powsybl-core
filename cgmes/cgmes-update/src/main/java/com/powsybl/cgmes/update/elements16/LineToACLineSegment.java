package com.powsybl.cgmes.update.elements16;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes16;
import com.powsybl.iidm.network.Line;

public class LineToACLineSegment extends IidmToCgmes16 implements ConversionMapper {

    public LineToACLineSegment(IidmChange change) {
        super(change);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicates() {
        double bchComputedValue = 0.0;
        double gchComputedValue = 0.0;
        if (change.getAttribute().equals("b1") || change.getAttribute().equals("b2")) {
            Line line = (Line) change.getIdentifiable();
            bchComputedValue = line.getB1() + line.getB2();
        }

        if (change.getAttribute().equals("g1") || change.getAttribute().equals("g2")) {
            Line line = (Line) change.getIdentifiable();
            gchComputedValue = line.getG1() + line.getG2();
        }
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("r", new CgmesPredicateDetails("cim:ACLineSegment.r", "_EQ", false)),
            entry("x", new CgmesPredicateDetails("cim:ACLineSegment.x", "_EQ", false)),
            entry("b1",
                new CgmesPredicateDetails("cim:Conductor.bch", "_EQ", false, null, String.valueOf(bchComputedValue))),
            entry("b2",
                new CgmesPredicateDetails("cim:Conductor.bch", "_EQ", false, null, String.valueOf(bchComputedValue))),
            entry("g1",
                new CgmesPredicateDetails("cim:Conductor.gch", "_EQ", false, null, String.valueOf(gchComputedValue))),
            entry("g2",
                new CgmesPredicateDetails("cim:Conductor.gch", "_EQ", false, null, String.valueOf(gchComputedValue))))
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

        double b1 = newLine.getB1();
        double b2 = newLine.getB2();
        if (!String.valueOf(b1).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("b1"),
                String.valueOf(b1 + b2));
        }

        double g1 = newLine.getG1();
        double g2 = newLine.getG2();
        if (!String.valueOf(g1).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("g1"),
                String.valueOf(g1 + g2));
        }

        return allCgmesDetails;
    }

}
