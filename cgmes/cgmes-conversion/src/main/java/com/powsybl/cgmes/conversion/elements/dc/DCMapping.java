/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.HashMap;
import java.util.Map;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

public class DCMapping {

    private final PropertyBags dcTerminals;
    private final PropertyBags dcNodes;
    private final Map<String, String> dcTerminalNodes = new HashMap<>();

    public DCMapping(Context context) {
        dcTerminals = context.cgmes().dcTerminals();
        dcNodes = context.cgmes().dcNodes();

        // Store the CGMES terminal to CGMES node association.
        String node = context.nodeBreaker() ? DC_NODE : DC_TOPOLOGICAL_NODE;
        dcTerminals.forEach(t -> dcTerminalNodes.put(t.getId(DC_TERMINAL), t.getId(node)));
    }

    public PropertyBags getDcNodes() {
        return dcNodes;
    }

    public String getDcNode(String dcTerminalId) {
        return dcTerminalNodes.get(dcTerminalId);
    }

    public boolean isConnected(String dcTerminalId) {
        PropertyBag terminalBag = dcTerminals.stream()
                .filter(b -> b.getId(CgmesNames.DC_TERMINAL).equals(dcTerminalId))
                .findFirst()
                .orElseThrow();
        return !terminalBag.containsKey("connected") || "true".equals(terminalBag.get("connected"));
    }
}
