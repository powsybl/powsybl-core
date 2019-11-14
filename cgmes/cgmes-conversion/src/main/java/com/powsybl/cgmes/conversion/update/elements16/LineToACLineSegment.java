package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

public class LineToACLineSegment extends IidmToCgmes {

    LineToACLineSegment() {
        addSimpleUpdate("r", "cim:ACLineSegment.r", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("x", "cim:ACLineSegment.x", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("b1", "cim:ACLineSegment.bch", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("b2", "cim:ACLineSegment.bch", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("g1", "cim:ACLineSegment.gch", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("g2", "cim:ACLineSegment.gch", CgmesSubset.EQUIPMENT, false);
    }

}
