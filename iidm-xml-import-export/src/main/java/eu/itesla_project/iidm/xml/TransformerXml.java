/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class TransformerXml<T extends Connectable, A extends IdentifiableAdder<A>> extends ConnectableXml<T, A, Substation> {

    protected static void writeTapChangerStep(TapChangerStep<?> tcs, XMLStreamWriter writer) throws XMLStreamException {
        writeFloat("r", tcs.getR(), writer);
        writeFloat("x", tcs.getX(), writer);
        writeFloat("g", tcs.getG(), writer);
        writeFloat("b", tcs.getB(), writer);
        writeFloat("rho", tcs.getRho(), writer);
    }

    protected static void writeTapChanger(TapChanger<?, ?> tc, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute("lowStepPosition", Integer.toString(tc.getLowStepPosition()));
        writer.writeAttribute("position", Integer.toString(tc.getCurrentStepPosition()));
        writer.writeAttribute("regulating", Boolean.toString(tc.isRegulating()));
    }

    protected static void writeRatioTapChanger(String name, RatioTapChanger rtc, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(IIDM_URI, name);
        writeTapChanger(rtc, context.getWriter());
        context.getWriter().writeAttribute("loadTapChangingCapabilities", Boolean.toString(rtc.hasLoadTapChangingCapabilities()));
        writeFloat("targetV", rtc.getTargetV(), context.getWriter());
        if (rtc.getTerminal() != null) {
            writeTerminalRef(rtc.getTerminal(), context, "terminalRef");
        }
        for (int p = rtc.getLowStepPosition(); p <= rtc.getHighStepPosition(); p++) {
            RatioTapChangerStep rtcs = rtc.getStep(p);
            context.getWriter().writeEmptyElement(IIDM_URI, "step");
            writeTapChangerStep(rtcs, context.getWriter());
        }
        context.getWriter().writeEndElement();
    }

    protected static void readRatioTapChanger(TwoWindingsTransformer twt, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        int lowStepPosition = readIntAttribute(reader, "lowStepPosition");
        int position = readIntAttribute(reader, "position");
        boolean regulating = readBoolAttribute(reader, "regulating");
        boolean loadTapChangingCapabilities = readBoolAttribute(reader, "loadTapChangingCapabilities");
        float targetV = readOptionalFloatAttribute(reader, "targetV");
        RatioTapChangerAdder adder = twt.newRatioTapChanger()
                .setLowStepPosition(lowStepPosition)
                .setCurrentStepPosition(position)
                .setLoadTapChangingCapabilities(loadTapChangingCapabilities)
                .setTargetV(targetV);
        if (loadTapChangingCapabilities) {
            adder.setRegulating(regulating);
        }
        boolean[] hasTerminalRef = new boolean[1];
        XmlUtil.readUntilEndElement("ratioTapChanger", reader, () -> {
            switch (reader.getLocalName()) {
                case "terminalRef":
                    String id = reader.getAttributeValue(null, "id");
                    String side = reader.getAttributeValue(null, "side");
                    endTasks.add(() ->  {
                        adder.setTerminal(readTerminalRef(twt.getTerminal1().getVoltageLevel().getSubstation().getNetwork(), id, side));
                        adder.add();
                    });
                    hasTerminalRef[0] = true;
                    break;

                case "step":
                    float r = readFloatAttribute(reader, "r");
                    float x = readFloatAttribute(reader, "x");
                    float g = readFloatAttribute(reader, "g");
                    float b = readFloatAttribute(reader, "b");
                    float rho = readFloatAttribute(reader, "rho");
                    adder.beginStep()
                            .setR(r)
                            .setX(x)
                            .setG(g)
                            .setB(b)
                            .setRho(rho)
                            .endStep();
                    break;

                default:
                    throw new AssertionError();
            }
        });
        if (!hasTerminalRef[0]) {
            adder.add();
        }
    }

    protected static void writePhaseTapChanger(String name, PhaseTapChanger ptc, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(IIDM_URI, name);
        writeTapChanger(ptc, context.getWriter());
        writeFloat("thresholdI", ptc.getThresholdI(), context.getWriter());
        if (ptc.getTerminal() != null) {
            writeTerminalRef(ptc.getTerminal(), context, "terminalRef");
        }
        for (int p = ptc.getLowStepPosition(); p <= ptc.getHighStepPosition(); p++) {
            PhaseTapChangerStep ptcs = ptc.getStep(p);
            context.getWriter().writeEmptyElement(IIDM_URI, "step");
            writeTapChangerStep(ptcs, context.getWriter());
            writeFloat("alpha", ptcs.getAlpha(), context.getWriter());
        }
        context.getWriter().writeEndElement();
    }

    protected static void readPhaseTapChanger(TwoWindingsTransformer twt, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        int lowStepPosition = readIntAttribute(reader, "lowStepPosition");
        int position = readIntAttribute(reader, "position");
        boolean regulating = readBoolAttribute(reader, "regulating");
        float thresholdI = readOptionalFloatAttribute(reader, "thresholdI");
        PhaseTapChangerAdder adder = twt.newPhaseTapChanger()
                .setLowStepPosition(lowStepPosition)
                .setCurrentStepPosition(position)
                .setRegulating(regulating)
                .setThresholdI(thresholdI);
        boolean[] hasTerminalRef = new boolean[1];
        XmlUtil.readUntilEndElement("phaseTapChanger", reader, () -> {
            switch (reader.getLocalName()) {
                case "terminalRef":
                    String id = reader.getAttributeValue(null, "id");
                    String side = reader.getAttributeValue(null, "side");
                    endTasks.add(() ->  {
                        adder.setTerminal(readTerminalRef(twt.getTerminal1().getVoltageLevel().getSubstation().getNetwork(), id, side));
                        adder.add();
                    });
                    hasTerminalRef[0] = true;
                    break;

                case "step":
                    float r = readFloatAttribute(reader, "r");
                    float x = readFloatAttribute(reader, "x");
                    float g = readFloatAttribute(reader, "g");
                    float b = readFloatAttribute(reader, "b");
                    float rho = readFloatAttribute(reader, "rho");
                    float alpha = readFloatAttribute(reader, "alpha");
                    adder.beginStep()
                            .setR(r)
                            .setX(x)
                            .setG(g)
                            .setB(b)
                            .setRho(rho)
                            .setAlpha(alpha)
                            .endStep();
                    break;

                default:
                    throw new AssertionError();
            }
        });
        if (!hasTerminalRef[0]) {
            adder.add();
        }
    }
}