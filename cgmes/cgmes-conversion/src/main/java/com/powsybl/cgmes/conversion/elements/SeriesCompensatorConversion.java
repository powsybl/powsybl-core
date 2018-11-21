/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Line;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SeriesCompensatorConversion extends AbstractBranchConversion {

    public SeriesCompensatorConversion(PropertyBag sc, Conversion.Context context) {
        super(CgmesNames.SERIES_COMPENSATOR, sc, context);
    }

    @Override
    public void convert() {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        String busId1 = busId(1);
        String busId2 = busId(2);
        final Line l = context.network().newLine()
                .setId(context.namingStrategy().getId(CgmesNames.SERIES_COMPENSATOR, id))
                .setName(context.namingStrategy().getName(CgmesNames.SERIES_COMPENSATOR, name))
                .setEnsureIdUnicity(false)
                .setBus1(terminalConnected(1) ? busId1 : null)
                .setBus2(terminalConnected(2) ? busId2 : null)
                .setConnectableBus1(busId1)
                .setConnectableBus2(busId2)
                .setVoltageLevel1(iidmVoltageLevelId(1))
                .setVoltageLevel2(iidmVoltageLevelId(2))
                .setR(r)
                .setX(x)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        convertedTerminals(l.getTerminal1(), l.getTerminal2());
    }
}
