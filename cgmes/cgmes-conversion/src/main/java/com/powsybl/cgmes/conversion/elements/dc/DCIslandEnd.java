/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import java.util.*;
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
        return dcEquipments.stream()
                .filter(DCEquipment::isLine)
                .sorted(Comparator.comparing(this::getTotalDistanceToConverters).reversed()
                        .thenComparing(DCEquipment::id))
                .toList();
    }

    public DCEquipment getNearestConverter(DCEquipment dcEquipment, Set<DCEquipment> usedConverters) {
        DCEquipment nearestConverter = getEquipmentDistances(dcEquipment)
                .entrySet().stream()
                .filter(e -> e.getKey().isConverter()
                        && !e.getKey().equals(dcEquipment)
                        && !usedConverters.contains(e.getKey()))
                .min(Map.Entry.comparingByValue())
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
        computeEquipmentDistances(Map.of(dcEquipment, 0), equipmentDistances);
        return equipmentDistances;
    }

    private void computeEquipmentDistances(Map<DCEquipment, Integer> adjacentDcEquipments, Map<DCEquipment, Integer> equipmentDistances) {
        for (Map.Entry<DCEquipment, Integer> adjacentDcEquipment : adjacentDcEquipments.entrySet()) {
            if (equipmentDistances.computeIfAbsent(adjacentDcEquipment.getKey(), v -> Integer.MAX_VALUE) > adjacentDcEquipment.getValue()) {
                equipmentDistances.put(adjacentDcEquipment.getKey(), adjacentDcEquipment.getValue());
                Map<DCEquipment, Integer> nextDcEquipments = dcEquipments.stream()
                        .filter(e -> e != adjacentDcEquipment.getKey() && e.isAdjacentTo(adjacentDcEquipment.getKey()))
                        .collect(Collectors.toMap(e -> e, e -> adjacentDcEquipment.getValue() + 1));
                computeEquipmentDistances(nextDcEquipments, equipmentDistances);
            }
        }
    }

}
