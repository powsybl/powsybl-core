/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.iidm.network.ShuntCompensatorModelType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class ShuntCompensatorEq {

    private static final double BPERSECTION_FORMAT_THRESHOLD = 1E-10;

    private static final String EQ_SHUNTCOMPENSATOR_NORMALSECTIONS = "ShuntCompensator.normalSections";
    private static final String EQ_SHUNTCOMPENSATOR_MAXIMUMSECTIONS = "ShuntCompensator.maximumSections";
    private static final String EQ_SHUNTCOMPENSATOR_NOMU = "ShuntCompensator.nomU";

    private static final String EQ_LINEARSHUNTCOMPENSATOR_BPERSECTION = "LinearShuntCompensator.bPerSection";
    private static final String EQ_LINEARSHUNTCOMPENSATOR_GPERSECTION = "LinearShuntCompensator.gPerSection";

    private static final String EQ_NONLINEARSHUNTCOMPENSATOR_SECTIONNUMBER = "NonlinearShuntCompensatorPoint.sectionNumber";
    private static final String EQ_NONLINEARSHUNTCOMPENSATOR_B = "NonlinearShuntCompensatorPoint.b";
    private static final String EQ_NONLINEARSHUNTCOMPENSATOR_G = "NonlinearShuntCompensatorPoint.g";

    public static void write(String id, String shuntCompensatorName, int normalSections, int maximumSections, double nomU, ShuntCompensatorModelType modelType, double bPerSection, double gPerSection, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(shuntCompensatorModelClassName(modelType), id, shuntCompensatorName, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, EQ_SHUNTCOMPENSATOR_NORMALSECTIONS);
        writer.writeCharacters(CgmesExportUtil.format(normalSections));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_SHUNTCOMPENSATOR_MAXIMUMSECTIONS);
        writer.writeCharacters(CgmesExportUtil.format(maximumSections));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_SHUNTCOMPENSATOR_NOMU);
        writer.writeCharacters(CgmesExportUtil.format(nomU));
        writer.writeEndElement();
        if (modelType.equals(ShuntCompensatorModelType.LINEAR)) {
            writer.writeStartElement(cimNamespace, EQ_LINEARSHUNTCOMPENSATOR_BPERSECTION);
            if (Math.abs(bPerSection) < BPERSECTION_FORMAT_THRESHOLD) {
                writer.writeCharacters(CgmesExportUtil.scientificFormat(bPerSection));
            } else {
                writer.writeCharacters(CgmesExportUtil.format(bPerSection));
            }
            writer.writeEndElement();
            if (!Double.isNaN(gPerSection)) {
                writer.writeStartElement(cimNamespace, EQ_LINEARSHUNTCOMPENSATOR_GPERSECTION);
                writer.writeCharacters(CgmesExportUtil.format(gPerSection));
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    public static void writePoint(String id, String shuntId, int sectionNumber, double b, double g, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartId("NonlinearShuntCompensatorPoint", id, false, cimNamespace, writer);
        CgmesExportUtil.writeReference("NonlinearShuntCompensatorPoint.NonlinearShuntCompensator", shuntId, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, EQ_NONLINEARSHUNTCOMPENSATOR_SECTIONNUMBER);
        writer.writeCharacters(CgmesExportUtil.format(sectionNumber));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_NONLINEARSHUNTCOMPENSATOR_B);
        writer.writeCharacters(CgmesExportUtil.format(b));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_NONLINEARSHUNTCOMPENSATOR_G);
        writer.writeCharacters(CgmesExportUtil.format(g));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String shuntCompensatorModelClassName(ShuntCompensatorModelType modelType) {
        if (ShuntCompensatorModelType.LINEAR.equals(modelType)) {
            return "LinearShuntCompensator";
        } else if (ShuntCompensatorModelType.NON_LINEAR.equals(modelType)) {
            return "NonlinearShuntCompensator";
        }
        return "LinearShuntCompensator";
    }

    private ShuntCompensatorEq() {
    }
}
