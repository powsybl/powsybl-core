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
public final class CgmesTapChanger {

    private static final String PHASE_TAP_CHANGER_TRANSFORMER_END = PHASE_TAP_CHANGER + "." + TRANSFORMER_END;
    private static final String PHASE_TAP_CHANGER_TABULAR_PHASE_TAP_CHANGER_TABLE = PHASE_TAP_CHANGER_TABULAR + "." + PHASE_TAP_CHANGER_TABLE;

    private static final String RATIO_TAP_CHANGER_TRANSFORMER_END = RATIO_TAP_CHANGER + "." + TRANSFORMER_END;
    private static final String RATIO_TAP_CHANGER_RATIO_TAP_CHANGER_TABLE = RATIO_TAP_CHANGER + "." + RATIO_TAP_CHANGER_TABLE;
    private static final String RATIO_TAP_CHANGER_STEP_VOLTAGE_INCREMENT = RATIO_TAP_CHANGER + "." + STEP_VOLTAGE_INCREMENT;
    private static final String RATIO_TAP_CHANGER_TCUL_CONTROL_MODE = RATIO_TAP_CHANGER + "." + TCUL_CONTROL_MODE;

    private static final String TAP_CHANGER_LOW_STEP = TAP_CHANGER + "." + LOW_STEP;
    private static final String TAP_CHANGER_HIGH_STEP = TAP_CHANGER + "." + HIGH_STEP;
    private static final String TAP_CHANGER_NORMAL_STEP = TAP_CHANGER + "." + NORMAL_STEP;
    private static final String TAP_CHANGER_NEUTRAL_STEP = TAP_CHANGER + "." + NEUTRAL_STEP;
    private static final String TAP_CHANGER_NEUTRAL_U = TAP_CHANGER + "." + NEUTRAL_U;
    private static final String TAP_CHANGER_LTC_FLAG = TAP_CHANGER + "." + LTC_FLAG;
    private static final String TAP_CHANGER_TAP_CHANGER_CONTROL = TAP_CHANGER + "." + TAP_CHANGER_CONTROL;

    private static final String TAP_CHANGER_TABLE_POINT_R = TAP_CHANGER_TABLE_POINT + "." + R;
    private static final String TAP_CHANGER_TABLE_POINT_X = TAP_CHANGER_TABLE_POINT + "." + X;
    private static final String TAP_CHANGER_TABLE_POINT_G = TAP_CHANGER_TABLE_POINT + "." + G;
    private static final String TAP_CHANGER_TABLE_POINT_B = TAP_CHANGER_TABLE_POINT + "." + B;
    private static final String TAP_CHANGER_TABLE_POINT_STEP = TAP_CHANGER_TABLE_POINT + "." + STEP;
    private static final String TAP_CHANGER_TABLE_POINT_RATIO = TAP_CHANGER_TABLE_POINT + "." + RATIO;

    private static final String PHASE_TAP_CHANGER_TABLE_POINT_ANGLE = PHASE_TAP_CHANGER_TABLE_POINT + "." + ANGLE;
    private static final String PHASE_TAP_CHANGER_TABLE_POINT_PHASE_TAP_CHANGER_TABLE = PHASE_TAP_CHANGER_TABLE_POINT + "." + PHASE_TAP_CHANGER_TABLE;

    private static final String RATIO_TAP_CHANGER_TABLE_POINT_RATIO_TAP_CHANGER_TABLE = RATIO_TAP_CHANGER_TABLE_POINT + "." + RATIO_TAP_CHANGER_TABLE;

    private static final String REGULATING_CONTROL_MODE = REGULATING_CONTROL + "." + MODE;
    private static final String REGULATING_CONTROL_TERMINAL = REGULATING_CONTROL + "." + TERMINAL;

    private CgmesTapChanger() {
    }

