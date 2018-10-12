/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.Line;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SwitchConversion extends AbstractConductingEquipmentConversion {

    public SwitchConversion(PropertyBag sw, Conversion.Context context) {
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
            Line line = context.network().newLine()
                    .setId(iidmId())
                    .setName(iidmName())
                    .setEnsureIdUnicity(false)
                    .setVoltageLevel1(iidmVoltageLevelId(1))
                    .setVoltageLevel2(iidmVoltageLevelId(2))
                    .setBus1(terminalConnected(1) && !open ? busId(1) : null)
                    .setConnectableBus1(busId(1))
                    .setBus2(terminalConnected(2) && !open ? busId(2) : null)
                    .setConnectableBus2(busId(2))
                    .setR(context.config().lowImpedanceLineR())
                    .setX(context.config().lowImpedanceLineX())
                    .setG1(0)
                    .setB1(0)
                    .setG2(0)
                    .setB2(0)
                    .add();
            convertedTerminals(line.getTerminal1(), line.getTerminal2());
        } else {
            voltageLevel().getBusBreakerView().newSwitch()
                    .setId(iidmId())
                    .setName(iidmName())
                    .setEnsureIdUnicity(false)
                    .setBus1(busId(1))
                    .setBus2(busId(2))
                    .setOpen(open || !terminalConnected(1) || !terminalConnected(2))
                    .add();
        }
    }

    private String switchVoltageLevelId() {
        return p.getId("VoltageLevel");
    }

    private boolean convertToLowImpedanceLine() {
        String vl = switchVoltageLevelId();
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
