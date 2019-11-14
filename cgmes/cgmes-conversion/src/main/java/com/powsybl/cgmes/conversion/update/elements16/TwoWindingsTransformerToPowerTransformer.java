package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;

public class TwoWindingsTransformerToPowerTransformer extends IidmToCgmes {

    TwoWindingsTransformerToPowerTransformer() {
        // XXX LUMA which end ???
        addSimpleUpdate("b", "cim:PowerTransformerEnd.b", "_EQ", false);
        addSimpleUpdate("r", "cim:PowerTransformerEnd.r", "_EQ", false);
        addSimpleUpdate("x", "cim:PowerTransformerEnd.x", "_EQ", false);
        addSimpleUpdate("g", "cim:PowerTransformerEnd.g", "_EQ", false);
        addSimpleUpdate("ratedU", "cim:PowerTransformerEnd.ratedU", "_EQ", false);
    }

}
