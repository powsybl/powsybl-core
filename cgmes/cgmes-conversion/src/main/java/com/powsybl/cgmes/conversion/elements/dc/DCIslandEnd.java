/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public record DCIslandEnd(Set<DCEquipment> dcEquipments) {

    boolean isAdjacentTo(DCIslandEnd otherDcIslandEnd) {
        // Two DCIslandEnd are adjacent if they share a DCLineSegment.
        Set<DCEquipment> commonDcLineSegments = dcEquipments.stream()
                .filter(DCEquipment::isLine)
                .collect(Collectors.toSet());
        commonDcLineSegments.retainAll(otherDcIslandEnd.dcEquipments);
        return !commonDcLineSegments.isEmpty();
    }

    public List<DCEquipment> getAcDcConverters() {
        return dcEquipments.stream()
                .filter(DCEquipment::isConverter)
                .sorted(Comparator.comparing(DCEquipment::id))
                .toList();
    }

    public List<DCEquipment> getDcLineSegments() {
        // Return DCLineSegment sorted by total distance to converters (furthest to nearest), then by id.
        // This allows DMR in case of bipole configuration to be the last element of the list as
        // since it's central, it's the closest to all converters.
        return dcEquipments.stream()
                .filter(DCEquipment::isLine)
                .sorted(Comparator.comparing(this::getTotalDistanceToConverters).reversed()
                        .thenComparing(DCEquipment::id))
                .toList();
    }

    public DCEquipment getNearestConverter(DCEquipment dcEquipment, Predicate<DCEquipment> isEligibleConverter, Set<DCEquipment> usedConverters) {
        DCEquipment nearestConverter = getEquipmentDistances(dcEquipment)
                .entrySet().stream()
                .filter(e -> isEligibleConverter.test(e.getKey())
                        && !e.getKey().equals(dcEquipment))
                .min(Map.Entry.<DCEquipment, Integer>comparingByValue()
                        .thenComparing(e -> e.getKey().id()))
                .map(Map.Entry::getKey)
                .orElseThrow();
        usedConverters.add(nearestConverter);
        return nearestConverter;
    }

    private Integer getTotalDistanceToConverters(DCEquipment dcEquipment) {
        return getEquipmentDistances(dcEquipment).entrySet().stream()
                .filter(e -> e.getKey().isConverter())
                .map(Map.Entry::getValue)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private Map<DCEquipment, Integer> getEquipmentDistances(DCEquipment dcEquipment) {
        Map<DCEquipment, Integer> equipmentDistances = new HashMap<>();
        computeEquipmentDistances(dcEquipment, 0, equipmentDistances);
        return equipmentDistances;
    }

    private void computeEquipmentDistances(DCEquipment dcEquipment, int distance, Map<DCEquipment, Integer> equipmentDistances) {
        if (equipmentDistances.computeIfAbsent(dcEquipment, e -> Integer.MAX_VALUE) > distance) {
            equipmentDistances.put(dcEquipment, distance);
            Set<DCEquipment> nextDcEquipments = dcEquipments.stream()
                    .filter(e -> e != dcEquipment && e.isAdjacentTo(dcEquipment))
                    .collect(Collectors.toSet());
            for (DCEquipment nextDcEquipment : nextDcEquipments) {
                computeEquipmentDistances(nextDcEquipment, distance + 1, equipmentDistances);
            }
        }
    }

}
