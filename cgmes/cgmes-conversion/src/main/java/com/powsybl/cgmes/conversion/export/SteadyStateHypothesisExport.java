/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.statement.CgmesObject;
import com.powsybl.cgmes.model.statement.CgmesObjectSink;
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
        SteadyStateHypothesisWriter sshWriter = new SteadyStateHypothesisWriter(writer, context);
        final Map<String, List<RegulatingControlView>> regulatingControlViews = new HashMap<>();
        CgmesObjectSink sink = sshWriter::write;

        try {
            sshWriter.writeRdfRoot();
            sshWriter.writeModelDescription(network, CgmesSubset.STEADY_STATE_HYPOTHESIS, model);

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

            sshWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void exportSwitches(Network network, CgmesObjectSink sink, CgmesExportContext context) {
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                String switchType = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS); // may be null
                if (!isSwitchImportedFromAcLineSegmentEquivalentBranchOrSeriesCompensator(switchType)) {
                    exportSwitch(sw, sink, context);
                }
            }
        }
    }

    private static void exportTerminalForSwitches(Network network, CgmesObjectSink sink, CgmesExportContext context) {
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                String switchType = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS); // may be null
                boolean connected = isConnected(sw, switchType);

                exportTerminal(context.getNamingStrategy().getCgmesIdFromAlias(sw, ALIAS_TERMINAL1), connected, sink, context);
                exportTerminal(context.getNamingStrategy().getCgmesIdFromAlias(sw, ALIAS_TERMINAL2), connected, sink, context);
            }
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

    private static void exportTerminalForBoundaryLines(Network network, CgmesObjectSink sink, CgmesExportContext context) {
        for (BoundaryLine bl : network.getBoundaryLines(BoundaryLineFilter.ALL)) {
            // Terminal for equivalent injection at boundary is always connected
            exportTerminal(context.getNamingStrategy().getCgmesIdFromProperty(bl, PROPERTY_EQUIVALENT_INJECTION_TERMINAL), true, sink, context);
            // Terminal for boundary side of original line/switch is always connected
            exportTerminal(CgmesExportUtil.getBoundaryLineBoundaryTerminalId(bl, context), true, sink, context);
        }
    }

    private static void exportTerminalForBuses(Network network, CgmesObjectSink sink, CgmesExportContext context) {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            String bbsTerminals = b.getProperty(PROPERTY_BUSBAR_SECTION_TERMINALS, "");
            if (!bbsTerminals.isEmpty()) {
                for (String bbsTerminal : bbsTerminals.split(",")) {
                    exportTerminal(bbsTerminal, true, sink, context);
                }
            }
        }
    }

    private static void exportTerminals(Network network, CgmesObjectSink sink, CgmesExportContext context) {
        for (Connectable<?> c : network.getConnectables()) { // TODO write boundary terminals for tie lines from CGMES
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
        exportTerminalForSwitches(network, sink, context);
        exportTerminalForBoundaryLines(network, sink, context);
        // If we are performing an updated export, write recorded busbar section terminals as connected
        if (!context.isExportEquipment()) {
            exportTerminalForBuses(network, sink, context);
        }
    }

    private static void exportFictitiousInjections(Network network, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER && !context.isBusBranchExport()) {
                exportNodeBreakerFictitiousInjections(vl, sink, context);
            } else {
                exportBusBranchFictitiousInjections(vl, sink, context);
            }
        }
    }

    private static void exportNodeBreakerFictitiousInjections(VoltageLevel vl, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
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

    private static void exportBusBranchFictitiousInjections(VoltageLevel vl, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
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
                                                  CgmesObjectSink sink,
                                                  CgmesExportContext context) throws XMLStreamException {
        if (p <= 0) {
            CgmesRecords.EnergySourceSsh s = new CgmesRecords.EnergySourceSsh(loadId, p, q);
            CgmesObject c = s.normalize(cimNamespace(context));
            sink.accept(c);
        } else {
            CgmesRecords.EnergyConsumerSsh s = new CgmesRecords.EnergyConsumerSsh(CgmesNames.NONCONFORM_LOAD, loadId, p, q);
            CgmesObject c = s.normalize(cimNamespace(context));
            sink.accept(c);
        }
        // Terminal connected state (always connected in SSH for fictitious terminals)
        exportTerminal(terminalId, true, sink, context);
    }

    private static void exportEquivalentInjections(Network network, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        // One equivalent injection for every boundary line
        List<String> exported = new ArrayList<>();

        for (BoundaryLine bl : network.getBoundaryLines(BoundaryLineFilter.ALL)) {
            String equivalentInjectionId = context.getNamingStrategy().getCgmesIdFromProperty(bl, PROPERTY_EQUIVALENT_INJECTION);
            if (!exported.contains(equivalentInjectionId)) {
                CgmesRecords.EquivalentInjectionSsh s = collectEquivalentInjection(bl, equivalentInjectionId);
                CgmesObject c = s.normalize(cimNamespace(context));
                sink.accept(c);
                exported.add(equivalentInjectionId);
            }
        }
    }

    /**
     * Collect the SSH data of the EquivalentInjection of the given boundary line.
     */
    private static CgmesRecords.EquivalentInjectionSsh collectEquivalentInjection(BoundaryLine bl, String equivalentInjectionId) {
        // regulationStatus and regulationTarget are optional,
        // but test cases contain the attributes with disabled and 0
        boolean regulationStatus = false;
        double regulationTarget = 0;
        if (bl.getGeneration() != null) {
            regulationStatus = bl.getGeneration().isVoltageRegulationOn();
            regulationTarget = bl.getGeneration().getTargetV();
        }
        return new CgmesRecords.EquivalentInjectionSsh(
            equivalentInjectionId,
            bl.getP0(),
            bl.getQ0(),
            regulationStatus,
            regulationTarget
        );
    }

    private static void exportTapChangers(Network network, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                          CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            exportTapChangers(twt, regulatingControlViews, sink, context);
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            exportTapChangers(twt, regulatingControlViews, sink, context);
        }
    }

    private static void exportTapChangers(TwoWindingsTransformer twt, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                          CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        if (twt.hasPhaseTapChanger()) {
            String aliasType = twt.getAliasFromType(ALIAS_PHASE_TAP_CHANGER2).isPresent() && twt.getAliasFromType(ALIAS_PHASE_TAP_CHANGER1).isEmpty() ?
                ALIAS_PHASE_TAP_CHANGER2 : ALIAS_PHASE_TAP_CHANGER1;
            exportTapChanger(twt, aliasType, PHASE_TAP_CHANGER, 1, regulatingControlViews, sink, context);
        }
        if (twt.hasRatioTapChanger()) {
            String aliasType = twt.getAliasFromType(ALIAS_RATIO_TAP_CHANGER2).isPresent() && twt.getAliasFromType(ALIAS_RATIO_TAP_CHANGER1).isEmpty() ?
                ALIAS_RATIO_TAP_CHANGER2 : ALIAS_RATIO_TAP_CHANGER1;
            exportTapChanger(twt, aliasType, RATIO_TAP_CHANGER, 1, regulatingControlViews, sink, context);
        }
    }

    private static void exportTapChangers(ThreeWindingsTransformer twt, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                          CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
            int endNumber = leg.getSide().getNum();
            if (leg.hasPhaseTapChanger()) {
                String aliasType = getPhaseTapChangerAliasType(Integer.toString(endNumber));
                exportTapChanger(twt, aliasType, PHASE_TAP_CHANGER, endNumber, regulatingControlViews, sink, context);
            }
            if (leg.hasRatioTapChanger()) {
                String aliasType = getRatioTapChangerAliasType(Integer.toString(endNumber));
                exportTapChanger(twt, aliasType, RATIO_TAP_CHANGER, endNumber, regulatingControlViews, sink, context);
            }
        }
    }

    private static <C extends Connectable<C>> void exportTapChanger(C twt, String aliasType, Part part, int endNumber,
                                                                    Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                                    CgmesObjectSink sink,
                                                                    CgmesExportContext context) throws XMLStreamException {
        String defaultType = part == PHASE_TAP_CHANGER ? CgmesNames.PHASE_TAP_CHANGER_TABULAR : CgmesNames.RATIO_TAP_CHANGER;
        TapChanger<?, ?, ?, ?> tc = getTapChanger(twt, part, endNumber);
        String tapChangerId = context.getNamingStrategy().getCgmesIdFromAlias(twt, aliasType);
        String cgmesTapChangerId = twt.getAliasFromType(aliasType).orElse(null);
        String tapChangerControlId = getTapChangerControlId(twt, part, endNumber, cgmesTapChangerId, context);
        String type = defaultType;
        if (tc instanceof PhaseTapChanger && !context.isExportEquipment()) {
            type = getPhaseTapChangerType(twt, cgmesTapChangerId);
        }

        CgmesRecords.TapChangerSsh s = new CgmesRecords.TapChangerSsh(type, tapChangerId, tc.isRegulating(), tc.getTapPosition());
        CgmesObject c = s.normalize(cimNamespace(context));
        sink.accept(c);
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

    /**
     * The phase or ratio tap changer of a two windings transformer, or of the leg endNumber of a three windings transformer.
     */
    private static TapChanger<?, ?, ?, ?> getTapChanger(Connectable<?> twt, Part part, int endNumber) {
        if (twt instanceof ThreeWindingsTransformer threeWindingsTransformer) {
            ThreeWindingsTransformer.Leg leg = threeWindingsTransformer.getLeg(ThreeSides.valueOf(endNumber));
            return part == PHASE_TAP_CHANGER ? leg.getPhaseTapChanger() : leg.getRatioTapChanger();
        }
        TwoWindingsTransformer twoWindingsTransformer = (TwoWindingsTransformer) twt;
        return part == PHASE_TAP_CHANGER ? twoWindingsTransformer.getPhaseTapChanger() : twoWindingsTransformer.getRatioTapChanger();
    }

    private static void exportShuntCompensators(Network network, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        for (ShuntCompensator shuntCompensator : network.getShuntCompensators()) {
            if ("true".equals(shuntCompensator.getProperty(PROPERTY_IS_EQUIVALENT_SHUNT))) {
                continue;
            }

            CgmesRecords.ShuntCompensatorSsh s = collectShuntCompensator(shuntCompensator, context);
            CgmesObject c = s.normalize(cimNamespace(context));
            sink.accept(c);

            addRegulatingControlView(shuntCompensator, getRegulatingControlId(shuntCompensator, context), regulatingControlViews, context);
        }
    }

    /**
     * Collect the SSH data of the given shunt compensator.
     */
    private static CgmesRecords.ShuntCompensatorSsh collectShuntCompensator(ShuntCompensator s, CgmesExportContext context) {
        return new CgmesRecords.ShuntCompensatorSsh(
            shuntCompensatorClassName(s.getModelType()),
            context.getNamingStrategy().getCgmesId(s),
            s.getSectionCount(),
            s.isRegulating()
        );
    }

    private static void exportGenerators(Network network, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                         CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        for (Generator g : network.getGenerators()) {
            String cgmesOriginalClass = g.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SYNCHRONOUS_MACHINE);

            switch (cgmesOriginalClass) {
                case CgmesNames.EQUIVALENT_INJECTION -> {
                    CgmesRecords.EquivalentInjectionSsh s = collectEquivalentInjection(g, context);
                    CgmesObject c = s.normalize(cimNamespace(context));
                    sink.accept(c);
                }
                case CgmesNames.EXTERNAL_NETWORK_INJECTION -> {
                    CgmesRecords.ExternalNetworkInjectionSsh s = collectExternalNetworkInjection(g, context);
                    CgmesObject c = s.normalize(cimNamespace(context));
                    sink.accept(c);
                    addRegulatingControlView(g, getRegulatingControlId(g, context), regulatingControlViews, context);
                }
                case CgmesNames.SYNCHRONOUS_MACHINE -> {
                    CgmesRecords.SynchronousMachineSsh s = collectSynchronousMachine(g, context);
                    CgmesObject c = s.normalize(cimNamespace(context));
                    sink.accept(c);
                    addRegulatingControlView(g, getRegulatingControlId(g, context), regulatingControlViews, context);
                }
                default ->
                    throw new PowsyblException("Unexpected cgmes equipment " + cgmesOriginalClass);
            }
        }
    }

    private static void exportBatteries(Network network, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        for (Battery b : network.getBatteries()) {
            CgmesRecords.SynchronousMachineSsh s = collectSynchronousMachine(b, context);
            CgmesObject c = s.normalize(cimNamespace(context));
            sink.accept(c);
        }
    }

    /**
     * Collect the SSH data of the EquivalentInjection of the given generator.
     */
    private static CgmesRecords.EquivalentInjectionSsh collectEquivalentInjection(Generator g, CgmesExportContext context) {
        return new CgmesRecords.EquivalentInjectionSsh(
            context.getNamingStrategy().getCgmesId(g),
            -g.getTargetP(),
            -g.getLocalTargetQ(),
            g.isRegulating(),
            g.getLocalTargetV()
        );
    }

    /**
     * Collect the SSH data of the ExternalNetworkInjection of the given generator.
     */
    private static CgmesRecords.ExternalNetworkInjectionSsh collectExternalNetworkInjection(Generator g, CgmesExportContext context) {
        return new CgmesRecords.ExternalNetworkInjectionSsh(
            context.getNamingStrategy().getCgmesId(g),
            g.isRegulating(),
            -g.getTargetP(),
            -g.getLocalTargetQ(),
            ReferencePriority.get(g)
        );
    }

    /**
     * Collect the SSH data of the SynchronousMachine of the given generator.
     */
    private static CgmesRecords.SynchronousMachineSsh collectSynchronousMachine(Generator g, CgmesExportContext context) {
        return new CgmesRecords.SynchronousMachineSsh(
            context.getNamingStrategy().getCgmesId(g),
            g.isRegulating(),
            -g.getTargetP(),
            -g.getLocalTargetQ(),
            ReferencePriority.get(g),
            obtainOperatingMode(g, g.getMinP(), g.getMaxP(), g.getTargetP())
        );
    }

    /**
     * Collect the SSH data of the SynchronousMachine of the given battery.
     */
    private static CgmesRecords.SynchronousMachineSsh collectSynchronousMachine(Battery b, CgmesExportContext context) {
        boolean controlEnabled = b.getVoltageRegulation() != null && b.getVoltageRegulation().isRegulating();
        return new CgmesRecords.SynchronousMachineSsh(
            context.getNamingStrategy().getCgmesId(b),
            controlEnabled,
            -b.getTargetP(),
            -b.getRegulatingTargetQ(),
            ReferencePriority.get(b),
            obtainOperatingMode(b, b.getMinP(), b.getMaxP(), b.getTargetP())
        );
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> String obtainOperatingMode(I i, double minP, double maxP, double targetP) {
        String calculatedKind = obtainCalculatedSynchronousMachineKind(minP, maxP, obtainCurve(i), i instanceof Battery || i instanceof Generator gen && gen.hasCondenserCapability());
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
                                                    CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            CgmesRecords.StaticVarCompensatorSsh s = collectStaticVarCompensator(svc, context);
            CgmesObject c = s.normalize(cimNamespace(context));
            sink.accept(c);

            addRegulatingControlView(svc, getRegulatingControlId(svc, context), regulatingControlViews, context);
        }
    }

    /**
     * Collect the SSH data of the given static var compensator.
     */
    private static CgmesRecords.StaticVarCompensatorSsh collectStaticVarCompensator(StaticVarCompensator svc, CgmesExportContext context) {
        return new CgmesRecords.StaticVarCompensatorSsh(
            context.getNamingStrategy().getCgmesId(svc),
            svc.isRegulating(),
            svc.getLocalTargetQ()
        );
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

    private static void exportHiddenTapChanger(CgmesTapChanger cgmesTc, String defaultType, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        CgmesRecords.TapChangerSsh s = new CgmesRecords.TapChangerSsh(Optional.ofNullable(cgmesTc.getType()).orElse(defaultType), cgmesTc.getId(), false,
                cgmesTc.getStep().orElseThrow(() -> new PowsyblException("Non null step expected for tap changer " + cgmesTc.getId())));
        CgmesObject c = s.normalize(cimNamespace(context));
        sink.accept(c);
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

    private static void exportRegulatingControls(Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                 CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
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

    private static void exportRegulatingControl(RegulatingControlView rc, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        CgmesRecords.RegulatingControlSsh s = collectRegulatingControl(rc);
        CgmesObject c = s.normalize(cimNamespace(context));
        sink.accept(c);
    }

    /**
     * Collect the SSH data of the given regulating control view.
     */
    private static CgmesRecords.RegulatingControlSsh collectRegulatingControl(RegulatingControlView rc) {
        return new CgmesRecords.RegulatingControlSsh(
            regulatingControlClassname(rc.type),
            rc.id,
            rc.discrete,
            rc.controlEnabled,
            rc.targetDeadband,
            rc.targetValue,
            rc.targetValueUnitMultiplier
        );
    }

    private static String regulatingControlClassname(RegulatingControlType type) {
        if (type == RegulatingControlType.TAP_CHANGER_CONTROL) {
            return "TapChangerControl";
        } else {
            return "RegulatingControl";
        }
    }

    private static void exportSwitch(Switch sw, CgmesObjectSink sink, CgmesExportContext context) {
        try {
            CgmesRecords.SwitchSsh s = collectSwitch(sw, context);
            CgmesObject c = s.normalize(cimNamespace(context));
            sink.accept(c);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    /**
     * Collect the SSH data of the given switch.
     */
    private static CgmesRecords.SwitchSsh collectSwitch(Switch sw, CgmesExportContext context) {
        String switchType = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS);
        String className = switchType != null ? switchType : CgmesExportUtil.switchClassname(sw.getKind());
        return new CgmesRecords.SwitchSsh(
            className,
            context.getNamingStrategy().getCgmesId(sw),
            sw.isOpen()
        );
    }

    private static void exportTerminal(Terminal t, CgmesObjectSink sink, CgmesExportContext context) {
        exportTerminal(CgmesExportUtil.getTerminalId(t, context), t.isConnected(), sink, context);
    }

    private static void exportTerminal(String terminalId, boolean connected, CgmesObjectSink sink, CgmesExportContext context) {
        try {
            CgmesRecords.TerminalSsh s = new CgmesRecords.TerminalSsh(terminalId, connected);
            CgmesObject c = s.normalize(cimNamespace(context));
            sink.accept(c);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void exportLoads(Network network, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            if (context.isExportedEquipment(load)) {
                String className = obtainLoadClassName(load, context);
                switch (className) {
                    case CgmesNames.ASYNCHRONOUS_MACHINE -> {
                        CgmesRecords.AsynchronousMachineSsh s = collectAsynchronousMachine(load, context);
                        CgmesObject c = s.normalize(cimNamespace(context));
                        sink.accept(c);
                    }
                    case CgmesNames.ENERGY_SOURCE -> {
                        CgmesRecords.EnergySourceSsh s = collectEnergySource(load, context);
                        CgmesObject c = s.normalize(cimNamespace(context));
                        sink.accept(c);
                    }
                    case CgmesNames.ENERGY_CONSUMER, CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY -> {
                        CgmesRecords.EnergyConsumerSsh s = collectEnergyConsumer(load, className, context);
                        CgmesObject c = s.normalize(cimNamespace(context));
                        sink.accept(c);
                    }
                    default -> throw new PowsyblException("Unexpected class name: " + className);
                }
            }
        }
    }

    /**
     * Collect the SSH data of the AsynchronousMachine of the given load.
     */
    private static CgmesRecords.AsynchronousMachineSsh collectAsynchronousMachine(Load load, CgmesExportContext context) {
        return new CgmesRecords.AsynchronousMachineSsh(
            context.getNamingStrategy().getCgmesId(load),
            load.getP0(),
            load.getQ0(),
            false,
            obtainAsynchronousMachineKind(load.getP0())
        );
    }

    /**
     * Collect the SSH data of the EnergySource of the given load.
     */
    private static CgmesRecords.EnergySourceSsh collectEnergySource(Load load, CgmesExportContext context) {
        return new CgmesRecords.EnergySourceSsh(
            context.getNamingStrategy().getCgmesId(load),
            load.getP0(),
            load.getQ0()
        );
    }

    /**
     * Collect the SSH data of the EnergyConsumer of the given load.
     */
    private static CgmesRecords.EnergyConsumerSsh collectEnergyConsumer(Load load, String className, CgmesExportContext context) {
        return new CgmesRecords.EnergyConsumerSsh(
            className,
            context.getNamingStrategy().getCgmesId(load),
            load.getP0(),
            load.getQ0()
        );
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

    private static void exportConverters(Network network, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
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

    private static void exportConverterStation(HvdcConverterStation<?> converterStation, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        String converterId = context.getNamingStrategy().getCgmesId(converterStation);
        double targetPpcc;
        double targetUdc;
        double p;
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
            exportLccConverterStation(lccConverterStation, converterId, targetPpcc, targetUdc, p, sink, context);
        } else if (converterStation instanceof VscConverterStation vscConverterStation) {
            exportVscConverterStation(vscConverterStation, converterId, targetPpcc, targetUdc, sink, context);
        }
    }

    private static void exportLccConverterStation(LccConverterStation lccConverterStation, String converterId, double targetPpcc, double targetUdc, double p,
                                                  CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        double q = Math.abs(getQfromPowerFactor(p, lccConverterStation.getPowerFactor()));
        String operatingMode = CgmesExportUtil.isConverterStationRectifier(lccConverterStation) ? "rectifier" : "inverter";
        String pPccControl = CgmesExportUtil.isConverterStationRectifier(lccConverterStation) ? "activePower" : "dcVoltage";
        CgmesRecords.CsConverterSsh s = new CgmesRecords.CsConverterSsh(converterId, targetPpcc, targetUdc, p, q, 0.0, 0.0, 0.0, operatingMode, pPccControl);
        CgmesObject c = s.normalize(cimNamespace(context));
        sink.accept(c);
    }

    private static void exportVscConverterStation(VscConverterStation vscConverterStation, String converterId, double targetPpcc, double targetUdc,
                                                  CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        // The active power of a VSC converter is the one of its regulating terminal
        double p = vscConverterStation.getRegulatingTerminal().getP();
        double q = -vscConverterStation.getLocalTargetQ();
        double targetQpcc = vscConverterStation.isWithMode(RegulationMode.REACTIVE_POWER) ? -vscConverterStation.getRegulatingTargetQ() : 0; // To be consistent with the import
        double targetUpcc = vscConverterStation.isWithMode(RegulationMode.VOLTAGE) ? vscConverterStation.getRegulatingTargetV() : 0;
        String pPccControl = CgmesExportUtil.isConverterStationRectifier(vscConverterStation) ? "pPcc" : "udc";
        String qPccControl = vscConverterStation.isRegulatingWithMode(RegulationMode.VOLTAGE) ? "voltagePcc" : "reactivePcc";
        CgmesRecords.VsConverterSsh s = new CgmesRecords.VsConverterSsh(converterId, targetPpcc, targetUdc, p, q, 0.0, 0.0, 0.0, targetQpcc, targetUpcc, pPccControl, qPccControl);
        CgmesObject c = s.normalize(cimNamespace(context));
        sink.accept(c);
    }

    private static void exportDCTerminals(Network network, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        for (HvdcLine line : network.getHvdcLines()) {
            exportHvdcLineDCTerminals(line, sink, context);
        }
        for (DcConnectable<?> dcConnectable : network.getDcConnectables()) {
            for (DcTerminal dcTerminal : dcConnectable.getDcTerminals()) {
                String dcTerminalId = CgmesExportUtil.getDcTerminalId(dcTerminal, context);
                String className = dcConnectable instanceof AcDcConverter<?> ? CgmesNames.ACDC_CONVERTER_DC_TERMINAL : CgmesNames.DC_TERMINAL;
                boolean connected = dcTerminal.isConnected();
                exportDCTerminal(dcTerminalId, className, connected, sink, context);
            }
        }
        for (DcSwitch dcSwitch : network.getDcSwitches()) {
            boolean connected = !dcSwitch.isOpen();
            String dcTerminal1Id = context.getNamingStrategy().getCgmesIdFromAlias(dcSwitch, ALIAS_DC_TERMINAL1);
            exportDCTerminal(dcTerminal1Id, CgmesNames.DC_TERMINAL, connected, sink, context);
            String dcTerminal2Id = context.getNamingStrategy().getCgmesIdFromAlias(dcSwitch, ALIAS_DC_TERMINAL2);
            exportDCTerminal(dcTerminal2Id, CgmesNames.DC_TERMINAL, connected, sink, context);
        }
    }

    private static void exportHvdcLineDCTerminals(HvdcLine line, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
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

    private static void exportDCTerminal(String terminalId, String className, boolean connected, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        CgmesRecords.DCTerminalSsh s = new CgmesRecords.DCTerminalSsh(className, terminalId, connected);
        CgmesObject c = s.normalize(cimNamespace(context));
        sink.accept(c);
    }

    private static double getQfromPowerFactor(double p, double powerFactor) {
        if (powerFactor == 0.0) {
            return 0.0;
        }
        return p * Math.sqrt((1 - powerFactor * powerFactor) / (powerFactor * powerFactor));
    }

    private static void exportGeneratingUnitsParticitationFactors(Network network, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        // Multiple generators may share the same generation unit,
        // we will choose the participation factor from the last generator that references the generating unit
        // We only consider generators and batteries that have participation factors
        Map<String, CgmesRecords.GeneratingUnitSsh> generatingUnits = new HashMap<>();
        for (Generator g : network.getGenerators()) {
            CgmesRecords.GeneratingUnitSsh gu = collectGeneratingUnit(g, context);
            if (gu != null) {
                generatingUnits.put(gu.rdfId(), gu);
            }
        }
        for (Battery b : network.getBatteries()) {
            CgmesRecords.GeneratingUnitSsh gu = collectGeneratingUnit(b, context);
            if (gu != null) {
                generatingUnits.put(gu.rdfId(), gu);
            }
        }
        for (CgmesRecords.GeneratingUnitSsh s : generatingUnits.values()) {
            CgmesObject c = s.normalize(cimNamespace(context));
            sink.accept(c);
        }
    }

    /**
     * Collect the SSH data of the GeneratingUnit of the given generator or battery, or null if it has no participation factor.
     */
    private static <I extends ReactiveLimitsHolder & Injection<I>> CgmesRecords.GeneratingUnitSsh collectGeneratingUnit(I i, CgmesExportContext context) {
        String kind = obtainSynchronousMachineKind(i);
        if (!OPERATING_MODE_CONDENSER.equals(kind) && (i.getExtension(ActivePowerControl.class) != null || i.hasProperty(PROPERTY_NORMAL_PF))) {
            double participationFactor;
            if (i.getExtension(ActivePowerControl.class) != null) {
                participationFactor = i.getExtension(ActivePowerControl.class).getParticipationFactor();
            } else {
                participationFactor = Double.parseDouble(i.getProperty(PROPERTY_NORMAL_PF));
            }
            return new CgmesRecords.GeneratingUnitSsh(
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

    private static void exportControlAreas(Network network, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        for (Area area : network.getAreas()) {
            if (CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE.equals(area.getAreaType())) {
                exportControlArea(area, sink, context);
            }
        }
    }

    private static void exportControlArea(Area controlArea, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        CgmesRecords.ControlAreaSsh s = collectControlArea(controlArea, context);
        CgmesObject c = s.normalize(cimNamespace(context));
        sink.accept(c);
    }

    /**
     * Collect the SSH data of the ControlArea of the given area.
     */
    private static CgmesRecords.ControlAreaSsh collectControlArea(Area controlArea, CgmesExportContext context) {
        String areaId = context.getNamingStrategy().getCgmesId(controlArea.getId());
        double netInterchange = controlArea.getInterchangeTarget().orElse(Double.NaN);
        Double pTolerance = null;
        if (controlArea.hasProperty("pTolerance")) {
            pTolerance = Double.parseDouble(controlArea.getProperty("pTolerance"));
        }
        return new CgmesRecords.ControlAreaSsh(areaId, netInterchange, pTolerance);
    }

    private static void exportAcDcConverter(AcDcConverter<?> converter, CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        String converterId = context.getNamingStrategy().getCgmesId(converter);
        double targetPpcc = converter.getControlMode() == AcDcConverter.ControlMode.P_PCC ? converter.getTargetP() : 0.0;
        double targetUdc = converter.getControlMode() == AcDcConverter.ControlMode.V_DC ? converter.getTargetVdc() : 0.0;
        double p = converter.getPccTerminal().getP();
        if (converter instanceof LineCommutatedConverter lcc) {
            exportLineCommutatedConverter(lcc, converterId, targetPpcc, targetUdc, p, sink, context);
        } else if (converter instanceof VoltageSourceConverter vsc) {
            exportVoltageSourceConverter(vsc, converterId, targetPpcc, targetUdc, p, sink, context);
        }
    }

    private static void exportLineCommutatedConverter(LineCommutatedConverter lcc, String converterId, double targetPpcc, double targetUdc, double p,
                                                      CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        double q = lcc.getPccTerminal().getQ();
        String operatingMode = targetPpcc > 0.0 ? "rectifier" : "inverter";
        String pPccControl = lcc.getControlMode() == AcDcConverter.ControlMode.P_PCC ? "activePower" : "dcVoltage";
        CgmesRecords.CsConverterSsh s = new CgmesRecords.CsConverterSsh(converterId, targetPpcc, targetUdc, p, q, 0.0, 0.0, 0.0, operatingMode, pPccControl);
        CgmesObject c = s.normalize(cimNamespace(context));
        sink.accept(c);
    }

    private static void exportVoltageSourceConverter(VoltageSourceConverter vsc, String converterId, double targetPpcc, double targetUdc, double p,
                                                      CgmesObjectSink sink, CgmesExportContext context) throws XMLStreamException {
        double targetQpcc = vsc.isWithMode(RegulationMode.REACTIVE_POWER) ? vsc.getRegulatingTargetQ() : 0;
        double targetUpcc = vsc.isWithMode(RegulationMode.VOLTAGE) ? vsc.getRegulatingTargetV() : 0;
        String pPccControl = vsc.getControlMode() == AcDcConverter.ControlMode.P_PCC ? "pPcc" : "udc";
        String qPccControl = vsc.isWithMode(RegulationMode.VOLTAGE) ? "voltagePcc" : "reactivePcc";
        CgmesRecords.VsConverterSsh s = new CgmesRecords.VsConverterSsh(
            converterId,
            targetPpcc,
            targetUdc,
            p,
            vsc.getLocalTargetQ(),
            0.0,
            0.0,
            0.0,
            targetQpcc,
            targetUpcc,
            pPccControl,
            qPccControl
        );
        CgmesObject c = s.normalize(cimNamespace(context));
        sink.accept(c);
    }

    /**
     * Get the CIM namespace of the export, for the enumeration values of the SSH records.
     */
    private static String cimNamespace(CgmesExportContext context) {
        return context.getCim().getNamespace();
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
