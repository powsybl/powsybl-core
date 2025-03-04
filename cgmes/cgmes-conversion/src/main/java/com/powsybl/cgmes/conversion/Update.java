/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.elements.*;
import com.powsybl.cgmes.conversion.elements.hvdc.DcLineSegmentConversion;
import com.powsybl.cgmes.conversion.elements.transformers.ThreeWindingsTransformerConversion;
import com.powsybl.cgmes.conversion.elements.transformers.TwoWindingsTransformerConversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

public final class Update {

    private static final String UNEXPECTED_ORIGINAL_CLASS = "Unexpected originalClass ";

    private Update() {
    }

    static void updateLoads(Network network, CgmesModel cgmes, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.LOAD.name()));

        Map<String, PropertyBag> equipmentIdPropertyBag = new HashMap<>();
        addPropertyBags(cgmes.energyConsumers(), CgmesNames.ENERGY_CONSUMER, equipmentIdPropertyBag);
        addPropertyBags(cgmes.energySources(), CgmesNames.ENERGY_SOURCE, equipmentIdPropertyBag);
        addPropertyBags(cgmes.asynchronousMachines(), CgmesNames.ASYNCHRONOUS_MACHINE, equipmentIdPropertyBag);

        network.getLoads().forEach(load -> updateLoad(load, getPropertyBag(load.getId(), equipmentIdPropertyBag), context));
        context.popReportNode();
    }

    private static void updateLoad(Load load, PropertyBag cgmesData, Context context) {
        if (!load.isFictitious()) { // Loads from SvInjections are fictitious
            String originalClass = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);

            switch (originalClass) {
                case CgmesNames.ENERGY_SOURCE -> EnergySourceConversion.update(load, cgmesData, context);
                case CgmesNames.ASYNCHRONOUS_MACHINE -> AsynchronousMachineConversion.update(load, cgmesData, context);
                case CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY, CgmesNames.ENERGY_CONSUMER ->
                        EnergyConsumerConversion.update(load, cgmesData, context);
                default ->
                        throw new ConversionException(UNEXPECTED_ORIGINAL_CLASS + originalClass + " for Load: " + load.getId());
            }
        }
    }

    static void updateGenerators(Network network, CgmesModel cgmes, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.GENERATOR.name()));

        Map<String, PropertyBag> equipmentIdPropertyBag = new HashMap<>();
        addPropertyBags(cgmes.synchronousMachinesForUpdate(), CgmesNames.SYNCHRONOUS_MACHINE, equipmentIdPropertyBag);
        addPropertyBags(cgmes.equivalentInjections(), CgmesNames.EQUIVALENT_INJECTION, equipmentIdPropertyBag);
        addPropertyBags(cgmes.externalNetworkInjections(), CgmesNames.EXTERNAL_NETWORK_INJECTION, equipmentIdPropertyBag);

        network.getGenerators().forEach(generator -> updateGenerator(generator, equipmentIdPropertyBag, context));
        context.popReportNode();
    }

    private static void updateGenerator(Generator generator, Map<String, PropertyBag> equipmentIdPropertyBag, Context context) {
        String originalClass = generator.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);

        switch (originalClass) {
            case CgmesNames.SYNCHRONOUS_MACHINE -> SynchronousMachineConversion.update(generator, getPropertyBag(generator.getId(), equipmentIdPropertyBag), context);
            case CgmesNames.EQUIVALENT_INJECTION -> EquivalentInjectionConversion.update(generator, getEquivalentInjectionPropertyBag(generator.getId(), context), context);
            case CgmesNames.EXTERNAL_NETWORK_INJECTION -> ExternalNetworkInjectionConversion.update(generator, getPropertyBag(generator.getId(), equipmentIdPropertyBag), context);
            default -> throw new ConversionException(UNEXPECTED_ORIGINAL_CLASS + originalClass + " for Generator: " + generator.getId());
        }
    }

    static void updateTransformers(Network network, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.TWO_WINDINGS_TRANSFORMER.name()));
        network.getTwoWindingsTransformers().forEach(t2w -> TwoWindingsTransformerConversion.update(t2w, context));
        context.popReportNode();

        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.THREE_WINDINGS_TRANSFORMER.name()));
        network.getThreeWindingsTransformers().forEach(t3w -> ThreeWindingsTransformerConversion.update(t3w, context));
        context.popReportNode();
    }

    static void updateStaticVarCompensators(Network network, CgmesModel cgmes, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.STATIC_VAR_COMPENSATOR.name()));

        Map<String, PropertyBag> equipmentIdPropertyBag = new HashMap<>();
        addPropertyBags(cgmes.staticVarCompensators(), CgmesNames.STATIC_VAR_COMPENSATOR, equipmentIdPropertyBag);

        network.getStaticVarCompensators().forEach(staticVarCompensator -> StaticVarCompensatorConversion.update(staticVarCompensator, getPropertyBag(staticVarCompensator.getId(), equipmentIdPropertyBag), context));
        context.popReportNode();
    }

    static void updateShuntCompensators(Network network, CgmesModel cgmes, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.SHUNT_COMPENSATOR.name()));

        Map<String, PropertyBag> equipmentIdPropertyBag = new HashMap<>();
        addPropertyBags(cgmes.shuntCompensators(), CgmesNames.SHUNT_COMPENSATOR, equipmentIdPropertyBag);
        addPropertyBags(cgmes.equivalentShunts(), CgmesNames.EQUIVALENT_SHUNT, equipmentIdPropertyBag);

        network.getShuntCompensators().forEach(shuntCompensator -> updateShuntCompensator(shuntCompensator, getPropertyBag(shuntCompensator.getId(), equipmentIdPropertyBag), context));
        context.popReportNode();
    }

    private static void updateShuntCompensator(ShuntCompensator shuntCompensator, PropertyBag cgmesData, Context context) {
        String isEquivalentShunt = shuntCompensator.getProperty(Conversion.PROPERTY_IS_EQUIVALENT_SHUNT);
        if (Boolean.parseBoolean(isEquivalentShunt)) {
            EquivalentShuntConversion.update(shuntCompensator, context);
        } else {
            ShuntConversion.update(shuntCompensator, cgmesData, context);
        }
    }

    static void updateHvdcLines(Network network, CgmesModel cgmes, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.HVDC_LINE.name()));

        Map<String, PropertyBag> equipmentIdPropertyBag = new HashMap<>();
        addPropertyBags(cgmes.acDcConverters(), CgmesNames.ACDC_CONVERTER, equipmentIdPropertyBag);
        network.getHvdcLines().forEach(hvdcLine -> DcLineSegmentConversion.update(hvdcLine, getPropertyBag(hvdcLine.getConverterStation1().getId(), equipmentIdPropertyBag), getPropertyBag(hvdcLine.getConverterStation2().getId(), equipmentIdPropertyBag), context));

        context.popReportNode();
    }

    static void updateDanglingLines(Network network, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.DANGLING_LINE.name()));
        network.getDanglingLines().forEach(danglingLine -> updateDanglingLine(danglingLine, context));
        context.popReportNode();
    }

    private static void updateDanglingLine(DanglingLine danglingLine, Context context) {
        String originalClass = danglingLine.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        switch (originalClass) {
            case CgmesNames.AC_LINE_SEGMENT -> ACLineSegmentConversion.update(danglingLine, context);
            case CgmesNames.POWER_TRANSFORMER -> TwoWindingsTransformerConversion.update(danglingLine, context);
            case CgmesNames.EQUIVALENT_BRANCH -> EquivalentBranchConversion.update(danglingLine, context);
            case CgmesNames.SWITCH -> SwitchConversion.update(danglingLine, getSwitchPropertyBag(danglingLine.getId(), context), context);
            default -> throw new ConversionException(UNEXPECTED_ORIGINAL_CLASS + originalClass + " for DanglingLine: " + danglingLine.getId());
        }
    }

    static void updateLines(Network network, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.LINE.name()));
        network.getLines().forEach(line -> updateLine(line, context));
        context.popReportNode();
    }

    private static void updateLine(Line line, Context context) {
        String originalClass = line.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        switch (originalClass) {
            case CgmesNames.AC_LINE_SEGMENT -> ACLineSegmentConversion.update(line, context);
            case CgmesNames.EQUIVALENT_BRANCH -> EquivalentBranchConversion.update(line, context);
            case CgmesNames.SERIES_COMPENSATOR -> SeriesCompensatorConversion.update(line, context);
            default -> throw new ConversionException(UNEXPECTED_ORIGINAL_CLASS + originalClass + " for Line: " + line.getId());
        }
    }

    static void updateSwitches(Network network, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.SWITCH.name()));
        network.getSwitches().forEach(sw -> updateSwitch(sw, context));
        context.popReportNode();
    }

    private static void updateSwitch(Switch sw, Context context) {
        if (sw.getProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL) != null) {
            TerminalConversion.update(sw, context);
            return;
        }
        String originalClass = sw.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        switch (originalClass) {
            case CgmesNames.AC_LINE_SEGMENT -> ACLineSegmentConversion.update(sw, context);
            case CgmesNames.EQUIVALENT_BRANCH -> EquivalentBranchConversion.update(sw, context);
            case CgmesNames.SERIES_COMPENSATOR -> SeriesCompensatorConversion.update(sw, context);
            case CgmesNames.SWITCH, "Breaker", "Disconnector", "LoadBreakSwitch", "ProtectedSwitch", "GroundDisconnector", "Jumper" ->
                    SwitchConversion.update(sw, getSwitchPropertyBag(sw.getId(), context), context);
            default -> throw new ConversionException(UNEXPECTED_ORIGINAL_CLASS + originalClass + " for Switch: " + sw.getId());
        }
    }

    // There are some node-breaker models that,
    // in addition to the information of opened switches also set
    // the terminal.connected property to false,
    // we have decided to create fictitious switches to precisely
    // map this situation to IIDM.
    // This behavior can be disabled through configuration.
    public static void createFictitiousSwitchesForDisconnectedTerminalsDuringUpdate(Network network, CgmesModel cgmes, Context context) {
        context.pushReportNode(CgmesReports.convertingDuringUpdateElementTypeReport(context.getReportNode(), CgmesNames.TERMINAL));
        if (createFictitiousSwitch(context)) {
            cgmes.terminals().forEach(cgmesTerminal -> TerminalConversion.create(network, cgmesTerminal, context));
        }
        context.popReportNode();
    }

    private static boolean createFictitiousSwitch(Context context) {
        return context.config().getCreateFictitiousSwitchesForDisconnectedTerminalsMode() != CgmesImport.FictitiousSwitchesCreationMode.NEVER;
    }

    // In some TYNDP there are three or more acLineSegments at the boundary node, only two connected.
    public static void createTieLinesWhenThereAreMoreThanTwoDanglingLinesAtBoundaryNodeDuringUpdate(Network network, Context context) {
        context.pushReportNode(CgmesReports.convertingDuringUpdateElementTypeReport(context.getReportNode(), IdentifiableType.TIE_LINE.name()));
        TieLineConversion.createDuringUpdate(network, context);
        context.popReportNode();
    }

    public static void updateVoltageLevels(Network network, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.VOLTAGE_LEVEL.name()));
        network.getVoltageLevels().forEach(voltageLevel -> VoltageLevelConversion.update(voltageLevel, context));
        context.popReportNode();
    }

    public static void updateGrounds(Network network, Context context) {
        context.pushReportNode(CgmesReports.updatingElementTypeReport(context.getReportNode(), IdentifiableType.GROUND.name()));
        network.getGrounds().forEach(ground -> GroundConversion.update(ground, context));
        context.popReportNode();
    }

    public static void createFictitiousLoadsForSvInjectionsDuringUpdate(Network network, CgmesModel cgmes, Context context) {
        context.pushReportNode(CgmesReports.convertingDuringUpdateElementTypeReport(context.getReportNode(), CgmesNames.SV_INJECTION));
        if (context.config().convertSvInjections()) {
            cgmes.svInjections().forEach(svInjection -> SvInjectionConversion.create(network, svInjection));
        }
        context.popReportNode();
    }

    public static void updateVoltageAndAnglesAndComplete(Network network, Context context) {
        context.pushReportNode(CgmesReports.settingVoltagesAndAnglesReport(context.getReportNode()));
        // update voltage and angles
        network.getBusView().getBuses().forEach(bus -> NodeConversion.update(bus, context));

        // Voltage and angle in boundary buses
        network.getDanglingLineStream(DanglingLineFilter.UNPAIRED)
                .forEach(AbstractConductingEquipmentConversion::calculateVoltageAndAngleInBoundaryBus);

        // Now in tieLines
        network.getTieLines().forEach(tieLine -> AbstractConductingEquipmentConversion.calculateVoltageAndAngleInBoundaryBus(tieLine.getDanglingLine1(), tieLine.getDanglingLine2()));

        // Voltage and angle in starBus as properties
        network.getThreeWindingsTransformers().forEach(ThreeWindingsTransformerConversion::calculateVoltageAndAngleInStarBus);
        context.popReportNode();
    }

    private static void addPropertyBags(PropertyBags propertyBags, String idTag, Map<String, PropertyBag> equipmentIdPropertyBag) {
        propertyBags.forEach(propertyBag -> equipmentIdPropertyBag.put(propertyBag.getId(idTag), propertyBag));
    }

    private static PropertyBag getPropertyBag(String identifiableId, Map<String, PropertyBag> equipmentIdPropertyBag) {
        return equipmentIdPropertyBag.containsKey(identifiableId) ? equipmentIdPropertyBag.get(identifiableId) : emptyPropertyBag();
    }

    private static PropertyBag getEquivalentInjectionPropertyBag(String equivalentInjectionId, Context context) {
        PropertyBag cgmesData = context.equivalentInjection(equivalentInjectionId);
        return cgmesData != null ? cgmesData : emptyPropertyBag();
    }

    private static PropertyBag getSwitchPropertyBag(String switchId, Context context) {
        PropertyBag cgmesData = context.cgmesSwitch(switchId);
        return cgmesData != null ? cgmesData : emptyPropertyBag();
    }

    private static PropertyBag emptyPropertyBag() {
        return new PropertyBag(Collections.emptyList(), false);
    }
}
