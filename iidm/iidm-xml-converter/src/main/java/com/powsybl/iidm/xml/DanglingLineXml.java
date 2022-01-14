/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineXml extends AbstractConnectableXml<DanglingLine, DanglingLineAdder, VoltageLevel> {
    private static final String GENERATION = "generation";
    private static final String GENERATION_MAX_P = "generationMaxP";
    private static final String GENERATION_MIN_P = "generationMinP";
    private static final String GENERATION_TARGET_P = "generationTargetP";
    private static final String GENERATION_TARGET_Q = "generationTargetQ";
    private static final String GENERATION_TARGET_V = "generationTargetV";

    static final DanglingLineXml INSTANCE = new DanglingLineXml();

    static final String ROOT_ELEMENT_NAME = "danglingLine";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(DanglingLine dl) {
        throw new AssertionError("Should not be called");
    }

    @Override
    protected boolean hasSubElements(DanglingLine dl, NetworkXmlWriterContext context) {
        return hasValidGeneration(dl, context) || hasValidOperationalLimits(dl, context);
    }

    private static boolean hasValidGeneration(DanglingLine dl, NetworkXmlWriterContext context) {
        if (dl.getGeneration() != null) {
            return context.getVersion().compareTo(IidmXmlVersion.V_1_3) > 0;
        }
        return false;
    }

    @Override
    protected void writeRootElementAttributes(DanglingLine dl, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        DanglingLine.Generation generation = dl.getGeneration();
        double[] p0 = new double[1];
        double[] q0 = new double[1];
        p0[0] = dl.getP0();
        q0[0] = dl.getQ0();
        if (generation != null) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, GENERATION, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
            IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
                if (!Double.isNaN(generation.getTargetP())) {
                    p0[0] -= generation.getTargetP();
                }
                if (!Double.isNaN(generation.getTargetQ())) {
                    q0[0] -= generation.getTargetQ();
                }
            });
        }
        XmlUtil.writeDouble("p0", p0[0], context.getWriter());
        XmlUtil.writeDouble("q0", q0[0], context.getWriter());
        XmlUtil.writeDouble("r", dl.getR(), context.getWriter());
        XmlUtil.writeDouble("x", dl.getX(), context.getWriter());
        XmlUtil.writeDouble("g", dl.getG(), context.getWriter());
        XmlUtil.writeDouble("b", dl.getB(), context.getWriter());
        if (generation != null) {
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_7, context, () -> context.getWriter().writeAttribute("withGeneration", "true"));
                XmlUtil.writeDouble(GENERATION_MIN_P, generation.getMinP(), context.getWriter());
                XmlUtil.writeDouble(GENERATION_MAX_P, generation.getMaxP(), context.getWriter());
                XmlUtil.writeOptionalBoolean("generationVoltageRegulationOn", generation.isVoltageRegulationOn(), false, context.getWriter());
                XmlUtil.writeDouble(GENERATION_TARGET_P, generation.getTargetP(), context.getWriter());
                XmlUtil.writeDouble(GENERATION_TARGET_V, generation.getTargetV(), context.getWriter());
                XmlUtil.writeDouble(GENERATION_TARGET_Q, generation.getTargetQ(), context.getWriter());
            });
        }
        if (dl.getUcteXnodeCode() != null) {
            context.getWriter().writeAttribute("ucteXnodeCode", dl.getUcteXnodeCode());
        }
        writeNodeOrBus(null, dl.getTerminal(), context);
        writePQ(null, dl.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(DanglingLine dl, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (dl.getGeneration() != null) {
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> ReactiveLimitsXml.INSTANCE.write(dl.getGeneration(), context));
        }
        if (dl.getActivePowerLimits() != null) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(null, dl.getActivePowerLimits(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        if (dl.getApparentPowerLimits() != null) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(null, dl.getApparentPowerLimits(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        if (dl.getCurrentLimits() != null) {
            writeCurrentLimits(null, dl.getCurrentLimits(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
        }
    }

    @Override
    protected DanglingLineAdder createAdder(VoltageLevel vl) {
        return vl.newDanglingLine();
    }

    @Override
    protected DanglingLine readRootElementAttributes(DanglingLineAdder adder, NetworkXmlReaderContext context) {
        double p0 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p0");
        double q0 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q0");
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r");
        double x = XmlUtil.readDoubleAttribute(context.getReader(), "x");
        double g = XmlUtil.readDoubleAttribute(context.getReader(), "g");
        double b = XmlUtil.readDoubleAttribute(context.getReader(), "b");
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            String voltageRegulationOnStr = context.getReader().getAttributeValue(null, "generationVoltageRegulationOn");
            if (context.getVersion().compareTo(IidmXmlVersion.V_1_7) < 0) {
                if (voltageRegulationOnStr != null) {
                    double minP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), GENERATION_MIN_P);
                    double maxP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), GENERATION_MAX_P);
                    boolean voltageRegulationOn = Boolean.parseBoolean(voltageRegulationOnStr);
                    double targetP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), GENERATION_TARGET_P);
                    double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), GENERATION_TARGET_V);
                    double targetQ = XmlUtil.readOptionalDoubleAttribute(context.getReader(), GENERATION_TARGET_Q);
                    adder.newGeneration()
                            .setMinP(minP)
                            .setMaxP(maxP)
                            .setVoltageRegulationOn(voltageRegulationOn)
                            .setTargetP(targetP)
                            .setTargetV(targetV)
                            .setTargetQ(targetQ)
                            .add();
                }
            } else if (Boolean.parseBoolean(context.getReader().getAttributeValue(null, "withGeneration"))) {
                double minP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), GENERATION_MIN_P);
                double maxP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), GENERATION_MAX_P);
                double targetP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), GENERATION_TARGET_P);
                double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), GENERATION_TARGET_V);
                double targetQ = XmlUtil.readOptionalDoubleAttribute(context.getReader(), GENERATION_TARGET_Q);
                DanglingLineAdder.GenerationAdder ga = adder.newGeneration()
                        .setMinP(minP)
                        .setMaxP(maxP)
                        .setTargetP(targetP)
                        .setTargetV(targetV)
                        .setTargetQ(targetQ);
                if (voltageRegulationOnStr != null) {
                    ga.setVoltageRegulationOn(Boolean.parseBoolean(voltageRegulationOnStr));
                }
                ga.add();
            }
        });
        String ucteXnodeCode = context.getReader().getAttributeValue(null, "ucteXnodeCode");
        readNodeOrBus(adder, context);
        DanglingLine dl = adder.setP0(p0)
                .setQ0(q0)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setUcteXnodeCode(ucteXnodeCode)
                .add();
        readPQ(null, dl.getTerminal(), context.getReader());
        return dl;
    }

    @Override
    protected void readSubElements(DanglingLine dl, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case ACTIVE_POWER_LIMITS:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(null, dl.newActivePowerLimits(), context.getReader()));
                    break;
                case APPARENT_POWER_LIMITS:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(null, dl.newApparentPowerLimits(), context.getReader()));
                    break;
                case "currentLimits":
                    readCurrentLimits(null, dl.newCurrentLimits(), context.getReader());
                    break;
                case "reactiveCapabilityCurve":
                case "minMaxReactiveLimits":
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME + ".generation", "reactiveLimits", IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
                    ReactiveLimitsXml.INSTANCE.read(dl.getGeneration(), context);
                    break;
                default:
                    super.readSubElements(dl, context);
            }
        });
    }
}
