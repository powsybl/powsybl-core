package com.powsybl.cgmes.update.elements;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.Line;

public class LineOnCreate extends IidmToCgmes implements ConversionOnCreate {

    public LineOnCreate(IidmChange change) {
        super(change);
    }

    @Override
    public Map<CgmesPredicateDetails, String> getIdentifiableAttributes() {
        Map<CgmesPredicateDetails, String> mapCgmesPredicateDetails = new HashMap<CgmesPredicateDetails, String>();
        Line newLine = (Line) change.getIdentifiable();
        Map<String, Object> iidmToCgmesMapper = lineToACLineSegment();

        mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("rdfType"),
            "cim:ACLineSegment");

        String name = newLine.getName();
        if (name != null) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("name"),
                name);
        }
        
        double r = newLine.getR();
        if (!String.valueOf(r).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("r"),
                String.valueOf(r));
        }
        
        double x = newLine.getX();
        if (!String.valueOf(x).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("x"),
                String.valueOf(x));
        }
        
        double bch = newLine.getB1();
        if (!String.valueOf(bch).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("bch"),
                String.valueOf(bch));
        }
        
        double gch = newLine.getG1();
        if (!String.valueOf(bch).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("gch"),
                String.valueOf(gch));
        }
        
        return mapCgmesPredicateDetails;
    }

}
