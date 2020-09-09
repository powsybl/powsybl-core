/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at rte-france.com>
 */
public final class SteadyStateHypothesisExport {

    private static final Logger LOG = LoggerFactory.getLogger(SteadyStateHypothesisExport.class);

    public static void write(Network network, XMLStreamWriter writer) {
        try {
            CgmesExport.writeRdfRoot(writer);

            // FIXME(Luma) work in progress
            LOG.info("work in progress ...");
            if ("16".equals(network.getProperty(CgmesExport.CIM_VERSION))) {
                writeModelDescription(network, writer);
            }
            writeEnergyConsumers(network, writer);
            writeEquivalentInjections(network, writer);
            writeTapChangers(network, writer);
            writeShuntCompensators(network, writer);
            writeSynchronousMachines(network, writer);
            writeTerminals(network, writer);
            // FIXME(Luma) pending: regulating controls for transformers (RegulatingControl)
            // FIXME(Luma) pending: control areas (ControlArea) and generating unit participation factors (GeneratingUnit.normalPF)

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeModelDescription(Network network, XMLStreamWriter writer) throws XMLStreamException {
        // FIXME(Luma) pending, most of the information is common with SvMetaData ...
        // We should have a single extension and multiple instances of MetaData (SV, SSH, ...)
    }

    private static void writeTerminals(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (Connectable<?> c : network.getConnectables()) {
            for (Terminal t : c.getTerminals()) {
                writeTerminal(t, c, writer);
            }
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            // Terminal for equivalent injection at boundary is always connected
            dl.getAliasFromType("EquivalentInjectionTerminal").ifPresent(tid -> {
                writeTerminal(tid, true, writer);
            });
            // Terminal for boundary side of original line/switch is always connected
            dl.getAliasFromType(CgmesNames.TERMINAL + dl.getProperty("boundarySide")).ifPresent(tid -> {
                writeTerminal(tid, true, writer);
            });
        }
    }

    private static void writeEquivalentInjections(Network network, XMLStreamWriter writer) throws XMLStreamException {
        // One equivalent injection for every dangling line
        for (DanglingLine dl : network.getDanglingLines()) {
            writeEquivalentInjection(dl, writer);
        }
    }

    private static void writeTapChangers(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String ptcId = twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + 1)
                        .orElseGet(() -> twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeTapChanger("Phase", ptcId, twt.getPhaseTapChanger(), writer);
            } else if (twt.hasRatioTapChanger()) {
                String rtcId = twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + 1)
                        .orElseGet(() -> twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeTapChanger("Ratio", rtcId, twt.getRatioTapChanger(), writer);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int i = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeTapChanger("Phase", ptcId, leg.getPhaseTapChanger(), writer);
                } else if (leg.hasRatioTapChanger()) {
                    String rtcId = twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeTapChanger("Ratio", rtcId, leg.getRatioTapChanger(), writer);
                }
                i++;
            }
        }
    }

    private static void writeShuntCompensators(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            String linearNonlinear;
            switch (s.getModelType()) {
                case LINEAR:
                    linearNonlinear = "Linear";
                    break;
                case NON_LINEAR:
                    linearNonlinear = "NonLinear";
                    break;
                default:
                    linearNonlinear = "";
                    break;
            }
            boolean controlEnabled = s.isVoltageRegulatorOn();
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, linearNonlinear + "ShuntCompensator");
            writer.writeAttribute(CgmesExport.RDF_NAMESPACE, "about", "#" + s.getId());
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "ShuntCompensator.sections");
            writer.writeCharacters(CgmesExport.format(s.getSectionCount()));
            writer.writeEndElement();
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "RegulatingCondEq.controlEnabled");
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeEndElement();

            if (s.hasProperty("RegulatingControl")) {
                // PowSyBl has considered the control as discrete, with a certain targetDeadband
                // The target value is stored in kV by PowSyBl, so unit multiplier is "k"
                writeRegulatingControl(s.getProperty("RegulatingControl"), true, s.isVoltageRegulatorOn(), s.getTargetDeadband(), s.getTargetV(), "k", writer);
            }
        }
    }

    private static void writeSynchronousMachines(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (Generator g : network.getGenerators()) {
            boolean controlEnabled = g.isVoltageRegulatorOn();
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "SynchronousMachine");
            writer.writeAttribute(CgmesExport.RDF_NAMESPACE, "about", "#" + g.getId());
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "RegulatingCondEq.controlEnabled");
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "RotatingMachine.p");
            writer.writeCharacters(CgmesExport.format(g.getTerminal().getP()));
            writer.writeEndElement();
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "RotatingMachine.q");
            writer.writeCharacters(CgmesExport.format(g.getTerminal().getQ()));
            writer.writeEndElement();
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "SynchronousMachine.referencePriority");
            // reference priority is used for angle reference selection (slack)
            writer.writeCharacters(isInSlackBus(g) ? "1" : "0");
            writer.writeEndElement();
            writer.writeEmptyElement(CgmesExport.CIM_NAMESPACE, "SynchronousMachine.operatingMode");
            // All generators in PowSyBl are considered as generator, not motor
            writer.writeAttribute(CgmesExport.RDF_NAMESPACE, CgmesNames.RESOURCE, CgmesExport.CIM_NAMESPACE + "SynchronousMachineOperatingMode.generator");
            writer.writeEndElement();

            if (g.hasProperty("RegulatingControl")) {
                // PowSyBl has considered the control as continuous and with targetDeadband of size 0
                // The target value is stored in kV by PowSyBl, so unit multiplier is "k"
                writeRegulatingControl(g.getProperty("RegulatingControl"), false, g.isVoltageRegulatorOn(), 0, g.getTargetV(), "k", writer);
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

    private static void writeRegulatingControl(String id, boolean discrete, boolean controlEnabled, double targetDeadband, double targetValue, String targetValueUnitMultiplier, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "RegulatingControl");
        writer.writeAttribute(CgmesExport.RDF_NAMESPACE, "about", "#" + id);
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "RegulatingControl.discrete");
        writer.writeCharacters(Boolean.toString(discrete));
        writer.writeEndElement();
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "RegulatingControl.enabled");
        writer.writeCharacters(Boolean.toString(controlEnabled));
        writer.writeEndElement();
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "RegulatingControl.targetDeadband");
        writer.writeCharacters(CgmesExport.format(targetDeadband));
        writer.writeEndElement();
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "RegulatingControl.targetValue");
        writer.writeCharacters(CgmesExport.format(targetValue));
        writer.writeEndElement();
        writer.writeEmptyElement(CgmesExport.CIM_NAMESPACE, "RegulatingControl.targetValueUnitMultiplier");
        writer.writeAttribute(CgmesExport.RDF_NAMESPACE, "resource", CgmesExport.CIM_NAMESPACE + "UnitMultiplier." + targetValueUnitMultiplier);
        writer.writeEndElement();
    }

    private static void writeTapChanger(String ratioPhase, String id, TapChanger<?, ?> tc, XMLStreamWriter writer) throws XMLStreamException {
        writeTapChanger(ratioPhase, id, tc.isRegulating(), tc.getTapPosition(), writer);
    }

    private static void writeTapChanger(String ratioPhase, String id, boolean controlEnabled, int step, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, ratioPhase + CgmesNames.TAP_CHANGER);
        writer.writeAttribute(CgmesExport.RDF_NAMESPACE, "about", "#" + id);
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "TapChanger.controlEnabled");
        writer.writeCharacters(Boolean.toString(controlEnabled));
        writer.writeEndElement();
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "TapChanger.step");
        writer.writeCharacters(CgmesExport.format(step));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeTerminal(Terminal t, Connectable<?> c, XMLStreamWriter writer) {
        int numt = 0;
        if (c.getTerminals().size() == 1) {
            numt = 1;
            if (c instanceof DanglingLine) {
                int boundarySide = Integer.valueOf(c.getProperty("boundarySide"));
                int modelSide = 3 - boundarySide;
                numt = modelSide;
            }
        } else {
            if (c instanceof Injection) {
                // An injection should have only one terminal
            } else if (c instanceof Branch) {
                switch (((Branch<?>) c).getSide(t)) {
                    case ONE:
                        numt = 1;
                        break;
                    case TWO:
                        numt = 2;
                        break;
                }
            } else if (c instanceof ThreeWindingsTransformer) {
                switch (((ThreeWindingsTransformer) c).getSide(t)) {
                    case ONE:
                        numt = 1;
                        break;
                    case TWO:
                        numt = 2;
                        break;
                    case THREE:
                        numt = 3;
                        break;
                }
            } else {
                throw new PowsyblException("Unexpected Connectable instance: " + c.getClass());
            }
        }
        if (numt > 0) {
            Optional<String> tid = c.getAliasFromType(CgmesNames.TERMINAL + numt);
            if (tid.isPresent()) {
                writeTerminal(tid.get(), t.isConnected(), writer);
            } else {
                LOG.error("Alias not found for terminal {} in connectable {}", numt, c.getId());
            }
        } else {
            LOG.error("Num terminal not found for connectable {}", c.getId());
        }
    }

    private static void writeTerminal(String terminalId, boolean connected, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, CgmesNames.TERMINAL);
            writer.writeAttribute(CgmesExport.RDF_NAMESPACE, "about", "#" + terminalId);
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "ACDCTerminal.connected");
            writer.writeCharacters(Boolean.toString(connected));
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeEquivalentInjection(DanglingLine dl, XMLStreamWriter writer) throws XMLStreamException {
        Optional<String> ei = dl.getAliasFromType("EquivalentInjection");
        if (ei.isPresent()) {
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EquivalentInjection");
            writer.writeAttribute(CgmesExport.RDF_NAMESPACE, "about", "#" + ei.get());
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EquivalentInjection.p");
            writer.writeCharacters(CgmesExport.format(dl.getP0()));
            writer.writeEndElement();
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EquivalentInjection.q");
            writer.writeCharacters(CgmesExport.format(dl.getQ0()));
            writer.writeEndElement();
            // regulationStatus and regulationTarget are optional,
            // but test cases contain the attributes with disabled and 0
            boolean regulationStatus = false;
            double regulationTarget = 0;
            if (dl.getGeneration() != null) {
                regulationStatus = dl.getGeneration().isVoltageRegulationOn();
                regulationTarget = dl.getGeneration().getTargetV();
            }
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EquivalentInjection.regulationStatus");
            writer.writeCharacters(Boolean.toString(regulationStatus));
            writer.writeEndElement();
            writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EquivalentInjection.regulationTarget");
            writer.writeCharacters(CgmesExport.format(regulationTarget));
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static void writeEnergyConsumers(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            writeSshEnergyConsumer(writer, load.getId(), load.getP0(), load.getQ0());
        }
    }

    private static void writeSshEnergyConsumer(XMLStreamWriter writer, String id, double p, double q) throws XMLStreamException {
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EnergyConsumer");
        writer.writeAttribute(CgmesExport.RDF_NAMESPACE, "about", "#" + id);
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EnergyConsumer.p");
        writer.writeCharacters(CgmesExport.format(p));
        writer.writeEndElement();
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EnergyConsumer.q");
        writer.writeCharacters(CgmesExport.format(q));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private SteadyStateHypothesisExport() {
    }
}
