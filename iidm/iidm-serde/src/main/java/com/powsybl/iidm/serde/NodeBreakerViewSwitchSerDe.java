/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeBreakerViewSwitchSerDe extends AbstractSwitchSerDe<VoltageLevel.NodeBreakerView.SwitchAdder> {

    static final NodeBreakerViewSwitchSerDe INSTANCE = new NodeBreakerViewSwitchSerDe();

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
    protected void writeRootElementAttributes(Switch s, VoltageLevel vl, NetworkSerializerContext context) {
        super.writeRootElementAttributes(s, vl, context);
        VoltageLevel.NodeBreakerView v = vl.getNodeBreakerView();
        context.getWriter().writeIntAttribute("node1", v.getNode1(s.getId()));
        context.getWriter().writeIntAttribute("node2", v.getNode2(s.getId()));
    }

    @Override
    protected VoltageLevel.NodeBreakerView.SwitchAdder createAdder(VoltageLevel vl) {
        return vl.getNodeBreakerView().newSwitch();
    }

    @Override
    protected Switch readRootElementAttributes(VoltageLevel.NodeBreakerView.SwitchAdder adder, VoltageLevel voltageLevel, NetworkDeserializerContext context) {
        SwitchKind kind = context.getReader().readEnumAttribute("kind", SwitchKind.class);
        boolean retained = context.getReader().readBooleanAttribute("retained");
        boolean open = context.getReader().readBooleanAttribute("open");
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_14, context, () -> {
            Optional<Boolean> solvedOpen = context.getReader().readOptionalBooleanAttribute("solvedOpen");
            solvedOpen.ifPresent(adder::setSolvedOpen);
        });
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_1, context, () -> {
            boolean fictitious = context.getReader().readBooleanAttribute("fictitious", false);
            adder.setFictitious(fictitious);
        });
        int node1 = context.getReader().readIntAttribute("node1");
        int node2 = context.getReader().readIntAttribute("node2");
        if (node1 == node2 && context.getVersion().compareTo(IidmVersion.V_1_8) < 0) {
            // Discard switches with same node at both ends instead of throwing exception in adder to support old xiidm files
            LOGGER.warn("Discard switch with same node {} at both ends", node1);
            return null;
        }
        return adder.setKind(kind)
            .setRetained(retained)
            .setOpen(open)
            .setNode1(node1)
            .setNode2(node2)
            .add();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeBreakerViewSwitchSerDe.class);
}
