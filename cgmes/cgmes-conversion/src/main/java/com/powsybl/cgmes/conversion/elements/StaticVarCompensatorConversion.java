/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class StaticVarCompensatorConversion extends AbstractConductingEquipmentConversion {

    public StaticVarCompensatorConversion(PropertyBag svc, Conversion.Context context) {
        super("StaticVarCompensator", svc, context);
    }

    @Override
    public void convert() {
        double slope = p.asDouble("slope", 0.0);
        ignored(String.format("Slope %f", slope));

        double capacitiveRating = p.asDouble("capacitiveRating", 0.0);
        double inductiveRating = p.asDouble("inductiveRating", 0.0);
        String controlMode = p.getId("controlMode");
        double voltageSetPoint = p.asDouble("voltageSetPoint", 1.0);
        boolean controlEnabled = p.asBoolean("controlEnabled", true);

        StaticVarCompensator.RegulationMode regulationMode;
        regulationMode = StaticVarCompensator.RegulationMode.OFF;
        if (controlEnabled) {
            if (controlMode.toLowerCase().endsWith("voltage")) {
                regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
            } else if (controlMode.toLowerCase().endsWith("reactivePower")) {
                regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
            }
        }

        StaticVarCompensator svc = voltageLevel().newStaticVarCompensator()
                .setId(iidmId())
                .setName(iidmName())
                .setEnsureIdUnicity(false)
                .setBus(terminalConnected() ? busId() : null)
                .setConnectableBus(busId())
                // TODO in IIDM Bmin and Bmax a susceptance,
                // while CGMES defines the limits as reactances
                .setBmin(inductiveRating)
                .setBmax(capacitiveRating)
                .setVoltageSetPoint(voltageSetPoint)
                .setRegulationMode(regulationMode)
                .add();

        convertedTerminals(svc.getTerminal());
    }
}
