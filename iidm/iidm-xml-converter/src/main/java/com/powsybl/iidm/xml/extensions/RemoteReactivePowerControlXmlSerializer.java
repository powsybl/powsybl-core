/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;
import com.powsybl.iidm.xml.TerminalRefXml;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class RemoteReactivePowerControlXmlSerializer extends AbstractExtensionXmlSerializer<Generator, RemoteReactivePowerControl> {

    public RemoteReactivePowerControlXmlSerializer() {
        super(RemoteReactivePowerControl.NAME, "network", RemoteReactivePowerControl.class,
                "remoteReactivePowerControl_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/remote_reactive_power_control/1_0", "rrpc");
    }

    @Override
    public void write(RemoteReactivePowerControl extension, XmlWriterContext context) {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        IidmXmlUtil.assertMinimumVersion(getName(), IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, networkContext);
        context.getWriter().writeBooleanAttribute("enabled", extension.isEnabled());
        context.getWriter().writeDoubleAttribute("targetQ", extension.getTargetQ());
        TerminalRefXml.writeTerminalRefAttribute(extension.getRegulatingTerminal(), networkContext);
    }

    @Override
    public RemoteReactivePowerControl read(Generator extendable, XmlReaderContext context) {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        IidmXmlUtil.assertMinimumVersion(getName(), IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, networkContext);
        boolean enabled = context.getReader().readBooleanAttribute("enabled");
        double targetQ = context.getReader().readDoubleAttribute("targetQ");
        Terminal terminal = TerminalRefXml.readTerminal(networkContext, extendable.getNetwork());
        return extendable.newExtension(RemoteReactivePowerControlAdder.class)
                .withEnabled(enabled)
                .withTargetQ(targetQ)
                .withRegulatingTerminal(terminal)
                .add();
    }
}
