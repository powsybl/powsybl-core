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
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SwitchConversion extends AbstractConductingEquipmentConversion {

    public SwitchConversion(PropertyBag sw, Context context) {
        super("Switch", sw, context, 2);
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        if (busId(1).equals(busId(2))) {
            ignored(String.format("end buses are the same bus %s", busId(1)));
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
        if (convertToLowImpedanceLine()) {
            warnLowImpedanceLineCreated();
            LineAdder adder = context.network().newLine()
                    .setR(context.config().lowImpedanceLineR())
                    .setX(context.config().lowImpedanceLineX())
                    .setG1(0)
                    .setB1(0)
                    .setG2(0)
                    .setB2(0);
            identify(adder);
            connect(adder, terminalConnected(1) && !open, terminalConnected(2) && !open);
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
        }
        return SwitchKind.BREAKER;
    }

    private String switchVoltageLevelId() {
        return context.cgmes().container(p.getId("EquipmentContainer")).voltageLevel();
    }

    private boolean convertToLowImpedanceLine() {
        String vl = switchVoltageLevelId();
        String vl1 = cgmesVoltageLevelId(1);
        String vl2 = cgmesVoltageLevelId(2);
        if (this.id.startsWith("_8e56ead3") && LOG.isInfoEnabled()) {
            LOG.info("voltage levels for switch {}", this.id);
            LOG.info("    vl  : {}", vl);
            LOG.info("    vl1 : {}", vl1);
            LOG.info("    vl2 : {}", vl2);
            LOG.info("    cn1 : {}", nodeId(1));
            LOG.info("    cn2 : {}", nodeId(2));
            LOG.info("");
        }
        return !cgmesVoltageLevelId(1).equals(vl) || !cgmesVoltageLevelId(2).equals(vl);
    }

    private void warnLowImpedanceLineCreated() {
        String reason = String.format(
                "Connected to a terminal not in the same voltage level %s (side 1: %s, side 2: %s)",
                switchVoltageLevelId(),
                cgmesVoltageLevelId(1),
                cgmesVoltageLevelId(2));
        fixed("Low impedance line", reason);
    }

    private static final Logger LOG = LoggerFactory.getLogger(SwitchConversion.class);
}
