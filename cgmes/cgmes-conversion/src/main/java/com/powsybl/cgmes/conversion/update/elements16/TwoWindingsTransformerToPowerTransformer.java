/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TwoWindingsTransformerToPowerTransformer extends IidmToCgmes {

    public TwoWindingsTransformerToPowerTransformer() {

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
