/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class EquivalentBranchConversion extends AbstractConductingEquipmentConversion {

    public EquivalentBranchConversion(PropertyBag b, Conversion.Context context) {
        super("EquivalentBranch", b, context, 2);
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
            invalid("Has TopologicalNode on boundary");
            return false;
        }
        if (!p.containsKey("r") || !p.containsKey("x")) {
            invalid("No r,x attribures");
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double r21 = p.asDouble("r21", r);
        double x21 = p.asDouble("x21", x);
        if (r21 != r || x21 != x) {
            // r21 Notes:
            // Resistance from terminal sequence 2 to terminal sequence 1.
            // Used for steady state power flow.
            // Attribute is optional and represent unbalanced network such as off-nominal
            // phase
            // shifter.
            // If only EquivalentBranch.r is given,
            // then EquivalentBranch.r21 is assumed equal to EquivalentBranch.r.
            // Usage rule:
            // EquivalentBranch is a result of network reduction prior to the data exchange.
            invalid("Impedance 21 different of impedance 12 not supported");
        }
        double bch = 0;
        double gch = 0;

        final LineAdder adder = context.network().newLine()
                .setR(r)
                .setX(x)
                .setG1(gch / 2)
                .setG2(gch / 2)
                .setB1(bch / 2)
                .setB2(bch / 2);
        identify(adder);
        connect(adder);
        Line l = adder.add();
        convertedTerminals(l.getTerminal1(), l.getTerminal2());
    }
}
