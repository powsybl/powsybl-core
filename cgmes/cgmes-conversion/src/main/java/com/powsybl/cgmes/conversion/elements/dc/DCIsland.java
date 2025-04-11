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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.elements.dc.DCConfiguration.*;
import static com.powsybl.cgmes.model.CgmesNames.*;

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
        Map<DCEquipment, Integer> dcLineSegmentsOccurrences = new HashMap<>();
        dcIslandEnds.stream()
                .flatMap(end -> end.dcEquipments().stream())
                .filter(DCEquipment::isLine)
                .forEach(l -> dcLineSegmentsOccurrences.put(l, dcLineSegmentsOccurrences.computeIfAbsent(l, e -> 0) + 1));
        for (Map.Entry<DCEquipment, Integer> dcLineSegmentOccurrences : dcLineSegmentsOccurrences.entrySet()) {
            if (dcLineSegmentOccurrences.getValue() != 2) {
                CgmesReports.dcLineSegmentNotInTwoDCIslandEndReport(context.getReportNode(), dcLineSegmentOccurrences.getKey().id());
                valid = false;
            }
        }
        return valid;
    }

    private boolean validAcDcConverters(Context context) {
        // Check that there is only one converter type overall in the DCIsland.
        Set<DCEquipment> csConverters = dcIslandEnds.stream()
                .flatMap(end -> end.dcEquipments().stream())
                .filter(e -> CS_CONVERTER.equals(e.type()))
                .collect(Collectors.toSet());
        Set<DCEquipment> vsConverters = dcIslandEnds.stream()
                .flatMap(end -> end.dcEquipments().stream())
                .filter(e -> VS_CONVERTER.equals(e.type()))
                .collect(Collectors.toSet());
        if (!csConverters.isEmpty() && !vsConverters.isEmpty()) {
            CgmesReports.multipleAcDcConverterTypesInSameDCIslandReport(context.getReportNode(), getConverterIds());
            return false;
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
            int numberOfConverters = ends.get(0).getAcDcConverters().size();
            int numberOfConvertersOtherEnd = ends.get(1).getAcDcConverters().size();
            if (!isMonopole(numberOfLines, numberOfConverters, numberOfConvertersOtherEnd)
                    && !isBipole(numberOfLines, numberOfConverters, numberOfConvertersOtherEnd)) {
                CgmesReports.unexpectedPointToPointDcConfigurationReport(
                        context.getReportNode(), getConverterIds(), numberOfLines, numberOfConverters, numberOfConvertersOtherEnd);
                return false;
            }
            return true;
        }
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

    private boolean isMonopole(int numberOfLines, int numberOfConverters, int numberOfConvertersOtherEnd) {
        return numberOfConverters == numberOfConvertersOtherEnd &&
                (numberOfLines == 1 && numberOfConverters == 1          // 1 bridge with ground return.
                 || numberOfLines == 1 && numberOfConverters == 2       // 2 bridges with ground return.
                 || numberOfLines == 2 && numberOfConverters == 1);     // 1 bridge with metallic return.
    }

    private boolean isBipole(int numberOfLines, int numberOfConverters, int numberOfConvertersOtherEnd) {
        return numberOfConverters == numberOfConvertersOtherEnd &&
                (numberOfLines == 2 && numberOfConverters == 2          // 1 bridge per pole.
                || numberOfLines == 2 && numberOfConverters == 4        // 2 bridges per pole.
                || numberOfLines == 3 && numberOfConverters == 2        // 1 bridge per pole, and a metallic return.
                || numberOfLines == 3 && numberOfConverters == 4);      // 2 bridges per pole, and a metallic return.
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
}
