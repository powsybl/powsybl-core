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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class DCConversion {

    private final Context context;

    private PropertyBags cgmesAcDcConverters;
    private PropertyBags cgmesDcLineSegments;
    private PropertyBags cgmesDcSwitches;
    private PropertyBags cgmesDcGrounds;

    private final Set<DCEquipment> dcEquipments = new HashSet<>();
    private final Set<DCIslandEnd> dcIslandEnds = new HashSet<>();
    private final Set<DCIsland> dcIslands = new HashSet<>();

    public DCConversion(Context context) {
        this.context = Objects.requireNonNull(context);
        cacheCgmesData(context.cgmes());
        computeDcData();
        convert();
    }

    private void cacheCgmesData(CgmesModel cgmesModel) {
        cgmesAcDcConverters = cgmesModel.acDcConverters();
        cgmesDcLineSegments = cgmesModel.dcLineSegments();
        cgmesDcSwitches = cgmesModel.dcSwitches();
        cgmesDcGrounds = cgmesModel.dcGrounds();
    }

    private void computeDcData() {
        computeDcEquipments();
        computeDcIslandEnds();
        computeDcIslands();
    }

    private void computeDcEquipments() {
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
                context.dcMapping().getDcNode(propertyBag.getId(DC_TERMINAL1)),
                DC_GROUND.equals(type) ? null : context.dcMapping().getDcNode(propertyBag.getId(DC_TERMINAL2))
        ));
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
                        getAdjacentDcEquipments(acDcConverter, dcIslandEnd);
                        visitedDcEquipments.addAll(dcIslandEnd);
                        dcIslandEnds.add(new DCIslandEnd(dcIslandEnd));
                    }
                });

        // Log not visited DCEquipment.
        Set<DCEquipment> notVisitedDcEquipments = new HashSet<>(dcEquipments);
        notVisitedDcEquipments.removeAll(visitedDcEquipments);
        notVisitedDcEquipments.forEach(dcEquipment ->
                CgmesReports.notVisitedDcEquipmentReport(context.getReportNode(), dcEquipment.id()));
    }

    private void getAdjacentDcEquipments(DCEquipment dcEquipment, Set<DCEquipment> dcIslandEnd) {
        // Recursively get all adjacent DCEquipment.
        // DCLineSegment are included but not traversed.
        if (!dcIslandEnd.contains(dcEquipment)) {
            dcIslandEnd.add(dcEquipment);
            if (!dcEquipment.isLine()) {
                dcEquipments.stream()
                        .filter(e -> e != dcEquipment && e.isAdjacentTo(dcEquipment) && !dcIslandEnd.contains(e))
                        .forEach(e -> getAdjacentDcEquipments(e, dcIslandEnd));
            }
        }
    }

    private void computeDcIslands() {
        // Compute DCIsland: a collection of DCIslandEnd connected together via shared DCLineSegment(s).
        Set<DCIslandEnd> visitedDcIslandEnds = new HashSet<>();
        dcIslandEnds.forEach(dcIslandEnd -> {
            if (!visitedDcIslandEnds.contains(dcIslandEnd)) {
                Set<DCIslandEnd> dcIsland = new HashSet<>();
                getAdjacentDcIslandEnds(dcIslandEnd, dcIsland);
                visitedDcIslandEnds.addAll(dcIsland);
                dcIslands.add(new DCIsland(dcIsland));
            }
        });
    }

    private void getAdjacentDcIslandEnds(DCIslandEnd dcIslandEnd, Set<DCIslandEnd> dcIsland) {
        // Recursively get all adjacent DCIslandEnd.
        if (!dcIsland.contains(dcIslandEnd)) {
            dcIsland.add(dcIslandEnd);
            dcIslandEnds.stream()
                    .filter(end -> end != dcIslandEnd && end.isAdjacentTo(dcIslandEnd) && !dcIsland.contains(end))
                    .forEach(end -> getAdjacentDcIslandEnds(end, dcIsland));
        }
    }

    private void convert() {
        for (DCIsland dcIsland : dcIslands) {
            if (dcIsland.valid(context)) {
                if (!context.config().getUseDetailedDcModel()) {
                    convertDcLinks(dcIsland);
                } else {
                    convertDcNodes(dcIsland);
                    convertDcSwitches(dcIsland);
                    convertDcGrounds(dcIsland);
                    convertDcLines(dcIsland);
                }
            }
        }
    }

    private void convertDcLinks(DCIsland dcIsland) {
        // Get island poles.
        List<DCPole> dcPoles = getDcPoles(dcIsland);
        List<DCLink> dcLinks = new ArrayList<>();
        for (DCPole dcPole : dcPoles) {
            // Create 1 or 2 DCLink per pole, depending on the number of bridges per pole.
            PropertyBag converter1 = getConverterBag(dcPole.getConverter1A());
            PropertyBag converter2 = getConverterBag(dcPole.getConverter2A());
            PropertyBag dcLine1 = getDcLineSegmentBag(dcPole.getDcLine1());
            PropertyBag dcLine2 = null;
            if (dcPole.getDcLine2() != null) {
                dcLine2 = getDcLineSegmentBag(dcPole.getDcLine2());
                if (dcPole.isHalfOfBipole()) {
                    // The Dedicated Metallic Return line of a bipole is retrieved solely to keep its id as an alias.
                    // Its resistance should not be taken into account since there is no flow through it in nominal cases.
                    dcLine2.put("r", "0");
                }
            }
            if (dcPole.getConverter1B() == null) {
                dcLinks.add(new DCLink(converter1, converter2, dcLine1, dcLine2));
            } else {
                PropertyBag converter1B = getConverterBag(dcPole.getConverter1B());
                PropertyBag converter2B = getConverterBag(dcPole.getConverter2B());
                PropertyBag dcLine1B = splitDcLineSegmentBag(dcLine1);
                dcLinks.add(new DCLink(converter1, converter2, dcLine1, dcLine2));
                dcLinks.add(new DCLink(converter1B, converter2B, dcLine1B, null));
            }
        }

        // Convert DCLink.
        dcLinks.forEach(dcLink -> {
            new HvdcConverterConversion(dcLink.getConverter1(), dcLink.getLossFactor1(), context).convert();
            new HvdcConverterConversion(dcLink.getConverter2(), dcLink.getLossFactor2(), context).convert();
            new HvdcLineConversion(dcLink, context).convert();
        });
    }

    private List<DCPole> getDcPoles(DCIsland dcIsland) {
        // Sort island ends.
        // The first island end is the one that has the more dc line segments node 1.
        // In case of equality, it is the one with the lowest converter id.
        List<DCIslandEnd> islandEnds = dcIsland.dcIslandEnds().stream()
                .sorted(Comparator.<DCIslandEnd>comparingLong(e -> e.getDcLineSegments().stream()
                                .filter(l -> e.dcEquipments().stream()
                                        .anyMatch(eq -> !eq.equals(l) && eq.isConnectedTo(l.node1())))
                                .count())
                        .reversed()
                        .thenComparing(e -> e.getAcDcConverters().stream()
                                .map(DCEquipment::id)
                                .min(Comparator.naturalOrder())
                                .orElseThrow()))
                .toList();

        // Retrieve ACDCConverter and DCLineSegment.
        List<DCEquipment> converters1 = islandEnds.get(0).getAcDcConverters();
        List<DCEquipment> dcLineSegments = islandEnds.get(0).getDcLineSegments();
        boolean isBipole = converters1.size() > 1 && dcLineSegments.size() > 1;

        // Get energized lines and DMR line (if any).
        DCEquipment dMRLine = null;
        List<DCEquipment> energizedLines = new ArrayList<>(dcLineSegments);
        if (hasDMRLine(converters1.size(), dcLineSegments.size())) {
            dMRLine = getDMRLine(dcIsland, dcLineSegments);
            energizedLines.remove(dMRLine);
        }

        // For each energized line, find the nearest converter on each side.
        List<DCPole> dcPoles = new ArrayList<>();
        Set<DCEquipment> usedConverters1 = new HashSet<>();
        Set<DCEquipment> usedConverters2 = new HashSet<>();
        for (DCEquipment dcLineSegment : energizedLines) {
            Predicate<DCEquipment> eligibleConverter1 = e -> e.isConverter() && !usedConverters1.contains(e);
            DCEquipment converter1 = islandEnds.get(0).getNearestConverter(dcLineSegment, eligibleConverter1);
            usedConverters1.add(converter1);

            Predicate<DCEquipment> eligibleConverter2 = e -> e.type().equals(converter1.type()) && !usedConverters2.contains(e);
            DCEquipment converter2 = islandEnds.get(1).getNearestConverter(dcLineSegment, eligibleConverter2);
            usedConverters2.add(converter2);

            dcPoles.add(new DCPole(converter1, converter2, dcLineSegment, isBipole));
        }

        // In case of multiple converters (bridges) per pole, add the second converter pair to an existing pole.
        Set<DCEquipment> unmappedConverters1 = new HashSet<>(converters1);
        Set<DCEquipment> eligibleConverters1A = new HashSet<>(usedConverters1);
        unmappedConverters1.removeAll(usedConverters1);
        for (DCEquipment converter1B : unmappedConverters1) {
            Predicate<DCEquipment> eligibleConverter1A = e -> e.type().equals(converter1B.type()) && eligibleConverters1A.contains(e);
            DCEquipment converter1A = islandEnds.get(0).getNearestConverter(converter1B, eligibleConverter1A);
            eligibleConverters1A.remove(converter1A);

            DCPole dcPole = dcPoles.stream().filter(p -> p.getConverter1A().equals(converter1A)).findFirst().orElseThrow();
            DCEquipment converter2A = dcPole.getConverter2A();

            Predicate<DCEquipment> eligibleConverter2B = e -> e.type().equals(converter1B.type()) && !usedConverters2.contains(e);
            DCEquipment converter2B = islandEnds.get(1).getNearestConverter(converter2A, eligibleConverter2B);
            usedConverters2.add(converter2B);

            dcPole.addSecondBridge(converter1B, converter2B);
        }

        // Add DMR line to the first pole.
        dcPoles.get(0).addMetallicReturnLine(dMRLine);

        return dcPoles;
    }

    private boolean hasDMRLine(int numberOfConverters, int numberOfLines) {
        return numberOfConverters == 1 && numberOfLines > 1 || numberOfLines > 2;
    }

    private DCEquipment getDMRLine(DCIsland dcIsland, List<DCEquipment> dcLineSegments) {
        // Either the DMR line is clearly identifiable since it's the only grounded line of the island.
        // Or it's not, and the most central line (the last one since they are sorted) is considered to be the DMR.
        List<DCEquipment> groundedLines = dcLineSegments.stream()
                .filter(dcIsland::isGrounded)
                .toList();
        if (groundedLines.size() == 1) {
            return groundedLines.get(0);
        }
        return dcLineSegments.get(dcLineSegments.size() - 1);
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

    private void convertDcNodes(DCIsland dcIsland) {
        // From CGM BUILDING PROCESS IMPLEMENTATION GUIDE_v2.0:
        // In CGMES v2.4 the attribute ACDCConverter.ratedUdc is assumed to be the same for all
        // DC equipment in the cim:DCConverterUnit which means that it is sufficient to locate the
        // cim:CsConverter or cim:VsConverter in the cim:DCConverterUnit to obtain the
        // information on rated DC voltage.
        Map<String, Double> unitRatedUdc = dcIsland.dcIslandEnds().stream()
                .flatMap(e -> e.dcEquipments().stream())
                .filter(DCEquipment::isConverter)
                .map(this::getConverterBag)
                .collect(Collectors.toMap(p -> p.getLocal("DCConverterUnit"), p -> p.asDouble(RATED_UDC), Math::max));

        dcIsland.dcIslandEnds().stream()
                .flatMap(e -> e.dcEquipments().stream())
                .flatMap(e -> Stream.of(e.node1(), e.node2()))
                .filter(Objects::nonNull)
                .distinct()
                .map(n -> context.dcMapping().getDcNodeBag(n))
                .forEach(p -> new DCNodeConversion(p, unitRatedUdc.getOrDefault(p.getLocal("DCConverterUnit"), 1.0), context).convert());
    }

    private void convertDcSwitches(DCIsland dcIsland) {
        dcIsland.dcIslandEnds().stream()
                .flatMap(e -> e.dcEquipments().stream())
                .filter(DCEquipment::isSwitch)
                .map(this::getDcSwitchBag)
                .forEach(p -> new DCSwitchConversion(p, context).convert());
    }

    private void convertDcGrounds(DCIsland dcIsland) {
        dcIsland.dcIslandEnds().stream()
                .flatMap(e -> e.dcEquipments().stream())
                .filter(DCEquipment::isGround)
                .map(this::getDcGroundBag)
                .forEach(p -> new DCGroundConversion(p, context).convert());
    }

    private void convertDcLines(DCIsland dcIsland) {
        dcIsland.dcIslandEnds().stream()
                .flatMap(e -> e.dcEquipments().stream())
                .filter(DCEquipment::isLine)
                .distinct()
                .map(this::getDcLineSegmentBag)
                .forEach(p -> new DCLineSegmentConversion(p, context).convert());
    }

    private PropertyBag getConverterBag(DCEquipment acDcConverter) {
        return getPropertyBag(acDcConverter.id(), cgmesAcDcConverters, ACDC_CONVERTER);
    }

    private PropertyBag getDcSwitchBag(DCEquipment dcSwitch) {
        return getPropertyBag(dcSwitch.id(), cgmesDcSwitches, DC_SWITCH);
    }

    private PropertyBag getDcGroundBag(DCEquipment dcGround) {
        return getPropertyBag(dcGround.id(), cgmesDcGrounds, DC_GROUND);
    }

    private PropertyBag getDcLineSegmentBag(DCEquipment dcLineSegment) {
        return getPropertyBag(dcLineSegment.id(), cgmesDcLineSegments, DC_LINE_SEGMENT);
    }

    private PropertyBag getPropertyBag(String dcEquipmentId, PropertyBags cachedPropertyBags, String propertyKey) {
        return cachedPropertyBags.stream()
                .filter(b -> b.getId(propertyKey).equals(dcEquipmentId))
                .findFirst()
                .orElseThrow();
    }

}
