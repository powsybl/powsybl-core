/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;
import com.powsybl.iidm.serde.TerminalRefSerDe;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(ExtensionSerDe.class)
public class RemoteReactivePowerControlSerDe extends AbstractExtensionSerDe<Generator, RemoteReactivePowerControl> {

    public RemoteReactivePowerControlSerDe() {
        super(RemoteReactivePowerControl.NAME, "network", RemoteReactivePowerControl.class,
                "remoteReactivePowerControl_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/remote_reactive_power_control/1_0", "rrpc");
    }

    @Override
    public void write(RemoteReactivePowerControl extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        IidmSerDeUtil.assertMinimumVersion(getName(), IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, networkContext);
        context.getWriter().writeBooleanAttribute("enabled", extension.isEnabled());
        context.getWriter().writeDoubleAttribute("targetQ", extension.getTargetQ());
        TerminalRefSerDe.writeTerminalRefAttribute(extension.getRegulatingTerminal(), networkContext);
    }

    @Override
    public RemoteReactivePowerControl read(Generator extendable, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        IidmSerDeUtil.assertMinimumVersion(getName(), IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, networkContext);
        boolean enabled = context.getReader().readBooleanAttribute("enabled");
        double targetQ = context.getReader().readDoubleAttribute("targetQ");
        Terminal terminal = TerminalRefSerDe.readTerminal(networkContext, extendable.getNetwork());
        return extendable.newExtension(RemoteReactivePowerControlAdder.class)
                .withEnabled(enabled)
                .withTargetQ(targetQ)
                .withRegulatingTerminal(terminal)
                .add();
    }
}