    public static void writePhaseTapChangerEq(PhaseTapChangerEq phaseTapChanger, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(PHASE_TAP_CHANGER_TABULAR, phaseTapChanger.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(phaseTapChanger.name), writer);
        CgmesExportUtil.writeResource(PHASE_TAP_CHANGER_TRANSFORMER_END, phaseTapChanger.transformerEnd, writer, context);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_LOW_STEP, CgmesExportUtil.format(phaseTapChanger.lowStep), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_HIGH_STEP, CgmesExportUtil.format(phaseTapChanger.highStep), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_NORMAL_STEP, CgmesExportUtil.format(phaseTapChanger.normalStep), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_NEUTRAL_STEP, CgmesExportUtil.format(phaseTapChanger.neutralStep), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_NEUTRAL_U, CgmesExportUtil.format(phaseTapChanger.neutralU), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_LTC_FLAG, CgmesExportUtil.format(phaseTapChanger.ltcFlag), writer);
        CgmesExportUtil.writeResource(PHASE_TAP_CHANGER_TABULAR_PHASE_TAP_CHANGER_TABLE, phaseTapChanger.phaseTapChangerTable, writer, context);
        if (phaseTapChanger.tapChangerControl != null) {
            CgmesExportUtil.writeResource(TAP_CHANGER_TAP_CHANGER_CONTROL, phaseTapChanger.tapChangerControl, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writePhaseTapChangerTableEq(PhaseTapChangerTableEq phaseTapChangerTable, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(PHASE_TAP_CHANGER_TABLE, phaseTapChangerTable.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(phaseTapChangerTable.name), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writePhaseTapChangerTablePointEq(PhaseTapChangerTablePointEq point, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(PHASE_TAP_CHANGER_TABLE_POINT, point.rdfId, false, writer, context);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_R, CgmesExportUtil.format(point.r), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_X, CgmesExportUtil.format(point.x), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_G, CgmesExportUtil.format(point.g), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_B, CgmesExportUtil.format(point.b), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_STEP, CgmesExportUtil.format(point.step), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_RATIO, CgmesExportUtil.format(point.ratio), writer);
        CgmesExportUtil.writeAttribute(namespace, PHASE_TAP_CHANGER_TABLE_POINT_ANGLE, CgmesExportUtil.format(point.angle), writer);
        CgmesExportUtil.writeResource(PHASE_TAP_CHANGER_TABLE_POINT_PHASE_TAP_CHANGER_TABLE, point.phaseTapChangerTable, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeRatioTapChangerEq(RatioTapChangerEq ratioTapChanger, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(RATIO_TAP_CHANGER, ratioTapChanger.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(ratioTapChanger.name), writer);
        CgmesExportUtil.writeResource(RATIO_TAP_CHANGER_TRANSFORMER_END, ratioTapChanger.transformerEnd, writer, context);
        CgmesExportUtil.writeAttribute(namespace, RATIO_TAP_CHANGER_STEP_VOLTAGE_INCREMENT, CgmesExportUtil.format(ratioTapChanger.stepVoltageIncrement), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_LOW_STEP, CgmesExportUtil.format(ratioTapChanger.lowStep), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_HIGH_STEP, CgmesExportUtil.format(ratioTapChanger.highStep), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_NORMAL_STEP, CgmesExportUtil.format(ratioTapChanger.normalStep), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_NEUTRAL_STEP, CgmesExportUtil.format(ratioTapChanger.neutralStep), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_NEUTRAL_U, CgmesExportUtil.format(ratioTapChanger.neutralU), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_LTC_FLAG, CgmesExportUtil.format(ratioTapChanger.ltcFlag), writer);
        if (context.getCimVersion() == 16) {
            CgmesExportUtil.writeResource(RATIO_TAP_CHANGER_TCUL_CONTROL_MODE, namespace,
                namespace + TRANSFORMER_CONTROL_MODE + "." + ratioTapChanger.tculControlMode, writer);
        }
        CgmesExportUtil.writeResource(RATIO_TAP_CHANGER_RATIO_TAP_CHANGER_TABLE, ratioTapChanger.ratioTapChangerTable, writer, context);
        if (ratioTapChanger.tapChangerControl != null) {
            CgmesExportUtil.writeResource(TAP_CHANGER_TAP_CHANGER_CONTROL, ratioTapChanger.tapChangerControl, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeRatioTapChangerTableEq(RatioTapChangerTableEq ratioTapChangerTable, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(RATIO_TAP_CHANGER_TABLE, ratioTapChangerTable.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(ratioTapChangerTable.name), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeRatioTapChangerTablePointEq(RatioTapChangerTablePointEq point, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(RATIO_TAP_CHANGER_TABLE_POINT, point.rdfId, false, writer, context);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_R, CgmesExportUtil.format(point.r), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_X, CgmesExportUtil.format(point.x), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_G, CgmesExportUtil.format(point.g), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_B, CgmesExportUtil.format(point.b), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_STEP, CgmesExportUtil.format(point.step), writer);
        CgmesExportUtil.writeAttribute(namespace, TAP_CHANGER_TABLE_POINT_RATIO, CgmesExportUtil.format(point.ratio), writer);
        CgmesExportUtil.writeResource(RATIO_TAP_CHANGER_TABLE_POINT_RATIO_TAP_CHANGER_TABLE, point.ratioTapChangerTable, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeTapChangerControlEq(TapChangerControlEq tapChangerControl, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(TAP_CHANGER_CONTROL, tapChangerControl.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(tapChangerControl.name), writer);
        CgmesExportUtil.writeResource(REGULATING_CONTROL_MODE, namespace,
            namespace + tapChangerControl.mode, writer);
        CgmesExportUtil.writeResource(REGULATING_CONTROL_TERMINAL, tapChangerControl.terminal, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record PhaseTapChangerEq(
        String rdfId,
        String name,
        double highStep,
        double lowStep,
        boolean ltcFlag,
        double neutralStep,
        double neutralU,
        double normalStep,
        String phaseTapChangerTable,
        String transformerEnd,
        String tapChangerControl
    ) {
    }

    public record PhaseTapChangerTableEq(
        String rdfId,
        String name
    ) {
    }

    public record PhaseTapChangerTablePointEq(
        String rdfId,
        String phaseTapChangerTable,
        int step,
        double r,
        double x,
        double g,
        double b,
        double ratio,
        double angle
    ) {
    }

    public record RatioTapChangerEq(
        String rdfId,
        String name,
        String tculControlMode,
        double stepVoltageIncrement,
        double highStep,
        double lowStep,
        boolean ltcFlag,
        double neutralStep,
        double neutralU,
        double normalStep,
        String ratioTapChangerTable,
        String transformerEnd,
        String tapChangerControl
    ) {
    }

    public record RatioTapChangerTableEq(
        String rdfId,
        String name
    ) {
    }

    public record RatioTapChangerTablePointEq(
        String rdfId,
        String ratioTapChangerTable,
        int step,
        double r,
        double x,
        double g,
        double b,
        double ratio
    ) {
    }

    public record TapChangerControlEq(
        String rdfId,
        String name,
        String mode,
        String terminal
    ) {
    }

}
