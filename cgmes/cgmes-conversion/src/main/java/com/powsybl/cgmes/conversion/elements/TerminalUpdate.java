/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.*;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.*;

import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class TerminalUpdate extends AbstractIdentifiedObjectConversion {

    public TerminalUpdate(PropertyBag t, Context context) {
        super("Terminal", t, context);
    }

    @Override
    public boolean valid() {
        throw new ConversionException("Unsupported method");
    }

    @Override
    public void convert() {
        throw new ConversionException("Unsupported method");
    }

    @Override
    public void update(Network network) {
        Connectable<?> connectable = network.getConnectable(id);
        if (connectable == null) {
            return;
        }
        if (connectable.getId().equals("2b45d24b-b4dc-4690-bd4b-f2e08c804333")) {
            System.err.printf("DanglingLine encontrada %s %n", connectable.getId());
            System.err.printf("%s %n", p.tabulateLocals());
        }
        updateTerminalConnectedStatusAndFlow(connectable);
    }

    private void updateTerminalConnectedStatusAndFlow(Connectable<?> connectable) {
        Terminal terminal = findTerminal(connectable, id);
        if (terminal == null) {
            throw new ConversionException("Unexpected terminal: " + id);
        }
        PowerFlow f = new PowerFlow(p, "p", "q");
        boolean connectedInUpdate = p.asBoolean("connected", true);

        if (terminal.isConnected() != connectedInUpdate) {
            if (connectedInUpdate) {
                terminal.connect();
            } else {
                terminal.disconnect();
            }
        }
        if (f.defined() && setPQAllowed(terminal)) {
            terminal.setP(f.p());
            terminal.setQ(f.q());
        }
    }

    private static Terminal findTerminal(Connectable<?> connectable, String terminalId) {
        for (int k = 0; k < connectable.getTerminals().size(); k++) {
            String alias = connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + (k + 1)).orElse(null);
            if (alias != null && alias.equals(terminalId)) {
                return connectable.getTerminals().get(k);
            } else {
                alias = connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_BOUNDARY).orElse(null);
                if (alias != null && alias.equals(terminalId)) {
                    return connectable.getTerminals().get(k);
                }
            }
        }
        return null;
    }

    private boolean setPQAllowed(Terminal t) {
        return t.getConnectable().getType() != IdentifiableType.BUSBAR_SECTION;
    }
}
