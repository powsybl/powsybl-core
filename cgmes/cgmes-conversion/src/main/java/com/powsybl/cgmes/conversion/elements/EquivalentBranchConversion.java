/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class EquivalentBranchConversion extends AbstractBranchConversion implements EquipmentAtBoundaryConversion {

    public EquivalentBranchConversion(PropertyBag b, Context context) {
        super("EquivalentBranch", b, context);
    }

    @Override
    public boolean valid() {
        // We check only that we have valid nodes because we can find equivalent branches with one end at boundary
        return validNodes();
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
        convertEquivalentBranch();
    }

    @Override
    public void convertAtBoundary() {
        if (isBoundary(1)) {
            convertEquivalentBranchAtBoundary(1);
        } else if (isBoundary(2)) {
            convertEquivalentBranchAtBoundary(2);
        } else {
            throw new ConversionException("Boundary must be at one end of the equivalent branch");
        }
    }

    @Override
    public BoundaryLine asBoundaryLine(String boundaryNode) {
        BoundaryLine boundaryLine = super.createBoundaryLine(boundaryNode);
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        boundaryLine.setParameters(r, x, 0.0, 0.0);
        return boundaryLine;
    }

    private void convertEquivalentBranch() {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
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
        addAliasesAndProperties(l);
        convertedTerminals(l.getTerminal1(), l.getTerminal2());
    }

    private void convertEquivalentBranchAtBoundary(int boundarySide) {
        // If we have created buses and substations for boundary nodes,
        // convert as a regular line
        if (context.config().convertBoundary()) {
            convertEquivalentBranch();
        } else {
            double r = p.asDouble("r");
            double x = p.asDouble("x");
            double bch = 0;
            double gch = 0;
            convertToDanglingLine(boundarySide, r, x, gch, bch);
        }
    }
}
