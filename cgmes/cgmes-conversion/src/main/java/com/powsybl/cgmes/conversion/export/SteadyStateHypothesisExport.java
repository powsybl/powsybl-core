/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_BUSBAR_SECTION_TERMINALS;
import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class SteadyStateHypothesisExport {

    private static final Logger LOG = LoggerFactory.getLogger(SteadyStateHypothesisExport.class);
    private static final String REGULATING_CONTROL_PROPERTY = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl";
    private static final String GENERATING_UNIT_PROPERTY = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit";
    private static final String ROTATING_MACHINE_P = "RotatingMachine.p";
    private static final String ROTATING_MACHINE_Q = "RotatingMachine.q";
    private static final String REGULATING_COND_EQ_CONTROL_ENABLED = "RegulatingCondEq.controlEnabled";

    private SteadyStateHypothesisExport() {
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        write(network, writer, context, Optional.empty());
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context, Optional<CgmesMetadataModel> model) {
        final Map<String, List<RegulatingControlView>> regulatingControlViews = new HashMap<>();
        String cimNamespace = context.getCim().getNamespace();

        try {
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), writer);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(network, CgmesSubset.STEADY_STATE_HYPOTHESIS, writer, model.orElseGet(context::getExportedSSHModel), context);
            }

            writeLoads(network, cimNamespace, writer, context);
            writeEquivalentInjections(network, cimNamespace, writer, context);
            writeTapChangers(network, cimNamespace, regulatingControlViews, writer, context);
            writeGenerators(network, cimNamespace, regulatingControlViews, writer, context);
            writeBatteries(network, cimNamespace, writer, context);
            writeShuntCompensators(network, cimNamespace, regulatingControlViews, writer, context);
            writeStaticVarCompensators(network, cimNamespace, regulatingControlViews, writer, context);
            writeRegulatingControls(regulatingControlViews, cimNamespace, writer, context);
            writeGeneratingUnitsParticitationFactors(network, cimNamespace, writer, context);
            writeConverters(network, cimNamespace, writer, context);
            // FIXME open status of retained switches in bus-branch models
            writeSwitches(network, cimNamespace, writer, context);
            writeTerminals(network, cimNamespace, writer, context);
            writeControlAreas(network, cimNamespace, writer, context);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSwitches(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                writeSwitch(sw, cimNamespace, writer, context);
            }
        }
    }

    private static final String ALIAS_TYPE_TERMINAL_1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "1";
    private static final String ALIAS_TYPE_TERMINAL_2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "2";

    private static void writeTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        for (Connectable<?> c : network.getConnectables()) { // TODO write boundary terminals for tie lines from CGMES
            if (context.isExportedEquipment(c)) {
                if (CgmesExportUtil.isEquivalentShuntWithZeroSectionCount(c)) {
                    // Equivalent shunts do not have a section count in SSH, SV profiles,
                    // the only way to make output consistent with IIDM section count == 0 is to disconnect its terminal
                    writeTerminal(CgmesExportUtil.getTerminalId(c.getTerminals().get(0), context), false, cimNamespace, writer, context);
                } else {
                    for (Terminal t : c.getTerminals()) {
                        writeTerminal(t, cimNamespace, writer, context);
                    }
                }
            }
        }
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                // Terminals for switches are exported as always connected
                // The status of the switch is "open" if any of the original terminals were not connected
                // An original "closed" switch with any terminal disconnected
                // will be exported as "open" with terminals connected
                if (sw.getAliasFromType(ALIAS_TYPE_TERMINAL_1).isPresent()) {
                    writeTerminal(context.getNamingStrategy().getCgmesIdFromAlias(sw, ALIAS_TYPE_TERMINAL_1), true, cimNamespace, writer, context);
                }
                if (sw.getAliasFromType(ALIAS_TYPE_TERMINAL_2).isPresent()) {
                    writeTerminal(context.getNamingStrategy().getCgmesIdFromAlias(sw, ALIAS_TYPE_TERMINAL_2), true, cimNamespace, writer, context);
                }
            }
        }
        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.ALL)) {
            // Terminal for equivalent injection at boundary is always connected
            if (dl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal") != null) {
                writeTerminal(context.getNamingStrategy().getCgmesIdFromProperty(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), true, cimNamespace, writer, context);
            }
            // Terminal for boundary side of original line/switch is always connected
            if (dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary").isPresent()) {
                writeTerminal(context.getNamingStrategy().getCgmesIdFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary"), true, cimNamespace, writer, context);
            }
        }
        // If we are performing an updated export, write recorded busbar section terminals as connected
        if (!context.isExportEquipment()) {
            for (Bus b : network.getBusBreakerView().getBuses()) {
                String bbsTerminals = b.getProperty(PROPERTY_BUSBAR_SECTION_TERMINALS, "");
                if (!bbsTerminals.isEmpty()) {
                    for (String bbsTerminal : bbsTerminals.split(",")) {
                        writeTerminal(bbsTerminal, true, cimNamespace, writer, context);
                    }
                }
            }
        }
    }

    private static void writeEquivalentInjections(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // One equivalent injection for every dangling line
        List<String> exported = new ArrayList<>();

        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.ALL)) {
            String ei = dl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION);
            if (!exported.contains(ei) && ei != null) {
                // Ensure equivalent injection identifier is valid
                String cgmesId = context.getNamingStrategy().getCgmesIdFromProperty(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION);
                // regulationStatus and regulationTarget are optional,
                // but test cases contain the attributes with disabled and 0
                boolean regulationStatus = false;
                double regulationTarget = 0;
                if (dl.getGeneration() != null) {
                    regulationStatus = dl.getGeneration().isVoltageRegulationOn();
                    regulationTarget = dl.getGeneration().getTargetV();
                }
                writeEquivalentInjection(cgmesId, dl.getP0(), dl.getQ0(), regulationStatus, regulationTarget, cimNamespace, writer, context);
                exported.add(ei);
            }
        }
    }

    private static String cgmesTapChangerId(TwoWindingsTransformer twt, String tapChangerKind, CgmesExportContext context) {
        String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tapChangerKind + 1;
        if (twt.getAliasFromType(aliasType).isEmpty()) {
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tapChangerKind + 2;
        }
        return context.getNamingStrategy().getCgmesIdFromAlias(twt, aliasType);
    }

    private static void writeTapChangers(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String ptcId = cgmesTapChangerId(twt, CgmesNames.PHASE_TAP_CHANGER, context);
                writeTapChanger(twt, ptcId, twt.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER_TABULAR, regulatingControlViews, cimNamespace, writer, context);
            }
            if (twt.hasRatioTapChanger()) {
                String rtcId = cgmesTapChangerId(twt, CgmesNames.RATIO_TAP_CHANGER, context);
                writeTapChanger(twt, rtcId, twt.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER, regulatingControlViews, cimNamespace, writer, context);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int i = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + i);
                    writeTapChanger(twt, ptcId, leg.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER_TABULAR, regulatingControlViews, cimNamespace, writer, context);
                }
                if (leg.hasRatioTapChanger()) {
                    String rtcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + i);
                    writeTapChanger(twt, rtcId, leg.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER, regulatingControlViews, cimNamespace, writer, context);
                }
                i++;
            }
        }
    }

    private static <C extends Connectable<C>> void writeTapChanger(C eq, String tcId, TapChanger<?, ?, ?, ?> tc, String defaultType, Map<String, List<RegulatingControlView>> regulatingControlViews, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String type = CgmesExportUtil.cgmesTapChangerType(eq, tcId).orElse(defaultType);
        writeTapChanger(type, tcId, tc, cimNamespace, writer, context);

        CgmesTapChangers<C> cgmesTcs = eq.getExtension(CgmesTapChangers.class);
        CgmesTapChanger cgmesTc = null;
        if (cgmesTcs != null) {
            cgmesTc = cgmesTcs.getTapChanger(tcId);
        }
        addRegulatingControlView(tc, cgmesTc, regulatingControlViews);
        // If we are exporting equipment definitions the hidden tap changer will not be exported
        // because it has been included in the model for the only tap changer left in IIDM
        // If we are exporting only SSH, SV, ... we have to write the step we have saved for it
        if (cgmesTcs != null && !context.isExportEquipment()) {
            for (CgmesTapChanger tapChanger : cgmesTcs.getTapChangers()) {
                if (tapChanger.isHidden() && tapChanger.getCombinedTapChangerId().equals(tcId)) {
                    writeHiddenTapChanger(tapChanger, defaultType, cimNamespace, writer, context);
                }
            }
        }
    }

    private static void writeShuntCompensators(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                               XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            if ("true".equals(s.getProperty(Conversion.PROPERTY_IS_EQUIVALENT_SHUNT))) {
                continue;
            }

            String shuntType = switch (s.getModelType()) {
                case LINEAR -> "Linear";
                case NON_LINEAR -> "Nonlinear";
                default -> throw new IllegalStateException("Unexpected shunt model type: " + s.getModelType());
            };
            boolean controlEnabled = s.isVoltageRegulatorOn();

            CgmesExportUtil.writeStartAbout(shuntType + "ShuntCompensator", context.getNamingStrategy().getCgmesId(s), cimNamespace, writer, context);
            writer.writeStartElement(cimNamespace, "ShuntCompensator.sections");
            writer.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED);
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeEndElement();
            addRegulatingControlView(s, regulatingControlViews, context);
        }
    }

    private static void addRegulatingControlView(ShuntCompensator s, Map<String, List<RegulatingControlView>> regulatingControlViews, CgmesExportContext context) {
        if (s.hasProperty(REGULATING_CONTROL_PROPERTY)) {
            // PowSyBl has considered the control as discrete, with a certain targetDeadband
            // The target value is stored in kV by PowSyBl, so unit multiplier is "k"
            String rcid = context.getNamingStrategy().getCgmesIdFromProperty(s, REGULATING_CONTROL_PROPERTY);
            RegulatingControlView rcv = new RegulatingControlView(rcid, RegulatingControlType.REGULATING_CONTROL, true,
                s.isVoltageRegulatorOn(), s.getTargetDeadband(), s.getTargetV(), "k");
            regulatingControlViews.computeIfAbsent(rcid, k -> new ArrayList<>()).add(rcv);
        }
    }

    private static void writeGenerators(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                 XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Generator g : network.getGenerators()) {
            String cgmesOriginalClass = g.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SYNCHRONOUS_MACHINE);

            switch (cgmesOriginalClass) {
                case CgmesNames.EQUIVALENT_INJECTION:
                    writeEquivalentInjection(context.getNamingStrategy().getCgmesId(g), -g.getTargetP(), -g.getTargetQ(),
                            g.isVoltageRegulatorOn(), g.getTargetV(), cimNamespace, writer, context);
                    break;
                case CgmesNames.EXTERNAL_NETWORK_INJECTION:
                    writeExternalNetworkInjection(context.getNamingStrategy().getCgmesId(g), g.isVoltageRegulatorOn(),
                            -g.getTargetP(), -g.getTargetQ(), ReferencePriority.get(g),
                            cimNamespace, writer, context);
                    addRegulatingControlView(g, regulatingControlViews, context);
                    break;
                case CgmesNames.SYNCHRONOUS_MACHINE:
                    writeSynchronousMachine(context.getNamingStrategy().getCgmesId(g), g.isVoltageRegulatorOn(),
                            -g.getTargetP(), -g.getTargetQ(), ReferencePriority.get(g), obtainOperatingMode(g, g.getMinP(), g.getMaxP(), g.getTargetP()),
                            cimNamespace, writer, context);
                    addRegulatingControlView(g, regulatingControlViews, context);
                    break;
                default:
                    throw new PowsyblException("Unexpected cgmes equipment " + cgmesOriginalClass);
            }
        }
    }

    private static void writeExternalNetworkInjection(String id, boolean controlEnabled, double p, double q, int referencePriority, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.EXTERNAL_NETWORK_INJECTION, id, cimNamespace, writer, context);
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

    private static void writeSynchronousMachine(String id, boolean controlEnabled, double p, double q, int referencePriority, String mode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.SYNCHRONOUS_MACHINE, id, cimNamespace, writer, context);
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

    private static void writeBatteries(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Battery b : network.getBatteries()) {
            writeSynchronousMachine(context.getNamingStrategy().getCgmesId(b), false,
                    -b.getTargetP(), -b.getTargetQ(), ReferencePriority.get(b), obtainOperatingMode(b, b.getMinP(), b.getMaxP(), b.getTargetP()),
                    cimNamespace, writer, context);
        }
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> String obtainOperatingMode(I i, double minP, double maxP, double targetP) {
        String kind = i.getProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_TYPE);
        String operatingMode = i.getProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_OPERATING_MODE);
        String calculatedKind = CgmesExportUtil.obtainCalculatedSynchronousMachineKind(minP, maxP, CgmesExportUtil.obtainCurve(i), kind);
        String calculatedOperatingMode = obtainOperatingMode(targetP);
        return operatingMode != null && calculatedKind.contains(operatingMode) ? operatingMode : calculatedOperatingMode;
    }

    private static String obtainOperatingMode(double targetP) {
        if (targetP < 0) {
            return "motor";
        } else if (targetP > 0) {
            return "generator";
        } else {
            return "condenser";
        }
    }

    private static void addRegulatingControlView(Generator g, Map<String, List<RegulatingControlView>> regulatingControlViews, CgmesExportContext context) {
        if (g.hasProperty(REGULATING_CONTROL_PROPERTY)) {
            // PowSyBl has considered the control as continuous and with targetDeadband of size 0
            // The target value is stored in kV by PowSyBl, so unit multiplier is "k"
            String rcid = context.getNamingStrategy().getCgmesIdFromProperty(g, REGULATING_CONTROL_PROPERTY);
            double targetDeadband = 0;
            RegulatingControlView rcv = new RegulatingControlView(rcid, RegulatingControlType.REGULATING_CONTROL, false,
                g.isVoltageRegulatorOn(), targetDeadband, g.getTargetV(), "k");
            regulatingControlViews.computeIfAbsent(rcid, k -> new ArrayList<>()).add(rcv);
        }
    }

    private static boolean isValidSvcVolatgeSetpoint(double v) {
        return Double.isFinite(v) && v > 0;
    }

    private static boolean isValidSvcReactivePowerSetpoint(double q) {
        return Double.isFinite(q);
    }

    private static void writeStaticVarCompensators(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews,
                                                   XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            StaticVarCompensator.RegulationMode regulationMode = svc.getRegulationMode();
            boolean controlEnabled = regulationMode != StaticVarCompensator.RegulationMode.OFF;

            CgmesExportUtil.writeStartAbout("StaticVarCompensator", context.getNamingStrategy().getCgmesId(svc), cimNamespace, writer, context);
            writer.writeStartElement(cimNamespace, REGULATING_COND_EQ_CONTROL_ENABLED);
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "StaticVarCompensator.q");
            writer.writeCharacters(CgmesExportUtil.format(svc.getTerminal().getQ()));
            writer.writeEndElement();
            writer.writeEndElement();

            if (svc.hasProperty(REGULATING_CONTROL_PROPERTY)) {
                String rcid = context.getNamingStrategy().getCgmesIdFromProperty(svc, REGULATING_CONTROL_PROPERTY);
                double targetDeadband = 0;
                // Regulating control could be reactive power or voltage
                double targetValue;
                String multiplier;
                if (regulationMode == StaticVarCompensator.RegulationMode.VOLTAGE
                        || regulationMode == StaticVarCompensator.RegulationMode.OFF && isValidSvcVolatgeSetpoint(svc.getVoltageSetpoint())) {
                    targetValue = svc.getVoltageSetpoint();
                    multiplier = "k";
                } else if (regulationMode == StaticVarCompensator.RegulationMode.REACTIVE_POWER
                        || regulationMode == StaticVarCompensator.RegulationMode.OFF && isValidSvcReactivePowerSetpoint(svc.getReactivePowerSetpoint())) {
                    targetValue = svc.getReactivePowerSetpoint();
                    multiplier = "M";
                } else {
                    targetValue = 0;
                    multiplier = "k";
                }
                RegulatingControlView rcv = new RegulatingControlView(rcid, RegulatingControlType.REGULATING_CONTROL, false,
                        controlEnabled, targetDeadband, targetValue, multiplier);
                regulatingControlViews.computeIfAbsent(rcid, k -> new ArrayList<>()).add(rcv);
            }
        }
    }

    private static void writeTapChanger(String type, String id, TapChanger<?, ?, ?, ?> tc, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeTapChanger(type, id, tc.isRegulating(), tc.getTapPosition(), cimNamespace, writer, context);
    }

    private static void writeTapChanger(String type, String id, boolean controlEnabled, int step, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(type, id, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "TapChanger.controlEnabled");
        writer.writeCharacters(Boolean.toString(controlEnabled));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "TapChanger.step");
        writer.writeCharacters(CgmesExportUtil.format(step));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void addRegulatingControlView(TapChanger<?, ?, ?, ?> tc, CgmesTapChanger cgmesTc, Map<String, List<RegulatingControlView>> regulatingControlViews) {
        // Multiple tap changers can be stored at the same equipment
        // We use the tap changer id as part of the key for storing the tap changer control id
        if (cgmesTc != null && cgmesTc.getControlId() != null) {
            String controlId = cgmesTc.getControlId();
            RegulatingControlView rcv = null;
            if (tc instanceof RatioTapChanger ratioTapChanger
                    && CgmesExportUtil.regulatingControlIsDefined(ratioTapChanger)) {
                rcv = new RegulatingControlView(controlId,
                        RegulatingControlType.TAP_CHANGER_CONTROL,
                        true,
                        ratioTapChanger.isRegulating(),
                        ratioTapChanger.getTargetDeadband(),
                        ratioTapChanger.getTargetV(),
                        // Unit multiplier is k for ratio tap changers (regulation value is a voltage in kV)
                        "k");
            } else if (tc instanceof PhaseTapChanger phaseTapChanger
                    && CgmesExportUtil.regulatingControlIsDefined(phaseTapChanger)) {
                boolean valid;
                String unitMultiplier = switch (phaseTapChanger.getRegulationMode()) {
                    case CURRENT_LIMITER -> {
                        // Unit multiplier is none (multiply by 1), regulation value is a current in Amperes
                        valid = true;
                        yield "none";
                    }
                    case ACTIVE_POWER_CONTROL -> {
                        // Unit multiplier is M, regulation value is an active power flow in MW
                        valid = true;
                        yield "M";
                    }
                    default -> {
                        valid = false;
                        yield "none";
                    }
                };
                if (valid) {
                    rcv = new RegulatingControlView(controlId,
                            RegulatingControlType.TAP_CHANGER_CONTROL,
                            true,
                            phaseTapChanger.isRegulating(),
                            phaseTapChanger.getTargetDeadband(),
                            phaseTapChanger.getRegulationValue(),
                            unitMultiplier);
                }
            }
            if (rcv != null) {
                regulatingControlViews.computeIfAbsent(controlId, k -> new ArrayList<>()).add(rcv);
            }
        }
    }

    private static void writeHiddenTapChanger(CgmesTapChanger cgmesTc, String defaultType, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeTapChanger(Optional.ofNullable(cgmesTc.getType()).orElse(defaultType), cgmesTc.getId(), false,
                cgmesTc.getStep().orElseThrow(() -> new PowsyblException("Non null step expected for tap changer " + cgmesTc.getId())),
                cimNamespace, writer, context);
    }

    private static void writeRegulatingControls(Map<String, List<RegulatingControlView>> regulatingControlViews, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (List<RegulatingControlView> views : regulatingControlViews.values()) {
            writeRegulatingControl(combineRegulatingControlViews(views), cimNamespace, writer, context);
        }
    }

    private static RegulatingControlView combineRegulatingControlViews(List<RegulatingControlView> rcs) {
        RegulatingControlView combined = rcs.get(0);
        if (rcs.size() > 1 && LOG.isWarnEnabled()) {
            LOG.warn("Multiple views ({}) for regulating control {} are combined", rcs.size(), rcs.get(0).id);
        }
        for (int k = 1; k < rcs.size(); k++) {
            RegulatingControlView current = rcs.get(k);
            if (combined.targetDeadband == 0 && current.targetDeadband > 0) {
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

    private static void writeRegulatingControl(RegulatingControlView rc, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(regulatingControlClassname(rc.type), rc.id, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "RegulatingControl.discrete");
        writer.writeCharacters(Boolean.toString(rc.discrete));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "RegulatingControl.enabled");
        writer.writeCharacters(Boolean.toString(rc.controlEnabled));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "RegulatingControl.targetDeadband");
        writer.writeCharacters(CgmesExportUtil.format(rc.targetDeadband));
        writer.writeEndElement();
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

    private static void writeSwitch(Switch sw, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            String switchType = sw.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "switchType");
            String className = switchType != null ? switchType : CgmesExportUtil.switchClassname(sw.getKind());
            CgmesExportUtil.writeStartAbout(className, context.getNamingStrategy().getCgmesId(sw), cimNamespace, writer, context);
            writer.writeStartElement(cimNamespace, "Switch.open");
            writer.writeCharacters(Boolean.toString(sw.isOpen()));
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeTerminal(Terminal t, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        writeTerminal(CgmesExportUtil.getTerminalId(t, context), t.isConnected(), cimNamespace, writer, context);
    }

    private static void writeTerminal(String terminalId, boolean connected, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            CgmesExportUtil.writeStartAbout(CgmesNames.TERMINAL, terminalId, cimNamespace, writer, context);
            writer.writeStartElement(cimNamespace, "ACDCTerminal.connected");
            writer.writeCharacters(Boolean.toString(connected));
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeEquivalentInjection(String cgmesId, double p, double q, boolean regulationStatus, double regulationTarget, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.EQUIVALENT_INJECTION, cgmesId, cimNamespace, writer, context);
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

    private static void writeLoads(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            if (context.isExportedEquipment(load)) {
                String className = obtainLoadClassName(load, context);
                switch (className) {
                    case CgmesNames.ASYNCHRONOUS_MACHINE ->
                            writeAsynchronousMachine(context.getNamingStrategy().getCgmesId(load), load.getP0(), load.getQ0(), cimNamespace, writer, context);
                    case CgmesNames.ENERGY_SOURCE ->
                            writeEnergySource(context.getNamingStrategy().getCgmesId(load), load.getP0(), load.getQ0(), cimNamespace, writer, context);
                    case CgmesNames.ENERGY_CONSUMER, CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY ->
                            writeSshEnergyConsumer(context.getNamingStrategy().getCgmesId(load), className, load.getP0(), load.getQ0(), cimNamespace, writer, context);
                    default -> throw new PowsyblException("Unexpected class name: " + className);
                }
            }
        }
    }

    // if EQ is not exported, the original class name is preserved
    private static String obtainLoadClassName(Load load, CgmesExportContext context) {
        String originalClassName = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        return (originalClassName != null && !context.isExportEquipment()) ? originalClassName : CgmesExportUtil.loadClassName(load);
    }

    private static void writeAsynchronousMachine(String id, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.ASYNCHRONOUS_MACHINE, id, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, ROTATING_MACHINE_P);
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, ROTATING_MACHINE_Q);
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeEnergySource(String id, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(CgmesNames.ENERGY_SOURCE, id, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "EnergySource.activePower");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "EnergySource.reactivePower");
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeSshEnergyConsumer(String id, String className, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(className, id, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "EnergyConsumer.p");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "EnergyConsumer.q");
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeConverters(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (HvdcConverterStation<?> converterStation : network.getHvdcConverterStations()) {
            CgmesExportUtil.writeStartAbout(CgmesExportUtil.converterClassName(converterStation), context.getNamingStrategy().getCgmesId(converterStation), cimNamespace, writer, context);
            double ppcc;
            if (CgmesExportUtil.isConverterStationRectifier(converterStation)) {
                ppcc = converterStation.getHvdcLine().getActivePowerSetpoint();
            } else {
                double otherConverterStationLossFactor = converterStation.getOtherConverterStation().map(HvdcConverterStation::getLossFactor).orElse(0.0f);
                double pDCInverter = converterStation.getHvdcLine().getActivePowerSetpoint() * (1 - otherConverterStationLossFactor / 100);
                double poleLoss = converterStation.getLossFactor() / 100 * pDCInverter;
                ppcc = -(pDCInverter - poleLoss);
            }
            writer.writeStartElement(cimNamespace, "ACDCConverter.targetPpcc");
            writer.writeCharacters(CgmesExportUtil.format(ppcc));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "ACDCConverter.targetUdc");
            writer.writeCharacters(CgmesExportUtil.format(0.0));
            writer.writeEndElement();
            if (converterStation instanceof LccConverterStation lccConverterStation) {

                writePandQ(cimNamespace, ppcc, getQfromPowerFactor(ppcc, lccConverterStation.getPowerFactor()), writer);
                writer.writeStartElement(cimNamespace, "CsConverter.targetAlpha");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
                writer.writeStartElement(cimNamespace, "CsConverter.targetGamma");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
                writer.writeStartElement(cimNamespace, "CsConverter.targetIdc");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
                writer.writeEmptyElement(cimNamespace, "CsConverter.operatingMode");
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + converterOperatingMode(converterStation));
                writer.writeEmptyElement(cimNamespace, "CsConverter.pPccControl");
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "CsPpccControlKind.activePower");
            } else if (converterStation instanceof VscConverterStation vscConverterStation) {

                writePandQ(cimNamespace, ppcc, vscConverterStation.getReactivePowerSetpoint(), writer);
                writer.writeStartElement(cimNamespace, "VsConverter.droop");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
                writer.writeStartElement(cimNamespace, "VsConverter.droopCompensation");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
                writer.writeStartElement(cimNamespace, "VsConverter.qShare");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
                writer.writeStartElement(cimNamespace, "VsConverter.targetQpcc");
                writer.writeCharacters(CgmesExportUtil.format(vscConverterStation.getReactivePowerSetpoint()));
                writer.writeEndElement();
                writer.writeStartElement(cimNamespace, "VsConverter.targetUpcc");
                writer.writeCharacters(CgmesExportUtil.format(vscConverterStation.getVoltageSetpoint()));
                writer.writeEndElement();
                writer.writeEmptyElement(cimNamespace, "VsConverter.pPccControl");
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + converterOperatingMode(converterStation));
                writer.writeEmptyElement(cimNamespace, "VsConverter.qPccControl");
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "VsQpccControlKind." + (vscConverterStation.isVoltageRegulatorOn() ? "voltagePcc" : "reactivePcc"));
            }
            writer.writeEndElement();
        }
    }

    private static void writePandQ(String cimNamespace, double p, double q, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "ACDCConverter.p");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ACDCConverter.q");
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
    }

    public static String converterOperatingMode(HvdcConverterStation<?> converterStation) {
        if (CgmesExportUtil.isConverterStationRectifier(converterStation)) {
            return converterStationRectifier(converterStation);
        } else {
            return converterStationInverter(converterStation);
        }
    }

    public static String converterStationRectifier(HvdcConverterStation<?> converterStation) {
        if (converterStation instanceof LccConverterStation) {
            return "CsOperatingModeKind.rectifier";
        } else if (converterStation instanceof VscConverterStation) {
            return "VsPpccControlKind.pPcc";
        }
        throw new PowsyblException("Invalid converter type");
    }

    public static String converterStationInverter(HvdcConverterStation<?> converterStation) {
        if (converterStation instanceof LccConverterStation) {
            return "CsOperatingModeKind.inverter";
        } else if (converterStation instanceof VscConverterStation) {
            return "VsPpccControlKind.udc";
        }
        throw new PowsyblException("Invalid converter type");
    }

    private static double getQfromPowerFactor(double p, double powerFactor) {
        if (powerFactor == 0.0) {
            return 0.0;
        }
        return p * Math.sqrt((1 - powerFactor * powerFactor) / (powerFactor * powerFactor));
    }

    private static void writeGeneratingUnitsParticitationFactors(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
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
            writeGeneratingUnitParticipationFactor(gu, cimNamespace, writer, context);
        }
    }

    private static GeneratingUnit generatingUnitForGeneratorAndBatteries(Injection<?> i, CgmesExportContext context) {
        if (i.hasProperty(GENERATING_UNIT_PROPERTY) && (i.getExtension(ActivePowerControl.class) != null || i.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "normalPF"))) {
            GeneratingUnit gu = new GeneratingUnit();
            gu.id = context.getNamingStrategy().getCgmesIdFromProperty(i, GENERATING_UNIT_PROPERTY);
            if (i.getExtension(ActivePowerControl.class) != null) {
                gu.participationFactor = i.getExtension(ActivePowerControl.class).getParticipationFactor();
            } else {
                gu.participationFactor = Double.parseDouble(i.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "normalPF"));
            }
            gu.className = generatingUnitClassname(i);
            return gu;
        }
        return null;
    }

    private static void writeGeneratingUnitParticipationFactor(GeneratingUnit gu, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout(gu.className, gu.id, cimNamespace, writer, context);
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

    private static void writeControlAreas(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesControlAreas areas = network.getExtension(CgmesControlAreas.class);
        for (CgmesControlArea area : areas.getCgmesControlAreas()) {
            writeControlArea(area, cimNamespace, writer, context);
        }
    }

    private static void writeControlArea(CgmesControlArea area, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String areaId = context.getNamingStrategy().getCgmesId(area.getId());
        CgmesExportUtil.writeStartAbout("ControlArea", areaId, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "ControlArea.netInterchange");
        writer.writeCharacters(CgmesExportUtil.format(area.getNetInterchange()));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ControlArea.pTolerance");
        writer.writeCharacters(CgmesExportUtil.format(area.getPTolerance()));
        writer.writeEndElement();
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
