/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.List;
import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.hvdc.DcLineSegmentConversion.DcLineSegmentConverter;
import com.powsybl.cgmes.conversion.elements.hvdc.Hvdc.HvdcConverter;
import com.powsybl.cgmes.model.CgmesDcTerminal;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class CgmesDcConversion {

    private static final String TYPE = "type";
    private static final String TARGET_PPCC = "targetPpcc";
    private static final String POLE_LOSS_P = "poleLossP";
    private static final String OPERATING_MODE = "operatingMode";

    public CgmesDcConversion(CgmesModel cgmes, Context context) {

        this.cgmesModel = Objects.requireNonNull(cgmes);
        this.context = Objects.requireNonNull(context);
    }

    public void convert() {

        // Get hvdc configurations
        Adjacency adjacency = new Adjacency(cgmesModel);
        if (adjacency.isEmpty()) {
            return;
        }
        NodeEquipment nodeEquipment = new NodeEquipment(cgmesModel, adjacency);
        Islands islands = new Islands(adjacency);

        IslandsEnds islandsEnds = new IslandsEnds();
        islands.getIslandsNodes().forEach(listNodes -> islandsEnds.add(adjacency, listNodes));

        Hvdc hvdc = new Hvdc();
        islandsEnds.getIslandsEndsNodes().forEach(ien -> {
            IslandEndHvdc islandEndHvdc1 = new IslandEndHvdc();
            islandEndHvdc1.add(adjacency, nodeEquipment, ien.getNodes1());

            IslandEndHvdc islandEndHvdc2 = new IslandEndHvdc();
            islandEndHvdc2.add(adjacency, nodeEquipment, ien.getNodes2());

            hvdc.add(nodeEquipment, islandEndHvdc1, islandEndHvdc2);
        });

        // Convert to IIDM each converter - dcLineSegment configuration
        hvdc.getHvdcData().forEach(h -> convert(nodeEquipment, h.converters, h.dcLineSegments));

        // warnings
        context.dc().reportCgmesConvertersNotUsed();
        context.dc().reportCgmesDcLineSegmentNotUsed();
    }

    // Supported configurations are:
    //
    // (1) One AcDcConverter pair One DcLineSegment
    // (2) Two AcDcConverter pairs One DcLineSegment
    // (3) One AcDcConverter pair Two DcLineSegments
    //
    private void convert(NodeEquipment nodeEquipment, List<HvdcConverter> converters, List<String> dcLineSegments) {
        int converterNum = converters.size();
        int dcLineSegmentNum = dcLineSegments.size();

        if (converterNum == 1 && dcLineSegmentNum == 1) {
            oneAcDcConverterPairOneDcLineSegment(nodeEquipment, converters, dcLineSegments);
        } else if (converterNum == 2 && dcLineSegmentNum == 1) {
            twoAcDcConverterPairsOneDcLineSegment(nodeEquipment, converters, dcLineSegments);
        } else if (converterNum == 1 && dcLineSegmentNum == 2) {
            oneAcDcConverterPairTwoDcLineSegments(nodeEquipment, converters, dcLineSegments);
        } else {
            throw new PowsyblException(String.format("Unexpected HVDC configuration: Converters %d DcLineSegments %d",
                converterNum, dcLineSegmentNum));
        }
    }

    // (1)
    //         CGMES Configuration                                                              IIDM configuration
    //
    //      AcDcConverterEnd1 --- DcLineSegment --- AcDcConverterEnd2        AcDcConverterEnd1 --- DcLineSegment --- AcDcConverterEnd2
    //
    private void oneAcDcConverterPairOneDcLineSegment(NodeEquipment nodeEquipment, List<HvdcConverter> converters,
        List<String> dcLineSegments) {
        if (isHvdcWellOriented(nodeEquipment, converters.get(0).acDcConvertersEnd1, dcLineSegments.get(0))) {
            convert(converters.get(0).acDcConvertersEnd1, converters.get(0).acDcConvertersEnd2, dcLineSegments.get(0));
        } else {
            convert(converters.get(0).acDcConvertersEnd2, converters.get(0).acDcConvertersEnd1, dcLineSegments.get(0));
        }
    }

    // (2)
    //         CGMES Configuration                                                              IIDM configuration
    //
    //      AcDcConverter1End1 ---                 --- AcDcConverter1End2
    //                           |                 |                         AcDcConverter1End1 --- DcLineSegment --- AcDcConverter1End2
    //                           -- DcLineSegment --
    //                           |                 |                         AcDcConverter2End1 --- DcLineSegment-1 --- AcDcConverter2End2
    //      AcDcConverter2End1 ---                 --- AcDcConverter2End2
    //
    //      where DcLineSegment is duplicated into DcLineSegment and DcLineSegment-1
    //
    private void twoAcDcConverterPairsOneDcLineSegment(NodeEquipment nodeEquipment, List<HvdcConverter> converters,
        List<String> dcLineSegments) {
        if (isHvdcWellOriented(nodeEquipment, converters.get(0).acDcConvertersEnd1, dcLineSegments.get(0))) {
            convert(converters.get(0).acDcConvertersEnd1, converters.get(0).acDcConvertersEnd2, dcLineSegments.get(0), false);
            convert(converters.get(1).acDcConvertersEnd1, converters.get(1).acDcConvertersEnd2, dcLineSegments.get(0), true);
        } else {
            convert(converters.get(0).acDcConvertersEnd2, converters.get(0).acDcConvertersEnd1, dcLineSegments.get(0), false);
            convert(converters.get(1).acDcConvertersEnd2, converters.get(1).acDcConvertersEnd1, dcLineSegments.get(0), true);
        }
    }

    // (3)
    //         CGMES Configuration                                                              IIDM configuration
    //
    //                           --- DcLineSegment1 ---
    //                           |                    |
    //      AcDcConverterEnd1 ----                    --- AcDcConverterEnd2   AcDcConverterEnd1 --- DcLineSegment --- AcDcConverterEnd2
    //                           |                    |
    //                           --- DcLineSegment2 ---
    //
    //      where DcLineSegment = DcLineSegment1 + DcLineSegment2
    //
    private void oneAcDcConverterPairTwoDcLineSegments(NodeEquipment nodeEquipment, List<HvdcConverter> converters,
        List<String> dcLineSegments) {
        if (isHvdcWellOriented(nodeEquipment, converters.get(0).acDcConvertersEnd1, dcLineSegments.get(0))) {
            convert(converters.get(0).acDcConvertersEnd1, converters.get(0).acDcConvertersEnd2, dcLineSegments.get(0), dcLineSegments.get(1));
        } else {
            convert(converters.get(0).acDcConvertersEnd2, converters.get(0).acDcConvertersEnd1, dcLineSegments.get(0), dcLineSegments.get(1));
        }
    }

    // Determine if the converter is well orientated. It is well orientated if acDcConverterId is at end1
    private boolean isHvdcWellOriented(NodeEquipment nodeEquipment, String acDcConverterId, String dcLineSegmentId) {
        PropertyBag pb = context.dc().getCgmesDcLineSegmentPropertyBag(dcLineSegmentId);
        CgmesDcTerminal t1 = cgmesModel.dcTerminal(pb.getId(CgmesNames.DC_TERMINAL + 1));
        String node = getDcNode(cgmesModel, t1);
        return nodeEquipment.containsAcDcConverter(node, acDcConverterId);
    }

    private void convert(String acDcConverterIdEnd1, String acDcConverterIdEnd2, String dcLineSegmentId) {
        if (!convertCommonData(acDcConverterIdEnd1, acDcConverterIdEnd2, dcLineSegmentId)) {
            return;
        }
        this.r = computeR(this.dcLineSegment);

        if (createHvdc()) {
            setCommonDataUsed();
        }
    }

    private void convert(String acDcConverterIdEnd1, String acDcConverterIdEnd2, String dcLineSegmentId, boolean isDuplicated) {
        if (!convertCommonData(acDcConverterIdEnd1, acDcConverterIdEnd2, dcLineSegmentId)) {
            return;
        }
        this.r = 2.0 * computeR(this.dcLineSegment);

        // After collecting all commomData the id can be modified to guarantee uniqueness
        if (isDuplicated) {
            defineACopyOfThePropertyBagWithUniqueId(dcLineSegment, dcLineSegmentId);
        }

        if (createHvdc()) {
            setCommonDataUsed();
        }
    }

    // We do not use "#n" to guarantee uniqueness since the getId() method does not support more than one '#' character
    private void defineACopyOfThePropertyBagWithUniqueId(PropertyBag dcLineSegment, String dcLineSegmentId) {

        // Create a copy of the property bag and change the id to guarantee uniqueness
        PropertyBag dcLineSegmentCopy = dcLineSegment.copy();

        String completeId = dcLineSegmentCopy.get("DCLineSegment");
        String uniqueId = dcLineSegmentId + "-1";
        dcLineSegmentCopy.put("DCLineSegment", completeId.replace(dcLineSegmentId, uniqueId));

        this.dcLineSegmentId = uniqueId;
        this.dcLineSegment = dcLineSegmentCopy;
    }

    private void convert(String acDcConverterIdEnd1, String acDcConverterIdEnd2, String dcLineSegmentId1, String dcLineSegmentId2) {
        if (!convertCommonData(acDcConverterIdEnd1, acDcConverterIdEnd2, dcLineSegmentId1)) {
            return;
        }
        PropertyBag dcLineSegment2 = context.dc().getCgmesDcLineSegmentPropertyBag(dcLineSegmentId2);
        if (dcLineSegment2 == null) {
            return;
        }
        this.r = 1.0 / (1.0 / computeR(this.dcLineSegment) + 1.0 / computeR(dcLineSegment2));

        if (createHvdc()) {
            setCommonDataUsed();
            context.dc().setCgmesDcLineSegmentUsed(dcLineSegmentId2);
        }
    }

    private void setCommonDataUsed() {
        context.dc().setCgmesConverterUsed(converter1Id);
        context.dc().setCgmesConverterUsed(converter2Id);
        context.dc().setCgmesDcLineSegmentUsed(dcLineSegmentId);
    }

    private boolean convertCommonData(String acDcConverterIdEnd1, String acDcConverterIdEnd2, String dcLineSegmentId) {
        this.converter1Id = acDcConverterIdEnd1;
        this.cconverter1 = context.dc().getCgmesConverterPropertyBag(acDcConverterIdEnd1);
        this.converter2Id = acDcConverterIdEnd2;
        this.cconverter2 = context.dc().getCgmesConverterPropertyBag(acDcConverterIdEnd2);
        this.dcLineSegmentId = dcLineSegmentId;
        this.dcLineSegment = context.dc().getCgmesDcLineSegmentPropertyBag(dcLineSegmentId);
        if (this.cconverter1 == null || this.cconverter2 == null || this.dcLineSegment == null) {
            return false;
        }
        this.converterType = decodeType(this.cconverter1.getLocal(TYPE));
        if (this.converterType == null || converterType != decodeType(this.cconverter2.getLocal(TYPE))) {
            return false;
        }
        this.operatingMode = decodeMode(this.converterType, this.cconverter1, this.cconverter2);
        this.ratedUdc = computeRatedUdc(this.cconverter1, this.cconverter2);

        return true;
    }

    private static HvdcType decodeType(String stype) {
        if (stype.equals("VsConverter")) {
            return HvdcType.VSC;
        } else if (stype.equals("CsConverter")) {
            return HvdcType.LCC;
        }
        throw new PowsyblException("Unexpected HVDC type: " + stype);
    }

    private static HvdcLine.ConvertersMode decodeMode(HvdcType converterType, PropertyBag cconverter1, PropertyBag cconverter2) {
        String mode1 = cconverter1.getLocal(OPERATING_MODE);
        String mode2 = cconverter2.getLocal(OPERATING_MODE);

        if (converterType.equals(HvdcConverterStation.HvdcType.LCC)) {
            if (inverter(mode1) && rectifier(mode2)) {
                return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
            } else if (rectifier(mode1) && inverter(mode2)) {
                return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            } else if (cconverter1.asDouble(TARGET_PPCC) == 0 && cconverter2.asDouble(TARGET_PPCC) == 0) {
                // Both ends are rectifier or inverter
                return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
            } else {
                LOG.warn("Undefined converter mode for the HVDC, assumed to be of type \"Side1 Rectifier - Side2 Inverter\"");
                return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            }
        } else {
            if (cconverter1.asDouble(TARGET_PPCC) > 0 || cconverter2.asDouble(TARGET_PPCC) < 0) {
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

    private double computeR(PropertyBag dcLs) {
        double rDcLink = dcLs.asDouble("r", 0);
        if (rDcLink < 0) {
            double rDcLink1 = 0.1;
            context.fixed("resistance", "was negative", rDcLink, rDcLink1);
            rDcLink = rDcLink1;
        }
        return rDcLink;
    }

    private static double computeRatedUdc(PropertyBag cconverter1, PropertyBag cconverter2) {
        double ratedUdc1 = cconverter1.asDouble(CgmesNames.RATED_UDC);
        double ratedUdc2 = cconverter2.asDouble(CgmesNames.RATED_UDC);
        if (ratedUdc1 != 0) {
            return ratedUdc1;
        }
        return ratedUdc2;
    }

    private boolean createHvdc() {

        // poleLossP is the active power loss at a DC Pole
        // for lossless operation: P(DC) = P(AC) => lossFactor = 0
        // for rectifier operation (conversion from AC to DC) with losses: P(DC) = P(AC) - poleLossP
        // In IIDM, for rectifier operation P(DC) / P(AC) = 1 - lossFactor / 100
        // => P(DC) / P(AC) = 1 - poleLossP / P(AC) = 1 - lossFactor / 100
        // for inverter operation (conversion from DC to AC) with losses: P(DC) = P(AC) + poleLossP
        // In IIDM, for inverter operation P(AC) / P(DC) = 1 - lossFactor / 100
        // => P(AC) / P(DC) = 1 - poleLossP / P(DC) = 1 - poleLossP / (P(AC) + poleLossP) = 1 - lossFactor / 100

        double poleLossP1 = cconverter1.asDouble(POLE_LOSS_P);
        double poleLossP2 = cconverter2.asDouble(POLE_LOSS_P);

        // load sign convention is used i.e. positive sign means flow out from a node
        // i.e. pACx >= 0 if converterx is rectifier and pACx <= 0 if converterx is
        // inverter

        double pAC1 = getPAc(cconverter1);
        double pAC2 = getPAc(cconverter2);

        LossFactor lossFactor = new LossFactor(context, operatingMode, pAC1, pAC2, poleLossP1, poleLossP2);
        lossFactor.compute();

        AcDcConverterConversion acDcConverterConversion1 = new AcDcConverterConversion(cconverter1, converterType, lossFactor.getLossFactor1(), context);
        AcDcConverterConversion acDcConverterConversion2 = new AcDcConverterConversion(cconverter2, converterType, lossFactor.getLossFactor2(), context);
        DcLineSegmentConverter converter1 = new DcLineSegmentConverter(converter1Id, poleLossP1, pAC1);
        DcLineSegmentConverter converter2 = new DcLineSegmentConverter(converter2Id, poleLossP2, pAC2);
        DcLineSegmentConversion dcLineSegmentConversion = new DcLineSegmentConversion(dcLineSegment, operatingMode, r, ratedUdc, converter1, converter2, context);

        if (!acDcConverterConversion1.valid() || !acDcConverterConversion2.valid() || !dcLineSegmentConversion.valid()) {
            return false;
        }

        if (converterType == HvdcType.VSC) {
            acDcConverterConversion1.convert();
            acDcConverterConversion2.convert();
            dcLineSegmentConversion.convert();

        } else { // LCC
            acDcConverterConversion1.convert();
            acDcConverterConversion2.convert();
            dcLineSegmentConversion.convert();

            updatePowerFactor(acDcConverterConversion1);
            updatePowerFactor(acDcConverterConversion2);
        }
        return true;
    }

    private static double getPAc(PropertyBag p) {
        // targetPpcc is the real power injection target in the AC grid in CGMES
        return Double.isNaN(p.asDouble(TARGET_PPCC)) ? 0 : p.asDouble(TARGET_PPCC);
    }

    private static void updatePowerFactor(AcDcConverterConversion acDcConverterConversion) {
        if (acDcConverterConversion == null) {
            return;
        }
        LccConverterStation iconverter = acDcConverterConversion.getLccConverter();
        double powerFactor = getPowerFactor(iconverter);
        if (!Double.isNaN(powerFactor)) {
            acDcConverterConversion.setLccPowerFactor(powerFactor);
        }
    }

    private static double getPowerFactor(LccConverterStation iconverter) {
        return iconverter.getTerminal().getP()
            / Math.hypot(iconverter.getTerminal().getP(), iconverter.getTerminal().getQ());
    }

    static String getAcNode(CgmesModel cgmesModel, CgmesTerminal terminal) {
        if (cgmesModel.isNodeBreaker()) {
            return terminal.connectivityNode();
        } else {
            return terminal.topologicalNode();
        }
    }

    static String getDcNode(CgmesModel cgmesModel, CgmesDcTerminal terminal) {
        if (cgmesModel.isNodeBreaker()) {
            return terminal.dcNode();
        } else {
            return terminal.dcTopologicalNode();
        }
    }

    private final CgmesModel cgmesModel;
    private final Context context;

    private HvdcType converterType;
    private HvdcLine.ConvertersMode operatingMode;
    private String converter1Id;
    private PropertyBag cconverter1;
    private String converter2Id;
    private PropertyBag cconverter2;
    private String dcLineSegmentId;
    private PropertyBag dcLineSegment;
    private double r;
    private double ratedUdc;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesDcConversion.class);
}
