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
import com.powsybl.iidm.network.util.ReorientedBranchCharacteristics;
import com.powsybl.cgmes.extensions.CgmesLineBoundaryNodeAdder;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
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
        if (boundaryLine.getBoundarySide().equals(Branch.Side.TWO)) {
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

        Line mline = createTieLine(context, boundaryNode, boundaryLine1, boundaryLine2);

        mline.addAlias(boundaryLine1.getModelTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1);
        mline.addAlias(boundaryLine1.getBoundaryTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_1");
        mline.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_1", boundaryLine1.getBoundaryTerminalId()); // TODO delete when aliases merging is handled
        mline.addAlias(boundaryLine2.getModelTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 2);
        mline.addAlias(boundaryLine2.getBoundaryTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_2");
        mline.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_2", boundaryLine2.getBoundaryTerminalId()); // TODO delete when aliases merging is handled

        context.convertedTerminal(boundaryLine1.getModelTerminalId(), mline.getTerminal1(), 1, boundaryLine1.getModelPowerFlow());
        context.convertedTerminal(boundaryLine2.getModelTerminalId(), mline.getTerminal2(), 2, boundaryLine2.getModelPowerFlow());

        TieLine tl = (TieLine) mline;
        context.terminalMapping().add(boundaryLine1.getBoundaryTerminalId(), tl.getHalf1().getBoundary(), 2);
        context.terminalMapping().add(boundaryLine2.getBoundaryTerminalId(), tl.getHalf2().getBoundary(), 1);

        context.namingStrategy().readIdMapping(tl, "TieLine"); // TODO: maybe this should be refined for merged line
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

    // Before creating the TieLine the initial branches are reoriented if it is necessary,
    // then the setG1, setB1 and setG2, setB2 will be associated to the end1 and end2 of the reoriented branch
    private static Line createTieLine(Context context, String boundaryNode, BoundaryLine boundaryLine1, BoundaryLine boundaryLine2) {

        ReorientedBranchCharacteristics brp1 = new ReorientedBranchCharacteristics(boundaryLine1.getR(), boundaryLine1.getX(),
            boundaryLine1.getG1(), boundaryLine1.getB1(), boundaryLine1.getG2(), boundaryLine1.getB2(),
            isLine1Reoriented(boundaryLine1.getBoundarySide()));
        ReorientedBranchCharacteristics brp2 = new ReorientedBranchCharacteristics(boundaryLine2.getR(), boundaryLine2.getX(),
            boundaryLine2.getG1(), boundaryLine2.getB1(), boundaryLine2.getG2(), boundaryLine2.getB2(),
            isLine2Reoriented(boundaryLine2.getBoundarySide()));

        TieLineAdder adder = context.network().newTieLine()
            .newHalfLine1()
            .setId(boundaryLine1.getId())
            .setName(boundaryLine1.getName())
            .setR(brp1.getR())
            .setX(brp1.getX())
            .setG1(brp1.getG1())
            .setB1(brp1.getB1())
            .setG2(brp1.getG2())
            .setB2(brp1.getB2())
            .add()
            .newHalfLine2()
            .setId(boundaryLine2.getId())
            .setName(boundaryLine2.getName())
            .setR(brp2.getR())
            .setX(brp2.getX())
            .setG1(brp2.getG1())
            .setB1(brp2.getB1())
            .setG2(brp2.getG2())
            .setB2(brp2.getB2())
            .add()
            .setUcteXnodeCode(findUcteXnodeCode(context, boundaryNode));
        identify(context, adder, context.namingStrategy().getIidmId("TieLine", TieLineUtil.buildMergedId(boundaryLine1.getId(), boundaryLine2.getId())),
                TieLineUtil.buildMergedName(boundaryLine1.getId(), boundaryLine2.getId(), boundaryLine1.getName(), boundaryLine2.getName()));
        connect(context, adder, boundaryLine1.getModelIidmVoltageLevelId(), boundaryLine1.getModelBus(), boundaryLine1.isModelTconnected(),
            boundaryLine1.getModelNode(), boundaryLine2.getModelIidmVoltageLevelId(), boundaryLine2.getModelBus(),
            boundaryLine2.isModelTconnected(), boundaryLine2.getModelNode());
        TieLine tieLine = adder.add();
        if (context.boundary().isHvdc(boundaryNode) || context.boundary().lineAtBoundary(boundaryNode) != null) {
            tieLine.newExtension(CgmesLineBoundaryNodeAdder.class)
                    .setHvdc(context.boundary().isHvdc(boundaryNode))
                    .setLineEnergyIdentificationCodeEic(context.boundary().lineAtBoundary(boundaryNode))
                    .add();
        }
        return tieLine;
    }

    private static boolean isLine1Reoriented(Branch.Side boundarySide) {
        return boundarySide.equals(Branch.Side.ONE);
    }

    private static boolean isLine2Reoriented(Branch.Side boundarySide) {
        return boundarySide.equals(Branch.Side.TWO);
    }
}
