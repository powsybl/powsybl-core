package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

public class TwoWindingsTransformerToPowerTransformer extends IidmToCgmes {

    TwoWindingsTransformerToPowerTransformer() {
        // XXX LUMA which end ???
        addSimpleUpdate("b", "cim:PowerTransformerEnd.b", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("r", "cim:PowerTransformerEnd.r", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("x", "cim:PowerTransformerEnd.x", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("g", "cim:PowerTransformerEnd.g", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("ratedU", "cim:PowerTransformerEnd.ratedU", CgmesSubset.EQUIPMENT, false);
    }

}
