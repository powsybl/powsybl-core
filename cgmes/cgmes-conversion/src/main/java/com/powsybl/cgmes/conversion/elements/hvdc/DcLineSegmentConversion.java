/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion;
import com.powsybl.cgmes.model.CgmesDcTerminal;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.cgmes.conversion.elements.AbstractConductingEquipmentConversion.updateTerminals;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class DcLineSegmentConversion extends AbstractIdentifiedObjectConversion {

    private static final String TARGET_PPCC = "targetPpcc";
    private static final String POLE_LOSS_P = "poleLossP";
    private static final String OPERATING_MODE = "operatingMode";
    private static final double DEFAULT_MAXP_FACTOR = 1.2;

    DcLineSegmentConversion(PropertyBag l, HvdcLine.ConvertersMode mode, double r, double ratedUdc,
                            String converterId1, String converterId2, boolean isDuplicated, Context context) {
        super(CgmesNames.DC_LINE_SEGMENT, l, context);

        this.mode = mode;
        this.r = r;
        this.ratedUdc = ratedUdc;
        this.converterId1 = Objects.requireNonNull(converterId1);
        this.converterId2 = Objects.requireNonNull(converterId2);
        this.isDuplicated = isDuplicated;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void convert() {

        // arbitrary value because there is no maxP attribute in CGMES
        double maxP = 0.0;

        HvdcLineAdder adder = context.network().newHvdcLine()
                .setR(r)
                .setNominalV(ratedUdc)
                .setMaxP(maxP)
                .setConvertersMode(mode)
                .setConverterStationId1(converterId1)
                .setConverterStationId2(converterId2);
        identify(adder, isDuplicated ? "-1" : "");
        HvdcLine hvdcLine = adder.add();

        //
        //      .setConvertersMode(mode)

        addHvdcAliasesAndProperties(super.p, isDuplicated ? "-1" : "", context.cgmes(), hvdcLine);
    }

    // We do not use "#n" to guarantee uniqueness since the getId() method does not support more than one '#' character
    private static void addHvdcAliasesAndProperties(PropertyBag pb, String duplicatedTag, CgmesModel cgmesModel, HvdcLine hvdcLine) {
        CgmesDcTerminal t1 = cgmesModel.dcTerminal(pb.getId(CgmesNames.DC_TERMINAL + 1));
        String dcNode1 = CgmesDcConversion.getDcNode(cgmesModel, t1);
        CgmesDcTerminal t2 = cgmesModel.dcTerminal(pb.getId(CgmesNames.DC_TERMINAL + 2));
        String dcNode2 = CgmesDcConversion.getDcNode(cgmesModel, t2);

        // connectiviyNode, topologicalNode or both ???
        hvdcLine.addAlias(t1.id() + duplicatedTag, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL + 1);
        hvdcLine.addAlias(t2.id() + duplicatedTag, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL + 2);
        hvdcLine.addAlias(dcNode1 + duplicatedTag, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode" + 1);
        hvdcLine.addAlias(dcNode2 + duplicatedTag, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode" + 2);
    }

    // poleLossP is the active power loss at a DC Pole
    // for lossless operation: P(DC) = P(AC) => lossFactor = 0
    // for rectifier operation (conversion from AC to DC) with losses: P(DC) = P(AC) - poleLossP
    // In IIDM, for rectifier operation P(DC) / P(AC) = 1 - lossFactor / 100
    // => P(DC) / P(AC) = 1 - poleLossP / P(AC) = 1 - lossFactor / 100
    // for inverter operation (conversion from DC to AC) with losses: P(DC) = P(AC) + poleLossP
    // In IIDM, for inverter operation P(AC) / P(DC) = 1 - lossFactor / 100
    // => P(AC) / P(DC) = 1 - poleLossP / P(DC) = 1 - poleLossP / (P(AC) + poleLossP) = 1 - lossFactor / 100
    public static void update(HvdcLine hvdcLine, PropertyBag cgmesDataAcDcConverter1, PropertyBag cgmesDataAcDcConverter2, Context context) {
        updateTerminals(hvdcLine.getConverterStation1(), context, hvdcLine.getConverterStation1().getTerminal());
        updateTerminals(hvdcLine.getConverterStation2(), context, hvdcLine.getConverterStation2().getTerminal());

        HvdcConverterStation.HvdcType hvdcType = findHvdcType(hvdcLine);
        LossFactor lossFactor = updateHvdcLine(hvdcLine, hvdcType, cgmesDataAcDcConverter1, cgmesDataAcDcConverter2, context);

        if (hvdcType.equals(HvdcConverterStation.HvdcType.LCC)) {
            AcDcConverterConversion.update((LccConverterStation) hvdcLine.getConverterStation1(), cgmesDataAcDcConverter1, lossFactor.getLossFactor1());
            AcDcConverterConversion.update((LccConverterStation) hvdcLine.getConverterStation2(), cgmesDataAcDcConverter2, lossFactor.getLossFactor2());
        } else {
            AcDcConverterConversion.update((VscConverterStation) hvdcLine.getConverterStation1(), cgmesDataAcDcConverter1, lossFactor.getLossFactor1(), context);
            AcDcConverterConversion.update((VscConverterStation) hvdcLine.getConverterStation2(), cgmesDataAcDcConverter2, lossFactor.getLossFactor2(), context);
        }
    }

    private static LossFactor updateHvdcLine(HvdcLine hvdcLine, HvdcConverterStation.HvdcType hvdcType, PropertyBag cgmesDataAcDcConverter1, PropertyBag cgmesDataAcDcConverter2, Context context) {
        HvdcLine.ConvertersMode operatingMode = decodeMode(hvdcType, cgmesDataAcDcConverter1, cgmesDataAcDcConverter2);

        double poleLossP1 = cgmesDataAcDcConverter1.asDouble(POLE_LOSS_P, 0.0);
        double poleLossP2 = cgmesDataAcDcConverter2.asDouble(POLE_LOSS_P, 0.0);

        // load sign convention is used i.e. positive sign means flow out from a node
        // i.e. pACx >= 0 if converterx is rectifier and pACx <= 0 if converterx is
        // inverter

        double pAC1 = getPAc(cgmesDataAcDcConverter1);
        double pAC2 = getPAc(cgmesDataAcDcConverter2);

        // arbitrary value because there is no maxP attribute in CGMES
        double maxP = getMaxP(pAC1, pAC2, operatingMode);
        double activePowerSetpoint = getActivePowerSetpoint(pAC1, pAC2, poleLossP1, poleLossP2, operatingMode);

        hvdcLine.setConvertersMode(operatingMode);
        hvdcLine.setActivePowerSetpoint(activePowerSetpoint);
        hvdcLine.setMaxP(maxP);

        LossFactor lossFactor = new LossFactor(context, operatingMode, pAC1, pAC2, poleLossP1, poleLossP2);
        lossFactor.compute();

        return lossFactor;
    }

    private static HvdcConverterStation.HvdcType findHvdcType(HvdcLine hvdcLine) {
        if (hvdcLine.getConverterStation1().getHvdcType().equals(HvdcConverterStation.HvdcType.LCC)
                && hvdcLine.getConverterStation2().getHvdcType().equals(HvdcConverterStation.HvdcType.LCC)) {
            return HvdcConverterStation.HvdcType.LCC;
        } else if (hvdcLine.getConverterStation1().getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)
                && hvdcLine.getConverterStation2().getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)) {
            return HvdcConverterStation.HvdcType.VSC;
        } else {
            throw new CgmesModelException("Unexpected Hvdc configuration type. HvdcLine: " + hvdcLine.getId()
                    + " HvdcConverterStation1: " + hvdcLine.getConverterStation1().getHvdcType().name()
                    + " HvdcConverterStation2: " + hvdcLine.getConverterStation2().getHvdcType().name());
        }
    }

    private static HvdcLine.ConvertersMode decodeMode(HvdcConverterStation.HvdcType converterType, PropertyBag converter1, PropertyBag converter2) {
        String mode1 = converter1.getLocal(OPERATING_MODE);
        String mode2 = converter2.getLocal(OPERATING_MODE);

        if (converterType.equals(HvdcConverterStation.HvdcType.LCC)) {
            if (inverter(mode1) && rectifier(mode2)) {
                return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
            } else if (rectifier(mode1) && inverter(mode2)) {
                return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            } else if (converter1.asDouble(TARGET_PPCC) == 0 && converter2.asDouble(TARGET_PPCC) == 0) {
                // Both ends are rectifier or inverter
                return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
            } else {
                LOG.warn("Undefined converter mode for the HVDC, assumed to be of type \"Side1 Rectifier - Side2 Inverter\"");
                return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            }
        } else {
            if (converter1.asDouble(TARGET_PPCC) > 0 || converter2.asDouble(TARGET_PPCC) < 0) {
                return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            } else {
                return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
            }
        }
    }

    private static boolean inverter(String operatingMode) {
        return operatingMode != null && operatingMode.toLowerCase().endsWith("inverter");
    }

    private static boolean rectifier(String operatingMode) {
        return operatingMode != null && operatingMode.toLowerCase().endsWith("rectifier");
    }

    private static double getPAc(PropertyBag p) {
        // targetPpcc is the real power injection target in the AC grid in CGMES
        return Double.isNaN(p.asDouble(TARGET_PPCC)) ? 0 : p.asDouble(TARGET_PPCC);
    }

    private static double getMaxP(double pAC1, double pAC2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) {
            if (pAC1 != 0) {
                return DEFAULT_MAXP_FACTOR * pAC1;
            }
            return DEFAULT_MAXP_FACTOR * Math.abs(pAC2);
        }
        if (pAC2 != 0) {
            return DEFAULT_MAXP_FACTOR * pAC2;
        }
        return DEFAULT_MAXP_FACTOR * Math.abs(pAC1);
    }

    private static double getActivePowerSetpoint(double pAC1, double pAC2, double poleLossP1, double poleLossP2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) {
            if (pAC1 != 0) {
                return pAC1;
            } else if (pAC2 != 0) {
                return Math.abs(pAC2) + poleLossP2 + poleLossP1;
            }
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)) {
            if (pAC2 != 0) {
                return pAC2;
            } else if (pAC1 != 0) {
                return Math.abs(pAC1) + poleLossP1 + poleLossP2;
            }
        }
        return 0;
    }

    private final HvdcLine.ConvertersMode mode;
    private final double r;
    private final double ratedUdc;
    private final String converterId1;
    private final String converterId2;
    private final boolean isDuplicated;

    private static final Logger LOG = LoggerFactory.getLogger(DcLineSegmentConversion.class);
}
