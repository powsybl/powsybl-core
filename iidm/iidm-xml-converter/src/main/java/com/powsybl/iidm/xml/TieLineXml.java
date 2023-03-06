/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.util.*;
import java.util.function.Consumer;

import static com.powsybl.iidm.xml.AbstractConnectableXml.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineXml extends AbstractIdentifiableXml<TieLine, TieLineAdder, Network> {

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
        return hasValidOperationalLimits(tl.getHalf1(), context) || hasValidOperationalLimits(tl.getHalf2(), context) || context.getVersion().compareTo(IidmXmlVersion.V_1_10) >= 0;
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

    private static void writeDanglingLines(TieLine tl, NetworkXmlWriterContext context) throws XMLStreamException {
        MergedDanglingLineXml.INSTANCE_1.write(tl.getHalf1(), tl, context);
        MergedDanglingLineXml.INSTANCE_2.write(tl.getHalf2(), tl, context);
    }

    @Override
    protected void writeRootElementAttributes(TieLine tl, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
        if (tl.getUcteXnodeCode() != null) {
            context.getWriter().writeAttribute("ucteXnodeCode", tl.getUcteXnodeCode());
        }
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () -> {
            context.getWriter().writeAttribute("voltageLevelId1", context.getAnonymizer().anonymizeString(tl.getHalf1().getTerminal().getVoltageLevel().getId()));
            context.getWriter().writeAttribute("voltageLevelId2", context.getAnonymizer().anonymizeString(tl.getHalf2().getTerminal().getVoltageLevel().getId()));
        });
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            writeNodeOrBus(1, tl.getHalf1().getTerminal(), context);
            writeNodeOrBus(2, tl.getHalf2().getTerminal(), context);
            if (context.getOptions().isWithBranchSV()) {
                writePQ(1, tl.getHalf1().getTerminal(), context.getWriter());
                writePQ(2, tl.getHalf2().getTerminal(), context.getWriter());
            }
            writeHalf(tl.getHalf1(), context, 1);
            writeHalf(tl.getHalf2(), context, 2);
        });
    }

    @Override
    protected void writeSubElements(TieLine tl, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () -> writeDanglingLines(tl, context));
        Optional<ActivePowerLimits> activePowerLimits1 = tl.getHalf1().getActivePowerLimits();
        if (activePowerLimits1.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(1, activePowerLimits1.get(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<ApparentPowerLimits> apparentPowerLimits1 = tl.getHalf1().getApparentPowerLimits();
        if (apparentPowerLimits1.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(1, apparentPowerLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<CurrentLimits> currentLimits1 = tl.getHalf1().getCurrentLimits();
        if (currentLimits1.isPresent()) {
            writeCurrentLimits(1, currentLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
        }
        Optional<ActivePowerLimits> activePowerLimits2 = tl.getHalf2().getActivePowerLimits();
        if (activePowerLimits2.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(2, activePowerLimits2.get(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<ApparentPowerLimits> apparentPowerLimits2 = tl.getHalf2().getApparentPowerLimits();
        if (apparentPowerLimits2.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(2, apparentPowerLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<CurrentLimits> currentLimits2 = tl.getHalf2().getCurrentLimits();
        if (currentLimits2.isPresent()) {
            writeCurrentLimits(2, currentLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
        }
    }

    @Override
    protected TieLineAdder createAdder(Network n) {
        return n.newTieLine();
    }

    @Override
    protected TieLine readRootElementAttributes(TieLineAdder adder, NetworkXmlReaderContext context) {
        throw new UnsupportedOperationException(); // should not be called
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
    protected void readElement(String id, TieLineAdder adder, NetworkXmlReaderContext context) throws XMLStreamException {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            readHalf(adder.newHalf1(), context, 1);
            readHalf(adder.newHalf2(), context, 2);
        });

        String voltageLevelId1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId1"));
        String voltageLevelId2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId2"));
        String ucteXnodeCode = context.getReader().getAttributeValue(null, "ucteXnodeCode");
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
        List<Consumer<DanglingLine>> subElementsDl1 = new ArrayList<>();
        List<Consumer<DanglingLine>> subElementsDl2 = new ArrayList<>();
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            double p1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p1");
            double q1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q1");
            double p2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p2");
            double q2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q2");
            subElementsDl1.add(dl -> dl.getTerminal().setP(p1));
            subElementsDl1.add(dl -> dl.getTerminal().setQ(q1));
            subElementsDl2.add(dl -> dl.getTerminal().setP(p2));
            subElementsDl2.add(dl -> dl.getTerminal().setQ(q2));
        });
        Map<String, String> properties = new HashMap<>();
        Map<String, String> aliases = new HashMap<>();
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case PropertiesXml.PROPERTY:
                    String name = context.getReader().getAttributeValue(null, "name");
                    String value = context.getReader().getAttributeValue(null, "value");
                    properties.put(name, value);
                    break;
                case AliasesXml.ALIAS:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, AliasesXml.ALIAS, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
                    String aliasType = context.getReader().getAttributeValue(null, "type");
                    String alias = context.getAnonymizer().deanonymizeString(context.getReader().getElementText());
                    aliases.put(alias, aliasType);
                    break;
                case "danglingLine1":
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, "danglingLine1", IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_10, context);
                    MergedDanglingLineXml.INSTANCE_1.read(adder.newHalf1().setUcteXnodeCode(ucteXnodeCode), subElementsDl1, context);
                    break;
                case "danglingLine2":
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, "danglingLine2", IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_10, context);
                    MergedDanglingLineXml.INSTANCE_2.read(adder.newHalf2().setUcteXnodeCode(ucteXnodeCode), subElementsDl2, context);
                    break;
                default:
                    if (tl[0] == null) {
                        tl[0] = createTieLine(adder, subElementsDl1, subElementsDl2, properties, aliases);
                    }
                    readSubElementsInternal(tl[0], context);
            }
        });
        if (tl[0] == null) {
            tl[0] = createTieLine(adder, subElementsDl1, subElementsDl2, properties, aliases);
        }
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> context.getEndTasks().add(() -> {
            checkBoundaryValue(half1BoundaryP[0], tl[0].getHalf1().getBoundary().getP(), "xnodeP_1", tl[0].getId());
            checkBoundaryValue(half2BoundaryP[0], tl[0].getHalf2().getBoundary().getP(), "xnodeP_2", tl[0].getId());
            checkBoundaryValue(half1BoundaryQ[0], tl[0].getHalf1().getBoundary().getQ(), "xnodeQ_1", tl[0].getId());
            checkBoundaryValue(half2BoundaryQ[0], tl[0].getHalf2().getBoundary().getQ(), "xnodeQ_2", tl[0].getId());
        }));
    }

    private static TieLine createTieLine(TieLineAdder adder, List<Consumer<DanglingLine>> subElementsDl1, List<Consumer<DanglingLine>> subElementsDl2,
                                         Map<String, String> properties, Map<String, String> aliases) {
        TieLine tl = adder.add();
        subElementsDl1.forEach(consumer -> consumer.accept(tl.getHalf1()));
        subElementsDl2.forEach(consumer -> consumer.accept(tl.getHalf2()));
        properties.forEach(tl::setProperty);
        aliases.forEach(tl::addAlias);
        return tl;
    }

    protected void readSubElementsInternal(TieLine tl, NetworkXmlReaderContext context) throws XMLStreamException {
        switch (context.getReader().getLocalName()) {
            case ACTIVE_POWER_LIMITS_1:
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(1, tl.getHalf1().newActivePowerLimits(), context.getReader()));
                break;

            case APPARENT_POWER_LIMITS_1:
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(1, tl.getHalf1().newApparentPowerLimits(), context.getReader()));
                break;

            case "currentLimits1":
                readCurrentLimits(1, tl.getHalf1().newCurrentLimits(), context.getReader());
                break;

            case ACTIVE_POWER_LIMITS_2:
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(2, tl.getHalf2().newActivePowerLimits(), context.getReader()));
                break;

            case APPARENT_POWER_LIMITS_2:
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(2, tl.getHalf2().newApparentPowerLimits(), context.getReader()));
                break;

            case "currentLimits2":
                readCurrentLimits(2, tl.getHalf2().newCurrentLimits(), context.getReader());
                break;
            default:
                super.readSubElements(tl, context);
        }
    }

    @Override
    protected void readSubElements(TieLine tl, NetworkXmlReaderContext context) throws XMLStreamException {
        throw new UnsupportedOperationException(); // should not be called
    }

    private static void checkBoundaryValue(double imported, double calculated, String name, String tlId) {
        if (!Double.isNaN(imported) && imported != calculated) {
            LOGGER.info("{} of TieLine {} is recalculated. Its imported value is not used (imported value = {}; calculated value = {})", name, tlId, imported, calculated);
        }
    }
}
