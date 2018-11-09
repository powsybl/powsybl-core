/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Objects;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class DcLineSegmentConversion extends AbstractIdentifiedObjectConversion {

    public DcLineSegmentConversion(PropertyBag l, Conversion.Context context) {
        super("DCLineSegment", l, context);

        iconverter1 = context.dc().converterAt(l.getId("DCTerminal1"));
        iconverter2 = context.dc().converterAt(l.getId("DCTerminal2"));
        cconverter1 = context.dc().cgmesConverterFor(iconverter1);
        cconverter2 = context.dc().cgmesConverterFor(iconverter2);
    }

    @Override
    public boolean valid() {
        if (iconverter1 == null) {
            missing("Converter1");
        }
        if (iconverter2 == null) {
            missing("Converter2");
        }
        return iconverter1 != null && iconverter2 != null;
    }

    @Override
    public void convert() {
        Objects.requireNonNull(iconverter1);
        Objects.requireNonNull(iconverter2);

        double p = activePowerSetpoint();
        double maxP = p * 1.2;
        missing("maxP", maxP);

        context.network().newHvdcLine()
                .setId(iidmId())
                .setName(iidmName())
                .setEnsureIdUnicity(false)
                .setR(r())
                .setNominalV(ratedUdc())
                .setActivePowerSetpoint(p)
                .setMaxP(maxP)
                .setConvertersMode(decodeMode())
                .setConverterStationId1(iconverter1.getId())
                .setConverterStationId2(iconverter2.getId())
                .add();
    }

    private double r() {
        double r = p.asDouble("r", 0);
        if (r < 0) {
            double r1 = 0.1;
            fixed("resistance", "was zero", r, r1);
            r = r1;
        }
        return r;
    }

    private double ratedUdc() {
        double ratedUdc1 = cconverter1.asDouble(CgmesNames.RATED_UDC);
        double ratedUdc2 = cconverter2.asDouble(CgmesNames.RATED_UDC);
        double ratedUdc = ratedUdc1;
        if (ratedUdc2 != ratedUdc1) {
            invalid(CgmesNames.RATED_UDC,
                    String.format("different ratedUdc1, ratedUdc2; use ratedUdc1 by default: %f %f",
                            ratedUdc1,
                            ratedUdc2),
                    ratedUdc);
        }
        return ratedUdc;
    }

    private double activePowerSetpoint() {
        // Take the targetPpcc from the side that regulates activePower
        double p = activePowerSetpoint(cconverter1);
        if (Double.isNaN(p)) {
            p = activePowerSetpoint(cconverter2);
        }
        if (Double.isNaN(p)) {
            p = 0;
            missing("activePowerSetpoint", p);
        }
        return p;
    }

    private double activePowerSetpoint(PropertyBag cc) {
        String control = cc.getLocal("CsPpccControlKind");
        if (control != null && control.endsWith("activePower")) {
            return cc.asDouble("targetPpcc");
        }
        return Double.NaN;
    }

    private HvdcLine.ConvertersMode decodeMode() {
        String mode1 = cconverter1.getLocal("operatingMode");
        String mode2 = cconverter2.getLocal("operatingMode");
        if (inverter(mode1) && rectifier(mode2)) {
            return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
        } else if (rectifier(mode1) && inverter(mode2)) {
            return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
        } else {
            invalid(String.format("Unsupported modeling. Converters modes %s %s", mode1, mode2));
            return null;
        }
    }

    private boolean inverter(String operatingMode) {
        return operatingMode.toLowerCase().endsWith("inverter");
    }

    private boolean rectifier(String operatingMode) {
        return operatingMode.toLowerCase().endsWith("rectifier");
    }

    private HvdcConverterStation iconverter1;
    private HvdcConverterStation iconverter2;
    private PropertyBag cconverter1;
    private PropertyBag cconverter2;
}
