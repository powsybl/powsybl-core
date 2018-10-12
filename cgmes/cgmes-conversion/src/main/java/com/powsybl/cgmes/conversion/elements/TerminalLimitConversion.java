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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TerminalLimitConversion extends AbstractIdentifiedObjectConversion {

    public TerminalLimitConversion(PropertyBag l, Conversion.Context context) {
        super("OperationalLimitSet", l, context);
        terminalId = l.getId("Terminal");
        terminal = context.terminalMapping().find(terminalId);
    }

    @Override
    public boolean valid() {
        if (terminal == null) {
            missing(String.format("Terminal %s", terminalId));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        String type = p.getLocal("operationalLimitTypeName");
        double value = p.asDouble("value");
        int terminalNumber = context.terminalMapping().number(terminalId);
        Connectable eq = terminal.getConnectable();
        boolean assigned = false;
        if (type.equals("PATL")) {
            if (value <= 0) {
                context.ignored("Operational limit", "value is <= 0");
            } else {
                // Enhancement: we should be able to use a CurrentLimitsAdder (an owner)
                // to avoid checking the class of the equipment
                // In terminal mapping insert a CurrentLimitsAdder instead of a Branch.side
                if (eq instanceof Branch) {
                    Branch b = (Branch) eq;
                    if (terminalNumber == 1) {
                        b.newCurrentLimits1().setPermanentLimit(value).add();
                        assigned = true;
                    } else if (terminalNumber == 2) {
                        b.newCurrentLimits2().setPermanentLimit(value).add();
                        assigned = true;
                    }
                }
            }
        }
        if (!assigned) {
            String reason = String.format(
                    "Not assigned for %s %s. Limit id, type, terminal : %s, %s, %s",
                    className(eq),
                    eq.getId(),
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
    private final Terminal terminal;
}
