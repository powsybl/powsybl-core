/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.OverloadManagementSystem;
import com.powsybl.iidm.network.extensions.TrippingTempo;
import com.powsybl.iidm.network.extensions.TrippingTempoContainer;
import com.powsybl.iidm.network.extensions.TrippingTempoContainerAdder;

@AutoService(ExtensionSerDe.class)
public class TrippingTempoSerDe extends AbstractExtensionSerDe<OverloadManagementSystem, TrippingTempoContainer> {

    public TrippingTempoSerDe() {
        super(TrippingTempoContainer.NAME, "network", TrippingTempoContainer.class, "trippingTempo.xsd",
                "http://www.powsybl.org/schema/iidm/ext/tripping_tempo/1_0", "tt");
    }

    @Override
    public void write(TrippingTempoContainer tempoPointer, SerializerContext context) {
        context.getWriter().writeStringAttribute("tripping", tempoPointer.getTripping().getKey());
        context.getWriter().writeIntAttribute("tempo", tempoPointer.getTripping().getExtension(TrippingTempo.class).getTempo());
    }

    @Override
    public TrippingTempoContainer read(OverloadManagementSystem oms, DeserializerContext context) {
        String tripping = context.getReader().readStringAttribute("tripping");
        Integer tempo = context.getReader().readIntAttribute("tempo");
        context.getReader().readEndNode();
        return oms.newExtension(TrippingTempoContainerAdder.class)
                .withTempo(tripping, tempo)
                .add();
    }
}
