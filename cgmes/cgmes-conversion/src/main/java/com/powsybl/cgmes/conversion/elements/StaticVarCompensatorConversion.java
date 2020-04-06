/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForStaticVarCompensators;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class StaticVarCompensatorConversion extends AbstractConductingEquipmentConversion {

    public StaticVarCompensatorConversion(PropertyBag svc, Context context) {
        super("StaticVarCompensator", svc, context);
    }

    @Override
    public void convert() {
        double slope = p.asDouble("slope", 0.0);
        ignored("Slope " + slope);

        double capacitiveRating = p.asDouble("capacitiveRating", 0.0);
        double inductiveRating = p.asDouble("inductiveRating", 0.0);

        StaticVarCompensatorAdder adder = voltageLevel().newStaticVarCompensator()
            .setBmin(1 / inductiveRating)
            .setBmax(1 / capacitiveRating);
        identify(adder);
        connect(adder);
        RegulatingControlMappingForStaticVarCompensators.initialize(adder);

        StaticVarCompensator svc = adder.add();
        convertedTerminals(svc.getTerminal());

        context.regulatingControlMapping().forStaticVarCompensators().add(svc.getId(), p);
    }
}
