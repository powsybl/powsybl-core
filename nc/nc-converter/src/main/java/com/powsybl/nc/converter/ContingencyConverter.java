/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.converter;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.BatteryContingency;
import com.powsybl.contingency.BoundaryLineContingency;
import com.powsybl.contingency.BusContingency;
import com.powsybl.contingency.BusbarSectionContingency;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.DcGroundContingency;
import com.powsybl.contingency.DcLineContingency;
import com.powsybl.contingency.GeneratorContingency;
import com.powsybl.contingency.HvdcLineContingency;
import com.powsybl.contingency.LineContingency;
import com.powsybl.contingency.LoadContingency;
import com.powsybl.contingency.ShuntCompensatorContingency;
import com.powsybl.contingency.StaticVarCompensatorContingency;
import com.powsybl.contingency.SwitchContingency;
import com.powsybl.contingency.ThreeWindingsTransformerContingency;
import com.powsybl.contingency.TieLineContingency;
import com.powsybl.contingency.TwoWindingsTransformerContingency;
import com.powsybl.contingency.VoltageSourceConverterContingency;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.model.NcModel;
import com.powsybl.nc.model.contingency.NcContingency;
import com.powsybl.nc.model.contingency.NcContingencyEquipment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
final class ContingencyConverter extends AbstractNcConverter {
    ContingencyConverter(NcModel model, Network network, ReportNode reportNode) {
        super(model, network, reportNode);
    }

    Map<String, Contingency> convert() {
        Map<String, Contingency> result = new LinkedHashMap<>();

        Map<String, List<NcContingencyEquipment>> equipmentByContingency = model.getContingencyEquipments().stream()
            .collect(Collectors.groupingBy(NcContingencyEquipment::contingency, LinkedHashMap::new, Collectors.toList()));

        List<NcContingency> contingencies = model.getContingencies().stream()
            .sorted(Comparator.comparing(NcContingency::mrid))
            .toList();

        for (NcContingency contingency : contingencies) {
            Contingency converted = process(contingency, equipmentByContingency);
            if (converted != null) {
                result.put(converted.getId(), converted);
            }
        }

        return result;
    }

    private Contingency process(NcContingency contingency,
                                Map<String, List<NcContingencyEquipment>> equipmentByContingency) {
        if (!contingency.mustStudy()) {
            NcConversionReports.contingencyNotToBeStudied(reportNode, contingency.mrid());
            return null;
        }

        List<ContingencyElement> elements = new ArrayList<>();
        for (NcContingencyEquipment equipment : equipmentByContingency.getOrDefault(contingency.mrid(), List.of())) {
            ContingencyElement element = resolveEquipment(equipment, contingency.mrid());
            if (element != null) {
                elements.add(element);
            }
        }
        if (elements.isEmpty()) {
            NcConversionReports.contingencyWithoutValidEquipment(reportNode, contingency.mrid());
            return null;
        }
        return new Contingency(contingency.mrid(), contingency.name(), elements);
    }

    private ContingencyElement resolveEquipment(NcContingencyEquipment equipment, String contingencyId) {
        if (!equipment.isEquipmentOutOfService()) {
            NcConversionReports.contingencyEquipmentNotOutOfService(reportNode, equipment.equipment(), contingencyId);
            return null;
        }
        Identifiable<?> element = equipment.equipment() == null ? null : network.getIdentifiable(equipment.equipment());
        if (element == null) {
            NcConversionReports.contingencyEquipmentNotFound(reportNode, equipment.equipment(), contingencyId);
            return null;
        }
        return switch (element.getType()) {
            case BATTERY -> new BatteryContingency(element.getId());
            case BOUNDARY_LINE -> new BoundaryLineContingency(element.getId());
            case BUS, DC_BUS -> new BusContingency(element.getId());
            case BUSBAR_SECTION -> new BusbarSectionContingency(element.getId());
            case DC_GROUND -> new DcGroundContingency(element.getId());
            case DC_LINE -> new DcLineContingency(element.getId());
            case GENERATOR -> new GeneratorContingency(element.getId());
            case HVDC_LINE -> new HvdcLineContingency(element.getId());
            case LINE -> new LineContingency(element.getId());
            case LOAD -> new LoadContingency(element.getId());
            case SHUNT_COMPENSATOR -> new ShuntCompensatorContingency(element.getId());
            case STATIC_VAR_COMPENSATOR -> new StaticVarCompensatorContingency(element.getId());
            case SWITCH, DC_SWITCH -> new SwitchContingency(element.getId());
            case THREE_WINDINGS_TRANSFORMER -> new ThreeWindingsTransformerContingency(element.getId());
            case TIE_LINE -> new TieLineContingency(element.getId());
            case TWO_WINDINGS_TRANSFORMER -> new TwoWindingsTransformerContingency(element.getId());
            case VOLTAGE_SOURCE_CONVERTER -> new VoltageSourceConverterContingency(element.getId());
            default -> {
                NcConversionReports.contingencyEquipmentTypeNotSupported(reportNode, element.getId(), contingencyId,
                    element.getType().name());
                yield null;
            }
        };
    }
}
