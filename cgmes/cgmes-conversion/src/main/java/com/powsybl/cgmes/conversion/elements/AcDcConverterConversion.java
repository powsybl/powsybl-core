/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;
import com.powsybl.iidm.network.LccConverterStationAdder;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class AcDcConverterConversion extends AbstractConductingEquipmentConversion {

    public AcDcConverterConversion(PropertyBag c, Context context) {
        super("ACDCConverter", c, context);
        converterType = decodeType(p.getLocal("type"));
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        if (converterType == null) {
            invalid(String.format("Type %s", p.getLocal("type")));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        Objects.requireNonNull(converterType);
        double lossFactor = p.asDouble("lossFactor", 0);

        HvdcConverterStation<?> c = null;
        if (converterType.equals(HvdcType.VSC)) {
            boolean voltageRegulatorOn = p.asBoolean("voltageRegulatorOn", false);
            VscConverterStationAdder adder = voltageLevel().newVscConverterStation()
                    .setId(iidmId())
                    .setName(iidmName())
                    .setEnsureIdUnicity(false)
                    .setLossFactor((float) lossFactor)
                    .setVoltageRegulatorOn(voltageRegulatorOn);
            identify(adder);
            connect(adder);
            c = adder.add();
        } else if (converterType.equals(HvdcType.LCC)) {
            LccConverterStationAdder adder = voltageLevel().newLccConverterStation()
                    .setId(iidmId())
                    .setName(iidmName())
                    .setEnsureIdUnicity(false)
                    .setBus(terminalConnected() ? busId() : null)
                    .setConnectableBus(busId())
                    .setLossFactor((float) lossFactor)
                    .setPowerFactor((float) 0.8);
            identify(adder);
            connect(adder);
            c = adder.add();
        }
        Objects.requireNonNull(c);
        context.dc().map(p, c);
        convertedTerminals(c.getTerminal());
    }

    private static HvdcType decodeType(String stype) {
        if (stype.equals("VsConverter")) {
            return HvdcType.VSC;
        } else if (stype.equals("CsConverter")) {
            return HvdcType.LCC;
        }
        return null;
    }

    private final HvdcType converterType;
}
