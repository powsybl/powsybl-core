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
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimatedAdder;

import javax.xml.stream.XMLStreamException;

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
        context.getWriter().writeAttribute("ratioTapChangerStatus", String.valueOf(extension.shouldEstimateRatioTapChanger()));
        context.getWriter().writeAttribute("phaseTapChangerStatus", String.valueOf(extension.shouldEstimatePhaseTapChanger()));
    }

    @Override
    public TwoWindingsTransformerToBeEstimated read(TwoWindingsTransformer extendable, XmlReaderContext context) {
        extendable.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class)
                .withRatioTapChangerStatus(XmlUtil.readBoolAttribute(context.getReader(), "ratioTapChangerStatus"))
                .withPhaseTapChangerStatus(XmlUtil.readBoolAttribute(context.getReader(), "phaseTapChangerStatus"))
                .add();
        return extendable.getExtension(TwoWindingsTransformerToBeEstimated.class);
    }
}
