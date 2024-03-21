/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class TapChangerEq {

    private static final String EQ_PHASETAPCHANGER_TRANSFORMEREND = "PhaseTapChanger.TransformerEnd";
    private static final String EQ_PHASETAPCHANGERTABULAR_PHASETAPCHANGERTABLE = "PhaseTapChangerTabular.PhaseTapChangerTable";

    private static final String EQ_RATIOTAPCHANGER_TRANSFORMEREND = "RatioTapChanger.TransformerEnd";
    private static final String EQ_RATIOTAPCHANGER_RATIOTAPCHANGERTABLE = "RatioTapChanger.RatioTapChangerTable";
    private static final String EQ_RATIOTAPCHANGER_SVI = "RatioTapChanger.stepVoltageIncrement";

    private static final String EQ_TAPCHANGER_LOWSTEP = "TapChanger.lowStep";
    private static final String EQ_TAPCHANGER_HIGHSTEP = "TapChanger.highStep";
    private static final String EQ_TAPCHANGER_NORMALSTEP = "TapChanger.normalStep";
    private static final String EQ_TAPCHANGER_NEUTRALSTEP = "TapChanger.neutralStep";
    private static final String EQ_TAPCHANGER_NEUTRALU = "TapChanger.neutralU";
    private static final String EQ_TAPCHANGER_LTCFLAG = "TapChanger.ltcFlag";

    private static final String EQ_TAPCHANGERTABLEPOINT_STEP = "TapChangerTablePoint.step";
    private static final String EQ_TAPCHANGERTABLEPOINT_R = "TapChangerTablePoint.r";
    private static final String EQ_TAPCHANGERTABLEPOINT_X = "TapChangerTablePoint.x";
    private static final String EQ_TAPCHANGERTABLEPOINT_G = "TapChangerTablePoint.g";
    private static final String EQ_TAPCHANGERTABLEPOINT_B = "TapChangerTablePoint.b";
    private static final String EQ_TAPCHANGERTABLEPOINT_RATIO = "TapChangerTablePoint.ratio";

    private static final String EQ_PHASETAPCHANGERTABLEPOINT_ANGLE = "PhaseTapChangerTablePoint.angle";
    private static final String EQ_PHASETAPCHANGERTABLEPOINT_PHASETAPCHANGERTABLE = "PhaseTapChangerTablePoint.PhaseTapChangerTable";

    private static final String EQ_RATIOTAPCHANGERTABLEPOINT_RATIOTAPCHANGERTABLE = "RatioTapChangerTablePoint.RatioTapChangerTable";

    public static void writePhase(String type, String id, String tapChangerName, String transformerEndId, double lowStep, double highStep, double neutralStep, double normalStep,
                                  double neutralU, boolean ltcFlag, String phaseTapChangerTableId, String cgmesRegulatingControlId, String cimNamespace, XMLStreamWriter writer,
                                  CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(type, id, tapChangerName, cimNamespace, writer, context);
        CgmesExportUtil.writeReference(EQ_PHASETAPCHANGER_TRANSFORMEREND, transformerEndId, cimNamespace, writer, context);
        writeSteps(lowStep, highStep, neutralStep, normalStep, neutralU, ltcFlag, cimNamespace, writer);
        CgmesExportUtil.writeReference(EQ_PHASETAPCHANGERTABULAR_PHASETAPCHANGERTABLE, phaseTapChangerTableId, cimNamespace, writer, context);
        if (cgmesRegulatingControlId != null) {
            CgmesExportUtil.writeReference("TapChanger.TapChangerControl", cgmesRegulatingControlId, cimNamespace, writer, context);
        }
        writer.writeEndElement();
    }

    public static void writePhaseTable(String id, String phaseTapChangerTableName, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("PhaseTapChangerTable", id, phaseTapChangerTableName, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    public static void writePhaseTablePoint(String id, String phaseTapChangerTableId, double r, double x, double g, double b,
                                            double ratio, double angle, Integer step, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId("PhaseTapChangerTablePoint", id, false, cimNamespace, writer, context);
        writeTablePoint(r, x, g, b, ratio, step, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, EQ_PHASETAPCHANGERTABLEPOINT_ANGLE);
        writer.writeCharacters(CgmesExportUtil.format(angle));
        writer.writeEndElement();
        CgmesExportUtil.writeReference(EQ_PHASETAPCHANGERTABLEPOINT_PHASETAPCHANGERTABLE, phaseTapChangerTableId, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    public static void writeRatio(String id, String tapChangerName, String transformerEndId, double lowStep, double highStep, double neutralStep, double normalStep,
                                  double neutralU, boolean ltcFlag, double stepVoltageIncrement, String ratioTapChangerTableId, String cgmesRegulatingControlId,
                                  String controlMode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("RatioTapChanger", id, tapChangerName, cimNamespace, writer, context);
        CgmesExportUtil.writeReference(EQ_RATIOTAPCHANGER_TRANSFORMEREND, transformerEndId, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, EQ_RATIOTAPCHANGER_SVI);
        writer.writeCharacters(CgmesExportUtil.format(stepVoltageIncrement));
        writer.writeEndElement();
        writeSteps(lowStep, highStep, neutralStep, normalStep, neutralU, ltcFlag, cimNamespace, writer);
        if (context.getCim().writeTculControlMode()) {
            writer.writeEmptyElement(cimNamespace, "RatioTapChanger.tculControlMode");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s.%s", cimNamespace, "TransformerControlMode", controlMode));
        }
        CgmesExportUtil.writeReference(EQ_RATIOTAPCHANGER_RATIOTAPCHANGERTABLE, ratioTapChangerTableId, cimNamespace, writer, context);
        if (cgmesRegulatingControlId != null) {
            CgmesExportUtil.writeReference("TapChanger.TapChangerControl", cgmesRegulatingControlId, cimNamespace, writer, context);
        }
        writer.writeEndElement();
    }

    public static void writeControl(String id, String name, String mode, String terminalId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("TapChangerControl", id, name, cimNamespace, writer, context);
        writer.writeEmptyElement(cimNamespace, "RegulatingControl.mode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s.%s", cimNamespace, "RegulatingControlModeKind", mode));
        CgmesExportUtil.writeReference("RegulatingControl.Terminal", terminalId, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    public static void writeSteps(double lowStep, double highStep, double neutralStep, double normalStep, double neutralU, boolean ltcFlag, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_LOWSTEP);
        writer.writeCharacters(CgmesExportUtil.format(lowStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_HIGHSTEP);
        writer.writeCharacters(CgmesExportUtil.format(highStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_NORMALSTEP);
        writer.writeCharacters(CgmesExportUtil.format(normalStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_NEUTRALSTEP);
        writer.writeCharacters(CgmesExportUtil.format(neutralStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_NEUTRALU);
        writer.writeCharacters(CgmesExportUtil.format(neutralU));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_LTCFLAG);
        writer.writeCharacters(CgmesExportUtil.format(ltcFlag));
        writer.writeEndElement();
    }

    public static void writeRatioTable(String id, String ratioTapChangerTableName, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("RatioTapChangerTable", id, ratioTapChangerTableName, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    public static void writeRatioTablePoint(String id, String ratioTapChangerTableId, double r, double x, double g, double b,
                                            double ratio, Integer step, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId("RatioTapChangerTablePoint", id, false, cimNamespace, writer, context);
        writeTablePoint(r, x, g, b, ratio, step, cimNamespace, writer);
        CgmesExportUtil.writeReference(EQ_RATIOTAPCHANGERTABLEPOINT_RATIOTAPCHANGERTABLE, ratioTapChangerTableId, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    public static void writeTablePoint(double r, double x, double g, double b, double ratio, Integer step, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_R);
        writer.writeCharacters(CgmesExportUtil.format(r));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_X);
        writer.writeCharacters(CgmesExportUtil.format(x));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_G);
        writer.writeCharacters(CgmesExportUtil.format(g));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_B);
        writer.writeCharacters(CgmesExportUtil.format(b));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_STEP);
        writer.writeCharacters(CgmesExportUtil.format(step));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_RATIO);
        writer.writeCharacters(CgmesExportUtil.format(ratio));
        writer.writeEndElement();
    }

    private TapChangerEq() {
    }
}
