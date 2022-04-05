/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class SteadyStateHypothesisExport {

    private static final Logger LOG = LoggerFactory.getLogger(SteadyStateHypothesisExport.class);

    private static final String REGULATING_CONTROL_PROPERTY = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl";
    private static final String GENERATING_UNIT_PROPERTY = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit";

    private SteadyStateHypothesisExport() {
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        final Map<String, List<RegulatingControlView>> regulatingControlViews = new HashMap<>();
        String cimNamespace = context.getCimNamespace();

        try {
            CgmesExportUtil.writeRdfRoot(context.getCimVersion(), writer);

            if (context.getCimVersion() == 16) {
                CgmesExportUtil.writeModelDescription(writer, context.getSshModelDescription(), context);
            }

            writeEnergyConsumers(network, cimNamespace, writer);
            writeEquivalentInjections(network, cimNamespace, writer);
            writeTapChangers(network, cimNamespace, regulatingControlViews, writer);
            writeSynchronousMachines(network, cimNamespace, regulatingControlViews, writer);
            writeShuntCompensators(network, cimNamespace, regulatingControlViews, writer);
            writeStaticVarCompensators(network, cimNamespace, regulatingControlViews, writer);
            writeRegulatingControls(regulatingControlViews, cimNamespace, writer);
            writeGeneratingUnitsParticitationFactors(network, cimNamespace, writer);
            writeConverters(network, cimNamespace, writer);
            // FIXME open status of retained switches in bus-branch models
            writeSwitches(network, cimNamespace, writer);
            // TODO writeControlAreas
            writeTerminals(network, cimNamespace, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSwitches(Network network, String cimNamespace, XMLStreamWriter writer) {
        for (Switch sw : network.getSwitches()) {
            writeSwitch(sw, cimNamespace, writer);
        }
    }

    private static final String ALIAS_TYPE_TERMINAL_1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "1";
    private static final String ALIAS_TYPE_TERMINAL_2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "2";

    private static void writeTerminals(Network network, String cimNamespace, XMLStreamWriter writer) {
        for (Connectable<?> c : network.getConnectables()) {
            for (Terminal t : c.getTerminals()) {
                writeTerminal(t, c, cimNamespace, writer);
            }
        }
        for (Switch sw : network.getSwitches()) {
            // Terminals for switches are exported as always connected
            // The status of the switch is "open" if any of the original terminals were not connected
            // An original "closed" switch with any terminal disconnected
            // will be exported as "open" with terminals connected
            sw.getAliasFromType(ALIAS_TYPE_TERMINAL_1)
                .ifPresent(tid1 -> writeTerminal(tid1, true, cimNamespace, writer));
            sw.getAliasFromType(ALIAS_TYPE_TERMINAL_2)
                .ifPresent(tid2 -> writeTerminal(tid2, true, cimNamespace, writer));
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            // Terminal for equivalent injection at boundary is always connected
            dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal")
                .ifPresent(tid -> writeTerminal(tid, true, cimNamespace, writer));
            // Terminal for boundary side of original line/switch is always connected
            dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary")
                .ifPresent(tid -> writeTerminal(tid, true, cimNamespace, writer));
        }
    }

    private static void writeEquivalentInjections(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // One equivalent injection for every dangling line
        for (DanglingLine dl : network.getDanglingLines()) {
            writeEquivalentInjection(dl, cimNamespace, writer);
        }
    }

    private static void writeTapChangers(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews, XMLStreamWriter writer) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1)
                    .orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeTapChanger(twt, ptcId, twt.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER_TABULAR, regulatingControlViews, cimNamespace, writer);
            }
            if (twt.hasRatioTapChanger()) {
                String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 1)
                    .orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeTapChanger(twt, rtcId, twt.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER, regulatingControlViews, cimNamespace, writer);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int i = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeTapChanger(twt, ptcId, leg.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER_TABULAR, regulatingControlViews, cimNamespace, writer);
                }
                if (leg.hasRatioTapChanger()) {
                    String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeTapChanger(twt, rtcId, leg.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER, regulatingControlViews, cimNamespace, writer);
                }
                i++;
            }
        }
    }

    private static <C extends Connectable<C>> void writeTapChanger(C eq, String tcId, TapChanger<?, ?> tc, String defaultType, Map<String, List<RegulatingControlView>> regulatingControlViews, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesTapChangers<C> cgmesTcs = eq.getExtension(CgmesTapChangers.class);
        String type = defaultType;
        CgmesTapChanger cgmesTc = null;
        if (cgmesTcs != null) {
            cgmesTc = cgmesTcs.getTapChanger(tcId);
            if (cgmesTc != null) {
                type = Optional.ofNullable(cgmesTc.getType()).orElse(defaultType);
            }
        }
        writeTapChanger(type, tcId, tc, cimNamespace, writer);
        addRegulatingControlView(tc, cgmesTc, regulatingControlViews);
        if (cgmesTcs != null) {
            for (CgmesTapChanger tapChanger : cgmesTcs.getTapChangers()) {
                if (tapChanger.isHidden() && tapChanger.getCombinedTapChangerId().equals(tcId)) {
                    writeHiddenTapChanger(tapChanger, defaultType, cimNamespace, writer);
                }
            }
        }
    }

    private static void writeShuntCompensators(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews, XMLStreamWriter writer) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            String shuntType;
            switch (s.getModelType()) {
                case LINEAR:
                    shuntType = "Linear";
                    break;
                case NON_LINEAR:
                    shuntType = "Nonlinear";
                    break;
                default:
                    throw new AssertionError("Unexpected shunt model type: " + s.getModelType());
            }
            boolean controlEnabled = s.isVoltageRegulatorOn();
            writer.writeStartElement(cimNamespace, shuntType + "ShuntCompensator");
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + s.getId());
            writer.writeStartElement(cimNamespace, "ShuntCompensator.sections");
            writer.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "RegulatingCondEq.controlEnabled");
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeEndElement();
            addRegulatingControlView(s, regulatingControlViews);
        }
    }

    private static void addRegulatingControlView(ShuntCompensator s, Map<String, List<RegulatingControlView>> regulatingControlViews) {
        if (s.hasProperty(REGULATING_CONTROL_PROPERTY)) {
            // PowSyBl has considered the control as discrete, with a certain targetDeadband
            // The target value is stored in kV by PowSyBl, so unit multiplier is "k"
            String rcid = s.getProperty(REGULATING_CONTROL_PROPERTY);
            RegulatingControlView rcv = new RegulatingControlView(rcid, RegulatingControlType.REGULATING_CONTROL, true,
                s.isVoltageRegulatorOn(), s.getTargetDeadband(), s.getTargetV(), "k");
            regulatingControlViews.computeIfAbsent(rcid, k -> new ArrayList<>()).add(rcv);
        }
    }

    private static void writeSynchronousMachines(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews, XMLStreamWriter writer) throws XMLStreamException {
        for (Generator g : network.getGenerators()) {
            boolean controlEnabled = g.isVoltageRegulatorOn();
            writer.writeStartElement(cimNamespace, "SynchronousMachine");
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + g.getId());
            writer.writeStartElement(cimNamespace, "RegulatingCondEq.controlEnabled");
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "RotatingMachine.p");
            writer.writeCharacters(CgmesExportUtil.format(-g.getTargetP()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "RotatingMachine.q");
            writer.writeCharacters(CgmesExportUtil.format(-g.getTargetQ()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SynchronousMachine.referencePriority");
            // reference priority is used for angle reference selection (slack)
            writer.writeCharacters(isInSlackBus(g) ? "1" : "0");
            writer.writeEndElement();
            writer.writeEmptyElement(cimNamespace, "SynchronousMachine.operatingMode");
            // All generators in PowSyBl are considered as generator, not motor
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "SynchronousMachineOperatingMode.generator");
            writer.writeEndElement();

            addRegulatingControlView(g, regulatingControlViews);
        }
    }

    private static void addRegulatingControlView(Generator g, Map<String, List<RegulatingControlView>> regulatingControlViews) {
        if (g.hasProperty(REGULATING_CONTROL_PROPERTY)) {
            // PowSyBl has considered the control as continuous and with targetDeadband of size 0
            // The target value is stored in kV by PowSyBl, so unit multiplier is "k"
            String rcid = g.getProperty(REGULATING_CONTROL_PROPERTY);
            double targetDeadband = 0;
            RegulatingControlView rcv = new RegulatingControlView(rcid, RegulatingControlType.REGULATING_CONTROL, false,
                g.isVoltageRegulatorOn(), targetDeadband, g.getTargetV(), "k");
            regulatingControlViews.computeIfAbsent(rcid, k -> new ArrayList<>()).add(rcv);
        }
    }

    private static void writeStaticVarCompensators(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews, XMLStreamWriter writer) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            StaticVarCompensator.RegulationMode regulationMode = svc.getRegulationMode();
            boolean controlEnabled = regulationMode != StaticVarCompensator.RegulationMode.OFF;
            writer.writeStartElement(cimNamespace, "StaticVarCompensator");
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + svc.getId());
            writer.writeStartElement(cimNamespace, "RegulatingCondEq.controlEnabled");
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "StaticVarCompensator.q");
            writer.writeCharacters(CgmesExportUtil.format(svc.getTerminal().getQ()));
            writer.writeEndElement();
            writer.writeEndElement();

            if (svc.hasProperty(REGULATING_CONTROL_PROPERTY)) {
                String rcid = svc.getProperty(REGULATING_CONTROL_PROPERTY);
                double targetDeadband = 0;
                // Regulating control could be reactive power or voltage
                double targetValue;
                String multiplier;
                if (regulationMode == StaticVarCompensator.RegulationMode.VOLTAGE) {
                    targetValue = svc.getVoltageSetpoint();
                    multiplier = "k";
                } else if (regulationMode == StaticVarCompensator.RegulationMode.REACTIVE_POWER) {
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

    private static boolean isInSlackBus(Generator g) {
        VoltageLevel vl = g.getTerminal().getVoltageLevel();
        SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
        if (slackTerminal != null) {
            Bus slackBus = slackTerminal.getTerminal().getBusBreakerView().getBus();
            if (slackBus == g.getTerminal().getBusBreakerView().getBus()) {
                return true;
            }
        }
        return false;
    }

    private static void writeTapChanger(String type, String id, TapChanger<?, ?> tc, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writeTapChanger(type, id, tc.isRegulating(), tc.getTapPosition(), cimNamespace, writer);
    }

    private static void writeTapChanger(String type, String id, boolean controlEnabled, int step, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, type);
        writer.writeAttribute(RDF_NAMESPACE, "about", "#" + id);
        writer.writeStartElement(cimNamespace, "TapChanger.controlEnabled");
        writer.writeCharacters(Boolean.toString(controlEnabled));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "TapChanger.step");
        writer.writeCharacters(CgmesExportUtil.format(step));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void addRegulatingControlView(TapChanger<?, ?> tc, CgmesTapChanger cgmesTc, Map<String, List<RegulatingControlView>> regulatingControlViews) {
        // Multiple tap changers can be stored at the same equipment
        // We use the tap changer id as part of the key for storing the tap changer control id
        if (cgmesTc != null && cgmesTc.getControlId() != null) {
            String controlId = cgmesTc.getControlId();
            RegulatingControlView rcv = null;
            if (tc instanceof RatioTapChanger) {
                rcv = new RegulatingControlView(controlId,
                        RegulatingControlType.TAP_CHANGER_CONTROL,
                        true,
                        tc.isRegulating(),
                        tc.getTargetDeadband(),
                        ((RatioTapChanger) tc).getTargetV(),
                        // Unit multiplier is k for ratio tap changers (regulation value is a voltage in kV)
                        "k");
            } else if (tc instanceof PhaseTapChanger) {
                rcv = new RegulatingControlView(controlId,
                        RegulatingControlType.TAP_CHANGER_CONTROL,
                        true,
                        tc.isRegulating(),
                        tc.getTargetDeadband(),
                        ((PhaseTapChanger) tc).getRegulationValue(),
                        // Unit multiplier is M for phase tap changers (regulation value is an active power flow in MW)
                        "M");
            }
            if (rcv != null) {
                regulatingControlViews.computeIfAbsent(controlId, k -> new ArrayList<>()).add(rcv);
            }
        }
    }

    private static void writeHiddenTapChanger(CgmesTapChanger cgmesTc, String defaultType, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writeTapChanger(Optional.ofNullable(cgmesTc.getType()).orElse(defaultType), cgmesTc.getId(), false,
                cgmesTc.getStep().orElseThrow(() -> new PowsyblException("Non null step expected for tap changer " + cgmesTc.getId())),
                cimNamespace, writer);
    }

    private static void writeRegulatingControls(Map<String, List<RegulatingControlView>> regulatingControlViews, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (List<RegulatingControlView> views : regulatingControlViews.values()) {
            writeRegulatingControl(combineRegulatingControlViews(views), cimNamespace, writer);
        }
    }

    private static RegulatingControlView combineRegulatingControlViews(List<RegulatingControlView> rcs) {
        RegulatingControlView combined = rcs.get(0);
        if (rcs.size() > 1) {
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

    private static void writeRegulatingControl(RegulatingControlView rc, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, regulatingControlClassname(rc.type));
        writer.writeAttribute(RDF_NAMESPACE, "about", "#" + rc.id);
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

    private static void writeSwitch(Switch sw, String cimNamespace, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(cimNamespace, switchClassname(sw.getKind()));
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + sw.getId());
            writer.writeStartElement(cimNamespace, "Switch.open");
            writer.writeCharacters(Boolean.toString(sw.isOpen()));
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static String switchClassname(SwitchKind kind) {
        switch (kind) {
            case BREAKER:
                return "Breaker";
            case DISCONNECTOR:
                return "Disconnector";
            case LOAD_BREAK_SWITCH:
                return "LoadBreakSwitch";
            default:
                throw new AssertionError("Unexpected switch king " + kind);
        }
    }

    private static void writeTerminal(Terminal t, Connectable<?> c, String cimNamespace, XMLStreamWriter writer) {
        Optional<String> tid;
        if (c instanceof DanglingLine) {
            tid = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Network");
        } else {
            int numt = CgmesExportUtil.getTerminalSide(t, c);
            tid = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + numt);
        }
        if (tid.isPresent()) {
            writeTerminal(tid.get(), t.isConnected(), cimNamespace, writer);
        } else {
            LOG.error("Alias not found for terminal {} in connectable {}", t, c.getId());
        }
    }

    private static void writeTerminal(String terminalId, boolean connected, String cimNamespace, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(cimNamespace, CgmesNames.TERMINAL);
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + terminalId);
            writer.writeStartElement(cimNamespace, "ACDCTerminal.connected");
            writer.writeCharacters(Boolean.toString(connected));
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeEquivalentInjection(DanglingLine dl, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        Optional<String> ei = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection");
        if (ei.isPresent()) {
            writer.writeStartElement(cimNamespace, "EquivalentInjection");
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + ei.get());
            writer.writeStartElement(cimNamespace, "EquivalentInjection.p");
            writer.writeCharacters(CgmesExportUtil.format(dl.getP0()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "EquivalentInjection.q");
            writer.writeCharacters(CgmesExportUtil.format(dl.getQ0()));
            writer.writeEndElement();
            // regulationStatus and regulationTarget are optional,
            // but test cases contain the attributes with disabled and 0
            boolean regulationStatus = false;
            double regulationTarget = 0;
            if (dl.getGeneration() != null) {
                regulationStatus = dl.getGeneration().isVoltageRegulationOn();
                regulationTarget = dl.getGeneration().getTargetV();
            }
            writer.writeStartElement(cimNamespace, "EquivalentInjection.regulationStatus");
            writer.writeCharacters(Boolean.toString(regulationStatus));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "EquivalentInjection.regulationTarget");
            writer.writeCharacters(CgmesExportUtil.format(regulationTarget));
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static void writeEnergyConsumers(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            writeSshEnergyConsumer(load.getId(), load.getP0(), load.getQ0(), load.getExtension(LoadDetail.class), cimNamespace, writer);
        }
    }

    private static void writeSshEnergyConsumer(String id, double p, double q, LoadDetail loadDetail, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, CgmesExportUtil.loadClassName(loadDetail));
        writer.writeAttribute(RDF_NAMESPACE, "about", "#" + id);
        writer.writeStartElement(cimNamespace, "EnergyConsumer.p");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "EnergyConsumer.q");
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeConverters(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (HvdcConverterStation<?> converterStation : network.getHvdcConverterStations()) {
            writer.writeStartElement(cimNamespace, CgmesExportUtil.converterClassName(converterStation));
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + converterStation.getId());
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
            if (converterStation instanceof LccConverterStation) {
                LccConverterStation lccConverterStation = (LccConverterStation) converterStation;

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
            } else if (converterStation instanceof VscConverterStation) {
                VscConverterStation vscConverterStation = (VscConverterStation) converterStation;

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

    private static void writeGeneratingUnitsParticitationFactors(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // Multiple generators may share the same generation unit,
        // we will choose the participation factor from the last generator that references the generating unit
        // We only consider generators that have participation factors
        Map<String, GeneratingUnit> generatingUnits = new HashMap<>();
        for (Generator g : network.getGenerators()) {
            GeneratingUnit gu = generatingUnitForGenerator(g);
            if (gu != null) {
                generatingUnits.put(gu.id, gu);
            }
        }
        for (GeneratingUnit gu : generatingUnits.values()) {
            writeGeneratingUnitParticipationFactor(gu, cimNamespace, writer);
        }
    }

    private static GeneratingUnit generatingUnitForGenerator(Generator g) {
        if (g.hasProperty(GENERATING_UNIT_PROPERTY) && g.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "normalPF")) {
            GeneratingUnit gu = new GeneratingUnit();
            gu.id = g.getProperty(GENERATING_UNIT_PROPERTY);
            gu.participationFactor = Double.valueOf(g.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "normalPF"));
            gu.className = generatingUnitClassname(g);
            return gu;
        }
        return null;
    }

    private static void writeGeneratingUnitParticipationFactor(GeneratingUnit gu, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, gu.className);
        writer.writeAttribute(RDF_NAMESPACE, "about", "#" + gu.id);
        writer.writeStartElement(cimNamespace, "GeneratingUnit.normalPF");
        writer.writeCharacters(CgmesExportUtil.format(gu.participationFactor));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String generatingUnitClassname(Generator g) {
        EnergySource energySource = g.getEnergySource();
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

    private enum RegulatingControlType {
        REGULATING_CONTROL, TAP_CHANGER_CONTROL
    }

    private static class GeneratingUnit {
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
