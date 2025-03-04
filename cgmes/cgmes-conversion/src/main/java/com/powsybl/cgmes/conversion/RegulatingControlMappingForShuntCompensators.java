/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class RegulatingControlMappingForShuntCompensators {

    private final RegulatingControlMapping parent;
    private final Context context;
    private final Map<String, String> mapping;

    RegulatingControlMappingForShuntCompensators(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        mapping = new HashMap<>();
    }

    public void add(String iidmId, PropertyBag p) {
        String rcId = RegulatingControlMapping.getRegulatingControlId(p);

        if (mapping.containsKey(iidmId)) {
            throw new CgmesModelException(
                    "ShuntCompensator already added, IIDM ShuntCompensator Id: " + iidmId);
        }

        mapping.put(iidmId, rcId);
    }

    void applyRegulatingControls(Network network) {
        network.getShuntCompensatorStream().forEach(this::apply);
    }

    private void apply(ShuntCompensator shuntCompensator) {
        String rcId = mapping.get(shuntCompensator.getId());
        apply(shuntCompensator, rcId);
    }

    private void apply(ShuntCompensator shuntCompensator, String regulatingControlId) {
        if (regulatingControlId == null) {
            return;
        }
        RegulatingControl rc = parent.cachedRegulatingControls().get(regulatingControlId);
        if (rc == null) {
            return;
        }
        // Finally, equipment participates in it regulating control,
        // and the regulating control information is present in the CGMES model
        setRegulatingControl(shuntCompensator, regulatingControlId, rc);
        rc.setCorrectlySet(true);
    }

    private void setRegulatingControl(ShuntCompensator shuntCompensator, String rcId, RegulatingControl rc) {
        // Take default terminal if it has not been defined in CGMES files (it is never null)
        shuntCompensator.setRegulatingTerminal(RegulatingTerminalMapper
                .mapForVoltageControl(rc.cgmesTerminal, context)
                .orElse(shuntCompensator.getTerminal()));
        shuntCompensator.setProperty(Conversion.PROPERTY_REGULATING_CONTROL, rcId);
    }
}
