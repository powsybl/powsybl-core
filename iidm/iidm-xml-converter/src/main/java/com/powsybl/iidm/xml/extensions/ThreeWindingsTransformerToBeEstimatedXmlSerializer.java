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
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimatedAdder;

import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class ThreeWindingsTransformerToBeEstimatedXmlSerializer extends AbstractExtensionXmlSerializer<ThreeWindingsTransformer, ThreeWindingsTransformerToBeEstimated>
        implements ExtensionXmlSerializer<ThreeWindingsTransformer, ThreeWindingsTransformerToBeEstimated> {

    public ThreeWindingsTransformerToBeEstimatedXmlSerializer() {
        super("threeWindingsTransformerToBeEstimated", "network", ThreeWindingsTransformerToBeEstimated.class, false,
                "threeWindingsTransformerToBeEstimated.xsd", "http://www.powsybl.org/schema/iidm/ext/three_windings_transformer_to_be_estimated/1_0", "threettbe");
    }

    @Override
    public void write(ThreeWindingsTransformerToBeEstimated extension, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("tapChangers", extension.getTapChangers().stream().sorted().map(Enum::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated read(ThreeWindingsTransformer extendable, XmlReaderContext context) throws XMLStreamException {
        ThreeWindingsTransformerToBeEstimatedAdder adder = extendable.newExtension(ThreeWindingsTransformerToBeEstimatedAdder.class);
        Arrays.stream(context.getReader().getAttributeValue(null, "tapChangers").split(", "))
                .forEach(tc -> adder.withTapChanger(ThreeWindingsTransformerToBeEstimated.TapChanger.valueOf(tc)));
        adder.add();
        return extendable.getExtension(ThreeWindingsTransformerToBeEstimated.class);
    }

    @Override
    public boolean isSerializable(ThreeWindingsTransformerToBeEstimated extension) {
        return !extension.getTapChangers().isEmpty();
    }
}
