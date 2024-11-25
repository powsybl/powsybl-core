/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForStaticVarCompensators;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class StaticVarCompensatorConversion extends AbstractConductingEquipmentConversion {

    public StaticVarCompensatorConversion(PropertyBag svc, Context context) {
        super(CgmesNames.STATIC_VAR_COMPENSATOR, svc, context);
    }

    @Override
    public void convert() {
        double slope = checkSlope(p.asDouble("slope"));
        double capacitiveRating = p.asDouble("capacitiveRating", 0.0);
        double inductiveRating = p.asDouble("inductiveRating", 0.0);

        StaticVarCompensatorAdder adder = voltageLevel().newStaticVarCompensator()
            .setBmin(getB(inductiveRating, "inductive"))
            .setBmax(getB(capacitiveRating, "capacitive"));
        identify(adder);
        connect(adder);
        RegulatingControlMappingForStaticVarCompensators.initialize(adder);

        StaticVarCompensator svc = adder.add();
        addAliasesAndProperties(svc);
        convertedTerminals(svc.getTerminal());
        if (slope >= 0) {
            svc.newExtension(VoltagePerReactivePowerControlAdder.class).withSlope(slope).add();
        }

        context.regulatingControlMapping().forStaticVarCompensators().add(svc.getId(), p);
    }

    private double getB(double rating, String name) {
        if (rating == 0.0) {
            fixed(name + "Rating", "Undefined or equal to 0. Corresponding susceptance is Double.MAX_VALUE");
            return name.equals("inductive") ? -Double.MAX_VALUE : Double.MAX_VALUE;
        }
        return 1 / rating;
    }

    private double checkSlope(double slope) {
        if (Double.isNaN(slope)) {
            missing("slope");
        }
        if (slope < 0) {
            ignored("Slope must be positive");
        }
        return slope;
    }
}
