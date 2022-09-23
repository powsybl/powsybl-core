/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LineXml extends AbstractConnectableXml<Line, LineAdder, Network> {

    static final LineXml INSTANCE = new LineXml();

    static final String ROOT_ELEMENT_NAME = "line";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(Line l) {
        throw new AssertionError("Should not be called");
    }

    @Override
    protected boolean hasSubElements(Line l, NetworkXmlWriterContext context) {
        return hasValidOperationalLimits(l, context);
    }

    @Override
    protected void writeRootElementAttributes(Line l, Network n, NetworkXmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("r", l.getR());
        context.getWriter().writeDoubleAttribute("x", l.getX());
        context.getWriter().writeDoubleAttribute("g1", l.getG1());
        context.getWriter().writeDoubleAttribute("b1", l.getB1());
        context.getWriter().writeDoubleAttribute("g2", l.getG2());
        context.getWriter().writeDoubleAttribute("b2", l.getB2());
        writeNodeOrBus(1, l.getTerminal1(), context);
        writeNodeOrBus(2, l.getTerminal2(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, l.getTerminal1(), context.getWriter());
            writePQ(2, l.getTerminal2(), context.getWriter());
        }
    }

    @Override
    protected void writeSubElements(Line l, Network n, NetworkXmlWriterContext context) {
        Optional<ActivePowerLimits> activePowerLimits1 = l.getActivePowerLimits1();
        if (activePowerLimits1.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(1, activePowerLimits1.get(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<ApparentPowerLimits> apparentPowerLimits1 = l.getApparentPowerLimits1();
        if (apparentPowerLimits1.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(1, apparentPowerLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<CurrentLimits> currentLimits1 = l.getCurrentLimits1();
        currentLimits1.ifPresent(currentLimits -> writeCurrentLimits(1, currentLimits, context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        Optional<ActivePowerLimits> activePowerLimits2 = l.getActivePowerLimits2();
        if (activePowerLimits2.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(2, activePowerLimits2.get(), context.getWriter(), context.getVersion(),
                    context.isValid(), context.getOptions()));
        }
        Optional<ApparentPowerLimits> apparentPowerLimits2 = l.getApparentPowerLimits2();
        if (apparentPowerLimits2.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(2, apparentPowerLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<CurrentLimits> currentLimits2 = l.getCurrentLimits2();
        currentLimits2.ifPresent(currentLimits -> writeCurrentLimits(2, currentLimits, context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
    }

    @Override
    protected LineAdder createAdder(Network n) {
        return n.newLine();
    }

    @Override
    protected Line readRootElementAttributes(LineAdder adder, NetworkXmlReaderContext context) {
        double r = context.getReader().readDoubleAttribute("r");
        double x = context.getReader().readDoubleAttribute("x");
        double g1 = context.getReader().readDoubleAttribute("g1");
        double b1 = context.getReader().readDoubleAttribute("b1");
        double g2 = context.getReader().readDoubleAttribute("g2");
        double b2 = context.getReader().readDoubleAttribute("b2");
        adder.setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2);
        readNodeOrBus(adder, context);
        Line l = adder.add();
        readPQ(1, l.getTerminal1(), context.getReader());
        readPQ(2, l.getTerminal2(), context.getReader());
        return l;
    }

    @Override
    protected void readSubElements(Line l, NetworkXmlReaderContext context) throws XMLStreamException {
        context.getReader().readUntilEndNode(getRootElementName(), () -> {
            switch (context.getReader().getNodeName()) {
                case ACTIVE_POWER_LIMITS_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(1, l.newActivePowerLimits1(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(1, l.newApparentPowerLimits1(), context.getReader()));
                    break;

                case "currentLimits1":
                    readCurrentLimits(1, l.newCurrentLimits1(), context.getReader());
                    break;

                case ACTIVE_POWER_LIMITS_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(2, l.newActivePowerLimits2(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(2, l.newApparentPowerLimits2(), context.getReader()));
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, l.newCurrentLimits2(), context.getReader());
                    break;

                default:
                    super.readSubElements(l, context);
            }
        });
    }
}
