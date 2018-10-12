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
public class ExternalNetworkInjectionConversion extends AbstractConductingEquipmentConversion {

    public ExternalNetworkInjectionConversion(PropertyBag sm, Conversion.Context context) {
        super("ExternalNetworkInjection", sm, context);
    }

    @Override
    public void convert() {
        double minP = p.asDouble("minP", 0);
        double maxP = p.asDouble("maxP", 0);

        double targetP = 0;
        double targetQ = 0;
        PowerFlow f = powerFlow();
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
                .setEnergySource(EnergySource.OTHER)
                .add();

        convertedTerminals(g.getTerminal());
        ReactiveLimitsConversion.convert(p, g);
    }
}
