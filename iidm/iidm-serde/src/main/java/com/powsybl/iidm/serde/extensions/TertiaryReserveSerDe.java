/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.extensions.TertiaryReserve;
import com.powsybl.iidm.network.extensions.TertiaryReserveAdder;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import static com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe.convertContext;

/**
 * @author Jacques Borsenberger {literal <jacques.borsenberger at rte-france.com}
 */
@AutoService(ExtensionSerDe.class)
public class TertiaryReserveSerDe extends AbstractExtensionSerDe<Generator, TertiaryReserve> {

    public TertiaryReserveSerDe() {
        super(TertiaryReserve.NAME, "network", TertiaryReserve.class, "tertiaryReserve_V1_0.xsd",
                "http://www.powsybl.org/schema/iidm/ext/tertiary_reserve/1_0", "tr");
    }

    @Override
    public void write(TertiaryReserve tertiaryReserve, SerializerContext context) {
        NetworkSerializerContext networkContext = convertContext(context);
        networkContext.getWriter().writeBooleanAttribute("participate", tertiaryReserve.isParticipate());
    }

    @Override
    public TertiaryReserve read(Generator generator, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        boolean participate = networkContext.getReader().readBooleanAttribute("participate");
        context.getReader().readEndNode();
        return generator.newExtension(TertiaryReserveAdder.class)
                .withParticipate(participate)
                .add();
    }
}
