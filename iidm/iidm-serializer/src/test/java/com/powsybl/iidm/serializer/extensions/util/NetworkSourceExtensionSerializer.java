/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serializer.extensions.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerializer;
import com.powsybl.commons.extensions.ExtensionSerializer;
import com.powsybl.commons.io.ReaderContext;
import com.powsybl.commons.io.WriterContext;
import com.powsybl.iidm.network.Network;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
@AutoService(ExtensionSerializer.class)
public class NetworkSourceExtensionSerializer extends AbstractExtensionSerializer<Network, NetworkSourceExtension> {

    public NetworkSourceExtensionSerializer() {
        super("networkSource", "network", NetworkSourceExtension.class, "networkSource.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/networksource/1_0", "extNetworkSource");
    }

    @Override
    public void write(NetworkSourceExtension extension, WriterContext context) {
        context.getWriter().writeStringAttribute("sourceData", extension.getSourceData());
    }

    @Override
    public NetworkSourceExtension read(Network extendable, ReaderContext context) {
        String sourceData = context.getReader().readStringAttribute("sourceData");
        context.getReader().readEndNode();
        NetworkSourceExtensionImpl extension = new NetworkSourceExtensionImpl(sourceData);
        extendable.addExtension(NetworkSourceExtension.class, extension);
        return extension;
    }
}
