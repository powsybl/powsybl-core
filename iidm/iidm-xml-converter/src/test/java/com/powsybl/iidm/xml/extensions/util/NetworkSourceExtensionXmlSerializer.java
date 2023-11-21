/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.xml.extensions.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Network;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class NetworkSourceExtensionXmlSerializer extends AbstractExtensionXmlSerializer<Network, NetworkSourceExtension> {

    public NetworkSourceExtensionXmlSerializer() {
        super("networkSource", "network", NetworkSourceExtension.class, "networkSource.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/networksource/1_0", "extNetworkSource");
    }

    @Override
    public void write(NetworkSourceExtension extension, XmlWriterContext context) {
        context.getWriter().writeStringAttribute("sourceData", extension.getSourceData());
    }

    @Override
    public NetworkSourceExtension read(Network extendable, XmlReaderContext context) {
        String sourceData = context.getReader().readStringAttribute("sourceData");
        context.getReader().readEndNode();
        NetworkSourceExtensionImpl extension = new NetworkSourceExtensionImpl(sourceData);
        extendable.addExtension(NetworkSourceExtension.class, extension);
        return extension;
    }
}
