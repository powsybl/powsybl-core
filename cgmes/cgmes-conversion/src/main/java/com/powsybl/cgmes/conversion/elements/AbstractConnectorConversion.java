/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.List;
import java.util.Optional;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DanglingLineAdder;
import com.powsybl.iidm.network.util.SV;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractConnectorConversion extends AbstractConductingEquipmentConversion {

    public AbstractConnectorConversion(
            String type,
            PropertyBag p,
            Context context) {
        super(type, p, context, 2);
    }

    public String boundaryNode() {
        // Only one of the end points can be in the boundary
        if (isBoundary(1)) {
            return nodeId(1);
        } else if (isBoundary(2)) {
            return nodeId(2);
        }
        return null;
    }

    public void convertToDanglingLine(int boundarySide) {
        convertToDanglingLine(boundarySide, 0.0, 0.0, 0.0, 0.0);
    }

    public void convertToDanglingLine(int boundarySide, double r, double x, double gch, double bch) {
        // Non-boundary side (other side) of the line
        int modelSide = 3 - boundarySide;
        String boundaryNode = nodeId(boundarySide);

        // check again boundary node is correct
        if (!isBoundary(boundarySide) || isBoundary(modelSide)) {
            throw new PowsyblException(String.format("Unexpected boundarySide and modelSide at boundaryNode: %s", boundaryNode));
        }

        PowerFlow f = new PowerFlow(0, 0);
        // Only consider potential power flow at boundary side if that side is connected
        if (terminalConnected(boundarySide) && context.boundary().hasPowerFlow(boundaryNode)) {
            f = context.boundary().powerFlowAtNode(boundaryNode);
        }
        // There should be some equipment at boundarySide to model exchange through that
        // point
        // But we have observed, for the test case conformity/miniBusBranch,
        // that the ACLineSegment:
        // _5150a037-e241-421f-98b2-fe60e5c90303 XQ1-N1
        // ends in a boundary node where there is no other line,
        // does not have energy consumer or equivalent injection
        if (terminalConnected(boundarySide)
            && !context.boundary().hasPowerFlow(boundaryNode)
            && context.boundary().equivalentInjectionsAtNode(boundaryNode).isEmpty()) {
            missing("Equipment for modeling consumption/injection at boundary node");
        }

        DanglingLineAdder dlAdder = voltageLevel(modelSide).newDanglingLine()
            .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
            .setR(r)
            .setX(x)
            .setG(gch)
            .setB(bch)
            .setUcteXnodeCode(findUcteXnodeCode(boundaryNode));
        identify(dlAdder);
        connect(dlAdder, modelSide);
        EquivalentInjectionConversion equivalentInjectionConversion = getEquivalentInjectionConversionForDanglingLine(
            boundaryNode);
        DanglingLine dl;
        if (equivalentInjectionConversion != null) {
            dl = equivalentInjectionConversion.convertOverDanglingLine(dlAdder, f);
            Optional.ofNullable(dl.getGeneration()).ifPresent(equivalentInjectionConversion::convertReactiveLimits);
        } else {
            dl = dlAdder
                    .setP0(f.p())
                    .setQ0(f.q())
                    .add();
        }
        context.terminalMapping().add(terminalId(boundarySide), dl.getBoundary().getTerminal(), 2);
        dl.addAlias(terminalId(boundarySide), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary");
        dl.addAlias(terminalId(boundarySide == 1 ? 2 : 1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Network");
        dl.addAlias(boundaryNode, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
        context.convertedTerminal(terminalId(modelSide), dl.getTerminal(), 1, powerFlow(modelSide));

        // If we do not have power flow at model side and we can compute it,
        // do it and assign the result at the terminal of the dangling line
        if (context.config().computeFlowsAtBoundaryDanglingLines()
            && terminalConnected(modelSide)
            && !powerFlow(modelSide).defined()
            && context.boundary().hasVoltage(boundaryNode)) {

            if (isZ0(dl)) {
                // Flow out must be equal to the consumption seen at boundary
                Optional<DanglingLine.Generation> generation = Optional.ofNullable(dl.getGeneration());
                dl.getTerminal().setP(dl.getP0() - generation.map(DanglingLine.Generation::getTargetP).orElse(0.0));
                dl.getTerminal().setQ(dl.getQ0() - generation.map(DanglingLine.Generation::getTargetQ).orElse(0.0));

            } else {
                setDanglingLineModelSideFlow(dl, boundaryNode);
            }
        }
    }

    private boolean isZ0(DanglingLine dl) {
        return dl.getR() == 0.0 && dl.getX() == 0.0 && dl.getG() == 0.0 && dl.getB() == 0.0;
    }

    private void setDanglingLineModelSideFlow(DanglingLine dl, String boundaryNode) {

        double v = context.boundary().vAtBoundary(boundaryNode);
        double angle = context.boundary().angleAtBoundary(boundaryNode);
        // The net sum of power flow "entering" at boundary is "exiting"
        // through the line, we have to change the sign of the sum of flows
        // at the node when we consider flow at line end
        Optional<DanglingLine.Generation> generation = Optional.ofNullable(dl.getGeneration());
        double p = dl.getP0() - generation.map(DanglingLine.Generation::getTargetP).orElse(0.0);
        double q = dl.getQ0() - generation.map(DanglingLine.Generation::getTargetQ).orElse(0.0);
        SV svboundary = new SV(-p, -q, v, angle);
        // The other side power flow must be computed taking into account
        // the same criteria used for ACLineSegment: total shunt admittance
        // is divided in 2 equal shunt admittance at each side of series impedance
        double g = dl.getG() / 2;
        double b = dl.getB() / 2;
        SV svmodel = svboundary.otherSide(dl.getR(), dl.getX(), g, b, g, b, 1);
        dl.getTerminal().setP(svmodel.getP());
        dl.getTerminal().setQ(svmodel.getQ());
    }

    private EquivalentInjectionConversion getEquivalentInjectionConversionForDanglingLine(String boundaryNode) {
        List<PropertyBag> eis = context.boundary().equivalentInjectionsAtNode(boundaryNode);
        if (eis.isEmpty()) {
            return null;
        } else if (eis.size() > 1) {
            // This should not happen
            // We have decided to create a dangling line,
            // so only one MAS at this boundary point,
            // so there must be only one equivalent injection
            invalid("Multiple equivalent injections at boundary node");
            return null;
        } else {
            return new EquivalentInjectionConversion(eis.get(0), context);
        }
    }
}
