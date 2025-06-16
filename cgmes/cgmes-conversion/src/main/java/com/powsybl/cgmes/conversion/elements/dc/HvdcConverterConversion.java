/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForVscConverters;
import com.powsybl.cgmes.conversion.elements.AbstractReactiveLimitsOwnerConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class HvdcConverterConversion extends AbstractReactiveLimitsOwnerConversion {

    private final PropertyBag converter;
    private final double lossFactor;

    private static final double DEFAULT_POWER_FACTOR = 0.8;

    public HvdcConverterConversion(PropertyBag converter, double lossFactor, Context context) {
        super(CgmesNames.ACDC_CONVERTER, converter, context);

        this.converter = converter;
        this.lossFactor = lossFactor;
    }

    @Override
    public void convert() {
        if (HvdcType.VSC == getHvdcType()) {
            VscConverterStationAdder adder = voltageLevel().newVscConverterStation()
                    .setLossFactor((float) lossFactor);
            identify(adder);
            connect(adder);
            RegulatingControlMappingForVscConverters.initialize(adder);
            VscConverterStation c = adder.add();

            addAliasesAndProperties(c);
            convertedTerminals(c.getTerminal());
            convertReactiveLimits(c);
            context.regulatingControlMapping().forVscConverters().add(c.getId(), p);
        } else {
            LccConverterStationAdder adder = voltageLevel().newLccConverterStation()
                    .setLossFactor((float) lossFactor)
                    .setPowerFactor((float) getPowerFactor());
            identify(adder);
            connect(adder);
            LccConverterStation c = adder.add();

            addAliasesAndProperties(c);
            convertedTerminals(c.getTerminal());
        }
    }

    @Override
    protected void addAliasesAndProperties(Identifiable<?> identifiable) {
        super.addAliasesAndProperties(identifiable);
        identifiable.addAlias(converter.getId(DC_TERMINAL1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL1);
        identifiable.addAlias(converter.getId(DC_TERMINAL2), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL2);
    }

    private double getPowerFactor() {
        double p = converter.asDouble("p");
        double q = converter.asDouble("q");
        double powerFactor = p / Math.hypot(p, q);
        if (Double.isNaN(powerFactor)) {
            return DEFAULT_POWER_FACTOR;
        }
        return powerFactor;
    }

    private HvdcType getHvdcType() {
        if (VS_CONVERTER.equals(converter.getLocal("type"))) {
            return HvdcType.VSC;
        } else {
            return HvdcType.LCC;
        }
    }
}
