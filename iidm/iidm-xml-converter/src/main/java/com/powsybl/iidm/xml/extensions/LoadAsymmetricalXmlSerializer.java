/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadAsymmetrical;
import com.powsybl.iidm.network.extensions.LoadAsymmetricalAdder;
import com.powsybl.iidm.network.extensions.LoadConnectionType;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class LoadAsymmetricalXmlSerializer extends AbstractExtensionXmlSerializer<Load, LoadAsymmetrical> {

    public LoadAsymmetricalXmlSerializer() {
        super("loadAsymmetrical", "network", LoadAsymmetrical.class, false,
                "loadAsymmetrical_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/load_asymmetrical/1_0",
                "las");
    }

    @Override
    public void write(LoadAsymmetrical loadAsym, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("connectionType", loadAsym.getConnectionType().name());
        XmlUtil.writeOptionalDouble("deltaPa", loadAsym.getDeltaPa(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("deltaQa", loadAsym.getDeltaQa(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("deltaPb", loadAsym.getDeltaPb(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("deltaQb", loadAsym.getDeltaQb(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("deltaPc", loadAsym.getDeltaPc(), 0, context.getWriter());
        XmlUtil.writeOptionalDouble("deltaQc", loadAsym.getDeltaQc(), 0, context.getWriter());
    }

    @Override
    public LoadAsymmetrical read(Load load, XmlReaderContext context) throws XMLStreamException {
        LoadConnectionType connectionType = LoadConnectionType.valueOf(context.getReader().getAttributeValue(null, "connectionType"));
        double deltaPa = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "deltaPa", 0);
        double deltaQa = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "deltaQa", 0);
        double deltaPb = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "deltaPb", 0);
        double deltaQb = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "deltaQb", 0);
        double deltaPc = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "deltaPc", 0);
        double deltaQc = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "deltaQc", 0);
        return load.newExtension(LoadAsymmetricalAdder.class)
                .withConnectionType(connectionType)
                .withDeltaPa(deltaPa)
                .withDeltaQa(deltaQa)
                .withDeltaPb(deltaPb)
                .withDeltaQb(deltaQb)
                .withDeltaPc(deltaPc)
                .withDeltaQc(deltaQc)
                .add();
    }
}
