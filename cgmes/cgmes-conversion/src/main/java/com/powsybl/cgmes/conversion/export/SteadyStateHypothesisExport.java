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
public final class SteadyStateHypothesisExport extends AbstractCgmesExporter {

    private static final Logger LOG = LoggerFactory.getLogger(SteadyStateHypothesisExport.class);
    private static final String REGULATING_CONTROL_PROPERTY = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl";
    private static final String GENERATING_UNIT_PROPERTY = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit";

    private final Map<String, List<RegulatingControlView>> regulatingControlViews = new HashMap<>();

    SteadyStateHypothesisExport(CgmesExportContext context, XMLStreamWriter xmlWriter) {
        super(context, xmlWriter);
    }

    public void export() {
        try {
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), xmlWriter);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(xmlWriter, context.getSshModelDescription(), context);
            }

            writeEnergyConsumers();
            writeEquivalentInjections();
            writeTapChangers();
            writeSynchronousMachines();
            writeShuntCompensators();
            writeStaticVarCompensators();
            writeRegulatingControls();
            writeGeneratingUnitsParticitationFactors();
            writeConverters();
            // FIXME open status of retained switches in bus-branch models
            writeSwitches();
            // TODO writeControlAreas
            writeTerminals();

            xmlWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeSwitches() {
        for (Switch sw : context.getNetwork().getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                writeSwitch(sw);
            }
        }
    }

    private static final String ALIAS_TYPE_TERMINAL_1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "1";
    private static final String ALIAS_TYPE_TERMINAL_2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "2";

    private void writeTerminals() {
        for (Connectable<?> c : context.getNetwork().getConnectables()) {
            if (context.isExportedEquipment(c)) {
                for (Terminal t : c.getTerminals()) {
                    writeTerminal(t);
                }
            }
        }
        for (Switch sw : context.getNetwork().getSwitches()) {
            // Terminals for switches are exported as always connected
            // The status of the switch is "open" if any of the original terminals were not connected
            // An original "closed" switch with any terminal disconnected
            // will be exported as "open" with terminals connected
            sw.getAliasFromType(ALIAS_TYPE_TERMINAL_1)
                .ifPresent(tid1 -> writeTerminal(tid1, true));
            sw.getAliasFromType(ALIAS_TYPE_TERMINAL_2)
                .ifPresent(tid2 -> writeTerminal(tid2, true));
        }
        for (DanglingLine dl : context.getNetwork().getDanglingLines()) {
            // Terminal for equivalent injection at boundary is always connected
            dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal")
                .ifPresent(tid -> writeTerminal(tid, true));
            // Terminal for boundary side of original line/switch is always connected
            dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary")
                .ifPresent(tid -> writeTerminal(tid, true));
        }
    }

    private void writeEquivalentInjections() throws XMLStreamException {
        // One equivalent injection for every dangling line
        for (DanglingLine dl : context.getNetwork().getDanglingLines()) {
            writeEquivalentInjection(dl);
        }
    }

    private void writeTapChangers() throws XMLStreamException {
        for (TwoWindingsTransformer twt : context.getNetwork().getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1)
                    .orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeTapChanger(twt, ptcId, twt.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER_TABULAR);
            }
            if (twt.hasRatioTapChanger()) {
                String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 1)
                    .orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeTapChanger(twt, rtcId, twt.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER);
            }
        }

        for (ThreeWindingsTransformer twt : context.getNetwork().getThreeWindingsTransformers()) {
            int i = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeTapChanger(twt, ptcId, leg.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER_TABULAR);
                }
                if (leg.hasRatioTapChanger()) {
                    String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeTapChanger(twt, rtcId, leg.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER);
                }
                i++;
            }
        }
    }

    private <C extends Connectable<C>> void writeTapChanger(C eq, String tcId, TapChanger<?, ?> tc, String defaultType) throws XMLStreamException {
        String type = CgmesExportUtil.cgmesTapChangerType(eq, tcId).orElse(defaultType);
        writeTapChanger(type, tcId, tc);

        CgmesTapChangers<C> cgmesTcs = eq.getExtension(CgmesTapChangers.class);
        CgmesTapChanger cgmesTc = null;
        if (cgmesTcs != null) {
            cgmesTc = cgmesTcs.getTapChanger(tcId);
        }
        addRegulatingControlView(tc, cgmesTc);
        if (cgmesTcs != null) {
            for (CgmesTapChanger tapChanger : cgmesTcs.getTapChangers()) {
                if (tapChanger.isHidden() && tapChanger.getCombinedTapChangerId().equals(tcId)) {
                    writeHiddenTapChanger(tapChanger, defaultType);
                }
            }
        }
    }

    private void writeShuntCompensators() throws XMLStreamException {
        for (ShuntCompensator s : context.getNetwork().getShuntCompensators()) {
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

            writeStartAbout(shuntType + "ShuntCompensator", context.getNamingStrategy().getCgmesId(s));
            xmlWriter.writeStartElement(cimNamespace, "ShuntCompensator.sections");
            xmlWriter.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "RegulatingCondEq.controlEnabled");
            xmlWriter.writeCharacters(Boolean.toString(controlEnabled));
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
            addRegulatingControlView(s);
        }
    }

    private void addRegulatingControlView(ShuntCompensator s) {
        if (s.hasProperty(REGULATING_CONTROL_PROPERTY)) {
            // PowSyBl has considered the control as discrete, with a certain targetDeadband
            // The target value is stored in kV by PowSyBl, so unit multiplier is "k"
            String rcid = s.getProperty(REGULATING_CONTROL_PROPERTY);
            RegulatingControlView rcv = new RegulatingControlView(rcid, RegulatingControlType.REGULATING_CONTROL, true,
                s.isVoltageRegulatorOn(), s.getTargetDeadband(), s.getTargetV(), "k");
            regulatingControlViews.computeIfAbsent(rcid, k -> new ArrayList<>()).add(rcv);
        }
    }

    private void writeSynchronousMachines() throws XMLStreamException {
        for (Generator g : context.getNetwork().getGenerators()) {
            boolean controlEnabled = g.isVoltageRegulatorOn();

            writeStartAbout("SynchronousMachine", context.getNamingStrategy().getCgmesId(g));
            xmlWriter.writeStartElement(cimNamespace, "RegulatingCondEq.controlEnabled");
            xmlWriter.writeCharacters(Boolean.toString(controlEnabled));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "RotatingMachine.p");
            xmlWriter.writeCharacters(CgmesExportUtil.format(-g.getTargetP()));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "RotatingMachine.q");
            xmlWriter.writeCharacters(CgmesExportUtil.format(-g.getTargetQ()));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "SynchronousMachine.referencePriority");
            // reference priority is used for angle reference selection (slack)
            xmlWriter.writeCharacters(isInSlackBus(g) ? "1" : "0");
            xmlWriter.writeEndElement();
            xmlWriter.writeEmptyElement(cimNamespace, "SynchronousMachine.operatingMode");
            // All generators in PowSyBl are considered as generator, not motor
            xmlWriter.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "SynchronousMachineOperatingMode.generator");
            xmlWriter.writeEndElement();

            addRegulatingControlView(g);
        }
    }

    private void addRegulatingControlView(Generator g) {
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

    private void writeStaticVarCompensators() throws XMLStreamException {
        for (StaticVarCompensator svc : context.getNetwork().getStaticVarCompensators()) {
            StaticVarCompensator.RegulationMode regulationMode = svc.getRegulationMode();
            boolean controlEnabled = regulationMode != StaticVarCompensator.RegulationMode.OFF;

            writeStartAbout("StaticVarCompensator", context.getNamingStrategy().getCgmesId(svc));
            xmlWriter.writeStartElement(cimNamespace, "RegulatingCondEq.controlEnabled");
            xmlWriter.writeCharacters(Boolean.toString(controlEnabled));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "StaticVarCompensator.q");
            xmlWriter.writeCharacters(CgmesExportUtil.format(svc.getTerminal().getQ()));
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();

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

    private void writeTapChanger(String type, String id, TapChanger<?, ?> tc) throws XMLStreamException {
        writeTapChanger(type, id, tc.isRegulating(), tc.getTapPosition());
    }

    private void writeTapChanger(String type, String id, boolean controlEnabled, int step) throws XMLStreamException {
        writeStartAbout(type, id);
        xmlWriter.writeStartElement(cimNamespace, "TapChanger.controlEnabled");
        xmlWriter.writeCharacters(Boolean.toString(controlEnabled));
        xmlWriter.writeEndElement();
        xmlWriter.writeStartElement(cimNamespace, "TapChanger.step");
        xmlWriter.writeCharacters(CgmesExportUtil.format(step));
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
    }

    private void addRegulatingControlView(TapChanger<?, ?> tc, CgmesTapChanger cgmesTc) {
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
                boolean valid;
                String unitMultiplier;
                switch (((PhaseTapChanger) tc).getRegulationMode()) {
                    case CURRENT_LIMITER:
                        // Unit multiplier is none (multiply by 1), regulation value is a current in Amperes
                        valid = true;
                        unitMultiplier = "none";
                        break;
                    case ACTIVE_POWER_CONTROL:
                        // Unit multiplier is M, regulation value is an active power flow in MW
                        valid = true;
                        unitMultiplier = "M";
                        break;
                    case FIXED_TAP:
                    default:
                        valid = false;
                        unitMultiplier = "none";
                        break;
                }
                if (valid) {
                    rcv = new RegulatingControlView(controlId,
                            RegulatingControlType.TAP_CHANGER_CONTROL,
                            true,
                            tc.isRegulating(),
                            tc.getTargetDeadband(),
                            ((PhaseTapChanger) tc).getRegulationValue(),
                            unitMultiplier);
                }
            }
            if (rcv != null) {
                regulatingControlViews.computeIfAbsent(controlId, k -> new ArrayList<>()).add(rcv);
            }
        }
    }

    private void writeHiddenTapChanger(CgmesTapChanger cgmesTc, String defaultType) throws XMLStreamException {
        writeTapChanger(Optional.ofNullable(cgmesTc.getType()).orElse(defaultType), cgmesTc.getId(), false,
                cgmesTc.getStep().orElseThrow(() -> new PowsyblException("Non null step expected for tap changer " + cgmesTc.getId())));
    }

    private void writeRegulatingControls() throws XMLStreamException {
        for (List<RegulatingControlView> views : regulatingControlViews.values()) {
            writeRegulatingControl(combineRegulatingControlViews(views));
        }
    }

    private RegulatingControlView combineRegulatingControlViews(List<RegulatingControlView> rcs) {
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

    private void writeRegulatingControl(RegulatingControlView rc) throws XMLStreamException {
        writeStartAbout(regulatingControlClassname(rc.type), rc.id);
        xmlWriter.writeStartElement(cimNamespace, "RegulatingControl.discrete");
        xmlWriter.writeCharacters(Boolean.toString(rc.discrete));
        xmlWriter.writeEndElement();
        xmlWriter.writeStartElement(cimNamespace, "RegulatingControl.enabled");
        xmlWriter.writeCharacters(Boolean.toString(rc.controlEnabled));
        xmlWriter.writeEndElement();
        xmlWriter.writeStartElement(cimNamespace, "RegulatingControl.targetDeadband");
        xmlWriter.writeCharacters(CgmesExportUtil.format(rc.targetDeadband));
        xmlWriter.writeEndElement();
        xmlWriter.writeStartElement(cimNamespace, "RegulatingControl.targetValue");
        xmlWriter.writeCharacters(CgmesExportUtil.format(rc.targetValue));
        xmlWriter.writeEndElement();
        xmlWriter.writeEmptyElement(cimNamespace, "RegulatingControl.targetValueUnitMultiplier");
        xmlWriter.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "UnitMultiplier." + rc.targetValueUnitMultiplier);
        xmlWriter.writeEndElement();
    }

    private static String regulatingControlClassname(RegulatingControlType type) {
        if (type == RegulatingControlType.TAP_CHANGER_CONTROL) {
            return "TapChangerControl";
        } else {
            return "RegulatingControl";
        }
    }

    private void writeSwitch(Switch sw) {
        try {
            writeStartAbout(switchClassname(sw.getKind()), context.getNamingStrategy().getCgmesId(sw));
            xmlWriter.writeStartElement(cimNamespace, "Switch.open");
            xmlWriter.writeCharacters(Boolean.toString(sw.isOpen()));
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
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

    private void writeTerminal(Terminal t) {
        writeTerminal(CgmesExportUtil.getTerminalId(t), t.isConnected());
    }

    private void writeTerminal(String terminalId, boolean connected) {
        try {
            writeStartAbout(CgmesNames.TERMINAL, terminalId);
            xmlWriter.writeStartElement(cimNamespace, "ACDCTerminal.connected");
            xmlWriter.writeCharacters(Boolean.toString(connected));
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeEquivalentInjection(DanglingLine dl) throws XMLStreamException {
        Optional<String> ei = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection");
        if (ei.isPresent()) {
            writeStartAbout("EquivalentInjection", ei.get());
            xmlWriter.writeStartElement(cimNamespace, "EquivalentInjection.p");
            xmlWriter.writeCharacters(CgmesExportUtil.format(dl.getP0()));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "EquivalentInjection.q");
            xmlWriter.writeCharacters(CgmesExportUtil.format(dl.getQ0()));
            xmlWriter.writeEndElement();
            // regulationStatus and regulationTarget are optional,
            // but test cases contain the attributes with disabled and 0
            boolean regulationStatus = false;
            double regulationTarget = 0;
            if (dl.getGeneration() != null) {
                regulationStatus = dl.getGeneration().isVoltageRegulationOn();
                regulationTarget = dl.getGeneration().getTargetV();
            }
            xmlWriter.writeStartElement(cimNamespace, "EquivalentInjection.regulationStatus");
            xmlWriter.writeCharacters(Boolean.toString(regulationStatus));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "EquivalentInjection.regulationTarget");
            xmlWriter.writeCharacters(CgmesExportUtil.format(regulationTarget));
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    private void writeEnergyConsumers() throws XMLStreamException {
        for (Load load : context.getNetwork().getLoads()) {
            if (context.isExportedEquipment(load)) {
                writeSshEnergyConsumer(context.getNamingStrategy().getCgmesId(load), load.getP0(), load.getQ0(), load.getExtension(LoadDetail.class));
            }
        }
    }

    private void writeSshEnergyConsumer(String id, double p, double q, LoadDetail loadDetail) throws XMLStreamException {
        writeStartAbout(CgmesExportUtil.loadClassName(loadDetail), id);
        xmlWriter.writeStartElement(cimNamespace, "EnergyConsumer.p");
        xmlWriter.writeCharacters(CgmesExportUtil.format(p));
        xmlWriter.writeEndElement();
        xmlWriter.writeStartElement(cimNamespace, "EnergyConsumer.q");
        xmlWriter.writeCharacters(CgmesExportUtil.format(q));
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
    }

    private void writeConverters() throws XMLStreamException {
        for (HvdcConverterStation<?> converterStation : context.getNetwork().getHvdcConverterStations()) {
            writeStartAbout(CgmesExportUtil.converterClassName(converterStation), context.getNamingStrategy().getCgmesId(converterStation));
            double ppcc;
            if (CgmesExportUtil.isConverterStationRectifier(converterStation)) {
                ppcc = converterStation.getHvdcLine().getActivePowerSetpoint();
            } else {
                double otherConverterStationLossFactor = converterStation.getOtherConverterStation().map(HvdcConverterStation::getLossFactor).orElse(0.0f);
                double pDCInverter = converterStation.getHvdcLine().getActivePowerSetpoint() * (1 - otherConverterStationLossFactor / 100);
                double poleLoss = converterStation.getLossFactor() / 100 * pDCInverter;
                ppcc = -(pDCInverter - poleLoss);
            }
            xmlWriter.writeStartElement(cimNamespace, "ACDCConverter.targetPpcc");
            xmlWriter.writeCharacters(CgmesExportUtil.format(ppcc));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement(cimNamespace, "ACDCConverter.targetUdc");
            xmlWriter.writeCharacters(CgmesExportUtil.format(0.0));
            xmlWriter.writeEndElement();
            if (converterStation instanceof LccConverterStation) {
                LccConverterStation lccConverterStation = (LccConverterStation) converterStation;

                writePandQ(ppcc, getQfromPowerFactor(ppcc, lccConverterStation.getPowerFactor()));
                xmlWriter.writeStartElement(cimNamespace, "CsConverter.targetAlpha");
                xmlWriter.writeCharacters(CgmesExportUtil.format(0));
                xmlWriter.writeEndElement();
                xmlWriter.writeStartElement(cimNamespace, "CsConverter.targetGamma");
                xmlWriter.writeCharacters(CgmesExportUtil.format(0));
                xmlWriter.writeEndElement();
                xmlWriter.writeStartElement(cimNamespace, "CsConverter.targetIdc");
                xmlWriter.writeCharacters(CgmesExportUtil.format(0));
                xmlWriter.writeEndElement();
                xmlWriter.writeEmptyElement(cimNamespace, "CsConverter.operatingMode");
                xmlWriter.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + converterOperatingMode(converterStation));
                xmlWriter.writeEmptyElement(cimNamespace, "CsConverter.pPccControl");
                xmlWriter.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "CsPpccControlKind.activePower");
            } else if (converterStation instanceof VscConverterStation) {
                VscConverterStation vscConverterStation = (VscConverterStation) converterStation;

                writePandQ(ppcc, vscConverterStation.getReactivePowerSetpoint());
                xmlWriter.writeStartElement(cimNamespace, "VsConverter.droop");
                xmlWriter.writeCharacters(CgmesExportUtil.format(0));
                xmlWriter.writeEndElement();
                xmlWriter.writeStartElement(cimNamespace, "VsConverter.droopCompensation");
                xmlWriter.writeCharacters(CgmesExportUtil.format(0));
                xmlWriter.writeEndElement();
                xmlWriter.writeStartElement(cimNamespace, "VsConverter.qShare");
                xmlWriter.writeCharacters(CgmesExportUtil.format(0));
                xmlWriter.writeEndElement();
                xmlWriter.writeStartElement(cimNamespace, "VsConverter.targetQpcc");
                xmlWriter.writeCharacters(CgmesExportUtil.format(vscConverterStation.getReactivePowerSetpoint()));
                xmlWriter.writeEndElement();
                xmlWriter.writeStartElement(cimNamespace, "VsConverter.targetUpcc");
                xmlWriter.writeCharacters(CgmesExportUtil.format(vscConverterStation.getVoltageSetpoint()));
                xmlWriter.writeEndElement();
                xmlWriter.writeEmptyElement(cimNamespace, "VsConverter.pPccControl");
                xmlWriter.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + converterOperatingMode(converterStation));
                xmlWriter.writeEmptyElement(cimNamespace, "VsConverter.qPccControl");
                xmlWriter.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "VsQpccControlKind." + (vscConverterStation.isVoltageRegulatorOn() ? "voltagePcc" : "reactivePcc"));
            }
            xmlWriter.writeEndElement();
        }
    }

    private void writePandQ(double p, double q) throws XMLStreamException {
        xmlWriter.writeStartElement(cimNamespace, "ACDCConverter.p");
        xmlWriter.writeCharacters(CgmesExportUtil.format(p));
        xmlWriter.writeEndElement();
        xmlWriter.writeStartElement(cimNamespace, "ACDCConverter.q");
        xmlWriter.writeCharacters(CgmesExportUtil.format(q));
        xmlWriter.writeEndElement();
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

    private void writeGeneratingUnitsParticitationFactors() throws XMLStreamException {
        // Multiple generators may share the same generation unit,
        // we will choose the participation factor from the last generator that references the generating unit
        // We only consider generators that have participation factors
        Map<String, GeneratingUnit> generatingUnits = new HashMap<>();
        for (Generator g : context.getNetwork().getGenerators()) {
            GeneratingUnit gu = generatingUnitForGenerator(g);
            if (gu != null) {
                generatingUnits.put(gu.id, gu);
            }
        }
        for (GeneratingUnit gu : generatingUnits.values()) {
            writeGeneratingUnitParticipationFactor(gu);
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

    private void writeGeneratingUnitParticipationFactor(GeneratingUnit gu) throws XMLStreamException {
        writeStartAbout(gu.className, gu.id);
        xmlWriter.writeStartElement(cimNamespace, "GeneratingUnit.normalPF");
        xmlWriter.writeCharacters(CgmesExportUtil.format(gu.participationFactor));
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
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
