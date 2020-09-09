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
            writeTerminals(network, writer);

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
                String ptcId = twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(CgmesNames.PHASE_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeTapChanger("Phase", ptcId, twt.getPhaseTapChanger(), writer);
            } else if (twt.hasRatioTapChanger()) {
                String rtcId = twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
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
