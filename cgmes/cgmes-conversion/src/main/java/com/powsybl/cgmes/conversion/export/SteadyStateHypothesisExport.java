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
import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;
import static com.powsybl.iidm.network.util.VoltageRegulationUtils.getSignToUseLoadSignConvention;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class SteadyStateHypothesisExport {

    private static final Logger LOG = LoggerFactory.getLogger(SteadyStateHypothesisExport.class);
    private static final String ROTATING_MACHINE_P = "RotatingMachine.p";
    private static final String ROTATING_MACHINE_Q = "RotatingMachine.q";
    private static final String REGULATING_COND_EQ_CONTROL_ENABLED = "RegulatingCondEq.controlEnabled";
    private static final String ACDC_CONVERTER_DC_TERMINAL = "ACDCConverterDCTerminal";
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
        String cimNamespace = context.getCim().getNamespace();

        try {
            CgmesExportUtil.writeRdfRoot(context, writer);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(network, CgmesSubset.STEADY_STATE_HYPOTHESIS, writer, model, context);
            }

            exportLoads(network, cimNamespace, writer, context);
            exportFictitiousInjections(network, cimNamespace, writer, context);
            exportEquivalentInjections(network, cimNamespace, writer, context);
            exportTapChangers(network, cimNamespace, regulatingControlViews, writer, context);
            exportGenerators(network, cimNamespace, regulatingControlViews, writer, context);
            exportBatteries(network, cimNamespace, writer, context);
            exportShuntCompensators(network, cimNamespace, regulatingControlViews, writer, context);
            exportStaticVarCompensators(network, cimNamespace, regulatingControlViews, writer, context);
            exportRegulatingControls(regulatingControlViews, cimNamespace, writer, context);
            exportGeneratingUnitsParticitationFactors(network, cimNamespace, writer, context);
            exportConverters(network, cimNamespace, writer, context);
            exportDCTerminals(network, cimNamespace, writer, context);
            // FIXME open status of retained switches in bus-branch models
            exportSwitches(network, cimNamespace, writer, context);
            exportTerminals(network, cimNamespace, writer, context);
            exportControlAreas(network, cimNamespace, writer, context);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void exportSwitches(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                String switchType = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS); // may be null
                if (!isSwitchImportedFromAcLineSegmentEquivalentBranchOrSeriesCompensator(switchType)) {
                    exportSwitch(sw, cimNamespace, writer, context);
                }
            }
        }
    }

    private static void exportTerminalForSwitches(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                String switchType = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS); // may be null
                boolean connected = isConnected(sw, switchType);

                exportTerminal(context.getNamingStrategy().getCgmesIdFromAlias(sw, ALIAS_TERMINAL1), connected, cimNamespace, writer, context);
                exportTerminal(context.getNamingStrategy().getCgmesIdFromAlias(sw, ALIAS_TERMINAL2), connected, cimNamespace, writer, context);
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

    private static void exportTerminalForBoundaryLines(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        for (BoundaryLine bl : network.getBoundaryLines(BoundaryLineFilter.ALL)) {
            // Terminal for equivalent injection at boundary is always connected
            exportTerminal(context.getNamingStrategy().getCgmesIdFromProperty(bl, PROPERTY_EQUIVALENT_INJECTION_TERMINAL), true, cimNamespace, writer, context);
            // Terminal for boundary side of original line/switch is always connected
            exportTerminal(CgmesExportUtil.getBoundaryLineBoundaryTerminalId(bl, context), true, cimNamespace, writer, context);
        }
    }

    private static void exportTerminalForBuses(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            String bbsTerminals = b.getProperty(PROPERTY_BUSBAR_SECTION_TERMINALS, "");
            if (!bbsTerminals.isEmpty()) {
                for (String bbsTerminal : bbsTerminals.split(",")) {
                    exportTerminal(bbsTerminal, true, cimNamespace, writer, context);
                }
            }
        }
    }

    private static void exportTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        for (Connectable<?> c : network.getConnectables()) { // TODO write boundary terminals for tie lines from CGMES
            if (context.isExportedEquipment(c)) {
                if (CgmesExportUtil.isEquivalentShuntWithZeroSectionCount(c)) {
                    // Equivalent shunts do not have a section count in SSH, SV profiles,
                    // the only way to make output consistent with IIDM section count == 0 is to disconnect its terminal
                    exportTerminal(CgmesExportUtil.getTerminalId(c.getTerminals().get(0), context), false, cimNamespace, writer, context);
                } else {
                    for (Terminal t : c.getTerminals()) {
                        exportTerminal(t, cimNamespace, writer, context);
                    }
                }
            }
        }
        exportTerminalForSwitches(network, cimNamespace, writer, context);
        exportTerminalForBoundaryLines(network, cimNamespace, writer, context);
        // If we are performing an updated export, write recorded busbar section terminals as connected
        if (!context.isExportEquipment()) {
            exportTerminalForBuses(network, cimNamespace, writer, context);
        }
    }

    private static void exportFictitiousInjections(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER && !context.isBusBranchExport()) {
                exportNodeBreakerFictitiousInjections(vl, cimNamespace, writer, context);
            } else {
                exportBusBranchFictitiousInjections(vl, cimNamespace, writer, context);
            }
        }
    }

    private static void exportNodeBreakerFictitiousInjections(VoltageLevel vl, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        VoltageLevel.NodeBreakerView nb = vl.getNodeBreakerView();
        for (int node : nb.getNodes()) {
            double p = nb.getFictitiousP0(node);
            double q = nb.getFictitiousQ0(node);
            if (p != 0.0 || q != 0.0) {
                String loadId = context.getNamingStrategy().getCgmesId(refTyped(vl), FICTITIOUS, ref("NCL"), ref(node));
                String terminalId = context.getNamingStrategy().getCgmesId(refTyped(vl), FICTITIOUS, TERMINAL, ref(node));
                exportFictitiousInjection(loadId, terminalId, p, q, cimNamespace, writer, context);
            }
        }
    }

    private static void exportBusBranchFictitiousInjections(VoltageLevel vl, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : vl.getBusBreakerView().getBuses()) {
            double p = b.getFictitiousP0();
            double q = b.getFictitiousQ0();
            if (p != 0.0 || q != 0.0) {
                String loadId = context.getNamingStrategy().getCgmesId(refTyped(b), FICTITIOUS, ref("NCL"));
                String terminalId = context.getNamingStrategy().getCgmesId(refTyped(b), FICTITIOUS, TERMINAL);
                exportFictitiousInjection(loadId, terminalId, p, q, cimNamespace, writer, context);
            }
        }
    }

    private static void exportFictitiousInjection(String loadId, String terminalId, double p, double q,
                                                  String cimNamespace, XMLStreamWriter writer,
                                                  CgmesExportContext context) throws XMLStreamException {
        if (p <= 0) {
            exportEnergySource(loadId, p, q, cimNamespace, writer, context);
        } else {
            exportSshEnergyConsumer(loadId, CgmesNames.NONCONFORM_LOAD, p, q, cimNamespace, writer, context);
        }
        // Terminal connected state (always connected in SSH for fictitious terminals)
        exportTerminal(terminalId, true, cimNamespace, writer, context);
    }

    private static void exportEquivalentInjections(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // One equivalent injection for every boundary line
        List<String> exported = new ArrayList<>();

        for (BoundaryLine bl : network.getBoundaryLines(BoundaryLineFilter.ALL)) {
            String equivalentInjectionId = context.getNamingStrategy().getCgmesIdFromProperty(bl, PROPERTY_EQUIVALENT_INJECTION);
            if (!exported.contains(equivalentInjectionId)) {
                // regulationStatus and regulationTarget are optional,
                // but test cases contain the attributes with disabled and 0
                boolean regulationStatus = false;
                double regulationTarget = 0;
                if (bl.getGeneration() != null) {
                    regulationStatus = bl.getGeneration().isVoltageRegulationOn();
                    regulationTarget = bl.getGeneration().getTargetV();
                }
                exportEquivalentInjection(equivalentInjectionId, bl.getP0(), bl.getQ0(), regulationStatus, regulationTarget, cimNamespace, writer, context);
                exported.add(equivalentInjectionId);
            }
        }
    }

    private static void exportTapChangers(Network network, String cimNamespace,
                                          Map<String, List<RegulatingControlView>> regulatingControlViews,
                                          XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String aliasType = twt.getAliasFromType(ALIAS_PHASE_TAP_CHANGER2).isPresent() && twt.getAliasFromType(ALIAS_PHASE_TAP_CHANGER1).isEmpty() ?
                    ALIAS_PHASE_TAP_CHANGER2 : ALIAS_PHASE_TAP_CHANGER1;
                exportTapChanger(twt, aliasType, PHASE_TAP_CHANGER, 1, CgmesNames.PHASE_TAP_CHANGER_TABULAR, twt.getPhaseTapChanger(), regulatingControlViews, cimNamespace, writer, context);
            }
            if (twt.hasRatioTapChanger()) {
                String aliasType = twt.getAliasFromType(ALIAS_RATIO_TAP_CHANGER2).isPresent() && twt.getAliasFromType(ALIAS_RATIO_TAP_CHANGER1).isEmpty() ?
                    ALIAS_RATIO_TAP_CHANGER2 : ALIAS_RATIO_TAP_CHANGER1;
                exportTapChanger(twt, aliasType, RATIO_TAP_CHANGER, 1, CgmesNames.RATIO_TAP_CHANGER, twt.getRatioTapChanger(), regulatingControlViews, cimNamespace, writer, context);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                int endNumber = leg.getSide().getNum();
                if (leg.hasPhaseTapChanger()) {
                    String aliasType = getPhaseTapChangerAliasType(Integer.toString(endNumber));
                    exportTapChanger(twt, aliasType, PHASE_TAP_CHANGER, endNumber, CgmesNames.PHASE_TAP_CHANGER_TABULAR,
                        leg.getPhaseTapChanger(), regulatingControlViews, cimNamespace, writer, context);
                }
                if (leg.hasRatioTapChanger()) {
                    String aliasType = getRatioTapChangerAliasType(Integer.toString(endNumber));
                    exportTapChanger(twt, aliasType, RATIO_TAP_CHANGER, endNumber, CgmesNames.RATIO_TAP_CHANGER, leg.getRatioTapChanger(), regulatingControlViews, cimNamespace, writer, context);
                }
            }
        }
    }

    private static <C extends Connectable<C>> void exportTapChanger(C twt, String aliasType, Part part, int endNumber,
                                                                    String defaultType, TapChanger<?, ?, ?, ?> tc,
                                                                    Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                                    String cimNamespace, XMLStreamWriter writer,
                                                                    CgmesExportContext context) throws XMLStreamException {
        String tapChangerId = context.getNamingStrategy().getCgmesIdFromAlias(twt, aliasType);
        String cgmesTapChangerId = twt.getAliasFromType(aliasType).orElse(null);
        String tapChangerControlId = getTapChangerControlId(twt, part, endNumber, cgmesTapChangerId, context);
        String type = defaultType;
        if (tc instanceof PhaseTapChanger && !context.isExportEquipment()) {
            type = getPhaseTapChangerType(twt, cgmesTapChangerId);
        }

        exportTapChanger(type, tapChangerId, tc, cimNamespace, writer, context);
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
                exportHiddenTapChanger(hiddenCombinedTapChanger.get(), defaultType, cimNamespace, writer, context);
            }
        }
    }

    private static void exportShuntCompensators(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            if ("true".equals(s.getProperty(PROPERTY_IS_EQUIVALENT_SHUNT))) {
                continue;
            }

            String shuntType = switch (s.getModelType()) {
                case LINEAR -> "Linear";
                case NON_LINEAR -> "Nonlinear";
            };
            boolean controlEnabled = s.isRegulating();

            CgmesExportUtil.writeStartAbout(shuntType + "ShuntCompensator", context.getNamingStrategy().getCgmesId(s), writer, context);
            writer.writeStartElement(cimNamespace, "ShuntCompensator.sections");
            writer.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED);
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeEndElement();

            addRegulatingControlView(s, getRegulatingControlId(s, context), regulatingControlViews, context);
        }
    }

    private static void exportGenerators(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                 XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Generator g : network.getGenerators()) {
            String cgmesOriginalClass = g.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SYNCHRONOUS_MACHINE);

            boolean controlEnabled = g.isRegulating();
            switch (cgmesOriginalClass) {
                case CgmesNames.EQUIVALENT_INJECTION:
                    exportEquivalentInjection(context.getNamingStrategy().getCgmesId(g), -g.getTargetP(), -g.getLocalTargetQ(),
                            controlEnabled, g.getLocalTargetV(), cimNamespace, writer, context);
                    break;
                case CgmesNames.EXTERNAL_NETWORK_INJECTION:
                    exportExternalNetworkInjection(context.getNamingStrategy().getCgmesId(g), controlEnabled,
                            -g.getTargetP(), -g.getLocalTargetQ(), ReferencePriority.get(g),
                            cimNamespace, writer, context);
                    addRegulatingControlView(g, getRegulatingControlId(g, context), regulatingControlViews, context);
                    break;
                case CgmesNames.SYNCHRONOUS_MACHINE:
                    exportSynchronousMachine(context.getNamingStrategy().getCgmesId(g), controlEnabled,
                            -g.getTargetP(), -g.getLocalTargetQ(), ReferencePriority.get(g), obtainOperatingMode(g, g.getMinP(), g.getMaxP(), g.getTargetP()),
                            cimNamespace, writer, context);
                    addRegulatingControlView(g, getRegulatingControlId(g, context), regulatingControlViews, context);
                    break;
                default:
                    throw new PowsyblException("Unexpected cgmes equipment " + cgmesOriginalClass);
            }
        }
    }

    private static void exportExternalNetworkInjection(String id, boolean controlEnabled, double p, double q, int referencePriority,
                                                       String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.EXTERNAL_NETWORK_INJECTION, id, writer, context);
        writer.writeStartElement(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED);
        writer.writeCharacters(Boolean.toString(controlEnabled));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ExternalNetworkInjection.p");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ExternalNetworkInjection.q");
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ExternalNetworkInjection.referencePriority");
        writer.writeCharacters(Integer.toString(referencePriority)); // reference priority is used for angle reference selection (slack)
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportSynchronousMachine(String id, boolean controlEnabled, double p, double q, int referencePriority,
                                                 String mode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.SYNCHRONOUS_MACHINE, id, writer, context);
        writer.writeStartElement(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED);
        writer.writeCharacters(Boolean.toString(controlEnabled));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, ROTATING_MACHINE_P);
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, ROTATING_MACHINE_Q);
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "SynchronousMachine.referencePriority");
        // reference priority is used for angle reference selection (slack)
        writer.writeCharacters(Integer.toString(referencePriority));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "SynchronousMachine.operatingMode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "SynchronousMachineOperatingMode." + mode);
        writer.writeEndElement();
    }

    private static void exportBatteries(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Battery b : network.getBatteries()) {
            boolean controlEnabled = b.getVoltageRegulation() != null && b.getVoltageRegulation().isRegulating();
            exportSynchronousMachine(context.getNamingStrategy().getCgmesId(b), controlEnabled,
                    -b.getTargetP(), -b.getRegulatingTargetQ(), ReferencePriority.get(b), obtainOperatingMode(b, b.getMinP(), b.getMaxP(), b.getTargetP()),
                    cimNamespace, writer, context);
        }
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

    private static void exportStaticVarCompensators(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                    XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            boolean controlEnabled = svc.isRegulating();

            CgmesExportUtil.writeStartAbout("StaticVarCompensator", context.getNamingStrategy().getCgmesId(svc), writer, context);
            writer.writeStartElement(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED);
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "StaticVarCompensator.q");
            writer.writeCharacters(CgmesExportUtil.format(svc.getLocalTargetQ()));
            writer.writeEndElement();
            writer.writeEndElement();

            addRegulatingControlView(svc, getRegulatingControlId(svc, context), regulatingControlViews, context);
        }
    }

    private static void exportTapChanger(String type, String id, TapChanger<?, ?, ?, ?> tc, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        exportTapChanger(type, id, tc.isRegulating(), tc.getTapPosition(), cimNamespace, writer, context);
    }

    private static void exportTapChanger(String type, String id, boolean controlEnabled, int step, String cimNamespace,
                                         XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(type, id, writer, context);
        writer.writeStartElement(cimNamespace, "TapChanger.controlEnabled");
        writer.writeCharacters(Boolean.toString(controlEnabled));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "TapChanger.step");
        writer.writeCharacters(CgmesExportUtil.format(step));
        writer.writeEndElement();
        writer.writeEndElement();
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

    private static void exportHiddenTapChanger(CgmesTapChanger cgmesTc, String defaultType, String cimNamespace,
                                               XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        exportTapChanger(Optional.ofNullable(cgmesTc.getType()).orElse(defaultType), cgmesTc.getId(), false,
                cgmesTc.getStep().orElseThrow(() -> new PowsyblException("Non null step expected for tap changer " + cgmesTc.getId())),
                cimNamespace, writer, context);
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

    private static void exportRegulatingControls(Map<String, List<RegulatingControlView>> regulatingControlViews, String cimNamespace,
                                                 XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (List<RegulatingControlView> views : regulatingControlViews.values()) {
            exportRegulatingControl(combineRegulatingControlViews(views), cimNamespace, writer, context);
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

    private static void exportRegulatingControl(RegulatingControlView rc, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(regulatingControlClassname(rc.type), rc.id, writer, context);
        writer.writeStartElement(cimNamespace, "RegulatingControl.discrete");
        writer.writeCharacters(Boolean.toString(rc.discrete));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "RegulatingControl.enabled");
        writer.writeCharacters(Boolean.toString(rc.controlEnabled));
        writer.writeEndElement();
        if (CgmesExportUtil.targetDeadbandIsDefined(rc.targetDeadband)) {
            writer.writeStartElement(cimNamespace, "RegulatingControl.targetDeadband");
            writer.writeCharacters(CgmesExportUtil.format(rc.targetDeadband));
            writer.writeEndElement();
        }
        writer.writeStartElement(cimNamespace, "RegulatingControl.targetValue");
        writer.writeCharacters(CgmesExportUtil.format(rc.targetValue));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "RegulatingControl.targetValueUnitMultiplier");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "UnitMultiplier." + rc.targetValueUnitMultiplier);
        writer.writeEndElement();
    }

    private static String regulatingControlClassname(RegulatingControlType type) {
        if (type == RegulatingControlType.TAP_CHANGER_CONTROL) {
            return "TapChangerControl";
        } else {
            return "RegulatingControl";
        }
    }

    private static void exportSwitch(Switch sw, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            String switchType = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS);
            String className = switchType != null ? switchType : CgmesExportUtil.switchClassname(sw.getKind());
            CgmesExportUtil.writeStartAbout(className, context.getNamingStrategy().getCgmesId(sw), writer, context);
            writer.writeStartElement(cimNamespace, "Switch.open");
            writer.writeCharacters(Boolean.toString(sw.isOpen()));
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void exportTerminal(Terminal t, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        exportTerminal(CgmesExportUtil.getTerminalId(t, context), t.isConnected(), cimNamespace, writer, context);
    }

    private static void exportTerminal(String terminalId, boolean connected, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            CgmesExportUtil.writeStartAbout(CgmesNames.TERMINAL, terminalId, writer, context);
            writer.writeStartElement(cimNamespace, "ACDCTerminal.connected");
            writer.writeCharacters(Boolean.toString(connected));
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void exportEquivalentInjection(String cgmesId, double p, double q, boolean regulationStatus, double regulationTarget, String cimNamespace,
                                                  XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.EQUIVALENT_INJECTION, cgmesId, writer, context);
        writer.writeStartElement(cimNamespace, "EquivalentInjection.p");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "EquivalentInjection.q");
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "EquivalentInjection.regulationStatus");
        writer.writeCharacters(Boolean.toString(regulationStatus));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "EquivalentInjection.regulationTarget");
        writer.writeCharacters(CgmesExportUtil.format(regulationTarget));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportLoads(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            if (context.isExportedEquipment(load)) {
                String className = obtainLoadClassName(load, context);
                switch (className) {
                    case CgmesNames.ASYNCHRONOUS_MACHINE ->
                        exportAsynchronousMachine(context.getNamingStrategy().getCgmesId(load), load.getP0(), load.getQ0(), cimNamespace, writer, context);
                    case CgmesNames.ENERGY_SOURCE ->
                        exportEnergySource(context.getNamingStrategy().getCgmesId(load), load.getP0(), load.getQ0(), cimNamespace, writer, context);
                    case CgmesNames.ENERGY_CONSUMER, CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY ->
                        exportSshEnergyConsumer(context.getNamingStrategy().getCgmesId(load), className, load.getP0(), load.getQ0(), cimNamespace, writer, context);
                    default -> throw new PowsyblException("Unexpected class name: " + className);
                }
            }
        }
    }

    // if EQ is not exported, the original class name is preserved
    private static String obtainLoadClassName(Load load, CgmesExportContext context) {
        String originalClassName = load.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS);
        return (originalClassName != null && !context.isExportEquipment()) ? originalClassName : CgmesExportUtil.loadClassName(load);
    }

    private static void exportAsynchronousMachine(String id, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.ASYNCHRONOUS_MACHINE, id, writer, context);
        writer.writeStartElement(cimNamespace, ROTATING_MACHINE_P);
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, ROTATING_MACHINE_Q);
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED);
        writer.writeCharacters(Boolean.toString(false));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "AsynchronousMachine.asynchronousMachineType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "AsynchronousMachineKind." + obtainAsynchronousMachineKind(p));
        writer.writeEndElement();
    }

    private static String obtainAsynchronousMachineKind(double p) {
        if (p < 0) {
            return OPERATING_MODE_GENERATOR;
        } else {
            return OPERATING_MODE_MOTOR;
        }
    }

    private static void exportEnergySource(String id, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.ENERGY_SOURCE, id, writer, context);
        writer.writeStartElement(cimNamespace, "EnergySource.activePower");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "EnergySource.reactivePower");
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportSshEnergyConsumer(String id, String className, double p, double q, String cimNamespace,
                                                XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(className, id, writer, context);
        writer.writeStartElement(cimNamespace, "EnergyConsumer.p");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "EnergyConsumer.q");
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportConverters(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (HvdcConverterStation<?> converterStation : network.getHvdcConverterStations()) {
            exportConverterStation(converterStation, cimNamespace, writer, context);
        }
        for (LineCommutatedConverter lccConverter : network.getLineCommutatedConverters()) {
            exportAcDcConverter(lccConverter, cimNamespace, writer, context);
        }
        for (VoltageSourceConverter vscConverter : network.getVoltageSourceConverters()) {
            exportAcDcConverter(vscConverter, cimNamespace, writer, context);
        }
    }

    private static void exportConverterStation(HvdcConverterStation<?> converterStation, String cimNamespace,
                                               XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
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
            exportCsConverter(converterId, targetPpcc, targetUdc, p, q, operatingMode, pPccControl, cimNamespace, writer, context);
        } else if (converterStation instanceof VscConverterStation vscConverterStation) {
            p = vscConverterStation.getRegulatingTerminal().getP();
            q = -vscConverterStation.getLocalTargetQ();
            double targetQpcc = vscConverterStation.isWithMode(RegulationMode.REACTIVE_POWER) ? -vscConverterStation.getRegulatingTargetQ() : 0; // To be consistent with the import
            double targetUpcc = vscConverterStation.isWithMode(RegulationMode.VOLTAGE) ? vscConverterStation.getRegulatingTargetV() : 0;
            String pPccControl = CgmesExportUtil.isConverterStationRectifier(converterStation) ? "pPcc" : "udc";
            String qPccControl = vscConverterStation.isRegulatingWithMode(RegulationMode.VOLTAGE) ? "voltagePcc" : "reactivePcc";
            exportVsConverter(converterId, targetPpcc, targetUdc, targetQpcc, targetUpcc, p, q, pPccControl, qPccControl, cimNamespace, writer, context);
        }
    }

    private static void exportDCTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (HvdcLine line : network.getHvdcLines()) {
            exportHvdcLineDCTerminals(line, cimNamespace, writer, context);
        }
        for (DcConnectable<?> dcConnectable : network.getDcConnectables()) {
            for (DcTerminal dcTerminal : dcConnectable.getDcTerminals()) {
                String dcTerminalId = CgmesExportUtil.getDcTerminalId(dcTerminal, context);
                String className = dcConnectable instanceof AcDcConverter<?> ? ACDC_CONVERTER_DC_TERMINAL : CgmesNames.DC_TERMINAL;
                boolean connected = dcTerminal.isConnected();
                exportDCTerminal(dcTerminalId, className, connected, cimNamespace, writer, context);
            }
        }
        for (DcSwitch dcSwitch : network.getDcSwitches()) {
            boolean connected = !dcSwitch.isOpen();
            String dcTerminal1Id = context.getNamingStrategy().getCgmesIdFromAlias(dcSwitch, ALIAS_DC_TERMINAL1);
            exportDCTerminal(dcTerminal1Id, CgmesNames.DC_TERMINAL, connected, cimNamespace, writer, context);
            String dcTerminal2Id = context.getNamingStrategy().getCgmesIdFromAlias(dcSwitch, ALIAS_DC_TERMINAL2);
            exportDCTerminal(dcTerminal2Id, CgmesNames.DC_TERMINAL, connected, cimNamespace, writer, context);
        }
    }

    private static void exportHvdcLineDCTerminals(HvdcLine line, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String acdcConverterDcTerminal1 = context.getNamingStrategy().getCgmesIdFromAlias(line.getConverterStation1(), ALIAS_DC_TERMINAL1);
        exportDCTerminal(acdcConverterDcTerminal1, ACDC_CONVERTER_DC_TERMINAL, true, cimNamespace, writer, context);
        String acdcConverterDcTerminal1G = context.getNamingStrategy().getCgmesIdFromAlias(line.getConverterStation1(), ALIAS_DC_TERMINAL2);
        exportDCTerminal(acdcConverterDcTerminal1G, ACDC_CONVERTER_DC_TERMINAL, true, cimNamespace, writer, context);

        String acdcConverterDcTerminal2 = context.getNamingStrategy().getCgmesIdFromAlias(line.getConverterStation2(), ALIAS_DC_TERMINAL1);
        exportDCTerminal(acdcConverterDcTerminal2, ACDC_CONVERTER_DC_TERMINAL, true, cimNamespace, writer, context);
        String acdcConverterDcTerminal2G = context.getNamingStrategy().getCgmesIdFromAlias(line.getConverterStation1(), ALIAS_DC_TERMINAL2);
        exportDCTerminal(acdcConverterDcTerminal2G, ACDC_CONVERTER_DC_TERMINAL, true, cimNamespace, writer, context);

        String dcTerminal1 = context.getNamingStrategy().getCgmesIdFromAlias(line, ALIAS_DC_TERMINAL1);
        exportDCTerminal(dcTerminal1, CgmesNames.DC_TERMINAL, true, cimNamespace, writer, context);
        String dcTerminal1G = context.getNamingStrategy().getCgmesId(refTyped(line), DC_TERMINAL, ref("1G"));
        exportDCTerminal(dcTerminal1G, CgmesNames.DC_TERMINAL, true, cimNamespace, writer, context);

        String dcTerminal2 = context.getNamingStrategy().getCgmesIdFromAlias(line, ALIAS_DC_TERMINAL2);
        exportDCTerminal(dcTerminal2, CgmesNames.DC_TERMINAL, true, cimNamespace, writer, context);
        String dcTerminal2G = context.getNamingStrategy().getCgmesId(refTyped(line), DC_TERMINAL, ref("2G"));
        exportDCTerminal(dcTerminal2G, CgmesNames.DC_TERMINAL, true, cimNamespace, writer, context);
    }

    private static void exportDCTerminal(String terminalId, String className, boolean connected, String cimNamespace,
                                         XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(className, terminalId, writer, context);
        writer.writeStartElement(cimNamespace, "ACDCTerminal.connected");
        writer.writeCharacters(Boolean.toString(connected));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static double getQfromPowerFactor(double p, double powerFactor) {
        if (powerFactor == 0.0) {
            return 0.0;
        }
        return p * Math.sqrt((1 - powerFactor * powerFactor) / (powerFactor * powerFactor));
    }

    private static void exportGeneratingUnitsParticitationFactors(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // Multiple generators may share the same generation unit,
        // we will choose the participation factor from the last generator that references the generating unit
        // We only consider generators and batteries that have participation factors
        Map<String, GeneratingUnit> generatingUnits = new HashMap<>();
        for (Generator g : network.getGenerators()) {
            GeneratingUnit gu = generatingUnitForGeneratorAndBatteries(g, context);
            if (gu != null) {
                generatingUnits.put(gu.id, gu);
            }
        }
        for (Battery b : network.getBatteries()) {
            GeneratingUnit gu = generatingUnitForGeneratorAndBatteries(b, context);
            if (gu != null) {
                generatingUnits.put(gu.id, gu);
            }
        }
        for (GeneratingUnit gu : generatingUnits.values()) {
            exportGeneratingUnitParticipationFactor(gu, cimNamespace, writer, context);
        }
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> GeneratingUnit generatingUnitForGeneratorAndBatteries(I i, CgmesExportContext context) {
        String kind = obtainSynchronousMachineKind(i);
        if (!OPERATING_MODE_CONDENSER.equals(kind) && (i.getExtension(ActivePowerControl.class) != null || i.hasProperty(PROPERTY_NORMAL_PF))) {
            GeneratingUnit gu = new GeneratingUnit();
            gu.id = context.getNamingStrategy().getCgmesIdFromProperty(i, PROPERTY_GENERATING_UNIT);
            if (i.getExtension(ActivePowerControl.class) != null) {
                gu.participationFactor = i.getExtension(ActivePowerControl.class).getParticipationFactor();
            } else {
                gu.participationFactor = Double.parseDouble(i.getProperty(PROPERTY_NORMAL_PF));
            }
            gu.className = generatingUnitClassname(i);
            return gu;
        }
        return null;
    }

    private static void exportGeneratingUnitParticipationFactor(GeneratingUnit gu, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(gu.className, gu.id, writer, context);
        writer.writeStartElement(cimNamespace, "GeneratingUnit.normalPF");
        writer.writeCharacters(CgmesExportUtil.format(gu.participationFactor));
        writer.writeEndElement();
        writer.writeEndElement();
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

    private static void exportControlAreas(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Area area : network.getAreas()) {
            if (CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE.equals(area.getAreaType())) {
                exportControlArea(area, cimNamespace, writer, context);
            }
        }
    }

    private static void exportControlArea(Area controlArea, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String areaId = context.getNamingStrategy().getCgmesId(controlArea.getId());
        CgmesExportUtil.writeStartAbout("ControlArea", areaId, writer, context);
        writer.writeStartElement(cimNamespace, "ControlArea.netInterchange");
        double netInterchange = controlArea.getInterchangeTarget().orElse(Double.NaN);
        writer.writeCharacters(CgmesExportUtil.format(netInterchange));
        writer.writeEndElement();
        if (controlArea.hasProperty("pTolerance")) {
            double pTolerance = Double.parseDouble(controlArea.getProperty("pTolerance"));
            writer.writeStartElement(cimNamespace, "ControlArea.pTolerance");
            writer.writeCharacters(CgmesExportUtil.format(pTolerance));
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private static void exportAcDcConverter(AcDcConverter<?> converter, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String converterId = context.getNamingStrategy().getCgmesId(converter);
        double targetPpcc = converter.getControlMode() == AcDcConverter.ControlMode.P_PCC ? converter.getTargetP() : 0.0;
        double targetUdc = converter.getControlMode() == AcDcConverter.ControlMode.V_DC ? converter.getTargetVdc() : 0.0;
        double p = converter.getPccTerminal().getP();
        double q = converter.getPccTerminal().getQ();
        if (converter instanceof LineCommutatedConverter) {
            String operatingMode = targetPpcc > 0.0 ? "rectifier" : "inverter";
            String pPccControl = converter.getControlMode() == AcDcConverter.ControlMode.P_PCC ? "activePower" : "dcVoltage";
            exportCsConverter(converterId, targetPpcc, targetUdc, p, q, operatingMode, pPccControl, cimNamespace, writer, context);
        } else if (converter instanceof VoltageSourceConverter vsc) {
            double targetQpcc = vsc.isWithMode(RegulationMode.REACTIVE_POWER) ? vsc.getRegulatingTargetQ() : 0;
            double targetUpcc = vsc.isWithMode(RegulationMode.VOLTAGE) ? vsc.getRegulatingTargetV() : 0;
            String pPccControl = vsc.getControlMode() == AcDcConverter.ControlMode.P_PCC ? "pPcc" : "udc";
            String qPccControl = vsc.isWithMode(RegulationMode.VOLTAGE) ? "voltagePcc" : "reactivePcc";
            exportVsConverter(converterId, targetPpcc, targetUdc, targetQpcc, targetUpcc, p, vsc.getLocalTargetQ(), pPccControl, qPccControl, cimNamespace, writer, context);
        }
    }

    private static void exportCsConverter(String converterId, double targetPpcc, double targetUdc,
                                          double p, double q, String operatingMode, String pPccControl,
                                          String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.CS_CONVERTER, converterId, writer, context);
        exportCommonAcDcConverter(targetPpcc, targetUdc, p, q, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, "CsConverter.targetAlpha");
        writer.writeCharacters(CgmesExportUtil.format(0.0));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "CsConverter.targetGamma");
        writer.writeCharacters(CgmesExportUtil.format(0.0));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "CsConverter.targetIdc");
        writer.writeCharacters(CgmesExportUtil.format(0.0));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "CsConverter.operatingMode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "CsOperatingModeKind." + operatingMode);
        writer.writeEmptyElement(cimNamespace, "CsConverter.pPccControl");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "CsPpccControlKind." + pPccControl);
        writer.writeEndElement();
    }

    private static void exportVsConverter(String converterId, double targetPpcc, double targetUdc, double targetQpcc, double targetUpcc,
                                          double p, double q, String pPccControl, String qPccControl,
                                          String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.VS_CONVERTER, converterId, writer, context);
        exportCommonAcDcConverter(targetPpcc, targetUdc, p, q, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, "VsConverter.droop");
        writer.writeCharacters(CgmesExportUtil.format(0.0));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "VsConverter.droopCompensation");
        writer.writeCharacters(CgmesExportUtil.format(0.0));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "VsConverter.qShare");
        writer.writeCharacters(CgmesExportUtil.format(0.0));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "VsConverter.targetQpcc");
        writer.writeCharacters(CgmesExportUtil.format(targetQpcc));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "VsConverter.targetUpcc");
        writer.writeCharacters(CgmesExportUtil.format(targetUpcc));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "VsConverter.pPccControl");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "VsPpccControlKind." + pPccControl);
        writer.writeEmptyElement(cimNamespace, "VsConverter.qPccControl");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "VsQpccControlKind." + qPccControl);
        writer.writeEndElement();
    }

    private static void exportCommonAcDcConverter(double targetPpcc, double targetUdc, double p, double q,
                                                  String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "ACDCConverter.targetPpcc");
        writer.writeCharacters(CgmesExportUtil.format(targetPpcc));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ACDCConverter.targetUdc");
        writer.writeCharacters(CgmesExportUtil.format(targetUdc));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ACDCConverter.p");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ACDCConverter.q");
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
    }

    private enum RegulatingControlType {
        REGULATING_CONTROL, TAP_CHANGER_CONTROL
    }

    private static final class GeneratingUnit {
        String id;
        String className;
        double participationFactor;
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
