package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;

public class LineToACLineSegment extends IidmToCgmes {

    LineToACLineSegment() {
        addSimpleUpdate("r", "cim:ACLineSegment.r", "_EQ", false);
        addSimpleUpdate("x", "cim:ACLineSegment.x", "_EQ", false);
        addSimpleUpdate("b1", "cim:ACLineSegment.bch", "_EQ", false);
        addSimpleUpdate("b2", "cim:ACLineSegment.bch", "_EQ", false);
        addSimpleUpdate("g1", "cim:ACLineSegment.gch", "_EQ", false);
        addSimpleUpdate("g2", "cim:ACLineSegment.gch", "_EQ", false);
    }

}
