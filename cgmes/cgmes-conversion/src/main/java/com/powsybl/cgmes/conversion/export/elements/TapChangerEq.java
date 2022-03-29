/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
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

    public static void writePhase(String id, String tapChangerName, String transformerEndId, double lowStep, double highStep, double neutralStep, double normalStep, double neutralU, boolean ltcFlag, String phaseTapChangerTableId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "PhaseTapChangerTabular");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(tapChangerName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_PHASETAPCHANGER_TRANSFORMEREND);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + transformerEndId);
        writeSteps(lowStep, highStep, neutralStep, normalStep, neutralU, ltcFlag, cimNamespace, writer);
        writer.writeEmptyElement(cimNamespace, EQ_PHASETAPCHANGERTABULAR_PHASETAPCHANGERTABLE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + phaseTapChangerTableId);
        writer.writeEndElement();
    }

    public static void writePhaseTable(String id, String phaseTapChangerTableName, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "PhaseTapChangerTable");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(phaseTapChangerTableName);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    public static void writePhaseTablePoint(String id, String phaseTapChangerTableId, double r, double x, double g, double b, double ratio, double angle, Integer step, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "PhaseTapChangerTablePoint");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writeTablePoint(r, x, g, b, ratio, step, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, EQ_PHASETAPCHANGERTABLEPOINT_ANGLE);
        writer.writeCharacters(CgmesExportUtil.format(angle));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_PHASETAPCHANGERTABLEPOINT_PHASETAPCHANGERTABLE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + phaseTapChangerTableId);
        writer.writeEndElement();
    }

    public static void writeRatio(String id, String tapChangerName, String transformerEndId, double lowStep, double highStep, double neutralStep, double normalStep, double neutralU, boolean ltcFlag, double stepVoltageIncrement, String ratioTapChangerTableId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "RatioTapChanger");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(tapChangerName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_RATIOTAPCHANGER_TRANSFORMEREND);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + transformerEndId);
        writer.writeStartElement(cimNamespace, EQ_RATIOTAPCHANGER_SVI);
        writer.writeCharacters(CgmesExportUtil.format(stepVoltageIncrement));
        writer.writeEndElement();
        writeSteps(lowStep, highStep, neutralStep, normalStep, neutralU, ltcFlag, cimNamespace, writer);
        writer.writeEmptyElement(cimNamespace, EQ_RATIOTAPCHANGER_RATIOTAPCHANGERTABLE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + ratioTapChangerTableId);
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

    public static void writeRatioTable(String id, String ratioTapChangerTableName, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "RatioTapChangerTable");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(ratioTapChangerTableName);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    public static void writeRatioTablePoint(String id, String ratioTapChangerTableId, double r, double x, double g, double b, double ratio, Integer step, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "RatioTapChangerTablePoint");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writeTablePoint(r, x, g, b, ratio, step, cimNamespace, writer);
        writer.writeEmptyElement(cimNamespace, EQ_RATIOTAPCHANGERTABLEPOINT_RATIOTAPCHANGERTABLE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + ratioTapChangerTableId);
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
