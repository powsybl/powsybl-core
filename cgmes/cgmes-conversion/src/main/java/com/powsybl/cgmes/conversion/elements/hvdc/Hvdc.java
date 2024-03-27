/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.powsybl.cgmes.conversion.elements.hvdc.IslandEndHvdc.HvdcEnd;
import com.powsybl.cgmes.conversion.elements.hvdc.IslandEndHvdc.HvdcEndType;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class Hvdc {
    private final List<HvdcEquipment> hvdcData;

    Hvdc() {
        this.hvdcData = new ArrayList<>();
    }

    void add(NodeEquipment nodeEquipment, IslandEndHvdc islandEndHvdc1, IslandEndHvdc islandEndHvdc2) {
        Associations associations = createAssociations(islandEndHvdc1, islandEndHvdc2);

        associations.associationList.forEach(match -> {
            HvdcEnd hvdcEnd1 = HvdcEnd.joinAll(match.end1);
            HvdcEnd hvdcEnd2 = HvdcEnd.joinAll(match.end2);

            add(nodeEquipment, hvdcEnd1, hvdcEnd2);
        });
    }

    // There is not a one to one mapping between the HvdcEnds of side 1 and side 2
    // In some configurations it is necessary to join several hvdcEnds on one side to map the hvdcEnd of the other side
    // See the configuration described in IslandsEnds class
    // Two hvdcEnds are associated if they share one or more dcLineSegments
    private static Associations createAssociations(IslandEndHvdc islandEndHvdc1, IslandEndHvdc islandEndHvdc2) {
        Associations associations = new Associations();
        islandEndHvdc1.getHvdc().forEach(hvdcEnd -> {
            List<HvdcEnd> associatedEnd2 = islandEndHvdc2.getHvdc().stream()
                .filter(otherHvdcEnd -> otherHvdcEnd.isAssociatedWith(hvdcEnd)).toList();

            associatedEnd2.forEach(otherHvdcEnd -> associations.add(hvdcEnd, otherHvdcEnd));
        });
        return associations;
    }

    private void add(NodeEquipment nodeEquipment, HvdcEnd hvdc1, HvdcEnd hvdc2) {
        if (!hvdc1.isMatchingTo(hvdc2)) {
            return;
        }
        HvdcEndType type = hvdc1.computeType();
        switch (type) {
            case HVDC_TN_C1_LS1:
                addC1LSn(hvdc1, hvdc2);
                break;
            case HVDC_TN_C2_LS1:
                addC2LS1(nodeEquipment, hvdc1, hvdc2);
                break;
            case HVDC_TN_CN_LSN:
                addCnLSn(nodeEquipment, hvdc1, hvdc2);
                break;
            case HVDC_TN_CN_LS2N:
                addCnLS2n(nodeEquipment, hvdc1, hvdc2);
                break;
        }
    }

    private void addC1LSn(HvdcEnd hvdc1, HvdcEnd hvdc2) {
        HvdcEquipment hvdcEq = new HvdcEquipment();
        HvdcConverter converter = new HvdcConverter(hvdc1.acDcConvertersEnd.iterator().next(),
            hvdc2.acDcConvertersEnd.iterator().next());
        hvdcEq.add(converter);
        hvdc1.dcLineSegmentsEnd.forEach(hvdcEq::add);
        this.hvdcData.add(hvdcEq);
    }

    private void addC2LS1(NodeEquipment nodeEquipment, HvdcEnd hvdc1, HvdcEnd hvdc2) {

        String dcLineSegment = hvdc1.dcLineSegmentsEnd.iterator().next();
        HvdcConverter hvdcConverter1 = computeConverter(nodeEquipment, dcLineSegment, hvdc1, hvdc2);
        if (hvdcConverter1 == null) {
            return;
        }
        HvdcConverter hvdcConverter2 = computeOtherConverter(hvdcConverter1, hvdc1, hvdc2);
        if (hvdcConverter2 == null) {
            return;
        }
        HvdcEquipment hvdcEq = new HvdcEquipment();
        hvdcEq.add(hvdcConverter1, dcLineSegment);
        hvdcEq.add(hvdcConverter2);
        this.hvdcData.add(hvdcEq);
    }

    private void addCnLSn(NodeEquipment nodeEquipment, HvdcEnd hvdc1, HvdcEnd hvdc2) {

        hvdc1.dcLineSegmentsEnd.forEach(dcLineSegment -> {
            HvdcConverter hvdcConverter = computeConverter(nodeEquipment, dcLineSegment, hvdc1, hvdc2);
            if (hvdcConverter == null) {
                return;
            }
            HvdcEquipment hvdcEq = new HvdcEquipment();
            hvdcEq.add(hvdcConverter, dcLineSegment);
            this.hvdcData.add(hvdcEq);
        });
    }

    private void addCnLS2n(NodeEquipment nodeEquipment, HvdcEnd hvdc1, HvdcEnd hvdc2) {

        Set<String> used = new HashSet<>();
        Optional<String> dcLineSegment1 = nextDcLineSegment(hvdc1, used);

        while (dcLineSegment1.isPresent()) {
            used.add(dcLineSegment1.get());

            HvdcConverter hvdcConverter = computeConverter(nodeEquipment, dcLineSegment1.get(), hvdc1, hvdc2);
            if (hvdcConverter != null && !used.contains(hvdcConverter.acDcConvertersEnd1) && !used.contains(hvdcConverter.acDcConvertersEnd2)) {
                used.add(hvdcConverter.acDcConvertersEnd1);
                used.add(hvdcConverter.acDcConvertersEnd2);

                String dcLineSegment2 = computeOtherDcLineSegment(nodeEquipment, dcLineSegment1.get(), hvdcConverter,
                    hvdc1, hvdc2).orElseThrow();
                used.add(dcLineSegment2);

                HvdcEquipment hvdcEq = new HvdcEquipment();
                hvdcEq.add(hvdcConverter);
                hvdcEq.add(dcLineSegment1.get());
                hvdcEq.add(dcLineSegment2);
                this.hvdcData.add(hvdcEq);
            }

            dcLineSegment1 = nextDcLineSegment(hvdc1, used);
        }
    }

    private static Optional<String> nextDcLineSegment(HvdcEnd hvdc1, Set<String> used) {
        return hvdc1.dcLineSegmentsEnd.stream().filter(adConverterEnd -> !used.contains(adConverterEnd)).findAny();
    }

    private static HvdcConverter computeConverter(NodeEquipment nodeEquipment, String dcLineSegment, HvdcEnd hvdc1,
        HvdcEnd hvdc2) {
        String acDcConverter1 = computeEquipmentConnectedToEquipment(nodeEquipment, dcLineSegment, hvdc1.acDcConvertersEnd, hvdc1.nodesEnd);
        if (acDcConverter1 == null) {
            return null;
        }
        String acDcConverter2 = computeEquipmentConnectedToEquipment(nodeEquipment, dcLineSegment, hvdc2.acDcConvertersEnd, hvdc2.nodesEnd);
        if (acDcConverter2 == null) {
            return null;
        }
        return new HvdcConverter(acDcConverter1, acDcConverter2);
    }

    private static HvdcConverter computeOtherConverter(HvdcConverter converter, HvdcEnd hvdc1, HvdcEnd hvdc2) {
        String acDcConverter1 = hvdc1.acDcConvertersEnd.stream().filter(c -> !converter.acDcConvertersEnd1.contentEquals(c)).findFirst().orElse(null);
        if (acDcConverter1 == null) {
            return null;
        }
        String acDcConverter2 = hvdc2.acDcConvertersEnd.stream().filter(c -> !converter.acDcConvertersEnd2.contentEquals(c)).findFirst().orElse(null);
        if (acDcConverter2 == null) {
            return null;
        }
        return new HvdcConverter(acDcConverter1, acDcConverter2);
    }

    private static Optional<String> computeOtherDcLineSegment(NodeEquipment nodeEquipment, String dcLineSegment,
        HvdcConverter converter, HvdcEnd hvdc1, HvdcEnd hvdc2) {
        return hvdc1.dcLineSegmentsEnd.stream()
            .filter(otherDcLineSegment -> isOtherDcLineSegment(nodeEquipment, otherDcLineSegment, dcLineSegment,
                converter, hvdc1, hvdc2))
            .findAny();
    }

    private static boolean isOtherDcLineSegment(NodeEquipment nodeEquipment, String otherDcLineSegment,
        String dcLineSegment, HvdcConverter converter, HvdcEnd hvdc1, HvdcEnd hvdc2) {
        if (otherDcLineSegment.equals(dcLineSegment)) {
            return false;
        }
        HvdcConverter sameConverter = computeConverter(nodeEquipment, otherDcLineSegment, hvdc1, hvdc2);
        if (sameConverter == null) {
            return false;
        }
        return converter.acDcConvertersEnd1.equals(sameConverter.acDcConvertersEnd1)
            && converter.acDcConvertersEnd2.equals(sameConverter.acDcConvertersEnd2);
    }

    private static String computeEquipmentConnectedToEquipment(NodeEquipment nodeEquipment, String equipment,
        Set<String> connectedEquipment, List<String> nodes) {
        return connectedEquipment.stream()
            .filter(eq -> nodeEquipment.connectedEquipment(equipment, eq, nodes))
            .findFirst()
            .orElse(null);
    }

    List<HvdcEquipment> getHvdcData() {
        return hvdcData;
    }

    static class HvdcEquipment {
        final List<HvdcConverter> converters;
        final List<String> dcLineSegments;

        HvdcEquipment() {
            this.converters = new ArrayList<>();
            this.dcLineSegments = new ArrayList<>();
        }

        void add(HvdcConverter converter, String dcLineSegment) {
            this.converters.add(converter);
            this.dcLineSegments.add(dcLineSegment);
        }

        void add(HvdcConverter converter) {
            this.converters.add(converter);
        }

        void add(String dcLineSegment) {
            this.dcLineSegments.add(dcLineSegment);
        }
    }

    static class HvdcConverter {
        String acDcConvertersEnd1;
        String acDcConvertersEnd2;

        HvdcConverter(String acDcConvertersEnd1, String acDcConvertersEnd2) {
            Objects.requireNonNull(acDcConvertersEnd1);
            Objects.requireNonNull(acDcConvertersEnd2);
            this.acDcConvertersEnd1 = acDcConvertersEnd1;
            this.acDcConvertersEnd2 = acDcConvertersEnd2;
        }
    }

    private static final class Associations {
        private final List<Association> associationList = new ArrayList<>();

        private void add(HvdcEnd hvdcEnd1, HvdcEnd hvdcEnd2) {
            Optional<Association> association1 = this.associationList.stream().filter(m -> m.end1.contains(hvdcEnd1)).findFirst();
            if (association1.isPresent() && !association1.get().end2.contains(hvdcEnd2)) {
                association1.get().end2.add(hvdcEnd2);
                return;
            }
            Optional<Association> association2 = this.associationList.stream().filter(m -> m.end2.contains(hvdcEnd2)).findFirst();
            if (association2.isPresent() && !association2.get().end1.contains(hvdcEnd1)) {
                association2.get().end1.add(hvdcEnd1);
                return;
            }
            this.associationList.add(new Association(hvdcEnd1, hvdcEnd2));
        }

        private static final class Association {
            private final List<HvdcEnd> end1 = new ArrayList<>();
            private final List<HvdcEnd> end2 = new ArrayList<>();

            private Association(HvdcEnd hvdcEnd1, HvdcEnd hvdcEnd2) {
                this.end1.add(hvdcEnd1);
                this.end2.add(hvdcEnd2);
            }
        }
    }
}
