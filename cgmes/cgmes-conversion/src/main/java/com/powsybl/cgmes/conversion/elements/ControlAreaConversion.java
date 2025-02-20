/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Area;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class ControlAreaConversion extends AbstractIdentifiedObjectConversion {

    public ControlAreaConversion(PropertyBag area, Context context) {
        super(CgmesNames.CONTROL_AREA, area, context);
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void convert() {
        String controlAreaId = p.getId(CgmesNames.CONTROL_AREA);
        String type = p.getLocal("controlAreaType");
        Area area = context.network().newArea()
                .setAreaType(type) // Copy the type defined by CGMES
                .setId(controlAreaId)
                .setName(p.getLocal("name"))
                .setInterchangeTarget(p.asDouble("netInterchange", Double.NaN))
                .add();
        if (p.containsKey(CgmesNames.P_TOLERANCE)) {
            String pTolerance = p.get(CgmesNames.P_TOLERANCE);
            area.setProperty(CgmesNames.P_TOLERANCE, pTolerance);
        }
        if (p.containsKey(CgmesNames.ENERGY_IDENT_CODE_EIC)) {
            area.addAlias(p.get(CgmesNames.ENERGY_IDENT_CODE_EIC), CgmesNames.ENERGY_IDENT_CODE_EIC);
        }
    }
}
