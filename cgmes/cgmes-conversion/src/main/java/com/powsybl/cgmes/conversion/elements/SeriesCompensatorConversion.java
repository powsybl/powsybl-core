/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SeriesCompensatorConversion extends AbstractBranchConversion {

    public SeriesCompensatorConversion(PropertyBag sc, Context context) {
        super(CgmesNames.SERIES_COMPENSATOR, sc, context);
    }

    @Override
    public void convert() {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        final LineAdder adder = context.network().newLine()
                .setR(r)
                .setX(x)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0);
        identify(adder);
        connect(adder);
        final Line l = adder.add();
        addAliasesAndProperties(l);
        convertedTerminals(l.getTerminal1(), l.getTerminal2());
    }
}
