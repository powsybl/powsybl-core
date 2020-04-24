/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.AbstractConductingEquipmentConversion;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.LccConverterStationAdder;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class AcDcConverterConversion extends AbstractConductingEquipmentConversion {

    private static final double DEFAULT_POWER_FACTOR = 0.8;

    enum VscRegulation {
        REACTIVE_POWER,
        VOLTAGE
    }

    public AcDcConverterConversion(PropertyBag c, HvdcType converterType, double lossFactor, Context context) {
        super("ACDCConverter", c, context);

        this.converterType = Objects.requireNonNull(converterType);
        this.lossFactor = lossFactor;
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        if (converterType == null) {
            invalid("Type " + p.getLocal("type"));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        Objects.requireNonNull(converterType);
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
                .setLossFactor((float) this.lossFactor)
                .setVoltageRegulatorOn(voltageRegulatorOn)
                .setVoltageSetpoint(voltageSetpoint)
                .setReactivePowerSetpoint(reactivePowerSetpoint);
            identify(adder);
            connect(adder);
            VscConverterStation c = adder.add();

            convertedTerminals(c.getTerminal());
        } else if (converterType.equals(HvdcType.LCC)) {

            // TODO: There are two modes of control: dcVoltage and activePower
            // For dcVoltage, setpoint is targetUdc,
            // For activePower, setpoint is targetPpcc

            LccConverterStationAdder adder = voltageLevel().newLccConverterStation()
                .setLossFactor((float) this.lossFactor)
                .setPowerFactor((float) DEFAULT_POWER_FACTOR);
            identify(adder);
            connect(adder);
            LccConverterStation c = adder.add();

            this.lccConverter = c;
            convertedTerminals(c.getTerminal());
        }
    }

    public void setLccPowerFactor(double powerFactor) {
        this.lccConverter.setPowerFactor((float) powerFactor);
    }

    private static VscRegulation decodeVscRegulation(String qPccControl) {
        if (qPccControl.endsWith("voltagePcc")) {
            return VscRegulation.VOLTAGE;
        } else if (qPccControl.endsWith("reactivePcc")) {
            return VscRegulation.REACTIVE_POWER;
        }
        return null;
    }

    LccConverterStation getLccConverter() {
        return this.lccConverter;
    }

    private final HvdcType converterType;
    private final double lossFactor;
    private LccConverterStation lccConverter = null;
}
