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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineXml extends AbstractConnectableXml<DanglingLine, DanglingLineAdder, VoltageLevel> {

    private static final Logger LOG = LoggerFactory.getLogger(DanglingLineXml.class);

    private static final String ANGLE = "angle";
    private static final String GENERATION = "generation";
    private static final String BOUNDARY_POINT = "boundaryPoint";

    static final DanglingLineXml INSTANCE = new DanglingLineXml();

    static final String ROOT_ELEMENT_NAME = "danglingLine";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(DanglingLine dl) {
        return dl.getCurrentLimits() != null || dl.getGeneration() != null;
    }

    @Override
    protected boolean hasSubElements(DanglingLine dl, NetworkXmlWriterContext context) {
        return hasSubElements(dl) || (hasDefinedBoundaryPoint(dl) && context.getVersion().compareTo(IidmXmlVersion.V_1_5) >= 0);
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
                XmlUtil.writeDouble("generationMinP", generation.getMinP(), context.getWriter());
                XmlUtil.writeDouble("generationMaxP", generation.getMaxP(), context.getWriter());
                context.getWriter().writeAttribute("generationVoltageRegulationOn", Boolean.toString(generation.isVoltageRegulationOn()));
                XmlUtil.writeDouble("generationTargetP", generation.getTargetP(), context.getWriter());
                XmlUtil.writeDouble("generationTargetV", generation.getTargetV(), context.getWriter());
                XmlUtil.writeDouble("generationTargetQ", generation.getTargetQ(), context.getWriter());
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
        if (hasDefinedBoundaryPoint(dl)) {
            IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> LOG.warn("Boundary Point values of dangling line {} are defined but are not serializable for XIIDM version < 1.5", dl.getId()));
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeBoundaryPoint(dl.getBoundaryPoint(), context.getWriter(), context.getVersion().getNamespaceURI()));
        }
        if (dl.getGeneration() != null) {
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> ReactiveLimitsXml.INSTANCE.write(dl.getGeneration(), context));
        }
        if (dl.getCurrentLimits() != null) {
            writeCurrentLimits(null, dl.getCurrentLimits(), context.getWriter(), context.getVersion(), context.getOptions());
        }
    }

    private static void writeBoundaryPoint(BoundaryPoint boundaryPoint, XMLStreamWriter writer, String namespaceUri) throws XMLStreamException {
        writer.writeEmptyElement(namespaceUri, BOUNDARY_POINT);
        XmlUtil.writeDouble("p", boundaryPoint.getP(), writer);
        XmlUtil.writeDouble("q", boundaryPoint.getQ(), writer);
        XmlUtil.writeDouble("v", boundaryPoint.getV(), writer);
        XmlUtil.writeDouble(ANGLE, boundaryPoint.getAngle(), writer);
    }

    @Override
    protected DanglingLineAdder createAdder(VoltageLevel vl) {
        return vl.newDanglingLine();
    }

    @Override
    protected DanglingLine readRootElementAttributes(DanglingLineAdder adder, NetworkXmlReaderContext context) {
        double p0 = XmlUtil.readDoubleAttribute(context.getReader(), "p0");
        double q0 = XmlUtil.readDoubleAttribute(context.getReader(), "q0");
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r");
        double x = XmlUtil.readDoubleAttribute(context.getReader(), "x");
        double g = XmlUtil.readDoubleAttribute(context.getReader(), "g");
        double b = XmlUtil.readDoubleAttribute(context.getReader(), "b");
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            String voltageRegulationOnStr = context.getReader().getAttributeValue(null, "generationVoltageRegulationOn");
            if (voltageRegulationOnStr != null) {
                double minP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "generationMinP");
                double maxP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "generationMaxP");
                boolean voltageRegulationOn = Boolean.parseBoolean(voltageRegulationOnStr);
                double targetP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "generationTargetP");
                double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "generationTargetV");
                double targetQ = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "generationTargetQ");
                adder.newGeneration()
                        .setMinP(minP)
                        .setMaxP(maxP)
                        .setVoltageRegulationOn(voltageRegulationOn)
                        .setTargetP(targetP)
                        .setTargetV(targetV)
                        .setTargetQ(targetQ)
                        .add();
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
                case BOUNDARY_POINT:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, BOUNDARY_POINT, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    readBoundaryPoint(dl.getId(), dl.getBoundaryPoint(), context);
                    break;
                case "currentLimits":
                    readCurrentLimits(null, dl::newCurrentLimits, context.getReader());
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

    private static void readBoundaryPoint(String dlId, BoundaryPoint boundaryPoint, NetworkXmlReaderContext context) {
        XMLStreamReader reader = context.getReader();
        double p = XmlUtil.readOptionalDoubleAttribute(reader, "p");
        double q = XmlUtil.readOptionalDoubleAttribute(reader, "q");
        double v = XmlUtil.readOptionalDoubleAttribute(reader, "v");
        double angle = XmlUtil.readOptionalDoubleAttribute(reader, ANGLE);
        context.getEndTasks().add(() -> {
            checkBoundaryValue(p, boundaryPoint.getP(), "p", dlId);
            checkBoundaryValue(q, boundaryPoint.getQ(), "q", dlId);
            checkBoundaryValue(v, boundaryPoint.getV(), "v", dlId);
            checkBoundaryValue(angle, boundaryPoint.getAngle(), ANGLE, dlId);
        });
    }

    private static void checkBoundaryValue(double imported, double calculated, String name, String dlId) {
        if (!Double.isNaN(imported) && imported != calculated) {
            LOG.info("boundaryPoint.{} of DanglingLine {} is recalculated. Its imported value is not used (imported value = {}; calculated value = {})", name, dlId, imported, calculated);
        }
    }

    private static boolean hasDefinedBoundaryPoint(DanglingLine dl) {
        BoundaryPoint boundaryPoint = dl.getBoundaryPoint();
        return !Double.isNaN(boundaryPoint.getP()) || !Double.isNaN(boundaryPoint.getQ()) ||
                !Double.isNaN(boundaryPoint.getV()) || !Double.isNaN(boundaryPoint.getAngle());
    }
}
