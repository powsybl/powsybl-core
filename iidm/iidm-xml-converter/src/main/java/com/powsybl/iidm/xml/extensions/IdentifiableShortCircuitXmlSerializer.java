/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;

/**
 *
 * @author Coline Piloquet <coline.piloquet@rte-france.fr>
 */
@AutoService(ExtensionXmlSerializer.class)
public class IdentifiableShortCircuitXmlSerializer<I extends Identifiable<I>> extends AbstractExtensionXmlSerializer<I, IdentifiableShortCircuit<I>> {

    public IdentifiableShortCircuitXmlSerializer() {
        super("identifiableShortCircuit", "network", IdentifiableShortCircuit.class, false,
                "identifiableShortCircuit.xsd", "http://www.powsybl.org/schema/iidm/ext/identifiable_short_circuit/1_0",
                "isc");
    }

    @Override
    public void write(IdentifiableShortCircuit identifiableShortCircuit, XmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("ipMax", identifiableShortCircuit.getIpMax());
        context.getWriter().writeDoubleAttribute("ipMin", identifiableShortCircuit.getIpMin());
    }

    @Override
    public IdentifiableShortCircuit read(I identifiable, XmlReaderContext context) {
        double ipMax = context.getReader().readDoubleAttribute("ipMax");
        double ipMin = context.getReader().readDoubleAttribute("ipMin");
        IdentifiableShortCircuitAdder<I> adder = identifiable.newExtension(IdentifiableShortCircuitAdder.class);
        return adder.withIpMax(ipMax)
                .withIpMin(ipMin)
                .add();
    }
}
