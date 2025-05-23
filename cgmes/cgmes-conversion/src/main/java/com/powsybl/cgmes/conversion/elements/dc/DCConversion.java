/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.conversion.CgmesReports;
import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.PowsyblException;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.model.CgmesNames.*;
import static java.lang.Math.min;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class DCConversion {

    private final Context context;

    private PropertyBags cgmesDcTerminalNodes;
    private PropertyBags cgmesAcDcConverters;
    private PropertyBags cgmesDcLineSegments;
    private PropertyBags cgmesDcSwitches;
    private PropertyBags cgmesDcGrounds;

    private final Map<String, String> dcTerminalNodes = new HashMap<>();
    private final Set<DCEquipment> dcEquipments = new HashSet<>();
    private final Set<DCIslandEnd> dcIslandEnds = new HashSet<>();
    private final Set<DCIsland> dcIslands = new HashSet<>();

    public DCConversion(CgmesModel cgmesModel, Context context) {
        this.context = Objects.requireNonNull(context);
        cacheCgmesData(cgmesModel);
        computeDcData();
        convert();
    }

    private void cacheCgmesData(CgmesModel cgmesModel) {
        cgmesDcTerminalNodes = cgmesModel.dcTerminals();
        cgmesAcDcConverters = cgmesModel.acDcConverters();
        cgmesDcLineSegments = cgmesModel.dcLineSegments();
        cgmesDcSwitches = cgmesModel.dcSwitches();
        cgmesDcGrounds = cgmesModel.dcGrounds();
    }

    public void computeDcData() {
        computeDcEquipments();
        computeDcIslandEnds();
        computeDcIslands();
    }

    private void computeDcEquipments() {
        // Store the CGMES terminal to CGMES node association.
        String node = context.nodeBreaker() ? DC_NODE : DC_TOPOLOGICAL_NODE;
        cgmesDcTerminalNodes.forEach(t -> dcTerminalNodes.put(t.getId(DC_TERMINAL), t.getId(node)));

        // Store the CGMES DCEquipment base data: id, type, node1, node2
        cgmesAcDcConverters.forEach(c -> addDcEquipment(c, ACDC_CONVERTER));
        cgmesDcLineSegments.forEach(l -> addDcEquipment(l, DC_LINE_SEGMENT));
        cgmesDcSwitches.forEach(s -> addDcEquipment(s, DC_SWITCH));
        cgmesDcGrounds.forEach(g -> addDcEquipment(g, DC_GROUND));
    }

    private void addDcEquipment(PropertyBag propertyBag, String type) {
        dcEquipments.add(new DCEquipment(
                propertyBag.getId(type),
                ACDC_CONVERTER.equals(type) ? propertyBag.getLocal("type") : type,
                terminalNode(propertyBag.getId(DC_TERMINAL1)),
                DC_GROUND.equals(type) ? null : terminalNode(propertyBag.getId(DC_TERMINAL2))
        ));
    }

    private String terminalNode(String terminalId) {
        // Get the CGMES node of a CGMES terminal.
        if (!dcTerminalNodes.containsKey(terminalId)) {
            throw new PowsyblException("DCTerminal not found");
        }
        return dcTerminalNodes.get(terminalId);
    }

    private void computeDcIslandEnds() {
        // Compute DCIslandEnd: a collection of DCEquipment connected together on the same side of a DCLineSegment.
        // This comprises DCLineSegment, so a specific DCLineSegment shall be present in 2 DCIslandEnds.
        // The exploration starts with ACDCConverters, this ensures that an island end has at least one converter.
        Set<DCEquipment> visitedDcEquipments = new HashSet<>();
        dcEquipments.stream().filter(DCEquipment::isConverter)
                .forEach(acDcConverter -> {
                    if (!visitedDcEquipments.contains(acDcConverter)) {
                        Set<DCEquipment> dcIslandEnd = new HashSet<>();
                        getAdjacentDcEquipments(Set.of(acDcConverter), dcIslandEnd, visitedDcEquipments);
                        dcIslandEnds.add(new DCIslandEnd(dcIslandEnd));
                    }
                });

        // Log not visited DCEquipment.
        Set<DCEquipment> notVisitedDcEquipments = new HashSet<>(dcEquipments);
        notVisitedDcEquipments.removeAll(visitedDcEquipments);
        notVisitedDcEquipments.forEach(dcEquipment ->
                CgmesReports.notVisitedDcEquipmentReport(context.getReportNode(), dcEquipment.id()));
    }

    private void getAdjacentDcEquipments(Set<DCEquipment> adjacentDcEquipments, Set<DCEquipment> dcIslandEnd, Set<DCEquipment> visitedDcEquipments) {
        // Recursively get all adjacent DCEquipment.
        // DCLineSegment are included but not traversed.
        for (DCEquipment adjacentDcEquipment : adjacentDcEquipments) {
            if (!dcIslandEnd.contains(adjacentDcEquipment)) {
                dcIslandEnd.add(adjacentDcEquipment);
                visitedDcEquipments.add(adjacentDcEquipment);
                if (!adjacentDcEquipment.isLine()) {
                    Set<DCEquipment> nextDcEquipments = dcEquipments.stream()
                            .filter(e -> e != adjacentDcEquipment && e.isAdjacentTo(adjacentDcEquipment))
                            .collect(Collectors.toSet());
                    getAdjacentDcEquipments(nextDcEquipments, dcIslandEnd, visitedDcEquipments);
                }
            }
        }
    }

    private void computeDcIslands() {
        // Compute DCIsland: a collection of DCIslandEnd connected together via shared DCLineSegment(s).
        Set<DCIslandEnd> visitedDcIslandEnds = new HashSet<>();
        dcIslandEnds.forEach(dcIslandEnd -> {
            if (!visitedDcIslandEnds.contains(dcIslandEnd)) {
                Set<DCIslandEnd> dcIsland = new HashSet<>();
                getAdjacentDcIslandEnds(Set.of(dcIslandEnd), dcIsland, visitedDcIslandEnds);
                dcIslands.add(new DCIsland(dcIsland));
            }
        });
    }

    private void getAdjacentDcIslandEnds(Set<DCIslandEnd> adjacentDcIslandEnds, Set<DCIslandEnd> dcIsland, Set<DCIslandEnd> visitedDcIslandEnds) {
        // Recursively get all adjacent DCIslandEnd.
        for (DCIslandEnd adjacentDcIslandEnd : adjacentDcIslandEnds) {
            if (!visitedDcIslandEnds.contains(adjacentDcIslandEnd)) {
                dcIsland.add(adjacentDcIslandEnd);
                visitedDcIslandEnds.add(adjacentDcIslandEnd);
                Set<DCIslandEnd> nextDcIslandEnds = dcIslandEnds.stream()
                        .filter(end -> end != adjacentDcIslandEnd && end.isAdjacentTo(adjacentDcIslandEnd))
                        .collect(Collectors.toSet());
                getAdjacentDcIslandEnds(nextDcIslandEnds, dcIsland, visitedDcIslandEnds);
            }
        }
    }

    public void convert() {
        for (DCIsland dcIsland : dcIslands) {
            if (dcIsland.valid(context)) {
                convertDcLinks(dcIsland);
            }
        }
    }

    private void convertDcLinks(DCIsland dcIsland) {
        // Sort island ends so that the lowest converter id is on side 1.
        List<DCIslandEnd> islandEnds = dcIsland.dcIslandEnds().stream()
                .sorted(Comparator.comparing(e -> e.getAcDcConverters().stream()
                        .map(DCEquipment::id)
                        .min(Comparator.naturalOrder())
                        .orElseThrow()))
                .toList();

        // Retrieve ACDCConverter and DCLineSegment.
        List<DCEquipment> converters1 = islandEnds.get(0).getAcDcConverters();
        List<DCEquipment> converters2 = islandEnds.get(1).getAcDcConverters();
        List<DCEquipment> dcLineSegments = islandEnds.get(0).getDcLineSegments();

        // Separate elements into DCLink (2 converters, 1 line and an optional second line).
        List<DCLink> dcLinks = new ArrayList<>();
        if (converters1.size() == 1 && dcLineSegments.size() == 2) {
            // This is a monopole with a metallic return line.
            // Create a DCLink for the pole. Add the metallic return line.
            PropertyBag converter1 = getConverterBag(converters1.get(0));
            PropertyBag converter2 = getConverterBag(converters2.get(0));
            PropertyBag dcLine1 = getDcLineSegmentBag(dcLineSegments.get(0));
            PropertyBag dcLine2 = getDcLineSegmentBag(dcLineSegments.get(1));

            dcLinks.add(new DCLink(converter1, converter2, dcLine1, dcLine2));
        } else if (converters1.size() == min(dcLineSegments.size(), 2)) {
            // This is either a monopole or a bipole.
            // Create a DCLink per pole. In case of a bipole with DMR line, add it to the first pole.
            Set<DCEquipment> usedConverters1 = new HashSet<>();
            Set<DCEquipment> usedConverters2 = new HashSet<>();
            PropertyBag dMRLine = dcLineSegments.size() == 3 ? getDMRLineBag(dcLineSegments.get(2)) : null;
            for (DCEquipment dcLineEq : dcLineSegments.stream().limit(2).toList()) {
                DCEquipment converter1Eq = islandEnds.get(0).getNearestConverter(dcLineEq, usedConverters1);
                DCEquipment converter2Eq = islandEnds.get(1).getNearestConverter(dcLineEq, usedConverters2);

                PropertyBag converter1 = getConverterBag(converter1Eq);
                PropertyBag converter2 = getConverterBag(converter2Eq);
                PropertyBag dcLine = getDcLineSegmentBag(dcLineEq);

                dcLinks.add(new DCLink(converter1, converter2, dcLine, dMRLine));
                dMRLine = null;
            }
        } else if (converters1.size() == 2 * min(dcLineSegments.size(), 2)) {
            // This is either a monopole or a bipole, but each pole has 2 ACDCConverter per unit
            // (each pole has 2 * 6-pulses bridges instead of 1 * 12-pulses bridge).
            // Create 2 DCLink per pole. In case of a bipole with DMR line, add it to the first DCLink of the first pole.
            Set<DCEquipment> usedConverters1 = new HashSet<>();
            Set<DCEquipment> usedConverters2 = new HashSet<>();
            PropertyBag dMRLine = dcLineSegments.size() == 3 ? getDMRLineBag(dcLineSegments.get(2)) : null;
            for (DCEquipment dcLineEq : dcLineSegments.stream().limit(2).toList()) {
                DCEquipment converter1AEq = islandEnds.get(0).getNearestConverter(dcLineEq, usedConverters1);
                DCEquipment converter2AEq = islandEnds.get(1).getNearestConverter(dcLineEq, usedConverters2);
                DCEquipment converter1BEq = islandEnds.get(0).getNearestConverter(converter1AEq, usedConverters1);
                DCEquipment converter2BEq = islandEnds.get(1).getNearestConverter(converter2AEq, usedConverters2);

                PropertyBag converter1A = getConverterBag(converter1AEq);
                PropertyBag converter2A = getConverterBag(converter2AEq);
                PropertyBag dcLineA = getDcLineSegmentBag(dcLineEq);
                PropertyBag converter1B = getConverterBag(converter1BEq);
                PropertyBag converter2B = getConverterBag(converter2BEq);
                PropertyBag dcLineB = splitDcLineSegmentBag(dcLineA);

                dcLinks.add(new DCLink(converter1A, converter2A, dcLineA, dMRLine));
                dcLinks.add(new DCLink(converter1B, converter2B, dcLineB, null));
                dMRLine = null;
            }
        } else {
            throw new PowsyblException(String.format("Invalid DCConfiguration. Number of converters1: %d, "
                            + "Number of converters2: %d, number of dc lines: %d",
                    converters1.size(), converters2.size(), dcLineSegments.size()));
        }

        // Convert DCLink.
        dcLinks.forEach(dcLink -> {
            new HvdcConverterConversion(dcLink.getConverter1(), dcLink.getLossFactor1(), context).convert();
            new HvdcConverterConversion(dcLink.getConverter2(), dcLink.getLossFactor2(), context).convert();
            new HvdcLineConversion(dcLink, context).convert();
        });
    }

    private PropertyBag splitDcLineSegmentBag(PropertyBag dcLineSegment) {
        // Divide the original resistance by 2, and make the identifiers of the copy unique.
        double r = dcLineSegment.asDouble("r");
        r = Double.isNaN(r) ? 0.1 : r / 2;
        dcLineSegment.put("r", Double.toString(r));
        PropertyBag otherDcLineSegment = (PropertyBag) dcLineSegment.clone();
        otherDcLineSegment.put(DC_LINE_SEGMENT, otherDcLineSegment.get(DC_LINE_SEGMENT) + "-1");
        otherDcLineSegment.put(DC_TERMINAL1, otherDcLineSegment.get(DC_TERMINAL1) + "-1");
        otherDcLineSegment.put(DC_TERMINAL2, otherDcLineSegment.get(DC_TERMINAL2) + "-1");
        otherDcLineSegment.put("name", otherDcLineSegment.get("name") + "-1");
        return otherDcLineSegment;
    }

    private PropertyBag getDMRLineBag(DCEquipment dcLineSegment) {
        // The Dedicated Metallic Return line is retrieved solely to keep its id as an alias.
        // Its resistance should not be taken into account since there is no flow through it in nominal cases.
        PropertyBag metallicReturnLineBag = getDcLineSegmentBag(dcLineSegment);
        metallicReturnLineBag.put("r", "0");
        return metallicReturnLineBag;
    }

    private PropertyBag getConverterBag(DCEquipment acDcConverter) {
        return getPropertyBag(acDcConverter, cgmesAcDcConverters, ACDC_CONVERTER);
    }

    private PropertyBag getDcLineSegmentBag(DCEquipment dcLineSegment) {
        return getPropertyBag(dcLineSegment, cgmesDcLineSegments, DC_LINE_SEGMENT);
    }

    private PropertyBag getPropertyBag(DCEquipment dcEquipment, PropertyBags cachedPropertyBags, String propertyKey) {
        return cachedPropertyBags.stream()
                .filter(b -> b.getId(propertyKey).equals(dcEquipment.id()))
                .findFirst()
                .orElseThrow();
    }

}
