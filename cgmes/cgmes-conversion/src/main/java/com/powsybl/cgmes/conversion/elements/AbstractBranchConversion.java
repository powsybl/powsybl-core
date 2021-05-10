/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public abstract class AbstractBranchConversion extends AbstractConductingEquipmentConversion {

    public AbstractBranchConversion(
            String type,
            PropertyBag p,
            Context context) {
        super(type, p, context, 2);
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        String node1 = nodeId(1);
        String node2 = nodeId(2);
        if (context.boundary().containsNode(node1)
                || context.boundary().containsNode(node2)) {
            invalid("Has " + nodeIdPropertyName() + " on boundary");
            return false;
        }
        if (!p.containsKey("r") || !p.containsKey("x")) {
            invalid("No r,x attributes");
            return false;
        }
        return true;
    }
}
