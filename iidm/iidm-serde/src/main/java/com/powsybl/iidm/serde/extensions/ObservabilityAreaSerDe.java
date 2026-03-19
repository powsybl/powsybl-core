/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.extensions.ObservabilityAreaAdder;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionSerDe.class)
public class ObservabilityAreaSerDe extends AbstractExtensionSerDe<VoltageLevel, ObservabilityArea> {

    private static final String AREA_NUMBER = "areaNumber";
    private static final String STATUS = "status";
    public static final String BUS_ROOT_ELEMENT_NAME = "bus";

    public ObservabilityAreaSerDe() {
        super("observabilityArea", "network", ObservabilityArea.class,
                "observabilityArea.xsd",
                "http://www.powsybl.org/schema/iidm/ext/observability_area/1_0",
                "oa");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of("buses", BUS_ROOT_ELEMENT_NAME);
    }

    @Override
    public void write(ObservabilityArea observabilityArea, SerializerContext xmlWriterContext) {
        NetworkSerializerContext context = (NetworkSerializerContext) xmlWriterContext;
        context.getWriter().writeBooleanAttribute("consistentWithTopology", observabilityArea.isConsistentWithTopology());
        VoltageLevel vl = observabilityArea.getExtendable();

        switch (context.getOptions().getTopologyLevel()) {
            case NODE_BREAKER -> writeInConfiguredTopology(context, observabilityArea, vl);
            case BUS_BREAKER -> {
                switch (vl.getTopologyKind()) {
                    case NODE_BREAKER -> writeInCalculatedTopology(context, observabilityArea, vl);
                    case BUS_BREAKER -> writeInConfiguredTopology(context, observabilityArea, vl);
                }
            }
            case BUS_BRANCH -> writeInCalculatedTopology(context, observabilityArea, vl);
        }
    }

    private void writeInConfiguredTopology(NetworkSerializerContext context, ObservabilityArea observabilityArea, VoltageLevel voltageLevel) {
        List<ObservabilityArea.AreaCharacteristics> sortedAreaCharacteristics;
        Consumer<ObservabilityArea.AreaCharacteristics> nodesOrBusWriter = switch (voltageLevel.getTopologyKind()) {
            case NODE_BREAKER -> {
                var sortedBusesMap = observabilityArea.getObservabilityAreas().stream()
                        .collect(Collectors.toMap(ac -> ac, ac -> ac.getNodeBreakerData().getNodes().stream().sorted().toList()));
                sortedAreaCharacteristics = observabilityArea.getObservabilityAreas().stream()
                        .sorted(Comparator.comparing(oa -> sortedBusesMap.get(oa).stream().findFirst().orElse(-1)))
                        .toList();
                yield c -> context.getWriter().writeIntArrayAttribute("nodes", sortedBusesMap.get(c));
            }
            case BUS_BREAKER -> {
                var sortedBusesMap = observabilityArea.getObservabilityAreas().stream()
                        .collect(Collectors.toMap(ac -> ac, ac -> ac.getBusBreakerData().getBusIds().stream().sorted().toList()));
                sortedAreaCharacteristics = observabilityArea.getObservabilityAreas().stream()
                        .sorted(Comparator.comparing(oa -> sortedBusesMap.get(oa).stream().findFirst().orElse("")))
                        .toList();
                yield c -> {
                    context.getWriter().writeStringAttribute("id", null);
                    context.getWriter().writeStringArrayAttribute("ids", sortedBusesMap.get(c));
                };
            }
        };

        context.getWriter().writeStartNodes();
        for (ObservabilityArea.AreaCharacteristics c : sortedAreaCharacteristics) {
            context.getWriter().writeStartNode(getNamespaceUri(), BUS_ROOT_ELEMENT_NAME);
            context.getWriter().writeIntAttribute(AREA_NUMBER, c.getAreaNumber());
            context.getWriter().writeEnumAttribute(STATUS, c.getStatus());
            nodesOrBusWriter.accept(c);
            context.getWriter().writeEndNode();
        }
        context.getWriter().writeEndNodes();
    }

    private void writeInCalculatedTopology(NetworkSerializerContext context,
                                           ObservabilityArea area,
                                           VoltageLevel voltageLevel) {
        context.getWriter().writeStartNodes();
        for (String busId : voltageLevel.getBusView().getBusStream().map(Identifiable::getId).sorted().toList()) {
            ObservabilityArea.AreaCharacteristics characteristics = area.getBusView().getObservabilityArea(busId, false);
            if (characteristics != null) {
                context.getWriter().writeStartNode(getNamespaceUri(), BUS_ROOT_ELEMENT_NAME);
                context.getWriter().writeIntAttribute(AREA_NUMBER, characteristics.getAreaNumber());
                context.getWriter().writeEnumAttribute(STATUS, characteristics.getStatus());
                switch (context.getOptions().getTopologyLevel()) {
                    case BUS_BREAKER -> {
                        context.getWriter().writeStringAttribute("id", null);
                        context.getWriter().writeStringArrayAttribute("ids",
                                voltageLevel.getBusBreakerView().getBusStreamFromBusViewBusId(busId).map(Identifiable::getId).sorted().toList());
                    }
                    case BUS_BRANCH -> {
                        context.getWriter().writeStringAttribute("id", busId);
                        context.getWriter().writeStringArrayAttribute("ids", Collections.emptyList());
                    }
                    case NODE_BREAKER -> throw new IllegalStateException();
                }
                context.getWriter().writeEndNode();
            }
        }
        context.getWriter().writeEndNodes();
    }

    @Override
    public ObservabilityArea read(VoltageLevel voltageLevel, DeserializerContext xmlReaderContext) {
        ObservabilityAreaAdder adder = voltageLevel.newExtension(ObservabilityAreaAdder.class);
        xmlReaderContext.getReader().readBooleanAttribute("consistentWithTopology");
        xmlReaderContext.getReader().readChildNodes(elemntName -> {
            if (elemntName.equals(BUS_ROOT_ELEMENT_NAME)) {
                setObservabilityArea(voltageLevel.getTopologyKind(), adder, xmlReaderContext);
            } else {
                throw new AssertionError("Unexpected element: " + elemntName);
            }
        });
        return adder.add();
    }

    private static void setObservabilityArea(TopologyKind topologyKind, ObservabilityAreaAdder adder, DeserializerContext context) {
        int areaNumber = context.getReader().readIntAttribute(AREA_NUMBER);
        ObservabilityArea.ObservabilityStatus status = context.getReader().readEnumAttribute(STATUS, ObservabilityArea.ObservabilityStatus.class);
        switch (topologyKind) {
            case NODE_BREAKER -> {
                List<Integer> nodesStr = context.getReader().readIntArrayAttribute("nodes");
                adder.withObservabilityAreaByNodes(new HashSet<>(nodesStr), areaNumber, status);
            }
            case BUS_BREAKER -> {
                String busId = context.getReader().readStringAttribute("id");
                List<String> busesId = context.getReader().readStringArrayAttribute("ids");
                List<String> busesStr = busId == null ? busesId : List.of(busId);
                adder.withObservabilityAreaByBusBreakerViewBuses(new HashSet<>(busesStr), areaNumber, status);
            }
        }
        context.getReader().readEndNode();
    }

    @Override
    public boolean isSerializable(ObservabilityArea extension) {
        return !extension.getObservabilityAreas().isEmpty();
    }
}
