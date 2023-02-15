/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineXml extends AbstractConnectableXml<TieLine, TieLineAdder, Network> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TieLineXml.class);

    static final TieLineXml INSTANCE = new TieLineXml();

    static final String ROOT_ELEMENT_NAME = "tieLine";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(TieLine tl) {
        throw new AssertionError("Should not be called");
    }

    @Override
    protected boolean hasSubElements(TieLine tl, NetworkXmlWriterContext context) {
        return hasValidOperationalLimits(tl, context) || hasDanglingLine(tl);
    }

    private static void writeHalf(DanglingLine halfLine, NetworkXmlWriterContext context, int side) throws XMLStreamException {
        Boundary boundary = halfLine.getBoundary();
        context.getWriter().writeAttribute("id_" + side, context.getAnonymizer().anonymizeString(halfLine.getId()));
        halfLine.getOptionalName().ifPresent(name -> {
            try {
                context.getWriter().writeAttribute("name_" + side, context.getAnonymizer().anonymizeString(name));
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
        XmlUtil.writeDouble("r_" + side, halfLine.getR(), context.getWriter());
        XmlUtil.writeDouble("x_" + side, halfLine.getX(), context.getWriter());
        // TODO change serialization
        XmlUtil.writeDouble("g1_" + side, halfLine.getG() / 2, context.getWriter());
        XmlUtil.writeDouble("b1_" + side, halfLine.getB() / 2, context.getWriter());
        XmlUtil.writeDouble("g2_" + side, halfLine.getG() / 2, context.getWriter());
        XmlUtil.writeDouble("b2_" + side, halfLine.getB() / 2, context.getWriter());
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> {
            XmlUtil.writeDouble("xnodeP_" + side, boundary.getP(), context.getWriter());
            XmlUtil.writeDouble("xnodeQ_" + side, boundary.getQ(), context.getWriter());
        });

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> XmlUtil.writeOptionalBoolean("fictitious_" + side, halfLine.isFictitious(), false, context.getWriter()));
    }

    private boolean hasDanglingLine(TieLine tl) {
        return tl.getHalf1() != null || tl.getHalf2() != null;
    }

    private static void writeDanglingLines(TieLine tl, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        DanglingLineXml.INSTANCE.write(tl.getHalf1(), tl, context);
        DanglingLineXml.INSTANCE.write(tl.getHalf2(), tl, context);
    }

    @Override
    protected void writeRootElementAttributes(TieLine tl, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
        if (tl.getUcteXnodeCode() != null) {
            context.getWriter().writeAttribute("ucteXnodeCode", tl.getUcteXnodeCode());
        }
        writeNodeOrBus(1, tl.getTerminal1(), context);
        writeNodeOrBus(2, tl.getTerminal2(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, tl.getTerminal1(), context.getWriter());
            writePQ(2, tl.getTerminal2(), context.getWriter());
        }
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            writeHalf(tl.getHalf1(), context, 1);
            writeHalf(tl.getHalf2(), context, 2);
        });
    }

    @Override
    protected void writeSubElements(TieLine tl, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () -> {
            writeDanglingLines(tl, tl.getTerminal1().getVoltageLevel(), context);
        });
        Optional<ActivePowerLimits> activePowerLimits1 = tl.getActivePowerLimits1();
        if (activePowerLimits1.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(1, activePowerLimits1.get(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<ApparentPowerLimits> apparentPowerLimits1 = tl.getApparentPowerLimits1();
        if (apparentPowerLimits1.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(1, apparentPowerLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<CurrentLimits> currentLimits1 = tl.getCurrentLimits1();
        if (currentLimits1.isPresent()) {
            writeCurrentLimits(1, currentLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
        }
        Optional<ActivePowerLimits> activePowerLimits2 = tl.getActivePowerLimits2();
        if (activePowerLimits2.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(2, activePowerLimits2.get(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<ApparentPowerLimits> apparentPowerLimits2 = tl.getApparentPowerLimits2();
        if (apparentPowerLimits2.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(2, apparentPowerLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<CurrentLimits> currentLimits2 = tl.getCurrentLimits2();
        if (currentLimits2.isPresent()) {
            writeCurrentLimits(2, currentLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
        }
    }

    @Override
    protected TieLineAdder createAdder(Network n) {
        return n.newTieLine();
    }

    private static void readHalf(MergedDanglingLineAdder adder, NetworkXmlReaderContext context, int side) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id_" + side));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "name_" + side));
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r_" + side);
        double x = XmlUtil.readDoubleAttribute(context.getReader(), "x_" + side);
        double g1 = XmlUtil.readDoubleAttribute(context.getReader(), "g1_" + side);
        double b1 = XmlUtil.readDoubleAttribute(context.getReader(), "b1_" + side);
        double g2 = XmlUtil.readDoubleAttribute(context.getReader(), "g2_" + side);
        double b2 = XmlUtil.readDoubleAttribute(context.getReader(), "b2_" + side);
        String ucteXnodeCode = context.getReader().getAttributeValue(null, "ucteXnodeCode");
        adder.setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG(g1 + g2)
                .setB(b1 + b2)
                .setUcteXnodeCode(ucteXnodeCode);
        readNodeOrBus(adder, String.valueOf(side), context);

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            boolean fictitious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "fictitious_" + side, false);
            adder.setFictitious(fictitious);
        });
        adder.add();
    }

    @Override
    protected TieLine readRootElementAttributes(TieLineAdder adder, NetworkXmlReaderContext context) {

        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            readHalf(adder.newHalf1(), context, 1);
            readHalf(adder.newHalf2(), context, 2);
        });

        String voltageLevelId1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId1"));
        String voltageLevelId2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId2"));
        String connectableBus1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "connectableBus1"));
        double p1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p1");
        double q1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q1");
        double p2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p2");
        double q2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q2");
        final double[] half1BoundaryP = new double[1];
        final double[] half2BoundaryP = new double[1];
        final double[] half1BoundaryQ = new double[1];
        final double[] half2BoundaryQ = new double[1];
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> {
            half1BoundaryP[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeP_1");
            half2BoundaryP[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeP_2");
            half1BoundaryQ[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeQ_1");
            half2BoundaryQ[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeQ_2");
        });
        adder.setVoltageLevel1(voltageLevelId1).setVoltageLevel2(voltageLevelId2);
        final TieLine[] tl = new TieLine[1];
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () ->  {
            try {
                readUntilEndRootElement(context.getReader(), () -> {
                    switch (context.getReader().getLocalName()) {
                        case "danglingLine":
                            String connectableBus = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "connectableBus"));
                            if (connectableBus1.equals(connectableBus)) {
                                MergedDanglingLineXml.read(adder.newHalf1(), context);
                            } else {
                                MergedDanglingLineXml.read(adder.newHalf2(), context);
                                tl[0] = adder.add();
                            }
                            break;
                        default:
                            if (tl[0] == null) {
                                throw new PowsyblException("Dangline lines for tie line must come first in xiidm.");
                            }
                            readSubElementsInternal(tl[0], context);
                    }
                });
            } catch (XMLStreamException e) {
                throw new PowsyblException("Exception reading tie line.");
            }
        });

        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            tl[0] = adder.add();
            readUntilEndRootElement(context.getReader(), () -> {
                readSubElementsInternal(tl[0], context);
            });
        });
        tl[0].getTerminal1().setP(p1);
        tl[0].getTerminal1().setQ(q1);
        tl[0].getTerminal2().setP(p2);
        tl[0].getTerminal2().setQ(q2);
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> {
            context.getEndTasks().add(() -> {
                checkBoundaryValue(half1BoundaryP[0], tl[0].getHalf1().getBoundary().getP(), "xnodeP_1", tl[0].getId());
                checkBoundaryValue(half2BoundaryP[0], tl[0].getHalf2().getBoundary().getP(), "xnodeP_2", tl[0].getId());
                checkBoundaryValue(half1BoundaryQ[0], tl[0].getHalf1().getBoundary().getQ(), "xnodeQ_1", tl[0].getId());
                checkBoundaryValue(half2BoundaryQ[0], tl[0].getHalf2().getBoundary().getQ(), "xnodeQ_2", tl[0].getId());
            });
        });
        return tl[0];
    }

    protected void readSubElementsInternal(TieLine tl, NetworkXmlReaderContext context) throws XMLStreamException {
            switch (context.getReader().getLocalName()) {
                case ACTIVE_POWER_LIMITS_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(1, tl.newActivePowerLimits1(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(1, tl.newApparentPowerLimits1(), context.getReader()));
                    break;

                case "currentLimits1":
                    readCurrentLimits(1, tl.newCurrentLimits1(), context.getReader());
                    break;

                case ACTIVE_POWER_LIMITS_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(2, tl.newActivePowerLimits2(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(2, tl.newApparentPowerLimits2(), context.getReader()));
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, tl.newCurrentLimits2(), context.getReader());
                    break;
                default:
                    super.readSubElements(tl, context);
            }
    }

    @Override
    protected void readSubElements(TieLine tl, NetworkXmlReaderContext context) throws XMLStreamException {
        //Already read by readSubElementsInternal
    }

    private static void checkBoundaryValue(double imported, double calculated, String name, String tlId) {
        if (!Double.isNaN(imported) && imported != calculated) {
            LOGGER.info("{} of TieLine {} is recalculated. Its imported value is not used (imported value = {}; calculated value = {})", name, tlId, imported, calculated);
        }
    }
}
