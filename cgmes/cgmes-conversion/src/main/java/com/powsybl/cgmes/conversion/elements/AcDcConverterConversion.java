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

    enum VscRegulation {
        REACTIVE_POWER,
        VOLTAGE
    }

    @Override
    public void convert() {
        Objects.requireNonNull(converterType);
        HvdcConverterStation<?> c = null;
        if (converterType.equals(HvdcType.VSC)) {
            VscRegulation vscRegulation = decodeVscRegulation(p.getLocal("qPccControl"));
            boolean voltageRegulatorOn = false;
            double voltageSetpoint = 0;
            double reactivePowerSetpoint = 0;
            if (vscRegulation == VscRegulation.VOLTAGE) {
                voltageRegulatorOn = true;
                voltageSetpoint = p.asDouble("targetUpcc");
            } else if (vscRegulation == VscRegulation.REACTIVE_POWER) {
                reactivePowerSetpoint = -p.asDouble("targetQpcc");
            }
            VscConverterStationAdder adder = voltageLevel().newVscConverterStation()
                    .setLossFactor(0.0f) // this attribute will be updated when the HVDC line attached to this station is imported
                    .setVoltageRegulatorOn(voltageRegulatorOn)
                    .setVoltageSetpoint(voltageSetpoint)
                    .setReactivePowerSetpoint(reactivePowerSetpoint);
            identify(adder);
            connect(adder);
            c = adder.add();
        } else if (converterType.equals(HvdcType.LCC)) {

            // TODO: There are two modes of control: dcVoltage and activePower
            // For dcVoltage, setpoint is targetUdc,
            // For activePower, setpoint is targetPpcc
            LccConverterStationAdder adder = voltageLevel().newLccConverterStation()
                    .setLossFactor(0.0f)
                    .setPowerFactor(0.8f);
            identify(adder);
            connect(adder);
            c = adder.add();
        }
        Objects.requireNonNull(c);
        context.dc().map(this.id, p, c);
        convertedTerminals(c.getTerminal());
    }

    private static VscRegulation decodeVscRegulation(String qPccControl) {
        if (qPccControl.endsWith("voltagePcc")) {
            return VscRegulation.VOLTAGE;
        } else if (qPccControl.endsWith("reactivePcc")) {
            return VscRegulation.REACTIVE_POWER;
        }
        return null;
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
