/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.extensions.CgmesLineBoundaryNodeAdder;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ACLineSegmentConversion extends AbstractBranchConversion implements EquipmentAtBoundaryConversion {

    public ACLineSegmentConversion(PropertyBag line, Context context) {
        super(CgmesNames.AC_LINE_SEGMENT, line, context);
    }

    @Override
    public boolean valid() {
        // An AC line segment end voltage level may be null
        // (when it is in the boundary and the boundary nodes are not converted)
        // So we do not use the generic validity check for conducting equipment
        // or branch. We only ensure we have nodes at both ends
        for (int k = 1; k <= 2; k++) {
            if (nodeId(k) == null) {
                missing(nodeIdPropertyName() + k);
                return false;
            }
        }
        return true;
    }

    @Override
    public void convert() {
        convertLine();
    }

    @Override
    public void convertAtBoundary() {
        if (isBoundary(1)) {
            convertLineAtBoundary(1);
        } else if (isBoundary(2)) {
            convertLineAtBoundary(2);
        } else {
            throw new ConversionException("Boundary must be at one end of the line");
        }
    }

    @Override
    public BoundaryLine asBoundaryLine(String boundaryNode) {
        BoundaryLine boundaryLine = super.createBoundaryLine(boundaryNode);
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double g = p.asDouble("gch", 0);
        double b = p.asDouble("bch", 0);
        // Assign all (g, b) to the network side
        if (boundaryLine.getBoundarySide().equals(TwoSides.TWO)) {
            boundaryLine.setParameters(r, x, g, b, 0, 0);
        } else {
            boundaryLine.setParameters(r, x, 0, 0, g, b);
        }
        return boundaryLine;
    }

    public boolean isConnectedAtBothEnds() {
        return terminalConnected(1) && terminalConnected(2);
    }

    public static void convertBoundaryLines(Context context, String boundaryNode, BoundaryLine boundaryLine1, BoundaryLine boundaryLine2) {

        TieLine mline = createTieLine(context, boundaryNode, boundaryLine1, boundaryLine2);

        // TODO(Luma) We should leave the standard mechanism for attaching alias of terminal to the dangling line
        // Exchange with Florian: all this specific treatment should be gone,
        // it should be handled by the dangling line created in a standard ways
        mline.getDanglingLine1().addAlias(boundaryLine1.getModelTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1);
        mline.getDanglingLine1().addAlias(boundaryLine1.getBoundaryTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary");
        mline.getDanglingLine1().setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary", boundaryLine1.getBoundaryTerminalId()); // TODO delete when aliases merging is handled

        mline.getDanglingLine2().addAlias(boundaryLine2.getModelTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1);
        mline.getDanglingLine2().addAlias(boundaryLine2.getBoundaryTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary");
        mline.getDanglingLine2().setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary", boundaryLine2.getBoundaryTerminalId()); // TODO delete when aliases merging is handled

        context.convertedTerminal(boundaryLine1.getModelTerminalId(), mline.getDanglingLine1().getTerminal(), 1, boundaryLine1.getModelPowerFlow());
        context.convertedTerminal(boundaryLine2.getModelTerminalId(), mline.getDanglingLine2().getTerminal(), 2, boundaryLine2.getModelPowerFlow());

        context.terminalMapping().add(boundaryLine1.getBoundaryTerminalId(), mline.getDanglingLine1().getBoundary(), 2);
        context.terminalMapping().add(boundaryLine2.getBoundaryTerminalId(), mline.getDanglingLine2().getBoundary(), 1);

        context.namingStrategy().readIdMapping(mline, "TieLine"); // TODO: maybe this should be refined for merged line
    }

    private void convertLine() {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double gch = p.asDouble("gch", 0.0);
        double bch = p.asDouble("bch");
        convertBranch(r, x, gch, bch);
    }

    private void convertLineAtBoundary(int boundarySide) {
        // If we have created buses and substations for boundary nodes,
        // convert as a regular line
        if (context.config().convertBoundary()) {
            convertLine();
        } else {
            double r = p.asDouble("r");
            double x = p.asDouble("x");
            double gch = p.asDouble("gch", 0.0);
            double bch = p.asDouble("bch");

            convertToDanglingLine(boundarySide, r, x, gch, bch);
        }
    }

    private static TieLine createTieLine(Context context, String boundaryNode, BoundaryLine boundaryLine1, BoundaryLine boundaryLine2) {
        DanglingLineAdder adder1 = getDanglingLineAdder(context, boundaryNode, boundaryLine1);
        DanglingLineAdder adder2 = getDanglingLineAdder(context, boundaryNode, boundaryLine2);
        connect(context, adder1, boundaryLine1.getModelBus(), boundaryLine1.isModelTconnected(), boundaryLine1.getModelNode());
        connect(context, adder2, boundaryLine2.getModelBus(), boundaryLine2.isModelTconnected(), boundaryLine2.getModelNode());
        DanglingLine dl1 = adder1.add();
        DanglingLine dl2 = adder2.add();
        TieLineAdder adder = context.network().newTieLine()
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId());
        identify(context, adder, context.namingStrategy().getIidmId("TieLine", TieLineUtil.buildMergedId(boundaryLine1.getId(), boundaryLine2.getId())),
                TieLineUtil.buildMergedName(boundaryLine1.getId(), boundaryLine2.getId(), boundaryLine1.getName(), boundaryLine2.getName()));
        TieLine tieLine = adder.add();
        if (context.boundary().isHvdc(boundaryNode) || context.boundary().lineAtBoundary(boundaryNode) != null) {
            tieLine.newExtension(CgmesLineBoundaryNodeAdder.class)
                    .setHvdc(context.boundary().isHvdc(boundaryNode))
                    .setLineEnergyIdentificationCodeEic(context.boundary().lineAtBoundary(boundaryNode))
                    .add();
        }
        return tieLine;
    }

    private static DanglingLineAdder getDanglingLineAdder(Context context, String boundaryNode, BoundaryLine boundaryLine) {
        return context.network().getVoltageLevel(boundaryLine.getModelIidmVoltageLevelId())
                .newDanglingLine()
                .setId(boundaryLine.getId())
                .setName(boundaryLine.getName())
                // We consider p0 and q0 are equal to zero because there is no possible way to retrieve the corresponding
                // equivalent injection. Note that the equivalent injections of both sides are compensating each other
                // (the sum of their flows is supposed to be equal to zero).
                .setP0(0.0)
                .setQ0(0.0)
                .setR(boundaryLine.getR())
                .setX(boundaryLine.getX())
                .setG(boundaryLine.getG1() + boundaryLine.getG2())
                .setB(boundaryLine.getB1() + boundaryLine.getB2())
                .setPairingKey(findPairingKey(context, boundaryNode));
    }
}
