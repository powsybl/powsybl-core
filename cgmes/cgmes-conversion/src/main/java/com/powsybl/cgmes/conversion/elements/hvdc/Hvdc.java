/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.elements.hvdc.IslandEndHvdc.HvdcEnd;
import com.powsybl.cgmes.conversion.elements.hvdc.IslandEndHvdc.HvdcEndType;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class Hvdc {
    List<HvdcEquipment> hvdcData;

    // The island includes dcTopologicalNodes and first acTopologicalNode
    Hvdc() {
        this.hvdcData = new ArrayList<>();
    }

    void add(TPnodeEquipments tpNodeEquipments, IslandEndHvdc islandEndHvdc1, IslandEndHvdc islandEndHvdc2) {
        islandEndHvdc1.hvdc.forEach(h -> add(tpNodeEquipments, h, islandEndHvdc2));
    }

    private void add(TPnodeEquipments tpNodeEquipments, HvdcEnd hvdc1, IslandEndHvdc islandEndHvdc2) {
        HvdcEnd hvdc2 = islandEndHvdc2.selectSimetricHvdcEnd(hvdc1);
        if (hvdc2 == null) {
            return;
        }
        HvdcEndType type = hvdc1.computeType();
        switch (type) {
            case HVDC_NONE:
                break;
            case HVDC_T1_C1_LS1:
                addT1C1LSn(hvdc1, hvdc2);
                break;
            case HVDC_T1_C1_LS2:
                addT1C1LSn(hvdc1, hvdc2);
                break;
            case HVDC_T2_C2_LS1:
                addT2C2LS1(tpNodeEquipments, hvdc1, hvdc2);
                break;
            case HVDC_TN_CN_LSN:
                addTnCnLSn(tpNodeEquipments, hvdc1, hvdc2);
                break;
        }
    }

    private void addT1C1LSn(HvdcEnd hvdc1, HvdcEnd hvdc2) {
        HvdcEquipment hvdcEq = new HvdcEquipment();
        HvdcConverter converter = new HvdcConverter(hvdc1.transformersEnd.iterator().next(),
            hvdc1.acDcConvertersEnd.iterator().next(), hvdc2.transformersEnd.iterator().next(),
            hvdc2.acDcConvertersEnd.iterator().next());
        hvdcEq.add(converter);
        hvdc1.dcLineSegmentsEnd.forEach(ls -> hvdcEq.add(ls));
        this.hvdcData.add(hvdcEq);
    }

    private void addT2C2LS1(TPnodeEquipments tpNodeEquipments, HvdcEnd hvdc1, HvdcEnd hvdc2) {

        String dcLineSegment = hvdc1.dcLineSegmentsEnd.iterator().next();
        HvdcConverter hvdcConverter1 = computeConverter(tpNodeEquipments, dcLineSegment, hvdc1, hvdc2);
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

    private void addTnCnLSn(TPnodeEquipments tpNodeEquipments, HvdcEnd hvdc1, HvdcEnd hvdc2) {

        hvdc1.dcLineSegmentsEnd.forEach(dcLineSegment -> {
            HvdcConverter hvdcConverter = computeConverter(tpNodeEquipments, dcLineSegment, hvdc1, hvdc2);
            if (hvdcConverter == null) {
                return;
            }
            HvdcEquipment hvdcEq = new HvdcEquipment();
            hvdcEq.add(hvdcConverter, dcLineSegment);
            this.hvdcData.add(hvdcEq);
        });
    }

    private static HvdcConverter computeConverter(TPnodeEquipments tpNodeEquipments, String dcLineSegment, HvdcEnd hvdc1,
        HvdcEnd hvdc2) {
        String acDcConverter1 = computeEquipmentConnectedToEquipment(tpNodeEquipments, dcLineSegment, hvdc1.acDcConvertersEnd, hvdc1.topologicalNodesEnd);
        if (acDcConverter1 == null) {
            return null;
        }
        String transformer1 = computeEquipmentConnectedToEquipment(tpNodeEquipments, acDcConverter1, hvdc1.transformersEnd, hvdc1.topologicalNodesEnd);
        if (transformer1 == null) {
            return null;
        }
        String acDcConverter2 = computeEquipmentConnectedToEquipment(tpNodeEquipments, dcLineSegment, hvdc2.acDcConvertersEnd, hvdc2.topologicalNodesEnd);
        if (acDcConverter2 == null) {
            return null;
        }
        String transformer2 = computeEquipmentConnectedToEquipment(tpNodeEquipments, acDcConverter2, hvdc2.transformersEnd, hvdc2.topologicalNodesEnd);
        if (transformer2 == null) {
            return null;
        }
        return new HvdcConverter(transformer1, acDcConverter1, transformer2, acDcConverter2);
    }

    private static HvdcConverter computeOtherConverter(HvdcConverter converter, HvdcEnd hvdc1, HvdcEnd hvdc2) {
        String acDcConverter1 = hvdc1.acDcConvertersEnd.stream().filter(c -> !converter.acDcConvertersEnd1.contentEquals(c)).findFirst().orElse(null);
        if (acDcConverter1 == null) {
            return null;
        }
        String transformer1 = hvdc1.transformersEnd.stream().filter(t -> !converter.transformersEnd1.contentEquals(t)).findFirst().orElse(null);
        if (transformer1 == null) {
            return null;
        }
        String acDcConverter2 = hvdc2.acDcConvertersEnd.stream().filter(c -> !converter.acDcConvertersEnd2.contentEquals(c)).findFirst().orElse(null);
        if (acDcConverter2 == null) {
            return null;
        }
        String transformer2 = hvdc2.transformersEnd.stream().filter(t -> !converter.transformersEnd2.contentEquals(t)).findFirst().orElse(null);
        if (transformer2 == null) {
            return null;
        }
        return new HvdcConverter(transformer1, acDcConverter1, transformer2, acDcConverter2);
    }

    private static String computeEquipmentConnectedToEquipment(TPnodeEquipments tpNodeEquipments, String equipment,
        Set<String> connectedEquipments, List<String> topologicalNodes) {
        return connectedEquipments.stream()
            .filter(eq -> tpNodeEquipments.connectedEquipments(equipment, eq, topologicalNodes))
            .findFirst()
            .orElse(null);
    }

    void print() {
        LOG.info("Hvdc");
        hvdcData.forEach(h -> h.print());
    }

    static class HvdcEquipment {
        List<HvdcConverter> converters;
        List<String> dcLineSegments;

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

        void print() {
            LOG.info("    Converters:");
            this.converters.forEach(c -> c.print());
            LOG.info("    dcLineSegments");
            this.dcLineSegments.forEach(ls -> LOG.info("    {} ", ls));
            LOG.info("---");
        }
    }

    static class HvdcConverter {
        String transformersEnd1;
        String acDcConvertersEnd1;
        String transformersEnd2;
        String acDcConvertersEnd2;

        HvdcConverter(String transformersEnd1, String acDcConvertersEnd1, String transformersEnd2,
            String acDcConvertersEnd2) {
            this.transformersEnd1 = transformersEnd1;
            this.acDcConvertersEnd1 = acDcConvertersEnd1;
            this.transformersEnd2 = transformersEnd2;
            this.acDcConvertersEnd2 = acDcConvertersEnd2;
        }

        void print() {
            LOG.info("    End1: {} - {} End2: {} {}", this.transformersEnd1, this.acDcConvertersEnd1,
                this.transformersEnd2, this.acDcConvertersEnd2);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(Hvdc.class);
}
