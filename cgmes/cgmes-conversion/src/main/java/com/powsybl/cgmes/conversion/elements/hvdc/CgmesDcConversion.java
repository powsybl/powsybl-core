/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.elements.hvdc.Hvdc.HvdcConverter;
import com.powsybl.cgmes.model.CgmesDcTerminal;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class CgmesDcConversion {

    private static final String TYPE = "type";

    public CgmesDcConversion(CgmesModel cgmes, Context context) {

        this.cgmesModel = Objects.requireNonNull(cgmes);
        this.context = Objects.requireNonNull(context);
    }

    public void convert() {

        // Get hvdc configurations
        AcDcConverterNodes acDcConverterNodes = new AcDcConverterNodes(cgmesModel);

        Adjacency adjacency = new Adjacency(cgmesModel, acDcConverterNodes);
        if (adjacency.isEmpty()) {
            return;
        }
        NodeEquipment nodeEquipment = new NodeEquipment(cgmesModel, acDcConverterNodes, adjacency);
        Islands islands = new Islands(adjacency);

        IslandsEnds islandsEnds = new IslandsEnds();
        islands.getIslandsNodes().forEach(listNodes -> islandsEnds.add(adjacency, nodeEquipment, listNodes));

        Hvdc hvdc = new Hvdc();
        islandsEnds.getIslandsEndsNodes().forEach(ien -> {
            IslandEndHvdc islandEndHvdc1 = new IslandEndHvdc();
            islandEndHvdc1.add(adjacency, nodeEquipment, ien.getNodes1());

            IslandEndHvdc islandEndHvdc2 = new IslandEndHvdc();
            islandEndHvdc2.add(adjacency, nodeEquipment, ien.getNodes2());

            hvdc.add(nodeEquipment, islandEndHvdc1, islandEndHvdc2);
        });

        // Convert to IIDM each converter - dcLineSegment configuration
        hvdc.getHvdcData().forEach(h -> convert(acDcConverterNodes, adjacency, h.converters, h.dcLineSegments));

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
    private void convert(AcDcConverterNodes acDcConverterNodes, Adjacency adjacency, List<HvdcConverter> converters,
        List<String> dcLineSegments) {
        int converterNum = converters.size();
        int dcLineSegmentNum = dcLineSegments.size();

        if (converterNum == 1 && dcLineSegmentNum == 1) {
            oneAcDcConverterPairOneDcLineSegment(acDcConverterNodes, adjacency, converters, dcLineSegments);
        } else if (converterNum == 2 && dcLineSegmentNum == 1) {
            twoAcDcConverterPairsOneDcLineSegment(acDcConverterNodes, adjacency, converters, dcLineSegments);
        } else if (converterNum == 1 && dcLineSegmentNum == 2) {
            oneAcDcConverterPairTwoDcLineSegments(acDcConverterNodes, adjacency, converters, dcLineSegments);
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
    private void oneAcDcConverterPairOneDcLineSegment(AcDcConverterNodes acDcConverterNodes, Adjacency adjacency,
        List<HvdcConverter> converters, List<String> dcLineSegments) {
        convert(acDcConverterNodes, adjacency, converters.get(0).acDcConvertersEnd1, converters.get(0).acDcConvertersEnd2, dcLineSegments.get(0));
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
    private void twoAcDcConverterPairsOneDcLineSegment(AcDcConverterNodes acDcConverterNodes, Adjacency adjacency,
        List<HvdcConverter> converters, List<String> dcLineSegments) {
        convert(acDcConverterNodes, adjacency, converters.get(0).acDcConvertersEnd1, converters.get(0).acDcConvertersEnd2, dcLineSegments.get(0), false);
        convert(acDcConverterNodes, adjacency, converters.get(1).acDcConvertersEnd1, converters.get(1).acDcConvertersEnd2, dcLineSegments.get(0), true);
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
    private void oneAcDcConverterPairTwoDcLineSegments(AcDcConverterNodes acDcConverterNodes, Adjacency adjacency,
        List<HvdcConverter> converters, List<String> dcLineSegments) {
        convert(acDcConverterNodes, adjacency, converters.get(0).acDcConvertersEnd1, converters.get(0).acDcConvertersEnd2, dcLineSegments.get(0), dcLineSegments.get(1));
    }

    private void convert(AcDcConverterNodes acDcConverterNodes, Adjacency adjacency, String acDcConverterIdEnd1,
        String acDcConverterIdEnd2, String dcLineSegmentId) {
        if (!convertCommonData(acDcConverterNodes, adjacency, acDcConverterIdEnd1, acDcConverterIdEnd2, dcLineSegmentId)) {
            return;
        }
        this.r = computeR(this.dcLineSegment);

        if (createHvdc()) {
            setCommonDataUsed();
        }
    }

    private void convert(AcDcConverterNodes acDcConverterNodes, Adjacency adjacency, String acDcConverterIdEnd1,
        String acDcConverterIdEnd2, String dcLineSegmentId, boolean isDuplicated) {
        if (!convertCommonData(acDcConverterNodes, adjacency, acDcConverterIdEnd1, acDcConverterIdEnd2, dcLineSegmentId)) {
            return;
        }
        this.r = 2.0 * computeR(this.dcLineSegment);

        if (createHvdc(isDuplicated)) {
            setCommonDataUsed();
        }
    }

    private void convert(AcDcConverterNodes acDcConverterNodes, Adjacency adjacency, String acDcConverterIdEnd1,
        String acDcConverterIdEnd2, String dcLineSegmentId1, String dcLineSegmentId2) {
        if (!convertCommonData(acDcConverterNodes, adjacency, acDcConverterIdEnd1, acDcConverterIdEnd2, dcLineSegmentId1)) {
            return;
        }
        PropertyBag dcLineSegment2 = context.dc().getCgmesDcLineSegmentPropertyBag(dcLineSegmentId2);
        if (dcLineSegment2 == null) {
            return;
        }
        this.r = 1.0 / (1.0 / computeR(this.dcLineSegment) + 1.0 / computeR(dcLineSegment2));

        if (createHvdc()) {
            setCommonDataUsed();
            // Add the second line segment identifier as an alias of the line created
            HvdcLine line = context.network().getHvdcLine(dcLineSegmentId1);
            if (line != null) {
                line.addAlias(dcLineSegmentId2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCLineSegment2");
            }
            context.dc().setCgmesDcLineSegmentUsed(dcLineSegmentId2);
        }
    }

    private void setCommonDataUsed() {
        context.dc().setCgmesConverterUsed(converter1Id);
        context.dc().setCgmesConverterUsed(converter2Id);
        context.dc().setCgmesDcLineSegmentUsed(dcLineSegmentId);
    }

    private boolean convertCommonData(AcDcConverterNodes acDcConverterNodes, Adjacency adjacency,
        String acDcConverterId1, String acDcConverterId2, String dcLineSegmentId) {

        PropertyBag pbDcLineSegment = context.dc().getCgmesDcLineSegmentPropertyBag(dcLineSegmentId);
        CgmesDcTerminal t1 = cgmesModel.dcTerminal(pbDcLineSegment.getId(CgmesNames.DC_TERMINAL + 1));
        String dcNode1 = getDcNode(cgmesModel, t1);
        CgmesDcTerminal t2 = cgmesModel.dcTerminal(pbDcLineSegment.getId(CgmesNames.DC_TERMINAL + 2));
        String dcNode2 = getDcNode(cgmesModel, t2);

        // Determine if the converter is well orientated. It is well orientated if acDcConverterId1 is at end1

        String acDcConverterIdEnd1 = findAcDcConverterConnectedTo(acDcConverterNodes, adjacency, acDcConverterId1, acDcConverterId2, dcNode1);
        String acDcConverterIdEnd2 = acDcConverterIdEnd1.equals(acDcConverterId1) ? acDcConverterId2 : acDcConverterId1;

        // In some cases DcLineSegment DcNode1 and acDcConverterDcNode1 are different.
        // Same happens with DcLineSegment DcNode2 and acDcConverterDcNode2
        String acDcConverterDcNode1 = findAcDcConverterNode(acDcConverterNodes, adjacency, acDcConverterIdEnd1, dcNode1);
        String acDcConverterDcNode2 = findAcDcConverterNode(acDcConverterNodes, adjacency, acDcConverterIdEnd2, dcNode2);

        this.converter1Id = acDcConverterIdEnd1;
        this.cconverter1 = context.dc().getCgmesConverterPropertyBag(acDcConverterIdEnd1);
        this.acDcConverterDcTerminal1Id = findAcDcConverterDcTerminal(acDcConverterIdEnd1, acDcConverterDcNode1);
        this.converter2Id = acDcConverterIdEnd2;
        this.cconverter2 = context.dc().getCgmesConverterPropertyBag(acDcConverterIdEnd2);
        this.acDcConverterDcTerminal2Id = findAcDcConverterDcTerminal(acDcConverterIdEnd2, acDcConverterDcNode2);
        this.dcLineSegmentId = dcLineSegmentId;
        this.dcLineSegment = pbDcLineSegment;
        if (this.cconverter1 == null || this.cconverter2 == null) {
            return false;
        }
        this.converterType = decodeType(this.cconverter1.getLocal(TYPE));
        if (this.converterType == null || converterType != decodeType(this.cconverter2.getLocal(TYPE))) {
            return false;
        }
        this.operatingMode = HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
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

    private String findAcDcConverterDcTerminal(String acDcConverterId, String dcNodeId) {
        String terminalNodeProperty = context.nodeBreaker() ? "DCNode" : "DCTopologicalNode";
        return context.cgmes().dcTerminals().stream()
                // A terminal of this converter
                .filter(t -> acDcConverterId.equals(t.getId("DCConductingEquipment")))
                // The equipment type of the terminal must be a converter (redundant, but safer)
                .filter(t -> isAcDcConverter(t.getId("dcConductingEquipmentType")))
                // The terminal is connected to the node we are looking for
                .filter(t -> dcNodeId.equals(t.getId(terminalNodeProperty)))
                .findFirst()
                .map(t -> t.getId("DCTerminal"))
                .orElseThrow(() -> new PowsyblException(String.format("Missing terminal for converter %s at %s %s", acDcConverterId, terminalNodeProperty, dcNodeId)));
    }

    private static boolean isAcDcConverter(String type) {
        return type != null && (type.equals("CsConverter") || type.equals("VsConverter"));
    }

    private boolean createHvdc() {
        return createHvdc(false);
    }

    private boolean createHvdc(boolean isDuplicated) {

        AcDcConverterConversion acDcConverterConversion1 = new AcDcConverterConversion(cconverter1, converterType, acDcConverterDcTerminal1Id, context);
        AcDcConverterConversion acDcConverterConversion2 = new AcDcConverterConversion(cconverter2, converterType, acDcConverterDcTerminal2Id, context);
        DcLineSegmentConversion dcLineSegmentConversion = new DcLineSegmentConversion(dcLineSegment, operatingMode, r, ratedUdc, converter1Id, converter2Id, isDuplicated, context);

        if (!acDcConverterConversion1.valid() || !acDcConverterConversion2.valid() || !dcLineSegmentConversion.valid()) {
            return false;
        }

        acDcConverterConversion1.convert();
        acDcConverterConversion2.convert();
        dcLineSegmentConversion.convert();

        return true;
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

    private static String findAcDcConverterConnectedTo(AcDcConverterNodes acDcConverterNodes, Adjacency adjacency,
        String acDcConverterId1, String acDcConverterId2, String dcNode) {
        List<String> dcNodes1 = acDcConverterNodes.getDcNodes(acDcConverterId1);
        if (dcNodes1.stream().anyMatch(dcNode1 -> isConnectedByOneStep(adjacency, dcNode1, dcNode))) {
            return acDcConverterId1;
        }
        List<String> dcNodes2 = acDcConverterNodes.getDcNodes(acDcConverterId2);
        if (dcNodes2.stream().anyMatch(dcNode2 -> isConnectedByOneStep(adjacency, dcNode2, dcNode))) {
            return acDcConverterId2;
        }
        throw new PowsyblException(String.format(
            "Unexpected HVDC configuration: One of the two converters %s, %s must be connected to the dcNode %s",
            acDcConverterId1, acDcConverterId2, dcNode));
    }

    private static String findAcDcConverterNode(AcDcConverterNodes acDcConverterNodes, Adjacency adjacency,
        String acDcConverterId, String dcNode) {
        List<String> dcNodes = acDcConverterNodes.getDcNodes(acDcConverterId);
        Optional<String> optional = dcNodes.stream().filter(dcNodeConverter -> isConnectedByOneStep(adjacency, dcNodeConverter, dcNode)).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new PowsyblException(
            String.format("Unexpected HVDC configuration: AcDcConverter %s must be connected to the dcNode %s",
                acDcConverterId, dcNode));
    }

    // Only one step is necessary for the supported configurations
    private static boolean isConnectedByOneStep(Adjacency adjacency, String dcNode1, String dcNode2) {
        if (dcNode1.equals(dcNode2)) {
            return true;
        }
        return adjacency.areAdjacentsByAcDcConverter(dcNode1, dcNode2);
    }

    private final CgmesModel cgmesModel;
    private final Context context;

    private HvdcType converterType;
    private HvdcLine.ConvertersMode operatingMode;
    private String converter1Id;
    private PropertyBag cconverter1;
    private String acDcConverterDcTerminal1Id;
    private String converter2Id;
    private PropertyBag cconverter2;
    private String acDcConverterDcTerminal2Id;
    private String dcLineSegmentId;
    private PropertyBag dcLineSegment;
    private double r;
    private double ratedUdc;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesDcConversion.class);
}
