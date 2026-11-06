/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesShuntCompensator {

    private static final double B_PER_SECTION_FORMAT_THRESHOLD = 1E-10;

    private static final String SHUNT_COMPENSATOR_NORMAL_SECTIONS = SHUNT_COMPENSATOR + "." + NORMAL_SECTIONS;
    private static final String SHUNT_COMPENSATOR_MAXIMUM_SECTIONS = SHUNT_COMPENSATOR + ".maximumSections";
    private static final String SHUNT_COMPENSATOR_NOM_U = SHUNT_COMPENSATOR + ".nomU";
    private static final String LINEAR_SHUNT_COMPENSATOR_B_PER_SECTION = LINEAR_SHUNT_COMPENSATOR + "." + B_PER_SECTION;
    private static final String LINEAR_SHUNT_COMPENSATOR_G_PER_SECTION = LINEAR_SHUNT_COMPENSATOR + ".gPerSection";
    private static final String NONLINEAR_SHUNT_COMPENSATOR_POINT_NONLINEAR_SHUNT_COMPENSATOR = NONLINEAR_SHUNT_COMPENSATOR_POINT + "." + NONLINEAR_SHUNT_COMPENSATOR;
    private static final String NONLINEAR_SHUNT_COMPENSATOR_POINT_SECTION_NUMBER = NONLINEAR_SHUNT_COMPENSATOR_POINT + ".sectionNumber";
    private static final String NONLINEAR_SHUNT_COMPENSATOR_POINT_B = NONLINEAR_SHUNT_COMPENSATOR_POINT + "." + B;
    private static final String NONLINEAR_SHUNT_COMPENSATOR_POINT_G = NONLINEAR_SHUNT_COMPENSATOR_POINT + "." + G;

    private CgmesShuntCompensator() {
    }

    public static void writeEq(ShuntCompensatorEq shuntCompensator, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(shuntCompensator.className, shuntCompensator.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(shuntCompensator.name), writer, context);
        CgmesExportUtil.writeAttribute(SHUNT_COMPENSATOR_NORMAL_SECTIONS, CgmesExportUtil.format(shuntCompensator.normalSections), writer, context);
        CgmesExportUtil.writeAttribute(SHUNT_COMPENSATOR_MAXIMUM_SECTIONS, CgmesExportUtil.format(shuntCompensator.maximumSections), writer, context);
        CgmesExportUtil.writeAttribute(SHUNT_COMPENSATOR_NOM_U, CgmesExportUtil.format(shuntCompensator.nomU), writer, context);
        if (LINEAR_SHUNT_COMPENSATOR.equals(shuntCompensator.className)) {
            String bPerSection = Math.abs(shuntCompensator.bPerSection) < B_PER_SECTION_FORMAT_THRESHOLD
                ? CgmesExportUtil.scientificFormat(shuntCompensator.bPerSection)
                : CgmesExportUtil.format(shuntCompensator.bPerSection);
            CgmesExportUtil.writeAttribute(LINEAR_SHUNT_COMPENSATOR_B_PER_SECTION, bPerSection, writer, context);
            String gPerSection = !Double.isNaN(shuntCompensator.gPerSection) ? CgmesExportUtil.format(shuntCompensator.gPerSection) : "0";
            CgmesExportUtil.writeAttribute(LINEAR_SHUNT_COMPENSATOR_G_PER_SECTION, gPerSection, writer, context);
        }
        if (shuntCompensator.regulatingControl != null) {
            CgmesExportUtil.writeResource(REGULATING_COND_EQ_REGULATING_CONTROL, shuntCompensator.regulatingControl, writer, context);
        }
        CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, shuntCompensator.equipmentContainer, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writePointEq(NonlinearShuntCompensatorPointEq point, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(NONLINEAR_SHUNT_COMPENSATOR_POINT, point.rdfId, false, writer, context);
        CgmesExportUtil.writeResource(NONLINEAR_SHUNT_COMPENSATOR_POINT_NONLINEAR_SHUNT_COMPENSATOR, point.nonlinearShuntCompensator, writer, context);
        CgmesExportUtil.writeAttribute(NONLINEAR_SHUNT_COMPENSATOR_POINT_SECTION_NUMBER, CgmesExportUtil.format(point.sectionNumber), writer, context);
        CgmesExportUtil.writeAttribute(NONLINEAR_SHUNT_COMPENSATOR_POINT_B, CgmesExportUtil.format(point.b), writer, context);
        CgmesExportUtil.writeAttribute(NONLINEAR_SHUNT_COMPENSATOR_POINT_G, CgmesExportUtil.format(point.g), writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record ShuntCompensatorEq(
        String className,
        String rdfId,
        String name,
        double bPerSection,
        double gPerSection,
        int maximumSections,
        double nomU,
        int normalSections,
        String regulatingControl,
        String equipmentContainer
    ) {
    }

    public record NonlinearShuntCompensatorPointEq(
        String rdfId,
        double b,
        double g,
        int sectionNumber,
        String nonlinearShuntCompensator
    ) {
    }

}
