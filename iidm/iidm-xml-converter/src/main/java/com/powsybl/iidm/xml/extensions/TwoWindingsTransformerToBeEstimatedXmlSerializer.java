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
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimatedAdder;

import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class TwoWindingsTransformerToBeEstimatedXmlSerializer extends AbstractExtensionXmlSerializer<TwoWindingsTransformer, TwoWindingsTransformerToBeEstimated>
        implements ExtensionXmlSerializer<TwoWindingsTransformer, TwoWindingsTransformerToBeEstimated> {

    public TwoWindingsTransformerToBeEstimatedXmlSerializer() {
        super("twoWindingsTransformerToBeEstimated", "network", TwoWindingsTransformerToBeEstimated.class, false,
                "twoWindingsTransformerToBeEstimated.xsd",
                "http://www.powsybl.org/schema/iidm/ext/two_windings_transformer_to_be_estimated/1_0", "twottbe");
    }

    @Override
    public void write(TwoWindingsTransformerToBeEstimated extension, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("tapChangers", extension.getTapChangers().stream().sorted().map(Enum::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public TwoWindingsTransformerToBeEstimated read(TwoWindingsTransformer extendable, XmlReaderContext context) throws XMLStreamException {
        TwoWindingsTransformerToBeEstimatedAdder adder = extendable.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class);
        Arrays.stream(context.getReader().getAttributeValue(null, "tapChangers").split(", "))
                .forEach(tc -> adder.withTapChanger(TwoWindingsTransformerToBeEstimated.TapChanger.valueOf(tc)));
        adder.add();
        return extendable.getExtension(TwoWindingsTransformerToBeEstimated.class);
    }

    @Override
    public boolean isSerializable(TwoWindingsTransformerToBeEstimated extension) {
        return !extension.getTapChangers().isEmpty();
    }
}
