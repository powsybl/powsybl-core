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
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimatedAdder;

import javax.xml.stream.XMLStreamException;

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
        context.getWriter().writeAttribute("ratioTapChanger1Status", String.valueOf(extension.shouldEstimateRatioTapChanger1()));
        context.getWriter().writeAttribute("ratioTapChanger2Status", String.valueOf(extension.shouldEstimateRatioTapChanger2()));
        context.getWriter().writeAttribute("ratioTapChanger3Status", String.valueOf(extension.shouldEstimateRatioTapChanger3()));
        context.getWriter().writeAttribute("phaseTapChanger1Status", String.valueOf(extension.shouldEstimatePhaseTapChanger1()));
        context.getWriter().writeAttribute("phaseTapChanger2Status", String.valueOf(extension.shouldEstimatePhaseTapChanger2()));
        context.getWriter().writeAttribute("phaseTapChanger3Status", String.valueOf(extension.shouldEstimatePhaseTapChanger3()));
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated read(ThreeWindingsTransformer extendable, XmlReaderContext context) {
        extendable.newExtension(ThreeWindingsTransformerToBeEstimatedAdder.class)
                .withRatioTapChanger1Status(XmlUtil.readBoolAttribute(context.getReader(), "ratioTapChanger1Status"))
                .withRatioTapChanger2Status(XmlUtil.readBoolAttribute(context.getReader(), "ratioTapChanger2Status"))
                .withRatioTapChanger3Status(XmlUtil.readBoolAttribute(context.getReader(), "ratioTapChanger3Status"))
                .withPhaseTapChanger1Status(XmlUtil.readBoolAttribute(context.getReader(), "phaseTapChanger1Status"))
                .withPhaseTapChanger2Status(XmlUtil.readBoolAttribute(context.getReader(), "phaseTapChanger2Status"))
                .withPhaseTapChanger3Status(XmlUtil.readBoolAttribute(context.getReader(), "phaseTapChanger3Status"))
                .add();
        return extendable.getExtension(ThreeWindingsTransformerToBeEstimated.class);
    }
}
