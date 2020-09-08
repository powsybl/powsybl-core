/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesContainer;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SwitchConversion extends AbstractConductingEquipmentConversion {

    public SwitchConversion(PropertyBag sw, Context context) {
        super("Switch", sw, context, 2);
    }

    @Override
    public boolean valid() {
        // super.valid checks nodes and voltage levels of all terminals
        // We may encounter boundary switches that do not have voltage level at boundary terminal
        // So we check only that we have valid nodes
        if (!validNodes()) {
            return false;
        }
        if (busId(1).equals(busId(2))) {
            ignored("end buses are the same bus " + busId(1));
            return false;
        }
        if ((isBoundary(1) || isBoundary(2)) && LOG.isWarnEnabled()) {
            LOG.warn("Switch {} has at least one end in the boundary", id);
            LOG.warn("    busId1, voltageLevel1 : {} {}", busId(1), voltageLevel(1));
            LOG.warn("    side 1 is boundary    : {}", isBoundary(1));
            LOG.warn("    busId2, voltageLevel2 : {} {}", busId(2), voltageLevel(2));
            LOG.warn("    side 2 is boundary    : {}", isBoundary(2));
        }
        return true;
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

    @Override
    public void convert() {
        if (isBoundary(1)) {
            convertSwitchAtBoundary(1);
        } else if (isBoundary(2)) {
            convertSwitchAtBoundary(2);
        } else {
            convertToSwitch();
        }
    }

    private void convertSwitchAtBoundary(int boundarySide) {
        if (context.config().convertBoundary()) {
            convertToSwitch();
        } else {
            warnDanglingLineCreated();
            convertToDanglingLine(boundarySide);
        }
    }

    private void convertToSwitch() {
        boolean normalOpen = p.asBoolean("normalOpen", false);
        boolean open = p.asBoolean("open", normalOpen);
        if (convertToLowImpedanceLine()) {
            warnLowImpedanceLineCreated();
            LineAdder adder = context.network().newLine().setR(context.config().lowImpedanceLineR())
                    .setX(context.config().lowImpedanceLineX()).setG1(0).setB1(0).setG2(0).setB2(0);
            identify(adder);
            boolean branchIsClosed = !open;
            connect(adder, terminalConnected(1), terminalConnected(2), branchIsClosed);
            Line line = adder.add();
            addAliases(line);
            convertedTerminals(line.getTerminal1(), line.getTerminal2());
        } else {
            Switch s;
            if (context.nodeBreaker()) {
                VoltageLevel.NodeBreakerView.SwitchAdder adder;
                adder = voltageLevel().getNodeBreakerView().newSwitch().setKind(kind());
                identify(adder);
                connect(adder, open);
                s = adder.add();
            } else {
                VoltageLevel.BusBreakerView.SwitchAdder adder;
                adder = voltageLevel().getBusBreakerView().newSwitch();
                identify(adder);
                connect(adder, open);
                s = adder.add();
            }
            addAliases(s);
        }
    }

    // FIXME(Luma) Most of this code is duplicated with ACLineSegmentConversion
    // Could be shared by equipments with two terminals (not branches)
    // Only difference is that branches will provide r, x, g, b
    // and for switches we have all these values = 0
    private void convertToDanglingLine(int boundarySide) {
        // Non-boundary side (other side) of the line
        int modelSide = 3 - boundarySide;
        String boundaryNode = nodeId(boundarySide);

        // check again boundary node is correct
        assert isBoundary(boundarySide) && !isBoundary(modelSide);

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

        double r = 0;
        double x = 0;
        double bch = 0;
        double gch = 0;
        DanglingLineAdder dlAdder = voltageLevel(modelSide).newDanglingLine()
                .setEnsureIdUnicity(false)
                .setR(r)
                .setX(x)
                .setG(gch)
                .setB(bch)
                .setUcteXnodeCode("FIXME");
        identify(dlAdder);
        connect(dlAdder, modelSide);
        EquivalentInjectionConversion equivalentInjectionConversion = getEquivalentInjectionConversionForDanglingLine(boundaryNode);
        DanglingLine dl;
        if (equivalentInjectionConversion != null) {
            dl = equivalentInjectionConversion.convertOverDanglingLine(dlAdder, f);
            equivalentInjectionConversion.convertReactiveLimits(dl.getGeneration());
        } else {
            dl = dlAdder.setP0(f.p())
                    .setQ0(f.q())
                    .newGeneration()
                        .setTargetP(0.0)
                        .setTargetQ(0.0)
                        .setTargetV(Double.NaN)
                        .setVoltageRegulationOn(false)
                    .add()
                    .add();
        }
        addAliases(dl);
        context.convertedTerminal(terminalId(modelSide), dl.getTerminal(), 1, powerFlow(modelSide));
        dl.addAlias(topologicalNodeId(boundarySide), CgmesNames.TOPOLOGICAL_NODE);

        // If we do not have power flow at model side of the switch
        // we can assign it directly without calculation
        // we do not have impedance on the switch
        // Flow out from the switch (dangling line)
        // must be equal to the consumption seen at boundary
        if (context.config().computeFlowsAtBoundaryDanglingLines()
                && terminalConnected(modelSide)
                && !powerFlow(modelSide).defined()) {
            double p = dl.getP0() - dl.getGeneration().getTargetP();
            double q = dl.getQ0() - dl.getGeneration().getTargetQ();
            dl.getTerminal().setP(p);
            dl.getTerminal().setQ(q);
        }
    }

    // FIXME(Luma) this method is duplicated with ACLineSegmentConversion
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

    private SwitchKind kind() {
        String type = p.getLocal("type").toLowerCase();
        if (type.contains("breaker")) {
            return SwitchKind.BREAKER;
        } else if (type.contains("disconnector")) {
            return SwitchKind.DISCONNECTOR;
        } else if (type.contains("loadbreak")) {
            return SwitchKind.LOAD_BREAK_SWITCH;
        }
        return SwitchKind.BREAKER;
    }

    private String switchVoltageLevelId() {
        CgmesContainer container = context.cgmes().container(p.getId("EquipmentContainer"));
        if (container == null) {
            LOG.error("Missing equipment container for switch {} {}", id, name);
        }
        return container == null ? null : container.voltageLevel();
    }

    private boolean convertToLowImpedanceLine() {
        String vl = switchVoltageLevelId();
        return !cgmesVoltageLevelId(1).equals(vl) || !cgmesVoltageLevelId(2).equals(vl);
    }

    private void warnLowImpedanceLineCreated() {
        Supplier<String> reason = () -> String.format(
                "Connected to a terminal not in the same voltage level %s (side 1: %s, side 2: %s)",
                switchVoltageLevelId(), cgmesVoltageLevelId(1), cgmesVoltageLevelId(2));
        fixed("Low impedance line", reason);
    }

    private void warnDanglingLineCreated() {
        fixed("Dangling line with low impedance", "Connected to a boundary node");
    }

    private static final Logger LOG = LoggerFactory.getLogger(SwitchConversion.class);
}
