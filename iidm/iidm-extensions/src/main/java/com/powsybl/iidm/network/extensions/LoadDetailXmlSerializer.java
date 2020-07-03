/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Load;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class LoadDetailXmlSerializer extends AbstractExtensionXmlSerializer<Load, LoadDetail> {

    public LoadDetailXmlSerializer() {
        super("detail", "network", LoadDetail.class,
                false, "loadDetail.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/load_detail/1_0",
                "ld");
    }

    @Override
    public void write(LoadDetail detail, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("fixedActivePower", detail.getFixedActivePower(), context.getExtensionsWriter());
        XmlUtil.writeFloat("fixedReactivePower", detail.getFixedReactivePower(), context.getExtensionsWriter());
        XmlUtil.writeFloat("variableActivePower", detail.getVariableActivePower(), context.getExtensionsWriter());
        XmlUtil.writeFloat("variableReactivePower", detail.getVariableReactivePower(), context.getExtensionsWriter());
    }

    @Override
    public LoadDetail read(Load load, XmlReaderContext context) throws XMLStreamException {
        float fixedActivePower = XmlUtil.readOptionalFloatAttribute(context.getReader(), "subLoad1ActivePower");
        if (Float.isNaN(fixedActivePower)) {
            fixedActivePower = XmlUtil.readFloatAttribute(context.getReader(), "fixedActivePower");
        }
        float fixedReactivePower = XmlUtil.readOptionalFloatAttribute(context.getReader(), "subLoad1ReactivePower");
        if (Float.isNaN(fixedReactivePower)) {
            fixedReactivePower = XmlUtil.readFloatAttribute(context.getReader(), "fixedReactivePower");
        }
        float variableActivePower = XmlUtil.readOptionalFloatAttribute(context.getReader(), "subLoad2ActivePower");
        if (Float.isNaN(variableActivePower)) {
            variableActivePower = XmlUtil.readFloatAttribute(context.getReader(), "variableActivePower");
        }
        float variableReactivePower = XmlUtil.readOptionalFloatAttribute(context.getReader(), "subLoad2ReactivePower");
        if (Float.isNaN(variableReactivePower)) {
            variableReactivePower = XmlUtil.readFloatAttribute(context.getReader(), "variableReactivePower");
        }
        load.newExtension(LoadDetailAdder.class)
                .withFixedActivePower(fixedActivePower)
                .withFixedReactivePower(fixedReactivePower)
                .withVariableActivePower(variableActivePower)
                .withVariableReactivePower(variableReactivePower)
                .add();
        return load.getExtension(LoadDetail.class);
    }
}
