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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.elements.dc.DCConfiguration.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public record DCIsland(Set<DCIslandEnd> dcIslandEnds) {

    public boolean valid(Context context) {
        boolean validDcLineSegments = validDcLineSegments(context);
        boolean validAcDcConverters = validAcDcConverters(context);
        boolean validConfiguration = validConfiguration(context);
        return validDcLineSegments && validAcDcConverters && validConfiguration;
    }

    private boolean validDcLineSegments(Context context) {
        // Check that each DCLineSegment is present in exactly 2 DCIslandEnd.
        boolean valid = true;
        Map<DCEquipment, Long> dcLineSegmentsOccurrences = dcIslandEnds.stream()
                .flatMap(end -> end.dcEquipments().stream())
                .filter(DCEquipment::isLine)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        for (Map.Entry<DCEquipment, Long> dcLineSegmentOccurrences : dcLineSegmentsOccurrences.entrySet()) {
            if (dcLineSegmentOccurrences.getValue() != 2) {
                CgmesReports.dcLineSegmentNotInTwoDCIslandEndReport(context.getReportNode(), dcLineSegmentOccurrences.getKey().id());
                valid = false;
            }
        }
        return valid;
    }

    private boolean validAcDcConverters(Context context) {
        DCConfiguration dcConfiguration = getDcConfiguration();
        if (dcConfiguration == POINT_TO_POINT) {
            List<DCIslandEnd> ends = dcIslandEnds.stream().toList();
            long numberOfCsConverters1 = ends.get(0).dcEquipments().stream().filter(DCEquipment::isCsConverter).count();
            long numberOfCsConverters2 = ends.get(1).dcEquipments().stream().filter(DCEquipment::isCsConverter).count();
            long numberOfVsConverters1 = ends.get(0).dcEquipments().stream().filter(DCEquipment::isVsConverter).count();
            long numberOfVsConverters2 = ends.get(1).dcEquipments().stream().filter(DCEquipment::isVsConverter).count();
            if (numberOfCsConverters1 != numberOfCsConverters2 || numberOfVsConverters1 != numberOfVsConverters2) {
                CgmesReports.inconsistentNumberOfConvertersReport(context.getReportNode(), getConverterIds());
                return false;
            }
        }
        return true;
    }

    private boolean validConfiguration(Context context) {
        DCConfiguration dcConfiguration = getDcConfiguration();
        if (dcConfiguration != POINT_TO_POINT) {
            CgmesReports.unsupportedDcConfigurationReport(context.getReportNode(), getConverterIds(), dcConfiguration.name());
            return false;
        } else {
            List<DCIslandEnd> ends = dcIslandEnds.stream().toList();
            int numberOfLines = ends.get(0).getDcLineSegments().size();
            int numberOfConverterPairs = ends.get(0).getAcDcConverters().size();
            if (numberOfLines > numberOfConverterPairs + 1 || numberOfConverterPairs > 2 * numberOfLines) {
                // There is more line that the number of converters + a metallic return line
                // or there is more converter pairs than 2 bridges per dc line.
                CgmesReports.unexpectedPointToPointDcConfigurationReport(
                        context.getReportNode(), getConverterIds(), numberOfLines, numberOfConverterPairs);
                return false;
            }
        }
        return true;
    }

    private DCConfiguration getDcConfiguration() {
        if (dcIslandEnds.size() > 2) {
            return MULTI_TERMINAL;
        } else if (dcIslandEnds.size() == 2) {
            return POINT_TO_POINT;
        } else {
            return BACK_TO_BACK;
        }
    }

    private String getConverterIds() {
        return String.join(", ",
                dcIslandEnds.stream()
                        .flatMap(end -> end.dcEquipments().stream())
                        .filter(DCEquipment::isConverter)
                        .map(DCEquipment::id)
                        .sorted()
                        .toList());
    }

    public boolean isGrounded(DCEquipment dcEquipment) {
        return dcIslandEnds.stream()
                .flatMap(e -> e.dcEquipments().stream())
                .filter(DCEquipment::isGround)
                .anyMatch(g -> g.isAdjacentTo(dcEquipment));
    }
}
