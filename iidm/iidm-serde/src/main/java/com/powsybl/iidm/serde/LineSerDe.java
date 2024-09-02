/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LineSerDe extends AbstractSimpleIdentifiableSerDe<Line, LineAdder, Network> {

    static final LineSerDe INSTANCE = new LineSerDe();

    static final String ROOT_ELEMENT_NAME = "line";
    static final String ARRAY_ELEMENT_NAME = "lines";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Line l, Network n, NetworkSerializerContext context) {
        context.getWriter().writeDoubleAttribute("r", l.getR());
        context.getWriter().writeDoubleAttribute("x", l.getX());
        context.getWriter().writeDoubleAttribute("g1", l.getG1());
        context.getWriter().writeDoubleAttribute("b1", l.getB1());
        context.getWriter().writeDoubleAttribute("g2", l.getG2());
        context.getWriter().writeDoubleAttribute("b2", l.getB2());
        writeNodeOrBus(1, l.getTerminal1(), context);
        writeNodeOrBus(2, l.getTerminal2(), context);
        writeOptionalPQ(1, l.getTerminal1(), context.getWriter(), context.getOptions()::isWithBranchSV);
        writeOptionalPQ(2, l.getTerminal2(), context.getWriter(), context.getOptions()::isWithBranchSV);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> {
            writeSelectedGroupId(1, l.getSelectedOperationalLimitsGroupId1().orElse(null), context.getWriter());
            writeSelectedGroupId(2, l.getSelectedOperationalLimitsGroupId2().orElse(null), context.getWriter());
        });
    }

    @Override
    protected void writeSubElements(Line l, Network n, NetworkSerializerContext context) {
        writeLimits(context, 1, ROOT_ELEMENT_NAME, l.getSelectedOperationalLimitsGroup1().orElse(null), l.getOperationalLimitsGroups1());
        writeLimits(context, 2, ROOT_ELEMENT_NAME, l.getSelectedOperationalLimitsGroup2().orElse(null), l.getOperationalLimitsGroups2());
    }

    @Override
    protected LineAdder createAdder(Network n) {
        return n.newLine();
    }

    @Override
    protected Line readRootElementAttributes(LineAdder adder, Network network, NetworkDeserializerContext context) {
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
        ConnectableSerDeUtil.readVoltageLevelAndNodeOrBus(adder, network, context);
        Line l = adder.add();
        readOptionalPQ(1, l.getTerminal1(), context.getReader());
        readOptionalPQ(2, l.getTerminal2(), context.getReader());
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> {
            readSelectedGroupId(1, l::setSelectedOperationalLimitsGroup1, context);
            readSelectedGroupId(2, l::setSelectedOperationalLimitsGroup2, context);
        });
        return l;
    }

    @Override
    protected void readSubElements(Line l, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case LIMITS_GROUP_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, LIMITS_GROUP_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_12, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> readLoadingLimitsGroup(l::newOperationalLimitsGroup1, LIMITS_GROUP_1, context));
                }
                case ACTIVE_POWER_LIMITS_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(l.newActivePowerLimits1(), context));
                }
                case APPARENT_POWER_LIMITS_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(l.newApparentPowerLimits1(), context));
                }
                case "currentLimits1" -> readCurrentLimits(l.newCurrentLimits1(), context);
                case LIMITS_GROUP_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, LIMITS_GROUPS + "2", IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_12, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> readLoadingLimitsGroup(l::newOperationalLimitsGroup2, LIMITS_GROUP_2, context));
                }
                case ACTIVE_POWER_LIMITS_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(l.newActivePowerLimits2(), context));
                }
                case APPARENT_POWER_LIMITS_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(l.newApparentPowerLimits2(), context));
                }
                case "currentLimits2" -> readCurrentLimits(l.newCurrentLimits2(), context);
                default -> super.readSubElement(elementName, l, context);
            }
        });
    }
}
