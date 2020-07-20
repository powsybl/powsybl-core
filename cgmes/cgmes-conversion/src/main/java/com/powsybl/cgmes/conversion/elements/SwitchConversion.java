/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesContainer;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DanglingLineAdder;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;

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
        // FIXME(Luma)
        // super.valid checks nodes and voltage levels of all terminals
        // boundary switches do not have voltage level of boundary terminal
        //        if (!super.valid()) {
        //            return false;
        //        }
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

    @Override
    public void convert() {
        boolean normalOpen = p.asBoolean("normalOpen", false);
        boolean open = p.asBoolean("open", normalOpen);
        if (isBoundary(1) || isBoundary(2)) {
            int networkTerminal = isBoundary(1) ? 2 : 1;
            int boundaryTerminal = 3 - networkTerminal;
            warnDanglingLineCreated();
            VoltageLevel vl = voltageLevel(networkTerminal);
            LOG.warn("    powerFlow at boundary terminal {} {}", powerFlow(boundaryTerminal).p(), powerFlow(boundaryTerminal).q());
            DanglingLineAdder dladder = vl.newDanglingLine()
                    .setR(0)
                    .setX(0)
                    .setG(0.0)
                    .setB(0)
                    // XXX LUMA should be filled with the equivalent injection
                    // XXX LUMA -powerFlow(boundaryTerminal) ???
                    .setP0(0)
                    .setQ0(0);
            identify(dladder);
            // XXX LUMA assume always closed
            boolean danglingLineFromSwitchIsClosed = !open;
            connect(dladder, networkTerminal /*, danglingLineFromSwitchIsClosed */);
            DanglingLine dline = dladder.add();
            context.convertedTerminal(terminalId(networkTerminal), dline.getTerminal(), networkTerminal, powerFlow(networkTerminal));
        } else if (convertToLowImpedanceLine()) {
            warnLowImpedanceLineCreated();
            LineAdder adder = context.network().newLine()
                    .setR(context.config().lowImpedanceLineR())
                    .setX(context.config().lowImpedanceLineX())
                    .setG1(0)
                    .setB1(0)
                    .setG2(0)
                    .setB2(0);
            identify(adder);
            boolean branchIsClosed = !open;
            connect(adder, terminalConnected(1), terminalConnected(2), branchIsClosed);
            Line line = adder.add();
            convertedTerminals(line.getTerminal1(), line.getTerminal2());
        } else {
            if (context.nodeBreaker()) {
                VoltageLevel.NodeBreakerView.SwitchAdder adder;
                adder = voltageLevel().getNodeBreakerView().newSwitch()
                        .setKind(kind());
                identify(adder);
                connect(adder, open);
                adder.add();
            } else {
                VoltageLevel.BusBreakerView.SwitchAdder adder;
                adder = voltageLevel().getBusBreakerView().newSwitch();
                identify(adder);
                connect(adder, open);
                adder.add();
            }
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
                switchVoltageLevelId(),
                cgmesVoltageLevelId(1),
                cgmesVoltageLevelId(2));
        fixed("Low impedance line", reason);
    }

    private void warnDanglingLineCreated() {
        fixed("Dangling line with low impedance", "Connected to a boundary node");
    }

    private static final Logger LOG = LoggerFactory.getLogger(SwitchConversion.class);
}
