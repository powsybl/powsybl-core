/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerViewSwitchXml extends AbstractSwitchXml<VoltageLevel.NodeBreakerView.SwitchAdder> {

    static final NodeBreakerViewSwitchXml INSTANCE = new NodeBreakerViewSwitchXml();

    @Override
    protected void writeRootElementAttributes(Switch s, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        super.writeRootElementAttributes(s, vl, context);
        VoltageLevel.NodeBreakerView v = vl.getNodeBreakerView();
        context.getWriter().writeAttribute("node1", Integer.toString(v.getNode1(s.getId())));
        context.getWriter().writeAttribute("node2", Integer.toString(v.getNode2(s.getId())));
    }

    @Override
    protected VoltageLevel.NodeBreakerView.SwitchAdder createAdder(VoltageLevel vl) {
        return vl.getNodeBreakerView().newSwitch();
    }

    @Override
    protected Switch readRootElementAttributes(VoltageLevel.NodeBreakerView.SwitchAdder adder, NetworkXmlReaderContext context) {
        boolean open = XmlUtil.readBoolAttribute(context.getReader(), "open");
        SwitchKind kind = SwitchKind.valueOf(context.getReader().getAttributeValue(null, "kind"));
        boolean retained = XmlUtil.readBoolAttribute(context.getReader(), "retained");
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> {
            boolean fictitious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "fictitious", false);
            adder.setFictitious(fictitious);
        });
        int node1 = XmlUtil.readIntAttribute(context.getReader(), "node1");
        int node2 = XmlUtil.readIntAttribute(context.getReader(), "node2");
        // Discard switches with same node at both ends
        if (node1 == node2) {
            LOGGER.info("Discard switch with same node at both ends. Id: {}", context.getReader().getAttributeValue(null, "id"));
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
