/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedanceAdder;
import com.powsybl.triplestore.api.PropertyBag;
import org.apache.commons.math3.complex.Complex;

import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EquivalentBranchConversion extends AbstractBranchConversion implements EquipmentAtBoundaryConversion {

    private BoundaryLine boundaryLine;

    public EquivalentBranchConversion(PropertyBag b, Context context) {
        super(CgmesNames.EQUIVALENT_BRANCH, b, context);
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
        // r21/x21 are optional and default to r/x; a reduced network equivalent may be non-reciprocal
        double r21 = p.asDouble("r21", r);
        double x21 = p.asDouble("x21", x);
        boolean nonReciprocal = r21 != r || x21 != x;
        if (nonReciprocal && (r == 0 && x == 0 || r21 == 0 && x21 == 0)) {
            invalid("Zero impedance on one side only not supported, impedance 21 considered equal to impedance 12");
            nonReciprocal = false;
        }
        double gch = 0;
        double bch = 0;
        convertBranch(r, x, gch, bch, CgmesNames.EQUIVALENT_BRANCH);
        // equivalent branches inside a voltage level with zero impedance are converted to switches
        // equivalent branches with impedance are converted to lines
        // equivalent branches with zero impedance between voltage levels are also converted to lines
        Line line = context.network().getLine(iidmId());
        if (line == null) {
            return;
        }
        Complex z21 = updateParametersForEquivalentBranchWithDifferentNominalVoltages(line, new Complex(r21, x21));
        if (nonReciprocal) {
            line.newExtension(LineNonReciprocalImpedanceAdder.class)
                    .withR21(z21.getReal())
                    .withX21(z21.getImaginary())
                    .add();
        }
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
    public Optional<BoundaryLine> getBoundaryLine() {
        return Optional.ofNullable(boundaryLine);
    }

    private void convertEquivalentBranchAtBoundary(int boundarySide) {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double bch = 0;
        double gch = 0;
        // When the parameter convertBoundary is active:
        // The boundary nodes are processed and included in the Network with a fictitious substation.
        // The branches that lie on boundaries can be mapped to the Network directly in the ::convert method.
        // So, this method should never be invoked when convertBoundary is active.
        if (context.config().convertBoundary()) {
            String message = "When convertBoundary is active, boundaries have been mapped to fictitious substations inside the Network." +
                    "This method should not be called, the mapping has already been performed in ::convert";
            throw new PowsyblException(message);
        }
        String eqInstance = p.get("graph");
        boundaryLine = convertToBoundaryLine(eqInstance, boundarySide, r, x, gch, bch, CgmesNames.EQUIVALENT_BRANCH);
    }

    /**
     * Fold the ideal ratio between the two nominal voltages into the line parameters, so the rest of PowSyBl can interpret
     * the equipment as a Line (not a transformer). The ratio is modelled in CGMES at the end whose nominal voltage differs
     * from the base voltage of the equivalent branch. Returns the side 2 impedance transformed the same way.
     */
    private Complex updateParametersForEquivalentBranchWithDifferentNominalVoltages(Line line, Complex z21) {
        double vnom1 = line.getTerminal1().getVoltageLevel().getNominalV();
        double vnom2 = line.getTerminal2().getVoltageLevel().getNominalV();
        if (vnom1 == vnom2) {
            return z21;
        }
        // Base voltage reference is required for equivalent branches
        // And the base voltage must be defined,
        // So we can obtain directly its nominal voltage through a SPARQL Query
        double baseVoltage = p.asDouble("baseVoltageNominalVoltage");
        Complex ytr = new Complex(line.getR(), line.getX()).reciprocal();
        Complex ytr21 = z21.reciprocal();
        Complex y1 = new Complex(line.getG1(), line.getB1());
        Complex y2 = new Complex(line.getG2(), line.getB2());
        Complex ytrl;
        Complex ytrl21;
        Complex y1l;
        Complex y2l;
        // Base voltage should be equal to one of the nominal voltages at line ends.
        // The series admittance of each side carries the ratio; the shunt of a side absorbs the difference between the
        // diagonal admittance term of that side and its series admittance.
        if (baseVoltage == vnom1) {
            // In the input CGMES model the ideal ratio between vnom1 and vnom2 is modelled at end2
            double ratio2 = vnom2 / vnom1;
            double ratio2Squared = ratio2 * ratio2;
            ytrl = ytr.multiply(1 / ratio2);
            ytrl21 = ytr21.multiply(1 / ratio2);
            y1l = ytr.multiply(1 - 1 / ratio2).add(y1);
            y2l = ytr21.multiply(1 / ratio2Squared - 1 / ratio2).add(y2.divide(ratio2Squared));
        } else if (baseVoltage == vnom2) {
            // In the input CGMES model the ideal ratio between vnom1 and vnom2 is modelled at end1
            double ratio1 = vnom1 / vnom2;
            double ratio1Squared = ratio1 * ratio1;
            ytrl = ytr.multiply(1 / ratio1);
            ytrl21 = ytr21.multiply(1 / ratio1);
            y1l = ytr.multiply(1 / ratio1Squared - 1 / ratio1).add(y1.divide(ratio1Squared));
            y2l = ytr21.multiply(1 - 1 / ratio1).add(y2);
        } else {
            context.ignored(
                    IGNORED_UPDATE_PARAMS_DIFFERENT_NOMINALV_WHAT + iidmId(),
                    "EquivalentBranch has been converted to a Line, but base voltage is different of nominal voltages of ends 1 and 2");
            return z21;
        }
        Complex ztrl = ytrl.reciprocal();
        line.setR(ztrl.getReal());
        line.setX(ztrl.getImaginary());
        line.setG1(y1l.getReal());
        line.setB1(y1l.getImaginary());
        line.setG2(y2l.getReal());
        line.setB2(y2l.getImaginary());
        return ytrl21.reciprocal();
    }

    public static void update(Line line, Context context) {
        updateBranch(line, context);
    }

    public static void update(Switch sw, Context context) {
        updateBranch(sw, context);
    }

    public static void update(BoundaryLine boundaryLine, Context context) {
        updateBoundaryLine(boundaryLine, isBoundaryTerminalConnected(boundaryLine, context), context);
    }

    private static final String IGNORED_UPDATE_PARAMS_DIFFERENT_NOMINALV_WHAT =
            "EquivalentBranch potential parameter update if different nominal voltages ";
}
