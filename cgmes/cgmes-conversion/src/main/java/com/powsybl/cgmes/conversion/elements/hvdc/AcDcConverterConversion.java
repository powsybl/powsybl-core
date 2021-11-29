/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForVscConverters;
import com.powsybl.cgmes.conversion.elements.AbstractReactiveLimitsOwnerConversion;
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
public class AcDcConverterConversion extends AbstractReactiveLimitsOwnerConversion {

    private static final double DEFAULT_POWER_FACTOR = 0.8;

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
            VscConverterStationAdder adder = voltageLevel().newVscConverterStation()
                .setLossFactor((float) this.lossFactor);
            identify(adder);
            connect(adder);
            RegulatingControlMappingForVscConverters.initialize(adder);
            VscConverterStation c = adder.add();
            addAliasesAndProperties(c);

            convertedTerminals(c.getTerminal());
            convertReactiveLimits(c);
            context.regulatingControlMapping().forVscConverters().add(c.getId(), p);
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
            addAliasesAndProperties(c);

            this.lccConverter = c;
            convertedTerminals(c.getTerminal());
        }
    }

    public void setLccPowerFactor(double powerFactor) {
        this.lccConverter.setPowerFactor((float) powerFactor);
    }

    LccConverterStation getLccConverter() {
        return this.lccConverter;
    }

    private final HvdcType converterType;
    private final double lossFactor;
    private LccConverterStation lccConverter = null;
}
