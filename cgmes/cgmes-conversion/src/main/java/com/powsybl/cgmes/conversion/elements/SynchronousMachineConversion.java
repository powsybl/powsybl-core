/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SynchronousMachineConversion extends AbstractConductingEquipmentConversion {

    public SynchronousMachineConversion(PropertyBag sm, Conversion.Context context) {
        super("SynchronousMachine", sm, context);
    }

    @Override
    public void convert() {
        double minP = p.asDouble("minP", 0);
        double maxP = p.asDouble("maxP", 0);
        double ratedS = p.asDouble("ratedS");
        ratedS = ratedS > 0 ? ratedS : Double.NaN;
        String generatingUnitType = p.getLocal("generatingUnitType");
        PowerFlow f = powerFlow();

        double targetP = p.asDouble("initialP", 0);
        double targetQ = 0;
        if (f.defined()) {
            targetP = -f.p();
            targetQ = -f.q();
        }

        RegulatingControlConversion.Data control = RegulatingControlConversion.convert(
                p,
                voltageLevel(),
                context);
        Generator g = voltageLevel().newGenerator()
                .setId(iidmId())
                .setName(iidmName())
                .setEnsureIdUnicity(false)
                .setBus(terminalConnected() ? busId() : null)
                .setConnectableBus(busId())
                .setMinP(minP)
                .setMaxP(maxP)
                .setVoltageRegulatorOn(control.on())
                .setRegulatingTerminal(control.terminal())
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .setTargetV(control.targetV())
                .setEnergySource(fromGeneratingUnitType(generatingUnitType))
                .setRatedS(ratedS)
                .add();

        convertedTerminals(g.getTerminal());
        ReactiveLimitsConversion.convert(p, g);
    }

    private static EnergySource fromGeneratingUnitType(String gut) {
        EnergySource es = EnergySource.OTHER;
        if (gut.contains("HydroGeneratingUnit")) {
            es = EnergySource.HYDRO;
        } else if (gut.contains("NuclearGeneratingUnit")) {
            es = EnergySource.NUCLEAR;
        } else if (gut.contains("ThermalGeneratingUnit")) {
            es = EnergySource.THERMAL;
        } else if (gut.contains("WindGeneratingUnit")) {
            es = EnergySource.WIND;
        }
        return es;
    }
}
