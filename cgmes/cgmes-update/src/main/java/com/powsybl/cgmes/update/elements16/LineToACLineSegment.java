package com.powsybl.cgmes.update.elements16;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.iidm.network.Line;

public class LineToACLineSegment implements ConversionMapper {

    public LineToACLineSegment(IidmChange change) {
        this.change = change;
    }

    @Override
    public Map<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Map<String, CgmesPredicateDetails> map = new HashMap<>();
        Line newLine = (Line) change.getIdentifiable();

        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:ACLineSegment"));

        String name = newLine.getName();
        if (name != null) {
            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));
        }

        double r = newLine.getR();
        if (!String.valueOf(r).equals("NaN")) {
            map.put("r", new CgmesPredicateDetails("cim:ACLineSegment.r", "_EQ", false, String.valueOf(r)));
        }

        double x = newLine.getX();
        if (!String.valueOf(x).equals("NaN")) {
            map.put("x", new CgmesPredicateDetails("cim:ACLineSegment.x", "_EQ", false, String.valueOf(x)));
        }

        double b1 = !String.valueOf(newLine.getB1()).equals("NaN") ? newLine.getB1() : 0.0;
        double b2 = !String.valueOf(newLine.getB2()).equals("NaN") ? newLine.getB2() : 0.0;
        map.put("b1",
            new CgmesPredicateDetails("cim:ACLineSegment.bch", "_EQ", false, String.valueOf(b1 + b2)));
        map.put("b2",
            new CgmesPredicateDetails("cim:ACLineSegment.bch", "_EQ", false, String.valueOf(b1 + b2)));

        double g1 = !String.valueOf(newLine.getG1()).equals("NaN") ? newLine.getG1() : 0.0;
        double g2 = !String.valueOf(newLine.getG2()).equals("NaN") ? newLine.getG2() : 0.0;
        map.put("g1",
            new CgmesPredicateDetails("cim:ACLineSegment.gch", "_EQ", false, String.valueOf(g1 + g2)));
        map.put("g2",
            new CgmesPredicateDetails("cim:ACLineSegment.gch", "_EQ", false, String.valueOf(g1 + g2)));

        return map;
    }

    private IidmChange change;
}
