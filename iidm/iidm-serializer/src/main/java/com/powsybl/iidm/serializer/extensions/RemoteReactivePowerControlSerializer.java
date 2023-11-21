/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerializer;
import com.powsybl.commons.extensions.ExtensionSerializer;
import com.powsybl.commons.io.ReaderContext;
import com.powsybl.commons.io.WriterContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import com.powsybl.iidm.serializer.IidmVersion;
import com.powsybl.iidm.serializer.NetworkSerializerReaderContext;
import com.powsybl.iidm.serializer.NetworkSerializerWriterContext;
import com.powsybl.iidm.serializer.TerminalRefSerializer;
import com.powsybl.iidm.serializer.util.IidmSerializerUtil;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(ExtensionSerializer.class)
public class RemoteReactivePowerControlSerializer extends AbstractExtensionSerializer<Generator, RemoteReactivePowerControl> {

    public RemoteReactivePowerControlSerializer() {
        super(RemoteReactivePowerControl.NAME, "network", RemoteReactivePowerControl.class,
                "remoteReactivePowerControl_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/remote_reactive_power_control/1_0", "rrpc");
    }

    @Override
    public void write(RemoteReactivePowerControl extension, WriterContext context) {
        NetworkSerializerWriterContext networkContext = (NetworkSerializerWriterContext) context;
        IidmSerializerUtil.assertMinimumVersion(getName(), IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, networkContext);
        context.getWriter().writeBooleanAttribute("enabled", extension.isEnabled());
        context.getWriter().writeDoubleAttribute("targetQ", extension.getTargetQ());
        TerminalRefSerializer.writeTerminalRefAttribute(extension.getRegulatingTerminal(), networkContext);
    }

    @Override
    public RemoteReactivePowerControl read(Generator extendable, ReaderContext context) {
        NetworkSerializerReaderContext networkContext = (NetworkSerializerReaderContext) context;
        IidmSerializerUtil.assertMinimumVersion(getName(), IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, networkContext);
        boolean enabled = context.getReader().readBooleanAttribute("enabled");
        double targetQ = context.getReader().readDoubleAttribute("targetQ");
        Terminal terminal = TerminalRefSerializer.readTerminal(networkContext, extendable.getNetwork());
        return extendable.newExtension(RemoteReactivePowerControlAdder.class)
                .withEnabled(enabled)
                .withTargetQ(targetQ)
                .withRegulatingTerminal(terminal)
                .add();
    }
}
