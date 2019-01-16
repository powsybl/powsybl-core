/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TerminalLimitConversion extends AbstractIdentifiedObjectConversion {

    public TerminalLimitConversion(PropertyBag l, Conversion.Context context) {
        super("OperationalLimitSet", l, context);
        // limit can associated to a terminal, a branch or a dangling line
        terminalId = l.getId("Terminal");
        if (terminalId != null) {
            terminal = context.terminalMapping().find(terminalId);
        }
        equipmentId = l.getId("Equipment");
        if (equipmentId != null) {
            branch = context.network().getBranch(equipmentId);
            if (branch == null) {
                danglingLine = context.network().getDanglingLine(equipmentId);
            }
        }
    }

    @Override
    public boolean valid() {
        if (terminal == null && branch == null && danglingLine == null) {
            missing(String.format("Terminal %s or Equipment %s", terminalId, equipmentId));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        String limitTypeName = p.getLocal("operationalLimitTypeName");
        String limitSubtype = p.getLocal("OperationalLimitSubclass");
        String limitType = p.getLocal("limitType");
        double value = p.asDouble("value");
        Identifiable eq = null;
        boolean assigned = false;
        if (limitTypeName.equals("PATL") || "LimitTypeKind.patl".equals(limitType)) {
            if (value <= 0) {
                context.ignored("Operational limit", "value is <= 0");
            } else {
                if (limitSubtype == null || limitSubtype.equals("CurrentLimit")) {
                    // Enhancement: we should be able to use a CurrentLimitsAdder (an owner)
                    // to avoid checking the class of the equipment
                    // In terminal mapping insert a CurrentLimitsAdder instead of a Branch.side
                    if (terminal != null) {
                        int terminalNumber = context.terminalMapping().number(terminalId);
                        Connectable connectable = terminal.getConnectable();
                        if (connectable instanceof Branch) {
                            Branch b = (Branch) connectable;
                            if (terminalNumber == 1) {
                                b.newCurrentLimits1().setPermanentLimit(value).add();
                                assigned = true;
                            } else if (terminalNumber == 2) {
                                b.newCurrentLimits2().setPermanentLimit(value).add();
                                assigned = true;
                            }
                        }
                        eq = connectable;
                    } else if (branch != null) {
                        // set limit at both sides
                        branch.newCurrentLimits1().setPermanentLimit(value).add();
                        branch.newCurrentLimits2().setPermanentLimit(value).add();
                        eq = branch;
                    } else if (danglingLine != null) {
                        danglingLine.newCurrentLimits().setPermanentLimit(value).add();
                        eq = danglingLine;
                    }
                } else if (limitSubtype.equals("ApparentPowerLimit")) {
                    // TODO
                } else if (limitSubtype.equals("VoltageLimit")) {
                    // TODO
                }
            }
        }
        if (!assigned) {
            String reason = String.format(
                    "Not assigned for %s %s. Limit id, type, terminal : %s, %s, %s",
                    eq != null ? className(eq) : "",
                    eq != null ? eq.getId() : "",
                    id,
                    type,
                    terminal);
            context.pending("Operational limit", reason);
        }
    }

    private static String className(Identifiable o) {
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
    private Branch branch;
    private DanglingLine danglingLine;
}
