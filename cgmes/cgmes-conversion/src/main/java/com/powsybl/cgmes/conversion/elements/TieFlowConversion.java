/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingTerminalMapper;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class TieFlowConversion extends AbstractIdentifiedObjectConversion {

    public TieFlowConversion(PropertyBag tieFlow, Context context) {
        super(CgmesNames.TIE_FLOW, tieFlow, context);
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void convert() {
        String controlAreaId = p.getId(CgmesNames.CONTROL_AREA);
        Area area = context.network().getArea(controlAreaId);
        if (area == null) {
            context.ignored("Tie Flow", String.format("Tie Flow %s refers to a non-existing control area", p.getId(CgmesNames.TIE_FLOW)));
            return;
        }
        String terminalId = p.getId(CgmesNames.TERMINAL);
        boolean isAc = isConsideredAcTieFlow(terminalId);
        Boundary boundary = context.terminalMapping().findBoundary(terminalId, context.cgmes());
        if (boundary != null) {
            area.newAreaBoundary()
                    .setAc(isAc)
                    .setBoundary(boundary)
                    .add();
            return;
        }
        RegulatingTerminalMapper.mapForTieFlow(terminalId, context)
                .ifPresent(t -> area.newAreaBoundary()
                        .setAc(isAc)
                        .setTerminal(t)
                        .add());
    }

    private boolean isConsideredAcTieFlow(String terminalId) {
        CgmesTerminal cgmesTerminal = context.cgmes().terminal(terminalId);
        String node = cgmesTerminal.topologicalNode() == null ? cgmesTerminal.connectivityNode() : cgmesTerminal.topologicalNode();
        boolean isDc = context.boundary().isHvdc(node);
        return !isDc;
    }
}
