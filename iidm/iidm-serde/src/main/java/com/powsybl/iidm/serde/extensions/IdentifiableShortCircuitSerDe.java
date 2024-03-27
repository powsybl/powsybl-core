/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
@AutoService(ExtensionSerDe.class)
public class IdentifiableShortCircuitSerDe<I extends Identifiable<I>> extends AbstractExtensionSerDe<I, IdentifiableShortCircuit<I>> {

    public IdentifiableShortCircuitSerDe() {
        super("identifiableShortCircuit", "network", IdentifiableShortCircuit.class,
                "identifiableShortCircuit.xsd", "http://www.powsybl.org/schema/iidm/ext/identifiable_short_circuit/1_0",
                "isc");
    }

    @Override
    public void write(IdentifiableShortCircuit identifiableShortCircuit, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("ipMax", identifiableShortCircuit.getIpMax());
        context.getWriter().writeDoubleAttribute("ipMin", identifiableShortCircuit.getIpMin());
    }

    @Override
    public IdentifiableShortCircuit read(I identifiable, DeserializerContext context) {
        double ipMax = context.getReader().readDoubleAttribute("ipMax");
        double ipMin = context.getReader().readDoubleAttribute("ipMin");
        context.getReader().readEndNode();
        IdentifiableShortCircuitAdder<I> adder = identifiable.newExtension(IdentifiableShortCircuitAdder.class);
        return adder.withIpMax(ipMax)
                .withIpMin(ipMin)
                .add();
    }
}
