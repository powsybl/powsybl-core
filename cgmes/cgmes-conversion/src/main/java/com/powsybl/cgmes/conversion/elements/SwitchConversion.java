/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class SwitchConversion extends AbstractConductingEquipmentConversion implements EquipmentAtBoundaryConversion {

    private DanglingLine danglingLine;

    public SwitchConversion(PropertyBag sw, Context context) {
        super(CgmesNames.SWITCH, sw, context, 2);
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
            LOG.warn("    busId1, voltageLevel1 : {} {}", busId(1), voltageLevel(1).orElse(null));
            LOG.warn("    side 1 is boundary    : {}", isBoundary(1));
            LOG.warn("    busId2, voltageLevel2 : {} {}", busId(2), voltageLevel(2).orElse(null));
            LOG.warn("    side 2 is boundary    : {}", isBoundary(2));
        }
        return true;
    }

    @Override
    public void convert() {
        convertToSwitch();
    }

    @Override
    public void convertAtBoundary() {
        if (isBoundary(1)) {
            convertSwitchAtBoundary(1);
        } else if (isBoundary(2)) {
            convertSwitchAtBoundary(2);
        } else {
            throw new ConversionException("Boundary must be at one end of the switch");
        }
    }

    @Override
    public Optional<DanglingLine> getDanglingLine() {
        return Optional.ofNullable(danglingLine);
    }

    private Switch convertToSwitch() {
        boolean normalOpen = p.asBoolean(CgmesNames.NORMAL_OPEN, false);
        Switch s;
        if (context.nodeBreaker()) {
            VoltageLevel.NodeBreakerView.SwitchAdder adder = voltageLevel().getNodeBreakerView().newSwitch().setKind(kind());
            identify(adder);
            connectWithOnlyEq(adder);
            boolean retained = p.asBoolean("retained", false);
            s = adder.setOpen(normalOpen).setRetained(retained).add();
        } else {
            VoltageLevel.BusBreakerView.SwitchAdder adder = voltageLevel().getBusBreakerView().newSwitch();
            identify(adder);
            connectWithOnlyEq(adder);
            s = adder.setOpen(normalOpen).add();
        }
        // Always preserve the original type
        addTypeAsProperty(s);
        addAliasesAndProperties(s);
        s.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_OPEN, String.valueOf(normalOpen));
        return s;
    }

    private void convertSwitchAtBoundary(int boundarySide) {
        if (context.config().convertBoundary()) {
            convertToSwitch().setRetained(true);
        } else {
            warnDanglingLineCreated();
            String eqInstance = p.get("graph");
            danglingLine = convertToDanglingLine(eqInstance, boundarySide, CgmesNames.SWITCH);
        }
    }

    private SwitchKind kind() {
        String type = p.getLocal("type");
        return switch (type) {
            case "Disconnector", "GroundDisconnector", "Jumper" -> SwitchKind.DISCONNECTOR;
            case "LoadBreakSwitch" -> SwitchKind.LOAD_BREAK_SWITCH;
            default -> SwitchKind.BREAKER;  // Breaker, Switch, ProtectedSwitch
        };
    }

    private void addTypeAsProperty(Switch s) {
        s.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, p.getLocal("type"));
    }

    private void warnDanglingLineCreated() {
        fixed("Dangling line with low impedance", "Connected to a boundary node");
    }

    public static void update(DanglingLine danglingLine, PropertyBag cgmesData, Context context) {
        updateTerminals(danglingLine, context, danglingLine.getTerminal());
        boolean isClosed = !cgmesData.asBoolean(CgmesNames.OPEN).orElse(defaultValue(defaultOpen(), context));
        updateTargetsAndRegulationAndOperationalLimits(danglingLine, isBoundaryTerminalConnected(danglingLine, context) && isClosed, context);
        computeFlowsOnModelSide(danglingLine, context);
    }

    private static DefaultValueBoolean defaultOpen() {
        return new DefaultValueBoolean(null, false, false, false);
    }

    public static void update(Switch sw, PropertyBag cgmesData, Context context) {
        boolean isOpenFromBothTerminalStatus = sw.getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER
                && getIsOpenFromBothTerminalStatus(sw, context).orElse(false);
        boolean isOpen = cgmesData.asBoolean(CgmesNames.OPEN).orElse(defaultValue(getDefaultIsOpen(sw), context));
        sw.setOpen(isOpen || isOpenFromBothTerminalStatus);
    }

    private static final Logger LOG = LoggerFactory.getLogger(SwitchConversion.class);
}
