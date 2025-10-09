/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class CimCharacteristicsSerDe extends AbstractExtensionSerDe<Network, CimCharacteristics> {

    public CimCharacteristicsSerDe() {
        super("cimCharacteristics", "network", CimCharacteristics.class, "cimCharacteristics.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cim_characteristics/1_0", "cc");
    }

    @Override
    public void write(CimCharacteristics extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        TreeDataWriter writer = networkContext.getWriter();
        writer.writeEnumAttribute("topologyKind", extension.getTopologyKind());
        writer.writeIntAttribute("cimVersion", extension.getCimVersion());
    }

    @Override
    public CimCharacteristics read(Network extendable, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        TreeDataReader reader = networkContext.getReader();
        var extension = extendable.newExtension(CimCharacteristicsAdder.class)
                .setTopologyKind(reader.readEnumAttribute("topologyKind", CgmesTopologyKind.class))
                .setCimVersion(reader.readIntAttribute("cimVersion"))
                .add();
        context.getReader().readEndNode();
        return extension;
    }
}
