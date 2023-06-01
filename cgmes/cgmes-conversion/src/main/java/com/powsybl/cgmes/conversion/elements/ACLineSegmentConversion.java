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
    public CgmesBoundaryLine asBoundaryLine(String boundaryNode) {
        CgmesBoundaryLine cgmesBoundaryLine = super.createBoundaryLine(boundaryNode);
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double g = p.asDouble("gch", 0);
        double b = p.asDouble("bch", 0);
        // Assign all (g, b) to the network side
        if (cgmesBoundaryLine.getBoundarySide().equals(Branch.Side.TWO)) {
            cgmesBoundaryLine.setParameters(r, x, g, b, 0, 0);
        } else {
            cgmesBoundaryLine.setParameters(r, x, 0, 0, g, b);
        }
        return cgmesBoundaryLine;
    }

    public boolean isConnectedAtBothEnds() {
        return terminalConnected(1) && terminalConnected(2);
    }

    public static void convertBoundaryLines(Context context, String boundaryNode, CgmesBoundaryLine cgmesBoundaryLine1, CgmesBoundaryLine cgmesBoundaryLine2) {

        TieLine mline = createTieLine(context, boundaryNode, cgmesBoundaryLine1, cgmesBoundaryLine2);

        mline.addAlias(cgmesBoundaryLine1.getModelTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1);
        mline.addAlias(cgmesBoundaryLine1.getBoundaryTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_1");
        mline.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_1", cgmesBoundaryLine1.getBoundaryTerminalId()); // TODO delete when aliases merging is handled
        mline.addAlias(cgmesBoundaryLine2.getModelTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 2);
        mline.addAlias(cgmesBoundaryLine2.getBoundaryTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_2");
        mline.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_2", cgmesBoundaryLine2.getBoundaryTerminalId()); // TODO delete when aliases merging is handled

        context.convertedTerminal(cgmesBoundaryLine1.getModelTerminalId(), mline.getBoundaryLine1().getTerminal(), 1, cgmesBoundaryLine1.getModelPowerFlow());
        context.convertedTerminal(cgmesBoundaryLine2.getModelTerminalId(), mline.getBoundaryLine2().getTerminal(), 2, cgmesBoundaryLine2.getModelPowerFlow());

        context.terminalMapping().add(cgmesBoundaryLine1.getBoundaryTerminalId(), mline.getBoundaryLine1().getBoundary(), 2);
        context.terminalMapping().add(cgmesBoundaryLine2.getBoundaryTerminalId(), mline.getBoundaryLine2().getBoundary(), 1);

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

            convertToBoundaryLine(boundarySide, r, x, gch, bch);
        }
    }

    private static TieLine createTieLine(Context context, String boundaryNode, CgmesBoundaryLine cgmesBoundaryLine1, CgmesBoundaryLine cgmesBoundaryLine2) {
        BoundaryLineAdder adder1 = context.network().getVoltageLevel(cgmesBoundaryLine1.getModelIidmVoltageLevelId())
                .newBoundaryLine()
                .setId(cgmesBoundaryLine1.getId())
                .setName(cgmesBoundaryLine1.getName())
                .setR(cgmesBoundaryLine1.getR())
                .setX(cgmesBoundaryLine1.getX())
                .setG(cgmesBoundaryLine1.getG1() + cgmesBoundaryLine1.getG2())
                .setB(cgmesBoundaryLine1.getB1() + cgmesBoundaryLine1.getB2())
                .setUcteXnodeCode(findUcteXnodeCode(context, boundaryNode));
        BoundaryLineAdder adder2 = context.network().getVoltageLevel(cgmesBoundaryLine2.getModelIidmVoltageLevelId())
                .newBoundaryLine()
                .setId(cgmesBoundaryLine2.getId())
                .setName(cgmesBoundaryLine2.getName())
                .setR(cgmesBoundaryLine2.getR())
                .setX(cgmesBoundaryLine2.getX())
                .setG(cgmesBoundaryLine2.getG1() + cgmesBoundaryLine2.getG2())
                .setB(cgmesBoundaryLine2.getB1() + cgmesBoundaryLine2.getB2())
                .setUcteXnodeCode(findUcteXnodeCode(context, boundaryNode));
        connect(context, adder1, cgmesBoundaryLine1.getModelBus(), cgmesBoundaryLine1.isModelTconnected(), cgmesBoundaryLine1.getModelNode());
        connect(context, adder2, cgmesBoundaryLine2.getModelBus(), cgmesBoundaryLine2.isModelTconnected(), cgmesBoundaryLine2.getModelNode());
        com.powsybl.iidm.network.BoundaryLine bl1 = adder1.add();
        com.powsybl.iidm.network.BoundaryLine bl2 = adder2.add();
        TieLineAdder adder = context.network().newTieLine()
                .setBoundaryLine1(bl1.getId())
                .setBoundaryLine2(bl2.getId());
        identify(context, adder, context.namingStrategy().getIidmId("TieLine", TieLineUtil.buildMergedId(cgmesBoundaryLine1.getId(), cgmesBoundaryLine2.getId())),
                TieLineUtil.buildMergedName(cgmesBoundaryLine1.getId(), cgmesBoundaryLine2.getId(), cgmesBoundaryLine1.getName(), cgmesBoundaryLine2.getName()));
        TieLine tieLine = adder.add();
        if (context.boundary().isHvdc(boundaryNode) || context.boundary().lineAtBoundary(boundaryNode) != null) {
            tieLine.newExtension(CgmesLineBoundaryNodeAdder.class)
                    .setHvdc(context.boundary().isHvdc(boundaryNode))
                    .setLineEnergyIdentificationCodeEic(context.boundary().lineAtBoundary(boundaryNode))
                    .add();
        }
        return tieLine;
    }
}
