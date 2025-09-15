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
import com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import static com.powsybl.cgmes.conversion.elements.AbstractConductingEquipmentConversion.updateTerminals;
import static com.powsybl.cgmes.conversion.elements.dc.DCLinkUpdate.getPdcInverter;
import static com.powsybl.cgmes.model.CgmesNames.*;
import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class HvdcLineConversion extends AbstractIdentifiedObjectConversion {

    private final DCLink dcLink;

    private static final double DEFAULT_MAXP_FACTOR = 1.2;

    public HvdcLineConversion(DCLink dcLink, Context context) {
        // Use line 1 id as the main identifier of this line.
        // If present, line 2 id is added as an alias.
        super(DC_LINE_SEGMENT, dcLink.getDcLine1(), context);
        this.dcLink = dcLink;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void convert() {

        // arbitrary value because there is no maxP attribute in CGMES
        double maxP = 0.0;
        HvdcLine.ConvertersMode convertersMode = HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
        double activePowerSetpoint = 0.0;

        HvdcLineAdder adder = context.network().newHvdcLine()
                .setR(dcLink.getR())
                .setNominalV(dcLink.getRatedUdc())
                .setMaxP(maxP)
                .setConvertersMode(convertersMode)
                .setActivePowerSetpoint(activePowerSetpoint)
                .setConverterStationId1(context.namingStrategy().getIidmId(ACDC_CONVERTER, dcLink.getConverter1().getId(ACDC_CONVERTER)))
                .setConverterStationId2(context.namingStrategy().getIidmId(ACDC_CONVERTER, dcLink.getConverter2().getId(ACDC_CONVERTER)));
        identify(adder);
        HvdcLine hvdcLine = adder.add();

        // Add aliases
        hvdcLine.addAlias(dcLink.getDcLine1().getId(DC_TERMINAL1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL1);
        hvdcLine.addAlias(dcLink.getDcLine1().getId(DC_TERMINAL2), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL2);
        if (dcLink.getDcLine2() != null) {
            hvdcLine.addAlias(dcLink.getDcLine2().getId(DC_LINE_SEGMENT), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_LINE_SEGMENT2);
        }
    }

    public static void update(HvdcLine hvdcLine, PropertyBag cgmesDataAcDcConverter1, PropertyBag cgmesDataAcDcConverter2, Context context) {
        updateTerminals(hvdcLine.getConverterStation1(), context, hvdcLine.getConverterStation1().getTerminal());
        updateTerminals(hvdcLine.getConverterStation2(), context, hvdcLine.getConverterStation2().getTerminal());

        DCLinkUpdate dcLinkUpdate = updateHvdcLine(hvdcLine, cgmesDataAcDcConverter1, cgmesDataAcDcConverter2, context);

        if (hvdcLine.getConverterStation1().getHvdcType() == HvdcConverterStation.HvdcType.LCC) {
            HvdcConverterConversion.update((LccConverterStation) hvdcLine.getConverterStation1(), cgmesDataAcDcConverter1, dcLinkUpdate.getLossFactor1(), context);
            HvdcConverterConversion.update((LccConverterStation) hvdcLine.getConverterStation2(), cgmesDataAcDcConverter2, dcLinkUpdate.getLossFactor2(), context);
        } else {
            HvdcConverterConversion.update((VscConverterStation) hvdcLine.getConverterStation1(), cgmesDataAcDcConverter1, dcLinkUpdate.getLossFactor1(), context);
            HvdcConverterConversion.update((VscConverterStation) hvdcLine.getConverterStation2(), cgmesDataAcDcConverter2, dcLinkUpdate.getLossFactor2(), context);
        }
    }

    private static DCLinkUpdate updateHvdcLine(HvdcLine hvdcLine, PropertyBag cgmesDataAcDcConverter1, PropertyBag cgmesDataAcDcConverter2, Context context) {

        DCLinkUpdate.DefaultData defaultData = getDefaultData(hvdcLine, context);
        DCLinkUpdate dcLinkUpdate = new DCLinkUpdate(hvdcLine, cgmesDataAcDcConverter1, cgmesDataAcDcConverter2, defaultData);
        hvdcLine.setConvertersMode(dcLinkUpdate.getMode())
                .setActivePowerSetpoint(dcLinkUpdate.getTargetP())
                .setMaxP(DEFAULT_MAXP_FACTOR * dcLinkUpdate.getTargetP());
        return dcLinkUpdate;
    }

    private static DCLinkUpdate.DefaultData getDefaultData(HvdcLine hvdcLine, Context context) {
        boolean defaultRectifierOnSide1 = getDefaultValue(null, isRectifierOnSide1(hvdcLine.getConvertersMode()), true, true, context);
        HvdcLine.ConvertersMode defaultMode = defaultRectifierOnSide1 ? SIDE_1_RECTIFIER_SIDE_2_INVERTER : SIDE_1_INVERTER_SIDE_2_RECTIFIER;
        double defaultTargetP = getDefaultValue(null, hvdcLine.getActivePowerSetpoint(), 0.0, 0.0, context);
        double defaultPdcInverter = getDefaultValue(null, getPdcInverter(hvdcLine), 0.0, 0.0, context);
        double lossFactor1 = getDefaultValue(null, (double) hvdcLine.getConverterStation1().getLossFactor(), 0.0, 0.0, context);
        double lossFactor2 = getDefaultValue(null, (double) hvdcLine.getConverterStation2().getLossFactor(), 0.0, 0.0, context);

        return new DCLinkUpdate.DefaultData(defaultMode, defaultTargetP, defaultPdcInverter, lossFactor1, lossFactor2);
    }

    private static boolean isRectifierOnSide1(HvdcLine.ConvertersMode mode) {
        return mode == SIDE_1_RECTIFIER_SIDE_2_INVERTER;
    }
}
