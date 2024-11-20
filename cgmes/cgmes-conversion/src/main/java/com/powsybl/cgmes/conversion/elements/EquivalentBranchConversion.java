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
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.triplestore.api.PropertyBag;
import org.apache.commons.math3.complex.Complex;

import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EquivalentBranchConversion extends AbstractBranchConversion implements EquipmentAtBoundaryConversion {

    private DanglingLine danglingLine;

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
        double gch = 0;
        double bch = 0;
        convertBranch(r, x, gch, bch);
        updateParametersForEquivalentBranchWithDifferentNominalVoltages();
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
    public Optional<DanglingLine> getDanglingLine() {
        return Optional.ofNullable(danglingLine);
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
        danglingLine = convertToDanglingLine(eqInstance, boundarySide, r, x, gch, bch);
    }

    private void updateParametersForEquivalentBranchWithDifferentNominalVoltages() {
        // We are going to adapt the parameters of the converted equipment,
        // so it can be interpreted by the rest of PowSyBl as a Line (not a transformer)
        // When normalizing its characteristics for power flow,
        // we will need to introduce a ratio in the branch expressed in pu
        // (OLF already does this, so no change is required).
        // Also, flows computed for this equipment as a Line will be correct in engineering units,
        // so no additional change is required in the utils inside IIDM

        // equivalent branches inside a voltage level with zero impedance are converted to switches
        // equivalent branches with impedance are converted to lines
        // equivalent branches with zero impedance between voltage levels are also converted to lines
        Line line = context.network().getLine(iidmId());
        if (line == null) {
            return;
        }

        double vnom1 = line.getTerminal1().getVoltageLevel().getNominalV();
        double vnom2 = line.getTerminal2().getVoltageLevel().getNominalV();
        if (vnom1 == vnom2) {
            return;
        }
        // Base voltage reference is required for equivalent branches
        // And the base voltage must be defined,
        // So we can obtain directly its nominal voltage through a SPARQL Query
        double baseVoltage = p.asDouble("baseVoltageNominalVoltage");
        Complex ztr = new Complex(line.getR(), line.getX());
        Complex ytr = ztr.reciprocal();
        Complex y1 = new Complex(line.getG1(), line.getB1());
        Complex y2 = new Complex(line.getG2(), line.getB2());
        Complex ytrl;
        Complex y1l;
        Complex y2l;
        // Base voltage should be equal to one of the nominal voltages at line ends
        if (baseVoltage == vnom1) {
            // In the input CGMES model the ideal ratio between vnom1 and vnom2 is modelled at end2
            double ratio2 = vnom2 / vnom1;
            double ratio2Squared = ratio2 * ratio2;
            ytrl = ytr.multiply(1 / ratio2);
            y1l = ytr.multiply(1 - 1 / ratio2).add(y1);
            y2l = ytr.multiply(1 / ratio2Squared - 1 / ratio2).add(y2.divide(ratio2Squared));

        } else if (baseVoltage == vnom2) {
            // In the input CGMES model the ideal ratio between vnom1 and vnom2 is modelled at end1
            double ratio1 = vnom1 / vnom2;
            double ratio1Squared = ratio1 * ratio1;
            ytrl = ytr.multiply(1 / ratio1);
            y1l = ytr.multiply(1 / ratio1Squared - 1 / ratio1).add(y1.divide(ratio1Squared));
            y2l = ytr.multiply(1 - 1 / ratio1).add(y2);
        } else {
            context.ignored(
                    IGNORED_UPDATE_PARAMS_DIFFERENT_NOMINALV_WHAT + iidmId(),
                    "EquivalentBranch has been converted to a Line, but base voltage is different of nominal voltages of ends 1 and 2");
            return;
        }
        Complex ztrl = ytrl.reciprocal();
        line.setR(ztrl.getReal());
        line.setX(ztrl.getImaginary());
        line.setG1(y1l.getReal());
        line.setB1(y1l.getImaginary());
        line.setG2(y2l.getReal());
        line.setB2(y2l.getImaginary());
    }

    private static final String IGNORED_UPDATE_PARAMS_DIFFERENT_NOMINALV_WHAT =
            "EquivalentBranch potential parameter update if different nominal voltages ";
}
