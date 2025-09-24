/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

public abstract class AbstractDCConductingEquipmentConversion extends AbstractIdentifiedObjectConversion {

    int numDcTerminals;
    String dcTerminal1;
    String dcTerminal2;
    String dcNode1;
    String dcNode2;
    boolean dcConnected1;
    boolean dcConnected2;

    protected AbstractDCConductingEquipmentConversion(String type, PropertyBag p, Context context, int numDcTerminals) {
        super(type, p, context);

        this.numDcTerminals = numDcTerminals;
        if (numDcTerminals < 1 || numDcTerminals > 2) {
            throw new IllegalArgumentException("Number of DC terminals must be 1 or 2 for dc equipment: " + id);
        }

        dcTerminal1 = p.getId(CgmesNames.DC_TERMINAL1);
        dcNode1 = context.dcMapping().getDcNode(dcTerminal1);
        dcConnected1 = context.dcMapping().isConnected(dcTerminal1);

        if (numDcTerminals == 2) {
            dcTerminal2 = p.getId(CgmesNames.DC_TERMINAL2);
            dcNode2 = context.dcMapping().getDcNode(dcTerminal2);
            dcConnected2 = context.dcMapping().isConnected(dcTerminal2);
        }
    }

    protected void addTerminalsAlias(Identifiable<?> identifiable) {
        identifiable.addAlias(dcTerminal1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL1, context.config().isEnsureIdAliasUnicity());
        if (numDcTerminals == 2) {
            identifiable.addAlias(dcTerminal2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL2, context.config().isEnsureIdAliasUnicity());
        }
    }

    @Override
    public boolean valid() {
        return dcNode1 != null && (dcNode2 != null || numDcTerminals == 1);
    }
}
