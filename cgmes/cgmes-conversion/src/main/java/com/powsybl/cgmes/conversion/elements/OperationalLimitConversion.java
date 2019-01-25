/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class OperationalLimitConversion extends AbstractIdentifiedObjectConversion {

    public OperationalLimitConversion(PropertyBag l, Conversion.Context context) {
        super("OperationalLimitSet", l, context);
        // Limit can associated to a Terminal or to an Equipment
        terminalId = l.getId("Terminal");
        if (terminalId != null) {
            terminal = context.terminalMapping().find(terminalId);
        }
        equipmentId = l.getId("Equipment");
        if (equipmentId != null) {
            // The equipment may be a Branch, a Dangling line, a Switch ...
            branch = context.network().getBranch(equipmentId);
            if (branch == null) {
                danglingLine = context.network().getDanglingLine(equipmentId);
                if (danglingLine == null) {
                    aswitch = context.network().getSwitch(equipmentId);
                }
            }
        }
    }

    @Override
    public boolean valid() {
        if (terminal == null && branch == null && danglingLine == null && aswitch == null) {
            missing(String.format("Terminal %s or Equipment %s", terminalId, equipmentId));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        // Permanent Admissible Transmission Loading (PATL)
        if (isPatl()) {
            convertPatl();
        }
        // ... other types of limits should go here
    }

    private boolean isPatl() {
        String limitTypeName = p.getLocal("operationalLimitTypeName");
        String limitType = p.getLocal("limitType");
        return limitTypeName.equals("PATL") || "LimitTypeKind.patl".equals(limitType);
    }

    private void convertPatl() {
        double value = p.asDouble("value");
        if (value <= 0) {
            context.ignored("Operational limit", "PATL value is <= 0");
            return;
        }
        String limitSubclass = p.getLocal("OperationalLimitSubclass");
        if (limitSubclass == null || limitSubclass.equals("CurrentLimit")) {
            convertPatlCurrent(value);
        } else if (limitSubclass.equals("ApparentPowerLimit")) {
            notAssigned();
        } else {
            notAssigned();
        }
    }

    private void convertPatlCurrent(double value) {
        // Enhancement: we should be able to use a CurrentLimitsAdder (an owner)
        // to avoid checking the class of the equipment
        // In terminal mapping insert a CurrentLimitsAdder instead of a Branch.side
        if (terminal != null) {
            convertPatlCurrentTerminal(value);
        } else if (branch != null) {
            // We should reject the value if the branch is a PowerTransformer
            if (branch instanceof TwoWindingsTransformer) {
                context.ignored("CurrentLimit", "Defined for Equipment TwoWindingsTransformer. Should be defined for one Terminal of Two");
                notAssigned(branch);
            } else {
                // Set same limit at both sides
                branch.newCurrentLimits1().setPermanentLimit(value).add();
                branch.newCurrentLimits2().setPermanentLimit(value).add();
            }
        } else if (danglingLine != null) {
            danglingLine.newCurrentLimits().setPermanentLimit(value).add();
        } else if (aswitch != null) {
            notAssigned(aswitch);
        } else {
            notAssigned();
        }
    }

    private void convertPatlCurrentTerminal(double value) {
        Connectable<?> connectable = terminal.getConnectable();
        if (connectable instanceof Branch) {
            int terminalNumber = context.terminalMapping().number(terminalId);
            Branch<?> b = (Branch<?>) connectable;
            if (terminalNumber == 1) {
                b.newCurrentLimits1().setPermanentLimit(value).add();
            } else if (terminalNumber == 2) {
                b.newCurrentLimits2().setPermanentLimit(value).add();
            } else {
                notAssigned(b);
            }
        } else if (connectable instanceof DanglingLine) {
            ((DanglingLine) connectable).newCurrentLimits().setPermanentLimit(value).add();
        } else {
            notAssigned(connectable);
        }
    }

    private void notAssigned() {
        notAssigned(null);
    }

    private void notAssigned(Identifiable<?> eq) {
        String type = p.getLocal("limitType");
        String typeName = p.getLocal("operationalLimitTypeName");
        String subclass = p.getLocal("OperationalLimitSubclass");
        String reason = String.format(
                "Not assigned for %s %s. Limit id, type, typeName, subClass, terminal : %s, %s, %s, %s, %s",
                eq != null ? className(eq) : "",
                eq != null ? eq.getId() : "",
                id,
                type,
                typeName,
                subclass,
                terminal);
        context.pending("Operational limit", reason);
    }

    private static String className(Identifiable<?> o) {
        String s = o.getClass().getName();
        int dot = s.lastIndexOf('.');
        if (dot >= 0) {
            s = s.substring(dot + 1);
        }
        s = s.replace("Impl", "");
        return s;
    }

    private final String terminalId;
    private Terminal terminal;

    private final String equipmentId;
    private Branch<?> branch;
    private DanglingLine danglingLine;
    private Switch aswitch;
}
