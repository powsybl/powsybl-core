package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

public class TwoWindingsTransformerToPowerTransformer extends IidmToCgmes {

    public TwoWindingsTransformerToPowerTransformer() {
        ignore("p1");
        ignore("q1");
        ignore("p2");
        ignore("q2");

        // These are examples of not-so-simple updates where
        // we have to find a CGMES sub-object related to IIDM main object
        // From the transformer, we have to find to proper transformer end
        unsupported("r", "cim:PowerTransformerEnd.r", CgmesSubset.EQUIPMENT);
        unsupported("x", "cim:PowerTransformerEnd.x", CgmesSubset.EQUIPMENT);
        unsupported("g", "cim:PowerTransformerEnd.g", CgmesSubset.EQUIPMENT);
        unsupported("b", "cim:PowerTransformerEnd.b", CgmesSubset.EQUIPMENT);
        unsupported("ratedU1", "cim:PowerTransformerEnd.ratedU", CgmesSubset.EQUIPMENT);
        unsupported("ratedU2", "cim:PowerTransformerEnd.ratedU", CgmesSubset.EQUIPMENT);
    }

    private void unsupported(String attribute, String predicate, CgmesSubset subset) {
        super.unsupported("TwoWindingsTransformer", attribute, predicate, subset);
    }
}
