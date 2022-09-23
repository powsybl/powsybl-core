/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerViewSwitchXml extends AbstractSwitchXml<VoltageLevel.NodeBreakerView.SwitchAdder> {

    static final NodeBreakerViewSwitchXml INSTANCE = new NodeBreakerViewSwitchXml();

    @Override
    protected boolean isValid(Switch s, VoltageLevel vl) {
        VoltageLevel.NodeBreakerView v = vl.getNodeBreakerView();
        if (v.getNode1(s.getId()) == v.getNode2(s.getId())) {
            LOGGER.warn("Discard switch with same node at both ends. Id: {}", s.getId());
            return false;
        }
        return true;
    }

    @Override
    protected void writeRootElementAttributes(Switch s, VoltageLevel vl, NetworkXmlWriterContext context) {
        super.writeRootElementAttributes(s, vl, context);
        VoltageLevel.NodeBreakerView v = vl.getNodeBreakerView();
        context.getWriter().writeStringAttribute("node1", Integer.toString(v.getNode1(s.getId())));
        context.getWriter().writeStringAttribute("node2", Integer.toString(v.getNode2(s.getId())));
    }

    @Override
    protected VoltageLevel.NodeBreakerView.SwitchAdder createAdder(VoltageLevel vl) {
        return vl.getNodeBreakerView().newSwitch();
    }

    @Override
    protected Switch readRootElementAttributes(VoltageLevel.NodeBreakerView.SwitchAdder adder, NetworkXmlReaderContext context) {
        boolean open = context.getReader().readBooleanAttribute("open");
        SwitchKind kind = context.getReader().readEnumAttribute("kind", SwitchKind.class);
        boolean retained = context.getReader().readBooleanAttribute("retained");
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> {
            boolean fictitious = context.getReader().readBooleanAttribute("fictitious", false);
            adder.setFictitious(fictitious);
        });
        int node1 = context.getReader().readIntAttribute("node1");
        int node2 = context.getReader().readIntAttribute("node2");
        // Discard switches with same node at both ends
        if (node1 == node2) {
            LOGGER.warn("Discard switch with same node at both ends. Id: {}", context.getReader().readStringAttribute("id"));
            return null;
        } else {
            return adder.setKind(kind)
                .setRetained(retained)
                .setOpen(open)
                .setNode1(node1)
                .setNode2(node2)
                .add();
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeBreakerViewSwitchXml.class);
}
