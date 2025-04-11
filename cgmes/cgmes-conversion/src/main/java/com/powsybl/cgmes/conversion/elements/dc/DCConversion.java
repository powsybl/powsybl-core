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

import static com.powsybl.cgmes.conversion.elements.dc.DCConfiguration.*;
import static com.powsybl.cgmes.model.CgmesNames.*;

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
            //TODO
        }
    }

}
