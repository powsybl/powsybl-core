/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.export.CgmesRecords.AsynchronousMachineSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.ControlAreaSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.CsConverterSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.DCTerminalSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.EnergyConsumerSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.EnergySourceSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.EquivalentInjectionSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.ExternalNetworkInjectionSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.GeneratingUnitSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.RegulatingControlSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.ShuntCompensatorSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.StaticVarCompensatorSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.SwitchSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.SynchronousMachineSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.TapChangerSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.TerminalSsh;
import com.powsybl.cgmes.conversion.export.CgmesRecords.VsConverterSsh;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

import static com.powsybl.cgmes.conversion.Conversion.*;
import static com.powsybl.cgmes.conversion.export.CgmesExportUtil.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.refTyped;
import static com.powsybl.iidm.network.util.VoltageRegulationUtils.getSignToUseLoadSignConvention;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class SteadyStateHypothesisExport {

    private static final Logger LOG = LoggerFactory.getLogger(SteadyStateHypothesisExport.class);
    private static final String OPERATING_MODE_GENERATOR = "generator";
    private static final String OPERATING_MODE_MOTOR = "motor";
    private static final String OPERATING_MODE_CONDENSER = "condenser";

    private SteadyStateHypothesisExport() {
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        CgmesMetadataModel model = CgmesExport.initializeModelForExport(
                network, CgmesSubset.STEADY_STATE_HYPOTHESIS, context, true, false);
        write(network, writer, context, model);
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context, CgmesMetadataModel model) {
        final Map<String, List<RegulatingControlView>> regulatingControlViews = new HashMap<>();
        SshSink sink = SshSink.immediate(writer, context);

        try {
            CgmesExportUtil.writeRdfRoot(context, writer);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(network, CgmesSubset.STEADY_STATE_HYPOTHESIS, writer, model, context);
            }

            exportLoads(network, sink, context);
            exportFictitiousInjections(network, sink, context);
            exportEquivalentInjections(network, sink, context);
            exportTapChangers(network, regulatingControlViews, sink, context);
            exportGenerators(network, regulatingControlViews, sink, context);
            exportBatteries(network, sink, context);
            exportShuntCompensators(network, regulatingControlViews, sink, context);
            exportStaticVarCompensators(network, regulatingControlViews, sink, context);
            exportRegulatingControls(regulatingControlViews, sink, context);
            exportGeneratingUnitsParticitationFactors(network, sink, context);
            exportConverters(network, sink, context);
            exportDCTerminals(network, sink, context);
            // FIXME open status of retained switches in bus-branch models
            exportSwitches(network, sink, context);
            exportTerminals(network, sink, context);
            exportControlAreas(network, sink, context);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void exportSwitches(Network network, SshSink sink, CgmesExportContext context) {
        for (Switch sw : network.getSwitches()) {
            exportSwitch(sw, sink, context);
        }
    }

    private static void exportSwitch(Switch sw, SshSink sink, CgmesExportContext context) {
        if (context.isExportedEquipment(sw)) {
            String switchType = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS); // may be null
            if (!isSwitchImportedFromAcLineSegmentEquivalentBranchOrSeriesCompensator(switchType)) {
                exportSwitchSsh(sw, sink, context);
            }
        }
    }

    private static void exportTerminalForSwitches(Network network, SshSink sink, CgmesExportContext context) {
        for (Switch sw : network.getSwitches()) {
            exportTerminalForSwitch(sw, sink, context);
        }
    }

    private static void exportTerminalForSwitch(Switch sw, SshSink sink, CgmesExportContext context) {
        if (context.isExportedEquipment(sw)) {
            String switchType = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS); // may be null
            boolean connected = isConnected(sw, switchType);

            exportTerminal(context.getNamingStrategy().getCgmesIdFromAlias(sw, ALIAS_TERMINAL1), connected, sink, context);
            exportTerminal(context.getNamingStrategy().getCgmesIdFromAlias(sw, ALIAS_TERMINAL2), connected, sink, context);
        }
    }

    private static boolean isSwitchImportedFromAcLineSegmentEquivalentBranchOrSeriesCompensator(String switchType) {
        return "ACLineSegment".equals(switchType) || "EquivalentBranch".equals(switchType) || "SeriesCompensator".equals(switchType);
    }

    private static boolean isConnected(Switch sw, String switchType) {
        if (isSwitchImportedFromAcLineSegmentEquivalentBranchOrSeriesCompensator(switchType)) {
            return !sw.isOpen();
        } else {
            // Terminals for switches are exported as always connected
            // The status of the switch is "open" if any of the original terminals were not connected
            // An original "closed" switch with any terminal disconnected
            // will be exported as "open" with terminals connected
            return true;
        }
    }

    private static void exportTerminalForBoundaryLines(Network network, SshSink sink, CgmesExportContext context) {
        for (BoundaryLine bl : network.getBoundaryLines(BoundaryLineFilter.ALL)) {
            exportTerminalForBoundaryLine(bl, sink, context);
        }
    }

    private static void exportTerminalForBoundaryLine(BoundaryLine bl, SshSink sink, CgmesExportContext context) {
        // Terminal for equivalent injection at boundary is always connected
        exportTerminal(context.getNamingStrategy().getCgmesIdFromProperty(bl, PROPERTY_EQUIVALENT_INJECTION_TERMINAL), true, sink, context);
        // Terminal for boundary side of original line/switch is always connected
        exportTerminal(CgmesExportUtil.getBoundaryLineBoundaryTerminalId(bl, context), true, sink, context);
    }

    private static void exportTerminalForBuses(Network network, SshSink sink, CgmesExportContext context) {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            exportTerminalForBus(b, sink, context);
        }
    }

    private static void exportTerminalForBus(Bus b, SshSink sink, CgmesExportContext context) {
        String bbsTerminals = b.getProperty(PROPERTY_BUSBAR_SECTION_TERMINALS, "");
        if (!bbsTerminals.isEmpty()) {
            for (String bbsTerminal : bbsTerminals.split(",")) {
                exportTerminal(bbsTerminal, true, sink, context);
            }
        }
    }

    private static void exportTerminals(Network network, SshSink sink, CgmesExportContext context) {
        for (Connectable<?> c : network.getConnectables()) { // TODO write boundary terminals for tie lines from CGMES
            exportTerminals(c, sink, context);
        }
        exportTerminalForSwitches(network, sink, context);
        exportTerminalForBoundaryLines(network, sink, context);
        // If we are performing an updated export, write recorded busbar section terminals as connected
        if (!context.isExportEquipment()) {
            exportTerminalForBuses(network, sink, context);
        }
    }

    private static void exportTerminals(Connectable<?> c, SshSink sink, CgmesExportContext context) {
        if (context.isExportedEquipment(c)) {
            if (CgmesExportUtil.isEquivalentShuntWithZeroSectionCount(c)) {
                // Equivalent shunts do not have a section count in SSH, SV profiles,
                // the only way to make output consistent with IIDM section count == 0 is to disconnect its terminal
                exportTerminal(CgmesExportUtil.getTerminalId(c.getTerminals().get(0), context), false, sink, context);
            } else {
                for (Terminal t : c.getTerminals()) {
                    exportTerminal(t, sink, context);
                }
            }
        }
    }

    private static void exportFictitiousInjections(Network network, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (VoltageLevel vl : network.getVoltageLevels()) {
            exportFictitiousInjections(vl, sink, context);
        }
    }

    private static void exportFictitiousInjections(VoltageLevel vl, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER && !context.isBusBranchExport()) {
            exportNodeBreakerFictitiousInjections(vl, sink, context);
        } else {
            exportBusBranchFictitiousInjections(vl, sink, context);
        }
    }

    private static void exportNodeBreakerFictitiousInjections(VoltageLevel vl, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        VoltageLevel.NodeBreakerView nb = vl.getNodeBreakerView();
        for (int node : nb.getNodes()) {
            double p = nb.getFictitiousP0(node);
            double q = nb.getFictitiousQ0(node);
            if (p != 0.0 || q != 0.0) {
                String loadId = context.getNamingStrategy().getCgmesId(refTyped(vl), FICTITIOUS, ref("NCL"), ref(node));
                String terminalId = context.getNamingStrategy().getCgmesId(refTyped(vl), FICTITIOUS, TERMINAL, ref(node));
                exportFictitiousInjection(loadId, terminalId, p, q, sink, context);
            }
        }
    }

    private static void exportBusBranchFictitiousInjections(VoltageLevel vl, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : vl.getBusBreakerView().getBuses()) {
            double p = b.getFictitiousP0();
            double q = b.getFictitiousQ0();
            if (p != 0.0 || q != 0.0) {
                String loadId = context.getNamingStrategy().getCgmesId(refTyped(b), FICTITIOUS, ref("NCL"));
                String terminalId = context.getNamingStrategy().getCgmesId(refTyped(b), FICTITIOUS, TERMINAL);
                exportFictitiousInjection(loadId, terminalId, p, q, sink, context);
            }
        }
    }

    private static void exportFictitiousInjection(String loadId, String terminalId, double p, double q,
                                                  SshSink sink,
                                                  CgmesExportContext context) throws XMLStreamException {
        if (p <= 0) {
            EnergySourceSsh energySource = new EnergySourceSsh(loadId, p, q);
            sink.accept(energySource);
        } else {
            EnergyConsumerSsh energyConsumer = new EnergyConsumerSsh(CgmesNames.NONCONFORM_LOAD, loadId, p, q);
            sink.accept(energyConsumer);
        }
        // Terminal connected state (always connected in SSH for fictitious terminals)
        exportTerminal(terminalId, true, sink, context);
    }

    private static void exportEquivalentInjections(Network network, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        // One equivalent injection for every boundary line
        List<String> exported = new ArrayList<>();

        for (BoundaryLine bl : network.getBoundaryLines(BoundaryLineFilter.ALL)) {
            String equivalentInjectionId = context.getNamingStrategy().getCgmesIdFromProperty(bl, PROPERTY_EQUIVALENT_INJECTION);
            if (!exported.contains(equivalentInjectionId)) {
                exportEquivalentInjection(bl, sink, context);
                exported.add(equivalentInjectionId);
            }
        }
    }

    private static void exportEquivalentInjection(BoundaryLine bl, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        // regulationStatus and regulationTarget are optional,
        // but test cases contain the attributes with disabled and 0
        boolean regulationStatus = false;
        double regulationTarget = 0;
        if (bl.getGeneration() != null) {
            regulationStatus = bl.getGeneration().isVoltageRegulationOn();
            regulationTarget = bl.getGeneration().getTargetV();
        }
        EquivalentInjectionSsh equivalentInjection = new EquivalentInjectionSsh(
            context.getNamingStrategy().getCgmesIdFromProperty(bl, PROPERTY_EQUIVALENT_INJECTION),
            bl.getP0(),
            bl.getQ0(),
            regulationStatus,
            regulationTarget
        );
        sink.accept(equivalentInjection);
    }

    private static void exportTapChangers(Network network, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                          SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            exportTapChangers(twt, regulatingControlViews, sink, context);
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            exportTapChangers(twt, regulatingControlViews, sink, context);
        }
    }

    private static void exportTapChangers(TwoWindingsTransformer twt, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                          SshSink sink, CgmesExportContext context) throws XMLStreamException {
        if (twt.hasPhaseTapChanger()) {
            String aliasType = twt.getAliasFromType(ALIAS_PHASE_TAP_CHANGER2).isPresent() && twt.getAliasFromType(ALIAS_PHASE_TAP_CHANGER1).isEmpty() ?
                ALIAS_PHASE_TAP_CHANGER2 : ALIAS_PHASE_TAP_CHANGER1;
            exportTapChanger(twt, aliasType, PHASE_TAP_CHANGER, 1, CgmesNames.PHASE_TAP_CHANGER_TABULAR, twt.getPhaseTapChanger(), regulatingControlViews, sink, context);
        }
        if (twt.hasRatioTapChanger()) {
            String aliasType = twt.getAliasFromType(ALIAS_RATIO_TAP_CHANGER2).isPresent() && twt.getAliasFromType(ALIAS_RATIO_TAP_CHANGER1).isEmpty() ?
                ALIAS_RATIO_TAP_CHANGER2 : ALIAS_RATIO_TAP_CHANGER1;
            exportTapChanger(twt, aliasType, RATIO_TAP_CHANGER, 1, CgmesNames.RATIO_TAP_CHANGER, twt.getRatioTapChanger(), regulatingControlViews, sink, context);
        }
    }

    private static void exportTapChangers(ThreeWindingsTransformer twt, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                          SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
            int endNumber = leg.getSide().getNum();
            if (leg.hasPhaseTapChanger()) {
                String aliasType = getPhaseTapChangerAliasType(Integer.toString(endNumber));
                exportTapChanger(twt, aliasType, PHASE_TAP_CHANGER, endNumber, CgmesNames.PHASE_TAP_CHANGER_TABULAR,
                    leg.getPhaseTapChanger(), regulatingControlViews, sink, context);
            }
            if (leg.hasRatioTapChanger()) {
                String aliasType = getRatioTapChangerAliasType(Integer.toString(endNumber));
                exportTapChanger(twt, aliasType, RATIO_TAP_CHANGER, endNumber, CgmesNames.RATIO_TAP_CHANGER, leg.getRatioTapChanger(), regulatingControlViews, sink, context);
            }
        }
    }

    private static <C extends Connectable<C>> void exportTapChanger(C twt, String aliasType, Part part, int endNumber,
                                                                    String defaultType, TapChanger<?, ?, ?, ?> tc,
                                                                    Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                                    SshSink sink,
                                                                    CgmesExportContext context) throws XMLStreamException {
        String tapChangerId = context.getNamingStrategy().getCgmesIdFromAlias(twt, aliasType);
        String cgmesTapChangerId = twt.getAliasFromType(aliasType).orElse(null);
        String tapChangerControlId = getTapChangerControlId(twt, part, endNumber, cgmesTapChangerId, context);
        String type = defaultType;
        if (tc instanceof PhaseTapChanger && !context.isExportEquipment()) {
            type = getPhaseTapChangerType(twt, cgmesTapChangerId);
        }

        TapChangerSsh tapChanger = new TapChangerSsh(type, tapChangerId, tc.isRegulating(), tc.getTapPosition());
        sink.accept(tapChanger);
        if (tc instanceof RatioTapChanger rtc) {
            addRegulatingControlView(rtc, tapChangerControlId, regulatingControlViews, context);
        } else if (tc instanceof PhaseTapChanger ptc) {
            addRegulatingControlView(ptc, tapChangerControlId, regulatingControlViews);
        }

        // If we are exporting equipment definitions the hidden tap changer will not be exported
        // because it has been included in the model for the only tap changer left in IIDM
        // If we are exporting only SSH, SV, ... we have to write the step we have saved for it
        if (!context.isExportEquipment()) {
            Optional<CgmesTapChanger> hiddenCombinedTapChanger = getHiddenCombinedTapChanger(twt, cgmesTapChangerId);
            if (hiddenCombinedTapChanger.isPresent()) {
                exportHiddenTapChanger(hiddenCombinedTapChanger.get(), defaultType, sink, context);
            }
        }
    }

    private static void exportShuntCompensators(Network network, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            exportShuntCompensator(s, regulatingControlViews, sink, context);
        }
    }

    private static void exportShuntCompensator(ShuntCompensator s, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                               SshSink sink, CgmesExportContext context) throws XMLStreamException {
        if ("true".equals(s.getProperty(PROPERTY_IS_EQUIVALENT_SHUNT))) {
            return;
        }

        ShuntCompensatorSsh shuntCompensator = new ShuntCompensatorSsh(
            shuntCompensatorClassName(s.getModelType()),
            context.getNamingStrategy().getCgmesId(s),
            s.getSectionCount(),
            s.isRegulating()
        );
        sink.accept(shuntCompensator);

        addRegulatingControlView(s, getRegulatingControlId(s, context), regulatingControlViews, context);
    }

    private static void exportGenerators(Network network, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                 SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (Generator g : network.getGenerators()) {
            exportGenerator(g, regulatingControlViews, sink, context);
        }
    }

    private static void exportGenerator(Generator g, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                        SshSink sink, CgmesExportContext context) throws XMLStreamException {
        String cgmesOriginalClass = g.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SYNCHRONOUS_MACHINE);

        boolean controlEnabled = g.isRegulating();
        switch (cgmesOriginalClass) {
            case CgmesNames.EQUIVALENT_INJECTION:
                EquivalentInjectionSsh equivalentInjection = new EquivalentInjectionSsh(
                    context.getNamingStrategy().getCgmesId(g),
                    -g.getTargetP(),
                    -g.getLocalTargetQ(),
                    controlEnabled,
                    g.getLocalTargetV()
                );
                sink.accept(equivalentInjection);
                break;
            case CgmesNames.EXTERNAL_NETWORK_INJECTION:
                ExternalNetworkInjectionSsh externalNetworkInjection = new ExternalNetworkInjectionSsh(
                    context.getNamingStrategy().getCgmesId(g),
                    controlEnabled,
                    -g.getTargetP(),
                    -g.getLocalTargetQ(),
                    ReferencePriority.get(g)
                );
                sink.accept(externalNetworkInjection);
                addRegulatingControlView(g, getRegulatingControlId(g, context), regulatingControlViews, context);
                break;
            case CgmesNames.SYNCHRONOUS_MACHINE:
                SynchronousMachineSsh synchronousMachine = new SynchronousMachineSsh(
                    context.getNamingStrategy().getCgmesId(g),
                    controlEnabled,
                    -g.getTargetP(),
                    -g.getLocalTargetQ(),
                    ReferencePriority.get(g),
                    obtainOperatingMode(g, g.getMinP(), g.getMaxP(), g.getTargetP())
                );
                sink.accept(synchronousMachine);
                addRegulatingControlView(g, getRegulatingControlId(g, context), regulatingControlViews, context);
                break;
            default:
                throw new PowsyblException("Unexpected cgmes equipment " + cgmesOriginalClass);
        }
    }

    private static void exportBatteries(Network network, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (Battery b : network.getBatteries()) {
            exportBattery(b, sink, context);
        }
    }

    private static void exportBattery(Battery b, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        boolean controlEnabled = b.getVoltageRegulation() != null && b.getVoltageRegulation().isRegulating();
        SynchronousMachineSsh synchronousMachine = new SynchronousMachineSsh(
            context.getNamingStrategy().getCgmesId(b),
            controlEnabled,
            -b.getTargetP(),
            -b.getRegulatingTargetQ(),
            ReferencePriority.get(b),
            obtainOperatingMode(b, b.getMinP(), b.getMaxP(), b.getTargetP())
        );
        sink.accept(synchronousMachine);
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> String obtainOperatingMode(I i, double minP, double maxP, double targetP) {
        String calculatedKind = obtainCalculatedSynchronousMachineKind(minP, maxP, obtainCurve(i), i instanceof Battery || i instanceof Generator gen && gen.isCondenser());
        return obtainOperatingMode(targetP, i, calculatedKind);
    }

    private static String obtainOperatingMode(double targetP, Injection<?> injection, String calculatedKind) {
        if (targetP < 0) {
            return OPERATING_MODE_MOTOR;
        } else if (targetP > 0) {
            return OPERATING_MODE_GENERATOR;
        } else {
            if (isOperatingAsACondenser(injection) && calculatedKind.toLowerCase().contains(OPERATING_MODE_CONDENSER)) {
                return OPERATING_MODE_CONDENSER;
            } else {
                if (calculatedKind.toLowerCase().contains(OPERATING_MODE_GENERATOR)) {
                    return OPERATING_MODE_GENERATOR;
                } else if (calculatedKind.toLowerCase().contains(OPERATING_MODE_MOTOR)) {
                    return OPERATING_MODE_MOTOR;
                } else {
                    return OPERATING_MODE_CONDENSER;
                }
            }
        }
    }

    private static boolean isOperatingAsACondenser(Injection<?> injection) {
        switch (injection) {
            case Generator generator -> {
                return generator.isRegulatingWithMode(RegulationMode.VOLTAGE) && !Double.isNaN(generator.getLocalTargetV())
                    || !Double.isNaN(generator.getRegulatingTargetQ()) && generator.getRegulatingTargetQ() != 0;
            }
            case Battery battery -> {
                return battery.isRegulatingWithMode(RegulationMode.VOLTAGE) && !Double.isNaN(battery.getLocalTargetV())
                    || !Double.isNaN(battery.getRegulatingTargetQ()) && battery.getRegulatingTargetQ() != 0;
            }
            default -> throw new IllegalStateException("Unexpected value: " + injection);
        }
    }

    private static void exportStaticVarCompensators(Network network, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                    SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            exportStaticVarCompensator(svc, regulatingControlViews, sink, context);
        }
    }

    private static void exportStaticVarCompensator(StaticVarCompensator svc, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                   SshSink sink, CgmesExportContext context) throws XMLStreamException {
        StaticVarCompensatorSsh staticVarCompensator = new StaticVarCompensatorSsh(
            context.getNamingStrategy().getCgmesId(svc),
            svc.isRegulating(),
            svc.getLocalTargetQ()
        );
        sink.accept(staticVarCompensator);

        addRegulatingControlView(svc, getRegulatingControlId(svc, context), regulatingControlViews, context);
    }

    private static void addRegulatingControlView(PhaseTapChanger ptc, String controlId, Map<String, List<RegulatingControlView>> regulatingControlViews) {
        // Multiple tap changers can be stored at the same equipment
        // We use the tap changer id as part of the key for storing the tap changer control id
        if (hasTapChangerControlCapability(ptc)) {
            RegulatingControlView rcv;
            PhaseTapChanger.RegulationMode mode = ptc.getRegulationMode();
            rcv = switch (mode) {
                case PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL ->
                    new RegulatingControlView(controlId, RegulatingControlType.TAP_CHANGER_CONTROL,
                        true, ptc.isRegulating(), ptc.getTargetDeadband(), ptc.getRegulationValue(), "M");
                case PhaseTapChanger.RegulationMode.CURRENT_LIMITER ->
                    new RegulatingControlView(controlId, RegulatingControlType.TAP_CHANGER_CONTROL,
                        true, false, 0.0, 0.0, "M");
            };

            regulatingControlViews.computeIfAbsent(controlId, k -> new ArrayList<>()).add(rcv);
        }
    }

    private static void exportHiddenTapChanger(CgmesTapChanger cgmesTc, String defaultType, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        TapChangerSsh tapChanger = new TapChangerSsh(Optional.ofNullable(cgmesTc.getType()).orElse(defaultType), cgmesTc.getId(), false,
                cgmesTc.getStep().orElseThrow(() -> new PowsyblException("Non null step expected for tap changer " + cgmesTc.getId())));
        sink.accept(tapChanger);
    }

    private static String getRegulatingControlId(Identifiable<?> identifiable, CgmesExportContext context) {
        return context.getNamingStrategy().getCgmesIdFromProperty(identifiable, PROPERTY_REGULATING_CONTROL);
    }

    private static void addRegulatingControlView(VoltageRegulationHolder<?> regulationHolder, String regulatingControlId,
                                                 Map<String, List<RegulatingControlView>> regulatingControlViews, CgmesExportContext context) {
        VoltageRegulation voltageRegulation = regulationHolder.getVoltageRegulation();
        if (voltageRegulation != null) {
            boolean enabled = voltageRegulation.isRegulating();

            // Only discrete regulation holders can have a non-zero deadband
            boolean discrete = false;
            double targetDeadband = 0.0;
            if (regulationHolder instanceof ShuntCompensator || regulationHolder instanceof RatioTapChanger) {
                discrete = true;
                targetDeadband = voltageRegulation.getTargetDeadband();
            }

            // VoltageRegulation Terminal can be left null to force the use of local target instead of the remote one,
            // thus targets should be determined with VoltageRegulationHolder.getRegulatingTargetQ/V
            double targetValue;
            String targetValueUnitMultiplier;
            String mode = getRegulatingControlMode(voltageRegulation);
            if (CgmesNames.REGULATING_CONTROL_REACTIVE_POWER.equals(mode)) {
                // The returned targetQ use the sign convention of the regulation holder
                targetValue = regulationHolder.getRegulatingTargetQ();
                // Generator, Battery and VscConverterStation are in generator sign convention in IIDM
                // and load sign convention in CGMES
                // We need to negate the target value to convert it to load sign convention for CGMES
                int signToUseLoadConvention = getSignToUseLoadSignConvention(regulationHolder);
                targetValue = signToUseLoadConvention * targetValue;
                targetValueUnitMultiplier = "M";
            } else if (CgmesNames.REGULATING_CONTROL_VOLTAGE.equals(mode)) {
                targetValue = regulationHolder.getRegulatingTargetV();
                if (regulationHolder instanceof Generator && context.isExportGeneratorsInLocalRegulationMode()) {
                    targetValue = regulationHolder.getLocalTargetV();
                }
                targetValueUnitMultiplier = "k";
            } else {
                throw new IllegalStateException("Unexpected regulation mode: " + mode);
            }

            // RatioTapChanger VoltageRegulation is exported to a specialized class
            RegulatingControlType regulatingControlType = RegulatingControlType.REGULATING_CONTROL;
            if (regulationHolder instanceof RatioTapChanger) {
                regulatingControlType = RegulatingControlType.TAP_CHANGER_CONTROL;
            }

            RegulatingControlView rcv = new RegulatingControlView(regulatingControlId, regulatingControlType,
                discrete, enabled, targetDeadband, targetValue, targetValueUnitMultiplier);
            regulatingControlViews.computeIfAbsent(regulatingControlId, k -> new ArrayList<>()).add(rcv);
        }
    }

    private static void exportRegulatingControls(Map<String, List<RegulatingControlView>> regulatingControlViews, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (List<RegulatingControlView> views : regulatingControlViews.values()) {
            exportRegulatingControl(combineRegulatingControlViews(views), sink, context);
        }
    }

    private static RegulatingControlView combineRegulatingControlViews(List<RegulatingControlView> rcs) {
        RegulatingControlView combined = rcs.get(0);
        if (rcs.size() > 1 && LOG.isWarnEnabled()) {
            LOG.warn("Multiple views ({}) for regulating control {} are combined", rcs.size(), rcs.get(0).id);
        }
        for (int k = 1; k < rcs.size(); k++) {
            RegulatingControlView current = rcs.get(k);
            if (combinedTargetDeadbandMustBeUpdated(current.targetDeadband, combined.targetDeadband)) {
                combined.targetDeadband = current.targetDeadband;
            }
            if (!combined.discrete && current.discrete) {
                combined.discrete = true;
            }
            if (!combined.controlEnabled && current.controlEnabled) {
                combined.controlEnabled = true;
            }
        }
        return combined;
    }

    private static boolean combinedTargetDeadbandMustBeUpdated(double currentTargetDeadband, double combinedTargetDeadband) {
        return currentTargetDeadband == 0 && (Double.isNaN(combinedTargetDeadband) || combinedTargetDeadband < 0)
                || currentTargetDeadband > 0 && (combinedTargetDeadband == 0 || currentTargetDeadband < combinedTargetDeadband);
    }

    private static void exportRegulatingControl(RegulatingControlView rc, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        RegulatingControlSsh regulatingControl = new RegulatingControlSsh(
            regulatingControlClassname(rc.type),
            rc.id,
            rc.discrete,
            rc.controlEnabled,
            rc.targetDeadband,
            rc.targetValue,
            rc.targetValueUnitMultiplier
        );
        sink.accept(regulatingControl);
    }

    private static String regulatingControlClassname(RegulatingControlType type) {
        if (type == RegulatingControlType.TAP_CHANGER_CONTROL) {
            return "TapChangerControl";
        } else {
            return "RegulatingControl";
        }
    }

    private static void exportSwitchSsh(Switch sw, SshSink sink, CgmesExportContext context) {
        try {
            String switchType = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS);
            String className = switchType != null ? switchType : CgmesExportUtil.switchClassname(sw.getKind());
            SwitchSsh switchSsh = new SwitchSsh(
                className,
                context.getNamingStrategy().getCgmesId(sw),
                sw.isOpen()
            );
            sink.accept(switchSsh);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void exportTerminal(Terminal t, SshSink sink, CgmesExportContext context) {
        exportTerminal(CgmesExportUtil.getTerminalId(t, context), t.isConnected(), sink, context);
    }

    private static void exportTerminal(String terminalId, boolean connected, SshSink sink, CgmesExportContext context) {
        try {
            TerminalSsh terminal = new TerminalSsh(terminalId, connected);
            sink.accept(terminal);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void exportLoads(Network network, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            exportLoad(load, sink, context);
        }
    }

    private static void exportLoad(Load load, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        if (context.isExportedEquipment(load)) {
            String className = obtainLoadClassName(load, context);
            switch (className) {
                case CgmesNames.ASYNCHRONOUS_MACHINE -> {
                    AsynchronousMachineSsh asynchronousMachine = new AsynchronousMachineSsh(
                        context.getNamingStrategy().getCgmesId(load),
                        load.getP0(),
                        load.getQ0(),
                        false,
                        obtainAsynchronousMachineKind(load.getP0())
                    );
                    sink.accept(asynchronousMachine);
                }
                case CgmesNames.ENERGY_SOURCE -> {
                    EnergySourceSsh energySource = new EnergySourceSsh(
                        context.getNamingStrategy().getCgmesId(load),
                        load.getP0(),
                        load.getQ0()
                    );
                    sink.accept(energySource);
                }
                case CgmesNames.ENERGY_CONSUMER, CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY -> {
                    EnergyConsumerSsh energyConsumer = new EnergyConsumerSsh(
                        className,
                        context.getNamingStrategy().getCgmesId(load),
                        load.getP0(),
                        load.getQ0()
                    );
                    sink.accept(energyConsumer);
                }
                default -> throw new PowsyblException("Unexpected class name: " + className);
            }
        }
    }

    // if EQ is not exported, the original class name is preserved
    private static String obtainLoadClassName(Load load, CgmesExportContext context) {
        String originalClassName = load.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS);
        return (originalClassName != null && !context.isExportEquipment()) ? originalClassName : CgmesExportUtil.loadClassName(load);
    }

    private static String obtainAsynchronousMachineKind(double p) {
        if (p < 0) {
            return OPERATING_MODE_GENERATOR;
        } else {
            return OPERATING_MODE_MOTOR;
        }
    }

    private static void exportConverters(Network network, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (HvdcConverterStation<?> converterStation : network.getHvdcConverterStations()) {
            exportConverterStation(converterStation, sink, context);
        }
        for (LineCommutatedConverter lccConverter : network.getLineCommutatedConverters()) {
            exportAcDcConverter(lccConverter, sink, context);
        }
        for (VoltageSourceConverter vscConverter : network.getVoltageSourceConverters()) {
            exportAcDcConverter(vscConverter, sink, context);
        }
    }

    private static void exportConverterStation(HvdcConverterStation<?> converterStation, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        String converterId = context.getNamingStrategy().getCgmesId(converterStation);
        double targetPpcc;
        double targetUdc;
        double p;
        double q;
        if (CgmesExportUtil.isConverterStationRectifier(converterStation)) {
            targetPpcc = converterStation.getHvdcLine().getActivePowerSetpoint();
            targetUdc = 0.0;
            p = targetPpcc;
        } else {
            HvdcLine hvdcLine = converterStation.getHvdcLine();
            double otherConverterStationLossFactor = converterStation.getOtherConverterStation().map(HvdcConverterStation::getLossFactor).orElse(0.0f);
            double pDCRectifier = hvdcLine.getActivePowerSetpoint() * (1 - otherConverterStationLossFactor / 100);
            double idc = pDCRectifier / hvdcLine.getNominalV();
            double pDCInverter = -1 * (pDCRectifier - hvdcLine.getR() * idc * idc);
            double poleLoss = converterStation.getLossFactor() / 100 * Math.abs(pDCInverter);
            targetPpcc = 0.0;
            targetUdc = hvdcLine.getNominalV() - hvdcLine.getR() * idc;
            p = pDCInverter + poleLoss;
        }

        if (converterStation instanceof LccConverterStation lccConverterStation) {
            q = Math.abs(getQfromPowerFactor(p, lccConverterStation.getPowerFactor()));
            String operatingMode = CgmesExportUtil.isConverterStationRectifier(converterStation) ? "rectifier" : "inverter";
            String pPccControl = CgmesExportUtil.isConverterStationRectifier(converterStation) ? "activePower" : "dcVoltage";
            CsConverterSsh csConverter = new CsConverterSsh(converterId, targetPpcc, targetUdc, p, q, 0.0, 0.0, 0.0, operatingMode, pPccControl);
            sink.accept(csConverter);
        } else if (converterStation instanceof VscConverterStation vscConverterStation) {
            p = vscConverterStation.getRegulatingTerminal().getP();
            q = -vscConverterStation.getLocalTargetQ();
            double targetQpcc = vscConverterStation.isWithMode(RegulationMode.REACTIVE_POWER) ? -vscConverterStation.getRegulatingTargetQ() : 0; // To be consistent with the import
            double targetUpcc = vscConverterStation.isWithMode(RegulationMode.VOLTAGE) ? vscConverterStation.getRegulatingTargetV() : 0;
            String pPccControl = CgmesExportUtil.isConverterStationRectifier(converterStation) ? "pPcc" : "udc";
            String qPccControl = vscConverterStation.isRegulatingWithMode(RegulationMode.VOLTAGE) ? "voltagePcc" : "reactivePcc";
            VsConverterSsh vsConverter = new VsConverterSsh(converterId, targetPpcc, targetUdc, p, q, 0.0, 0.0, 0.0, targetQpcc, targetUpcc, pPccControl, qPccControl);
            sink.accept(vsConverter);
        }
    }

    private static void exportDCTerminals(Network network, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (HvdcLine line : network.getHvdcLines()) {
            exportHvdcLineDCTerminals(line, sink, context);
        }
        for (DcConnectable<?> dcConnectable : network.getDcConnectables()) {
            exportDCTerminals(dcConnectable, sink, context);
        }
        for (DcSwitch dcSwitch : network.getDcSwitches()) {
            exportDCTerminals(dcSwitch, sink, context);
        }
    }

    private static void exportDCTerminals(DcConnectable<?> dcConnectable, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (DcTerminal dcTerminal : dcConnectable.getDcTerminals()) {
            String dcTerminalId = CgmesExportUtil.getDcTerminalId(dcTerminal, context);
            String className = dcConnectable instanceof AcDcConverter<?> ? CgmesNames.ACDC_CONVERTER_DC_TERMINAL : CgmesNames.DC_TERMINAL;
            boolean connected = dcTerminal.isConnected();
            exportDCTerminal(dcTerminalId, className, connected, sink, context);
        }
    }

    private static void exportDCTerminals(DcSwitch dcSwitch, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        boolean connected = !dcSwitch.isOpen();
        String dcTerminal1Id = context.getNamingStrategy().getCgmesIdFromAlias(dcSwitch, ALIAS_DC_TERMINAL1);
        exportDCTerminal(dcTerminal1Id, CgmesNames.DC_TERMINAL, connected, sink, context);
        String dcTerminal2Id = context.getNamingStrategy().getCgmesIdFromAlias(dcSwitch, ALIAS_DC_TERMINAL2);
        exportDCTerminal(dcTerminal2Id, CgmesNames.DC_TERMINAL, connected, sink, context);
    }

    private static void exportHvdcLineDCTerminals(HvdcLine line, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        String acdcConverterDcTerminal1 = context.getNamingStrategy().getCgmesIdFromAlias(line.getConverterStation1(), ALIAS_DC_TERMINAL1);
        exportDCTerminal(acdcConverterDcTerminal1, CgmesNames.ACDC_CONVERTER_DC_TERMINAL, true, sink, context);
        String acdcConverterDcTerminal1G = context.getNamingStrategy().getCgmesIdFromAlias(line.getConverterStation1(), ALIAS_DC_TERMINAL2);
        exportDCTerminal(acdcConverterDcTerminal1G, CgmesNames.ACDC_CONVERTER_DC_TERMINAL, true, sink, context);

        String acdcConverterDcTerminal2 = context.getNamingStrategy().getCgmesIdFromAlias(line.getConverterStation2(), ALIAS_DC_TERMINAL1);
        exportDCTerminal(acdcConverterDcTerminal2, CgmesNames.ACDC_CONVERTER_DC_TERMINAL, true, sink, context);
        String acdcConverterDcTerminal2G = context.getNamingStrategy().getCgmesIdFromAlias(line.getConverterStation1(), ALIAS_DC_TERMINAL2);
        exportDCTerminal(acdcConverterDcTerminal2G, CgmesNames.ACDC_CONVERTER_DC_TERMINAL, true, sink, context);

        String dcTerminal1 = context.getNamingStrategy().getCgmesIdFromAlias(line, ALIAS_DC_TERMINAL1);
        exportDCTerminal(dcTerminal1, CgmesNames.DC_TERMINAL, true, sink, context);
        String dcTerminal1G = context.getNamingStrategy().getCgmesId(refTyped(line), DC_TERMINAL, ref("1G"));
        exportDCTerminal(dcTerminal1G, CgmesNames.DC_TERMINAL, true, sink, context);

        String dcTerminal2 = context.getNamingStrategy().getCgmesIdFromAlias(line, ALIAS_DC_TERMINAL2);
        exportDCTerminal(dcTerminal2, CgmesNames.DC_TERMINAL, true, sink, context);
        String dcTerminal2G = context.getNamingStrategy().getCgmesId(refTyped(line), DC_TERMINAL, ref("2G"));
        exportDCTerminal(dcTerminal2G, CgmesNames.DC_TERMINAL, true, sink, context);
    }

    private static void exportDCTerminal(String terminalId, String className, boolean connected, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        DCTerminalSsh dcTerminal = new DCTerminalSsh(className, terminalId, connected);
        sink.accept(dcTerminal);
    }

    private static double getQfromPowerFactor(double p, double powerFactor) {
        if (powerFactor == 0.0) {
            return 0.0;
        }
        return p * Math.sqrt((1 - powerFactor * powerFactor) / (powerFactor * powerFactor));
    }

    private static void exportGeneratingUnitsParticitationFactors(Network network, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        // Multiple generators may share the same generation unit,
        // we will choose the participation factor from the last generator that references the generating unit
        // We only consider generators and batteries that have participation factors
        Map<String, GeneratingUnitSsh> generatingUnits = new HashMap<>();
        for (Generator g : network.getGenerators()) {
            GeneratingUnitSsh gu = generatingUnitForGeneratorAndBatteries(g, context);
            if (gu != null) {
                generatingUnits.put(gu.rdfId(), gu);
            }
        }
        for (Battery b : network.getBatteries()) {
            GeneratingUnitSsh gu = generatingUnitForGeneratorAndBatteries(b, context);
            if (gu != null) {
                generatingUnits.put(gu.rdfId(), gu);
            }
        }
        for (GeneratingUnitSsh gu : generatingUnits.values()) {
            sink.accept(gu);
        }
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> GeneratingUnitSsh generatingUnitForGeneratorAndBatteries(I i, CgmesExportContext context) {
        String kind = obtainSynchronousMachineKind(i);
        if (!OPERATING_MODE_CONDENSER.equals(kind) && (i.getExtension(ActivePowerControl.class) != null || i.hasProperty(PROPERTY_NORMAL_PF))) {
            double participationFactor;
            if (i.getExtension(ActivePowerControl.class) != null) {
                participationFactor = i.getExtension(ActivePowerControl.class).getParticipationFactor();
            } else {
                participationFactor = Double.parseDouble(i.getProperty(PROPERTY_NORMAL_PF));
            }
            return new GeneratingUnitSsh(
                generatingUnitClassname(i),
                context.getNamingStrategy().getCgmesIdFromProperty(i, PROPERTY_GENERATING_UNIT),
                participationFactor
            );
        }
        return null;
    }

    private static String generatingUnitClassname(Injection<?> i) {
        if (i instanceof Generator generator) {
            EnergySource energySource = generator.getEnergySource();
            if (energySource == EnergySource.HYDRO) {
                return "HydroGeneratingUnit";
            } else if (energySource == EnergySource.NUCLEAR) {
                return "NuclearGeneratingUnit";
            } else if (energySource == EnergySource.SOLAR) {
                return "SolarGeneratingUnit";
            } else if (energySource == EnergySource.THERMAL) {
                return "ThermalGeneratingUnit";
            } else if (energySource == EnergySource.WIND) {
                return "WindGeneratingUnit";
            } else {
                return "GeneratingUnit";
            }
        }
        if (i instanceof Battery) {
            return "HydroGeneratingUnit"; // TODO export battery differently in CGMES 3.0
        }
        throw new PowsyblException("Unexpected class for " + i.getId() + " using generating units: " + i.getClass());
    }

    private static void exportControlAreas(Network network, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        for (Area area : network.getAreas()) {
            exportControlArea(area, sink, context);
        }
    }

    private static void exportControlArea(Area controlArea, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        if (!CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE.equals(controlArea.getAreaType())) {
            return;
        }
        String areaId = context.getNamingStrategy().getCgmesId(controlArea.getId());
        double netInterchange = controlArea.getInterchangeTarget().orElse(Double.NaN);
        Double pTolerance = null;
        if (controlArea.hasProperty("pTolerance")) {
            pTolerance = Double.parseDouble(controlArea.getProperty("pTolerance"));
        }
        ControlAreaSsh controlAreaSsh = new ControlAreaSsh(areaId, netInterchange, pTolerance);
        sink.accept(controlAreaSsh);
    }

    private static void exportAcDcConverter(AcDcConverter<?> converter, SshSink sink, CgmesExportContext context) throws XMLStreamException {
        String converterId = context.getNamingStrategy().getCgmesId(converter);
        double targetPpcc = converter.getControlMode() == AcDcConverter.ControlMode.P_PCC ? converter.getTargetP() : 0.0;
        double targetUdc = converter.getControlMode() == AcDcConverter.ControlMode.V_DC ? converter.getTargetVdc() : 0.0;
        double p = converter.getPccTerminal().getP();
        double q = converter.getPccTerminal().getQ();
        if (converter instanceof LineCommutatedConverter) {
            String operatingMode = targetPpcc > 0.0 ? "rectifier" : "inverter";
            String pPccControl = converter.getControlMode() == AcDcConverter.ControlMode.P_PCC ? "activePower" : "dcVoltage";
            CsConverterSsh csConverter = new CsConverterSsh(converterId, targetPpcc, targetUdc, p, q, 0.0, 0.0, 0.0, operatingMode, pPccControl);
            sink.accept(csConverter);
        } else if (converter instanceof VoltageSourceConverter vsc) {
            double targetQpcc = vsc.isWithMode(RegulationMode.REACTIVE_POWER) ? vsc.getRegulatingTargetQ() : 0;
            double targetUpcc = vsc.isWithMode(RegulationMode.VOLTAGE) ? vsc.getRegulatingTargetV() : 0;
            String pPccControl = vsc.getControlMode() == AcDcConverter.ControlMode.P_PCC ? "pPcc" : "udc";
            String qPccControl = vsc.isWithMode(RegulationMode.VOLTAGE) ? "voltagePcc" : "reactivePcc";
            VsConverterSsh vsConverter = new VsConverterSsh(converterId, targetPpcc, targetUdc, p, vsc.getLocalTargetQ(), 0.0, 0.0, 0.0, targetQpcc, targetUpcc, pPccControl, qPccControl);
            sink.accept(vsConverter);
        }
    }

    private enum RegulatingControlType {
        REGULATING_CONTROL, TAP_CHANGER_CONTROL
    }

    static class RegulatingControlView {
        String id;
        RegulatingControlType type;
        boolean discrete;
        boolean controlEnabled;
        double targetDeadband;
        double targetValue;
        String targetValueUnitMultiplier;

        RegulatingControlView(String id, RegulatingControlType type, boolean discrete, boolean controlEnabled,
                              double targetDeadband, double targetValue, String targetValueUnitMultiplier) {
            this.id = id;
            this.type = type;
            this.discrete = discrete;
            this.controlEnabled = controlEnabled;
            this.targetDeadband = targetDeadband;
            this.targetValue = targetValue;
            this.targetValueUnitMultiplier = targetValueUnitMultiplier;
        }
    }
}
