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
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class DcLineSegmentConversion extends AbstractIdentifiedObjectConversion {

    private static final double DEFAULT_MAXP_FACTOR = 1.2;

    DcLineSegmentConversion(PropertyBag l, HvdcLine.ConvertersMode mode, double r, double ratedUdc,
        DcLineSegmentConverter converter1, DcLineSegmentConverter converter2, Context context) {
        this(l, mode, r, ratedUdc, converter1, converter2, false, context);
    }

    DcLineSegmentConversion(PropertyBag l, HvdcLine.ConvertersMode mode, double r, double ratedUdc,
        DcLineSegmentConverter converter1, DcLineSegmentConverter converter2, boolean isDuplicated, Context context) {
        super("DCLineSegment", l, context);

        Objects.requireNonNull(converter1);
        Objects.requireNonNull(converter2);
        this.mode = mode;
        this.r = r;
        this.ratedUdc = ratedUdc;
        this.converter1 = converter1;
        this.converter2 = converter2;
        this.isDuplicated = isDuplicated;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void convert() {

        // arbitrary value because there is no maxP attribute in CGMES
        double maxP = getMaxP(converter1.pAC, converter2.pAC, mode);
        missing("maxP", maxP);

        HvdcLineAdder adder = context.network().newHvdcLine()
            .setR(r)
            .setNominalV(ratedUdc)
            .setActivePowerSetpoint(
                getActivePowerSetpoint(converter1.pAC, converter2.pAC, converter1.poleLossP, converter2.poleLossP, mode))
            .setMaxP(maxP)
            .setConvertersMode(mode)
            .setConverterStationId1(context.namingStrategy().getIidmId("ACDCConverter", converter1.converterId))
            .setConverterStationId2(context.namingStrategy().getIidmId("ACDCConverter", converter2.converterId));
        identify(adder, isDuplicated ? "-1" : "");
        HvdcLine hvdcLine = adder.add();

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

    private static double getActivePowerSetpoint(double pAC1, double pAC2, double poleLossP1, double poleLossP2,
                                                 HvdcLine.ConvertersMode mode) {
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

    static class DcLineSegmentConverter {
        String converterId;
        double poleLossP;
        double pAC;

        DcLineSegmentConverter(String stationId, double poleLossP, double pAC) {
            this.converterId = stationId;
            this.poleLossP = poleLossP;
            this.pAC = pAC;
        }
    }

    private final HvdcLine.ConvertersMode mode;
    private final double r;
    private final double ratedUdc;
    private final DcLineSegmentConverter converter1;
    private final DcLineSegmentConverter converter2;
    private final boolean isDuplicated;
}
